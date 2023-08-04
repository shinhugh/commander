package org.dev.pixels.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.pixels.model.IncomingMessage;
import org.dev.pixels.model.OutgoingMessage;
import org.dev.pixels.model.game.Character;
import org.dev.pixels.model.game.*;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.websocket.ConnectionEventHandler;
import org.dev.pixels.websocket.IncomingMessageHandler;
import org.dev.pixels.websocket.MessageBroker;
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
    private final Map<String, Long> sessionTokenToPlayerIdMap = new HashMap<>();
    private final Lock playerMapReadLock;
    private final Lock playerMapWriteLock;

    public GameManager(PlayerService playerService, MessageBroker messageBroker, IdentificationService identificationService) {
        this.playerService = playerService;
        this.messageBroker = messageBroker;
        this.identificationService = identificationService;
        ReadWriteLock playerMapReadWriteLock = new ReentrantReadWriteLock();
        playerMapReadLock = playerMapReadWriteLock.readLock();
        playerMapWriteLock = playerMapReadWriteLock.writeLock();
        this.messageBroker.registerConnectionEventHandler(this);
        this.messageBroker.registerIncomingMessageHandler(this);
        game.resetProcessingPoint();
    }

    @Override
    public void handleEstablishedConnection(Authentication authentication) { }

    @Override
    public void handleClosedConnection(Authentication authentication) {
        String sessionToken = (String) authentication.getCredentials();
        Long playerId = getPlayerIdForSession(sessionToken);
        if (playerId == null) {
            return;
        }
        unmapPlayerBySessionToken(sessionToken);
        GameInput input = new GameInput();
        input.setPlayerId(playerId);
        input.setType(GameInput.Type.LEAVE);
        game.input(input);
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
            case GAME_CHAT -> {
                GameChat gameChat;
                try {
                    gameChat = objectMapper.convertValue(message.getPayload(), GameChat.class);
                }
                catch (IllegalArgumentException ex) {
                    return;
                }
                handleGameChat(authentication, gameChat);
            }
        }
    }

    @Scheduled(fixedRate = PROCESS_INTERVAL)
    public void process() {
        Set<Long> offenders = game.process();
        for (long playerId : offenders) {
            String sessionToken = getSessionTokenForPlayer(playerId);
            if (sessionToken == null) {
                continue;
            }
            OutgoingMessage<Void> message = new OutgoingMessage<>();
            message.setType(OutgoingMessage.Type.GAME_INTEGRITY_VIOLATION);
            messageBroker.sendMessageBySessionToken(sessionToken, message);
            unmapPlayerById(playerId);
        }
    }

    @Scheduled(fixedRate = BROADCAST_INTERVAL)
    public void broadcast() {
        Map<Long, GameState> snapshot = game.snapshot();
        Map<Long, String> destinations = clonePlayerMap();
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
        String sessionToken = (String) authentication.getCredentials();
        Long playerId = getPlayerIdForSession(sessionToken);
        if (playerId != null) {
            return;
        }
        playerId = getPlayerIdForAccount(identificationService.identifyAccount(authentication).getId());
        String evictedSessionToken = getSessionTokenForPlayer(playerId);
        unmapPlayerById(playerId);
        mapPlayer(playerId, sessionToken);
        GameInput input = new GameInput();
        input.setPlayerId(playerId);
        input.setType(GameInput.Type.JOIN);
        game.input(input);
        if (evictedSessionToken != null) {
            OutgoingMessage<Void> message = new OutgoingMessage<>();
            message.setType(OutgoingMessage.Type.GAME_SEAT_USURPED);
            messageBroker.sendMessageBySessionToken(evictedSessionToken, message);
        }
    }

    private void handleGameInput(Authentication authentication, GameInput input) {
        String sessionToken = (String) authentication.getCredentials();
        Long playerId = getPlayerIdForSession(sessionToken);
        if (playerId == null) {
            return;
        }
        input.setPlayerId(playerId);
        game.input(input);
    }

    private void handleGameChat(Authentication authentication, GameChat chat) {
        String srcSessionToken = (String) authentication.getCredentials();
        Long srcPlayerId = getPlayerIdForSession(srcSessionToken);
        if (srcPlayerId == null) {
            return;
        }
        if (chat.getContent() == null || chat.getContent().length() == 0) {
            return;
        }
        chat.setSrcPlayerId(srcPlayerId);
        Long dstPlayerId = chat.getDstPlayerId();
        chat.setDstPlayerId(null);
        OutgoingMessage<GameChat> message = new OutgoingMessage<>();
        message.setType(OutgoingMessage.Type.GAME_CHAT);
        message.setPayload(chat);
        if (chat.getToPublic() != null && chat.getToPublic()) {
            Map<Long, String> destinations = clonePlayerMap();
            for (Map.Entry<Long, String> destination : destinations.entrySet()) {
                messageBroker.sendMessageBySessionToken(destination.getValue(), message);
            }
            return;
        }
        if (dstPlayerId == null || dstPlayerId <= 0) {
            return;
        }
        messageBroker.sendMessageBySessionToken(getSessionTokenForPlayer(dstPlayerId), message);
    }

    private long getPlayerIdForAccount(long accountId) {
        List<Player> players = playerService.readPlayers(null, accountId);
        if (!players.isEmpty()) {
            return players.get(0).getId();
        }
        Player player = new Player();
        player.setAccountId(accountId);
        player = playerService.createPlayer(player);
        return player.getId();
    }

    private Long getPlayerIdForSession(String sessionToken) {
        playerMapReadLock.lock();
        try {
            return sessionTokenToPlayerIdMap.get(sessionToken);
        }
        finally {
            playerMapReadLock.unlock();
        }
    }

    private String getSessionTokenForPlayer(long playerId) {
        playerMapReadLock.lock();
        try {
            return playerIdToSessionTokenMap.get(playerId);
        }
        finally {
            playerMapReadLock.unlock();
        }
    }

    private Map<Long, String> clonePlayerMap() {
        playerMapReadLock.lock();
        try {
            return new HashMap<>(playerIdToSessionTokenMap);
        }
        finally {
            playerMapReadLock.unlock();
        }
    }

    private void mapPlayer(long id, String sessionToken) {
        playerMapWriteLock.lock();
        try {
            playerIdToSessionTokenMap.put(id, sessionToken);
            sessionTokenToPlayerIdMap.put(sessionToken, id);
        }
        finally {
            playerMapWriteLock.unlock();
        }
    }

    private void unmapPlayerBySessionToken(String sessionToken) {
        playerMapWriteLock.lock();
        try {
            Long id = sessionTokenToPlayerIdMap.get(sessionToken);
            playerIdToSessionTokenMap.remove(id);
            sessionTokenToPlayerIdMap.remove(sessionToken);
        }
        finally {
            playerMapWriteLock.unlock();
        }
    }

    private void unmapPlayerById(long id) {
        playerMapWriteLock.lock();
        try {
            String sessionToken = playerIdToSessionTokenMap.get(id);
            playerIdToSessionTokenMap.remove(id);
            sessionTokenToPlayerIdMap.remove(sessionToken);
        }
        finally {
            playerMapWriteLock.unlock();
        }
    }

    // TODO: Read map data from external configuration file
    private static GameEntry generateGameEntry() {
        Space space = new Space();
        space.setWidth(64);
        space.setHeight(8);
        Obstacle obstacle = new Obstacle();
        obstacle.setId(1);
        obstacle.setWidth(4);
        obstacle.setHeight(2);
        obstacle.setPosX(30);
        obstacle.setPosY(3);
        Set<Obstacle> obstacles = new HashSet<>();
        obstacles.add(obstacle);
        GameState gameState = new GameState();
        gameState.setSpace(space);
        gameState.setCharacters(new HashMap<>());
        gameState.setObstacles(obstacles);
        return new GameEntry(gameState);
    }

    private static class GameEntry {
        private static final double CHARACTER_LENGTH = 1;
        private static final double CHARACTER_MOVEMENT_SPEED = 1;
        private static final double CHARACTER_SPEED_SCALING = 0.005;
        private static final double CHARACTER_POSITION_VALIDATION_MARGIN = 0.12;
        private static final long CHARACTER_SILENT_MOVEMENT_DURATION_MAX = 100;
        private static final long CHARACTER_MOVEMENT_TIMEOUT_DURATION = 50;
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
            long playerId = input.getPlayerId();
            switch (input.getType()) {
                case JOIN -> {
                    if (gameState.getCharacters().containsKey(playerId)) {
                        return true;
                    }
                    double posX = (gameState.getSpace().getWidth() - CHARACTER_LENGTH) / 2;
                    double posY = gameState.getSpace().getHeight() - CHARACTER_LENGTH - 1;
                    Character character = new Character();
                    character.setId(generateCharacterId(gameState));
                    character.setPlayerId(playerId);
                    character.setWidth(CHARACTER_LENGTH);
                    character.setHeight(CHARACTER_LENGTH);
                    character.setPosX(posX);
                    character.setPosY(posY);
                    character.setMovementSpeed(CHARACTER_MOVEMENT_SPEED);
                    character.setLastPositionUpdateTime(currentTime);
                    character.setOrientation(Direction.DOWN);
                    character.setMoving(false);
                    gameState.getCharacters().put(playerId, character);
                }
                case LEAVE -> {
                    gameState.getCharacters().remove(playerId);
                }
                case POSITION -> {
                    Character character = gameState.getCharacters().get(playerId);
                    if (character == null) {
                        return true;
                    }
                    Double newPosX = input.getPosX();
                    Double newPosY = input.getPosY();
                    if (newPosX == null || newPosY == null) {
                        return true;
                    }
                    double deltaX = newPosX - character.getPosX();
                    double deltaY = newPosY - character.getPosY();
                    if (deltaX == 0 && deltaY == 0) {
                        character.setLastPositionUpdateTime(currentTime);
                        character.setMoving(false);
                        return true;
                    }
                    double width = character.getWidth();
                    double height = character.getHeight();
                    if (newPosX < 0 || newPosX + width > gameState.getSpace().getWidth() || newPosY < 0 || newPosY + height > gameState.getSpace().getHeight()) {
                        gameState.getCharacters().remove(playerId);
                        return false;
                    }
                    long duration = Math.min(currentTime - character.getLastPositionUpdateTime(), CHARACTER_SILENT_MOVEMENT_DURATION_MAX);
                    double radius = character.getMovementSpeed() * duration * CHARACTER_SPEED_SCALING;
                    double proposedDistance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
                    if (proposedDistance > radius + CHARACTER_POSITION_VALIDATION_MARGIN) {
                        gameState.getCharacters().remove(playerId);
                        return false;
                    }
                    // TODO: Need a better way to check for collision; current technique allows jumping over obstacles
                    //       as long as the final coordinates don't cause any overlap
                    //       Practically impossible since the maximum duration a position update can retrospectively
                    //       represent is 100ms, but theoretically possible with extremely high movement speed
                    for (Obstacle obstacle : gameState.getObstacles()) {
                        if (testForOverlap(character, obstacle)) {
                            gameState.getCharacters().remove(playerId);
                            return false;
                        }
                    }
                    Direction orientation = determineOrientation(deltaX, deltaY);
                    character.setPosX(newPosX);
                    character.setPosY(newPosY);
                    character.setOrientation(orientation);
                    character.setMoving(true);
                    character.setLastPositionUpdateTime(currentTime);
                }
            }
            return true;
        }

        private static void processDuration(GameState gameState, long duration) {
            long currentTime = currentTimeMillis();
            for (Character character : gameState.getCharacters().values()) {
                if (character.isMoving() && character.getLastPositionUpdateTime() + CHARACTER_MOVEMENT_TIMEOUT_DURATION < currentTime) {
                    character.setMoving(false);
                }
            }
        }

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
            if (matterAXUpper > matterBXLower && matterAXLower < matterBXUpper) {
                return matterAYUpper > matterBYLower && matterAYLower < matterBYUpper;
            }
            return false;
        }

        private static Direction determineOrientation(double deltaX, double deltaY) {
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
                characterClone.setMoving(character.isMoving());
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
