package org.dev.commander.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.commander.model.IncomingMessage;
import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.model.game.Character;
import org.dev.commander.model.game.*;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.websocket.ConnectionEventHandler;
import org.dev.commander.websocket.IncomingMessageHandler;
import org.dev.commander.websocket.MessageBroker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.currentTimeMillis;

@Service
public class GameManager implements ConnectionEventHandler, IncomingMessageHandler {
    private static final long PROCESS_INTERVAL = 13;
    private static final long BROADCAST_INTERVAL = 15;
    private final PlayerService playerService;
    private final MessageBroker messageBroker;
    private final IdentificationService identificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameEntry game = generateGameEntry();
    private final Map<Long, String> playerIdToSessionTokenMap = new HashMap<>();
    private final Lock playerIdToSessionTokenMapReadLock;
    private final Lock playerIdToSessionTokenMapWriteLock;
    private final Map<Long, Long> accountIdToPlayerIdMap = new HashMap<>();
    private final Lock accountIdToPlayerIdMapReadLock;
    private final Lock accountIdToPlayerIdMapWriteLock;

    public GameManager(PlayerService playerService, MessageBroker messageBroker, IdentificationService identificationService) {
        this.playerService = playerService;
        this.messageBroker = messageBroker;
        this.identificationService = identificationService;
        ReadWriteLock playerIdToSessionTokenMapReadWriteLock = new ReentrantReadWriteLock();
        playerIdToSessionTokenMapReadLock = playerIdToSessionTokenMapReadWriteLock.readLock();
        playerIdToSessionTokenMapWriteLock = playerIdToSessionTokenMapReadWriteLock.writeLock();
        ReadWriteLock accountIdToPlayerIdMapReadWriteLock = new ReentrantReadWriteLock();
        accountIdToPlayerIdMapReadLock = accountIdToPlayerIdMapReadWriteLock.readLock();
        accountIdToPlayerIdMapWriteLock = accountIdToPlayerIdMapReadWriteLock.writeLock();
        this.messageBroker.registerConnectionEventHandler(this);
        this.messageBroker.registerIncomingMessageHandler(this);
        game.resetProcessingPoint();
    }

    @Override
    public void handleEstablishedConnection(Authentication authentication) { }

    @Override
    public void handleClosedConnection(Authentication authentication) {
        long accountId = identificationService.identifyAccount(authentication).getId();
        String sessionToken = (String) authentication.getCredentials();
        long playerId = getPlayerId(accountId);
        boolean playerLeft = false;
        playerIdToSessionTokenMapWriteLock.lock();
        try {
            if (Objects.equals(playerIdToSessionTokenMap.get(playerId), sessionToken)) {
                playerIdToSessionTokenMap.remove(playerId);
                playerLeft = true;
            }
        }
        finally {
            playerIdToSessionTokenMapWriteLock.unlock();
        }
        if (playerLeft) {
            GameInput input = new GameInput();
            input.setPlayerId(getPlayerId(accountId));
            input.setType(GameInput.Type.LEAVE);
            game.input(input);
            accountIdToPlayerIdMapWriteLock.lock();
            try {
                accountIdToPlayerIdMap.remove(accountId);
            }
            finally {
                accountIdToPlayerIdMapWriteLock.unlock();
            }
        }
    }

    @Override
    public void handleIncomingMessage(Authentication authentication, IncomingMessage message) {
        switch (message.getType()) {
            case GAME_JOIN -> handleGameJoin(authentication);
            case GAME_INPUT -> {
                GameInput input;
                try {
                    input = objectMapper.convertValue(message.getPayload(), GameInput.class);
                }
                catch (IllegalArgumentException ex) {
                    return;
                }
                handleGameInput(authentication, input);
            }
        }
    }

    @Scheduled(fixedRate = PROCESS_INTERVAL)
    public void process() {
        Set<Long> offenders = game.process();
        for (long playerId : offenders) {
            // TODO: Handle offender (evict from game?)
        }
    }

    @Scheduled(fixedRate = BROADCAST_INTERVAL)
    public void broadcast() {
        Map<Long, GameState> snapshot = game.snapshot();
        Map<Long, String> destinations;
        playerIdToSessionTokenMapReadLock.lock();
        try {
            destinations = new HashMap<>(playerIdToSessionTokenMap);
        }
        finally {
            playerIdToSessionTokenMapReadLock.unlock();
        }
        for (Map.Entry<Long, String> destination : destinations.entrySet()) { // TODO: Parallelize
            long playerId = destination.getKey();
            String sessionToken = destination.getValue();
            GameState gameState = snapshot.get(playerId);
            if (gameState == null) {
                continue;
            }
            OutgoingMessage<GameState> message = new OutgoingMessage<>();
            message.setType(OutgoingMessage.Type.GAME_SNAPSHOT);
            message.setPayload(gameState);
            messageBroker.sendMessageBySessionToken(sessionToken, message);
        }
    }

