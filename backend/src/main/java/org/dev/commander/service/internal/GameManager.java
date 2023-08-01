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
    private final GameEntry game = new GameEntry();
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

    private static class GameEntry {
        private static final double SPACE_WIDTH = 16;
        private static final double SPACE_HEIGHT = 26;
        private static final double SPEED_SCALING = 0.002;
        private static final double CHARACTER_LENGTH = 1;
        private static final double CHARACTER_MOVEMENT_SPEED = 1;
        private static final double CHARACTER_MOVEMENT_VALIDATION_MARGIN = 0.06;
        private long lastProcessTime;
        private final List<GameInput> inputQueue = new ArrayList<>();
        private final Lock inputQueueLock = new ReentrantLock();
        private final GameState gameState;
        private final Lock gameStateReadLock;
        private final Lock gameStateWriteLock;

        public GameEntry() {
            Space space = new Space();
            space.setWidth(SPACE_WIDTH);
            space.setHeight(SPACE_HEIGHT);
            gameState = new GameState();
            gameState.setSpace(space);
            gameState.setCharacters(new HashMap<>());
            ReadWriteLock gameStateReadWriteLock = new ReentrantReadWriteLock();
            gameStateReadLock = gameStateReadWriteLock.readLock();
            gameStateWriteLock = gameStateReadWriteLock.writeLock();
        }

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
                    double posX = input.getPosX();
                    double posY = input.getPosY();
                    if (posX < 0 || posX + character.getWidth() > gameState.getSpace().getWidth() || posY < 0 || posY + character.getHeight() > gameState.getSpace().getHeight()) {
                        return false;
                    }
                    long duration = currentTime - character.getLastPositionUpdateTime();
                    double radius = character.getMovementSpeed() * duration * SPEED_SCALING;
                    double proposedDistance = Math.sqrt(Math.pow(posX - character.getPosX(), 2) + Math.pow(posY - character.getPosY(), 2));
                    if (proposedDistance > radius + CHARACTER_MOVEMENT_VALIDATION_MARGIN) {
                        return false;
                    }
                    character.setPosX(posX);
                    character.setPosY(posY);
                    character.setOrientation(input.getOrientation());
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
            GameState clone = new GameState();
            clone.setClientPlayerId(gameState.getClientPlayerId());
            clone.setSnapshotTime(gameState.getSnapshotTime());
            clone.setSpace(space);
            clone.setCharacters(characters);
            return clone;
        }
    }
}
