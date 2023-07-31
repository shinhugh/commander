package org.dev.commander.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.commander.model.IncomingMessage;
import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.model.game.Character;
import org.dev.commander.model.game.*;
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
        game.process();
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

    private static GameEntry generateGameEntry() {
        // TODO: Populate GameEntry from persistent storage; remove method generateMockGameEntry()
        return generateMockGameEntry();
    }

    private static GameEntry generateMockGameEntry() {
        Space space = new Space();
        space.setWidth(16);
        space.setHeight(26);
        GameState gameState = new GameState();
        gameState.setSpace(space);
        gameState.setCharacters(new HashMap<>());
        return new GameEntry(gameState);
    }

    private static class GameEntry {
        private static final double DIAGONAL_MOVEMENT_SCALING = 0.707107;
        private static final double SPEED_SCALING = 0.005;
        private static final long MOVEMENT_DURATION_PER_INPUT = 25;
        private static final double CHARACTER_LENGTH = 1;
        private long lastProcessTime;
        private final List<GameInput> inputQueue = new ArrayList<>();
        private final Lock inputQueueLock = new ReentrantLock();
        private final GameState gameState;
        private final Lock gameStateReadLock;
        private final Lock gameStateWriteLock;

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
            return applyPerspectives(commonGameState);
        }

        public void process() {
            long currentTime = currentTimeMillis();
            List<GameInput> inputs = cloneAndClearInputQueue();
            gameStateWriteLock.lock();
            try {
                for (GameInput input : inputs) {
                    processInput(gameState, input);
                }
                processDuration(gameState, currentTime - lastProcessTime);
            }
            finally {
                gameStateWriteLock.unlock();
            }
            lastProcessTime = currentTime;
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

        private static void processDuration(GameState gameState, long duration) {
            double spaceWidth = gameState.getSpace().getWidth();
            double spaceHeight = gameState.getSpace().getHeight();
            for (Character character : gameState.getCharacters().values()) {
                long movementDuration = Math.min(character.getPendingMovementDuration(), duration);
                if (movementDuration > 0) {
                    double movementDistance = SPEED_SCALING * character.getMovementSpeed() * movementDuration;
                    double deltaPosX = 0;
                    double deltaPosY = 0;
                    switch (character.getPendingMovementDirection()) {
                        case UP -> {
                            deltaPosY = -1 * movementDistance;
                        }
                        case UP_RIGHT -> {
                            deltaPosX = DIAGONAL_MOVEMENT_SCALING * movementDistance;
                            deltaPosY = -1 * DIAGONAL_MOVEMENT_SCALING * movementDistance;
                        }
                        case RIGHT -> {
                            deltaPosX = movementDistance;
                        }
                        case DOWN_RIGHT -> {
                            deltaPosX = DIAGONAL_MOVEMENT_SCALING * movementDistance;
                            deltaPosY = DIAGONAL_MOVEMENT_SCALING * movementDistance;
                        }
                        case DOWN -> {
                            deltaPosY = movementDistance;
                        }
                        case DOWN_LEFT -> {
                            deltaPosX = -1 * DIAGONAL_MOVEMENT_SCALING * movementDistance;
                            deltaPosY = DIAGONAL_MOVEMENT_SCALING * movementDistance;
                        }
                        case LEFT -> {
                            deltaPosX = -1 * movementDistance;
                        }
                        case UP_LEFT -> {
                            deltaPosX = -1 * DIAGONAL_MOVEMENT_SCALING * movementDistance;
                            deltaPosY = -1 * DIAGONAL_MOVEMENT_SCALING * movementDistance;
                        }
                    }
                    double posX = character.getPosX() + deltaPosX;
                    if (posX < 0) {
                        posX = 0;
                    }
                    else if (posX + character.getWidth() > spaceWidth) {
                        posX = spaceWidth - character.getWidth();
                    }
                    double posY = character.getPosY() + deltaPosY;
                    if (posY < 0) {
                        posY = 0;
                    }
                    else if (posY + character.getHeight() > spaceHeight) {
                        posY = spaceHeight - character.getHeight();
                    }
                    character.setPosX(posX);
                    character.setPosY(posY);
                    character.setPendingMovementDuration(character.getPendingMovementDuration() - movementDuration);
                }
            }
        }

        private static void processInput(GameState gameState, GameInput input) {
            switch (input.getType()) {
                case JOIN -> {
                    if (gameState.getCharacters().containsKey(input.getPlayerId())) {
                        return;
                    }
                    double posX = (gameState.getSpace().getWidth() - CHARACTER_LENGTH) / 2;
                    double posY = (gameState.getSpace().getHeight() - CHARACTER_LENGTH) / 2;
                    Character character = new Character();
                    character.setPlayerId(input.getPlayerId());
                    character.setWidth(CHARACTER_LENGTH);
                    character.setHeight(CHARACTER_LENGTH);
                    character.setPosX(posX);
                    character.setPosY(posY);
                    character.setOrientationDirection(Direction.DOWN);
                    character.setMovementSpeed(1);
                    gameState.getCharacters().put(input.getPlayerId(), character);
                }
                case LEAVE -> {
                    gameState.getCharacters().remove(input.getPlayerId());
                }
                case MOVE -> {
                    if (input.getMovementDirection() != null) {
                        Character character = gameState.getCharacters().get(input.getPlayerId());
                        if (character == null) {
                            return;
                        }
                        character.setPendingMovementDirection(input.getMovementDirection());
                        character.setPendingMovementDuration(MOVEMENT_DURATION_PER_INPUT);
                        character.setOrientationDirection(input.getMovementDirection());
                    }
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
                characterClone.setPendingMovementDirection(character.getPendingMovementDirection());
                characterClone.setPendingMovementDuration(character.getPendingMovementDuration());
                characterClone.setMovementSpeed(character.getMovementSpeed());
                characterClone.setOrientationDirection(character.getOrientationDirection());
                characters.put(playerId, characterClone);
            }
            GameState clone = new GameState();
            clone.setClientPlayerId(gameState.getClientPlayerId());
            clone.setSpace(space);
            clone.setCharacters(characters);
            return clone;
        }
    }
}