    private void handleGameJoin(Authentication authentication) {
        long playerId = getPlayerId(identificationService.identifyAccount(authentication).getId());
        String sessionToken = (String) authentication.getCredentials();
        String evictedSessionToken;
        playerIdToSessionTokenMapWriteLock.lock();
        try {
            evictedSessionToken = playerIdToSessionTokenMap.get(playerId);
            playerIdToSessionTokenMap.put(playerId, sessionToken);
        }
        finally {
            playerIdToSessionTokenMapWriteLock.unlock();
        }
        GameInput input = new GameInput();
        input.setPlayerId(playerId);
        input.setType(GameInput.Type.JOIN);
        game.input(input);
        if (evictedSessionToken != null) {
            OutgoingMessage<Void> message = new OutgoingMessage<>();
            message.setType(OutgoingMessage.Type.GAME_EVICTION);
            messageBroker.sendMessageBySessionToken(evictedSessionToken, message);
        }
    }

    private void handleGameInput(Authentication authentication, GameInput input) {
        String sessionToken = (String) authentication.getCredentials();
        long playerId = getPlayerId(identificationService.identifyAccount(authentication).getId());
        String expectedSessionToken;
        playerIdToSessionTokenMapReadLock.lock();
        try {
            expectedSessionToken = playerIdToSessionTokenMap.get(playerId);
        }
        finally {
            playerIdToSessionTokenMapReadLock.unlock();
        }
        if (!Objects.equals(expectedSessionToken, sessionToken)) {
            return;
        }
        input.setPlayerId(playerId);
        game.input(input);
    }

    private long getPlayerId(long accountId) {
        Long playerId;
        accountIdToPlayerIdMapReadLock.lock();
        try {
            playerId = accountIdToPlayerIdMap.get(accountId);
        }
        finally {
            accountIdToPlayerIdMapReadLock.unlock();
        }
        if (playerId != null) {
            return playerId;
        }
        List<Player> players = playerService.readPlayers(null, accountId);
        Player player = players.isEmpty() ? null : players.get(0);
        if (player != null) {
            accountIdToPlayerIdMapWriteLock.lock();
            try {
                accountIdToPlayerIdMap.put(accountId, player.getId());
            }
            finally {
                accountIdToPlayerIdMapWriteLock.unlock();
            }
            return player.getId();
        }
        player = new Player();
        player.setAccountId(accountId);
        player = playerService.createPlayer(player);
        accountIdToPlayerIdMapWriteLock.lock();
        try {
            accountIdToPlayerIdMap.put(accountId, player.getId());
        }
        finally {
            accountIdToPlayerIdMapWriteLock.unlock();
        }
        return player.getId();
    }

    // TODO: Read map data from external configuration file
    private static GameEntry generateGameEntry() {
        Space space = new Space();
        space.setWidth(48);
        space.setHeight(16);
        Obstacle obstacle = new Obstacle();
        obstacle.setId(1);
        obstacle.setWidth(3);
        obstacle.setHeight(2);
        obstacle.setPosX(18);
        obstacle.setPosY(8);
        Set<Obstacle> obstacles = new HashSet<>();
        obstacles.add(obstacle);
        GameState gameState = new GameState();
        gameState.setSpace(space);
        gameState.setCharacters(new HashMap<>());
        gameState.setObstacles(obstacles);
        return new GameEntry(gameState);
    }

    // TODO: Use as data object; move logic into parent class GameManager
    private static class GameEntry {
        private static final double CHARACTER_SPEED_SCALING = 0.005;
        private static final double CHARACTER_LENGTH = 1;
        private static final double CHARACTER_MOVEMENT_SPEED = 1;
        private static final double CHARACTER_MOVEMENT_VALIDATION_MARGIN = 0.06;
        private static final long CHARACTER_POSITION_SILENT_INTERVAL_MAX = 100;
        private final List<GameInput> inputQueue = new ArrayList<>();
        private final Lock inputQueueLock = new ReentrantLock();
        private final GameState gameState;
        private final Lock gameStateReadLock;
        private final Lock gameStateWriteLock;
        private long lastProcessTime;

        public GameEntry(GameState gameState) {
            this.gameState = cloneGameState(gameState);
            ReadWriteLock gameStateReadWriteLock = new ReentrantReadWriteLock();
            gameStateReadLock = gameStateReadWriteLock.readLock();
            gameStateWriteLock = gameStateReadWriteLock.writeLock();
        }

        public void input(GameInput input) {
            inputQueueLock.lock();
            try {
                inputQueue.add(input);
            }
            finally {
                inputQueueLock.unlock();
            }
        }

        public Map<Long, GameState> snapshot() {
            GameState commonGameState;
            gameStateReadLock.lock();
            try {
                commonGameState = cloneGameState(gameState);
            }
            finally {
                gameStateReadLock.unlock();
            }
            commonGameState.setSnapshotTime(currentTimeMillis());
            return applyPerspectives(commonGameState);
        }

        public Set<Long> process() {
            Set<Long> offenders = new HashSet<>();
            long currentTime = currentTimeMillis();
            long duration = currentTime - lastProcessTime;
            List<GameInput> inputs = cloneAndClearInputQueue();
            gameStateWriteLock.lock();
            try {
                for (GameInput input : inputs) {
                    if (!processInput(gameState, input, currentTime)) {
                        offenders.add(input.getPlayerId());
                    }
                }
                processDuration(gameState, duration);
            }
            finally {
                gameStateWriteLock.unlock();
            }
            lastProcessTime = currentTime;
            return offenders;
        }

        public void resetProcessingPoint() {
            this.lastProcessTime = currentTimeMillis();
        }

        private List<GameInput> cloneAndClearInputQueue() {
            List<GameInput> clone;
            inputQueueLock.lock();
            try {
                clone = new ArrayList<>(inputQueue);
                inputQueue.clear();
            }
            finally {
                inputQueueLock.unlock();
            }
            return clone;
        }

        private static boolean processInput(GameState gameState, GameInput input, long currentTime) {
            switch (input.getType()) {
                case JOIN -> {
                    if (gameState.getCharacters().containsKey(input.getPlayerId())) {
                        return true;
                    }
                    double posX = (gameState.getSpace().getWidth() - CHARACTER_LENGTH) / 2;
                    double posY = (gameState.getSpace().getHeight() - CHARACTER_LENGTH) / 2;
                    Character character = new Character();
                    character.setId(generateCharacterId(gameState));
                    character.setPlayerId(input.getPlayerId());
                    character.setWidth(CHARACTER_LENGTH);
                    character.setHeight(CHARACTER_LENGTH);
                    character.setPosX(posX);
                    character.setPosY(posY);
                    character.setMovementSpeed(CHARACTER_MOVEMENT_SPEED);
                    character.setLastPositionUpdateTime(currentTime);
                    character.setOrientation(Direction.DOWN);
                    gameState.getCharacters().put(input.getPlayerId(), character);
                }
                case LEAVE -> {
                    gameState.getCharacters().remove(input.getPlayerId());
                }
                case POSITION -> {
                    Character character = gameState.getCharacters().get(input.getPlayerId());
                    if (character == null) {
                        return true;
                    }
                    Double newPosX = input.getPosX();
                    Double newPosY = input.getPosY();
                    if (newPosX == null || newPosY == null) {
                        return true;
                    }
                    double oldPosX = character.getPosX();
                    double oldPosY = character.getPosY();
                    double width = character.getWidth();
                    double height = character.getHeight();
                    if (newPosX < 0 || newPosX + width > gameState.getSpace().getWidth() || newPosY < 0 || newPosY + height > gameState.getSpace().getHeight()) {
                        return false;
                    }
                    long duration = Math.min(currentTime - character.getLastPositionUpdateTime(), CHARACTER_POSITION_SILENT_INTERVAL_MAX);
                    double radius = character.getMovementSpeed() * duration * CHARACTER_SPEED_SCALING;
                    double proposedDistance = Math.sqrt(Math.pow(newPosX - oldPosX, 2) + Math.pow(newPosY - oldPosY, 2));
                    if (proposedDistance > radius + CHARACTER_MOVEMENT_VALIDATION_MARGIN) {
                        return false;
                    }
                    for (Obstacle obstacle : gameState.getObstacles()) {
                        if (testForOverlap(character, obstacle)) {
                            return false;
                        }
                    }
                    Direction orientation = input.getOrientation();
                    if (orientation == null) {
                        orientation = determineDirection(newPosX - oldPosX, newPosY - oldPosY);
                        orientation = orientation == null ? Direction.DOWN : orientation;
                    }
                    character.setPosX(newPosX);
                    character.setPosY(newPosY);
                    character.setOrientation(orientation);
                    character.setLastPositionUpdateTime(currentTime);
                }
            }
            return true;
        }

        private static void processDuration(GameState gameState, long duration) { }

        private static Map<Long, GameState> applyPerspectives(GameState gameState) {
            Map<Long, GameState> playerSpecificGameStates = new HashMap<>();
            for (Character character : gameState.getCharacters().values()) {
                long playerId = character.getPlayerId();
                GameState playerSpecificGameState = cloneGameState(gameState);
                playerSpecificGameState.setClientPlayerId(playerId);
                playerSpecificGameStates.put(playerId, playerSpecificGameState);
            }
            return playerSpecificGameStates;
        }

        private static boolean testForOverlap(Matter matterA, Matter matterB) {
            double matterAXLower = matterA.getPosX();
            double matterAXUpper = matterAXLower + matterA.getWidth();
            double matterAYLower = matterA.getPosY();
            double matterAYUpper = matterAYLower + matterA.getHeight();
            double matterBXLower = matterB.getPosX();
            double matterBXUpper = matterBXLower + matterB.getWidth();
            double matterBYLower = matterB.getPosY();
            double matterBYUpper = matterBYLower + matterB.getHeight();
            if ((matterAXLower >= matterBXLower && matterAXLower < matterBXUpper) || (matterAXUpper > matterBXLower && matterAXUpper <= matterBXUpper) || (matterAXLower <= matterBXLower && matterAXUpper >= matterBXUpper)) {
                return (matterAYLower >= matterBYLower && matterAYLower < matterBYUpper) || (matterAYUpper > matterBYLower && matterAYUpper <= matterBYUpper) || (matterAYLower <= matterBYLower && matterAYUpper >= matterBYUpper);
            }
            return false;
        }

        private static Direction determineDirection(double deltaX, double deltaY) {
            if (deltaX < 0) {
                if (deltaY < 0) {
                    return Direction.UP_LEFT;
                }
                else if (deltaY > 0) {
                    return Direction.DOWN_LEFT;
                }
                else {
                    return Direction.LEFT;
                }
            }
            else if (deltaX > 0) {
                if (deltaY < 0) {
                    return Direction.UP_RIGHT;
                }
                else if (deltaY > 0) {
                    return Direction.DOWN_RIGHT;
                }
                else {
                    return Direction.RIGHT;
                }
            }
            if (deltaY < 0) {
                return Direction.UP;
            }
            else if (deltaY > 0) {
                return Direction.DOWN;
            }
            return null;
        }

        private static long generateCharacterId(GameState gameState) {
            long characterId;
            do {
                characterId = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE) + 1;
            } while (gameState.getCharacters().containsKey(characterId));
            return characterId;
        }

        private static GameState cloneGameState(GameState gameState) {
            if (gameState == null) {
                return null;
            }
            Space space = new Space();
            space.setWidth(gameState.getSpace().getWidth());
            space.setHeight(gameState.getSpace().getHeight());
            Map<Long, Character> characters = new HashMap<>();
            for (Map.Entry<Long, Character> characterEntry : gameState.getCharacters().entrySet()) {
                long playerId = characterEntry.getKey();
                Character character = characterEntry.getValue();
                Character characterClone = new Character();
                characterClone.setId(character.getId());
                characterClone.setPlayerId(character.getPlayerId());
                characterClone.setWidth(character.getWidth());
                characterClone.setHeight(character.getHeight());
                characterClone.setPosX(character.getPosX());
                characterClone.setPosY(character.getPosY());
                characterClone.setMovementSpeed(character.getMovementSpeed());
                characterClone.setLastPositionUpdateTime(character.getLastPositionUpdateTime());
                characterClone.setOrientation(character.getOrientation());
                characters.put(playerId, characterClone);
            }
            Set<Obstacle> obstacles = new HashSet<>();
            for (Obstacle obstacle : gameState.getObstacles()) {
                Obstacle obstacleClone = new Obstacle();
                obstacleClone.setId(obstacle.getId());
                obstacleClone.setWidth(obstacle.getWidth());
                obstacleClone.setHeight(obstacle.getHeight());
                obstacleClone.setPosX(obstacle.getPosX());
                obstacleClone.setPosY(obstacle.getPosY());
                obstacles.add(obstacleClone);
            }
            GameState clone = new GameState();
            clone.setClientPlayerId(gameState.getClientPlayerId());
            clone.setSnapshotTime(gameState.getSnapshotTime());
            clone.setSpace(space);
            clone.setCharacters(characters);
            clone.setObstacles(obstacles);
            return clone;
        }
    }
}
