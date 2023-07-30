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
    private final MessageBroker messageBroker;
    private final IdentificationService identificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameEntry game = generateGameEntry();
    private final Map<Long, String> accountIdToSessionTokenMap = new HashMap<>();
    private final Lock accountIdToSessionTokenMapReadLock;
    private final Lock accountIdToSessionTokenMapWriteLock;

    public GameManager(MessageBroker messageBroker, IdentificationService identificationService) {
        this.messageBroker = messageBroker;
        this.identificationService = identificationService;
        ReadWriteLock accountIdToSessionTokenMapReadWriteLock = new ReentrantReadWriteLock();
        accountIdToSessionTokenMapReadLock = accountIdToSessionTokenMapReadWriteLock.readLock();
        accountIdToSessionTokenMapWriteLock = accountIdToSessionTokenMapReadWriteLock.writeLock();
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
        accountIdToSessionTokenMapWriteLock.lock();
        try {
            if (Objects.equals(accountIdToSessionTokenMap.get(accountId), sessionToken)) {
                accountIdToSessionTokenMap.remove(accountId);
            }
        }
        finally {
            accountIdToSessionTokenMapWriteLock.unlock();
        }
        // TODO: Game state logic (e.g. remove character)
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
        GameState snapshot = game.snapshot();
        OutgoingMessage<GameState> message = new OutgoingMessage<>();
        message.setType(OutgoingMessage.Type.GAME_SNAPSHOT);
        message.setPayload(snapshot);
        Set<String> sessionTokens;
        accountIdToSessionTokenMapReadLock.lock();
        try {
            sessionTokens = new HashSet<>(accountIdToSessionTokenMap.values());
        }
        finally {
            accountIdToSessionTokenMapReadLock.unlock();
        }
        for (String sessionToken : sessionTokens) { // TODO: Parallelize
            messageBroker.sendMessageBySessionToken(sessionToken, message);
        }
    }

    private void handleGameJoin(Authentication authentication) {
        long accountId = identificationService.identifyAccount(authentication).getId();
        String sessionToken = (String) authentication.getCredentials();
        String evictedSessionToken;
        accountIdToSessionTokenMapWriteLock.lock();
        try {
            evictedSessionToken = accountIdToSessionTokenMap.get(accountId);
            accountIdToSessionTokenMap.put(accountId, sessionToken);
        }
        finally {
            accountIdToSessionTokenMapWriteLock.unlock();
        }
        // TODO: Game state logic (e.g. spawn character)
        if (evictedSessionToken != null) {
            OutgoingMessage<Void> message = new OutgoingMessage<>();
            message.setType(OutgoingMessage.Type.GAME_EVICTION);
            messageBroker.sendMessageBySessionToken(evictedSessionToken, message);
        }
    }

    private void handleGameInput(Authentication authentication, GameInput input) {
        long accountId = identificationService.identifyAccount(authentication).getId();
        String sessionToken = (String) authentication.getCredentials();
        if (!Objects.equals(accountIdToSessionTokenMap.get(accountId), sessionToken)) {
            return;
        }
        // TODO: Use authentication to populate playerId
        input.setPlayerId(1L); // TODO: For testing only
        game.input(input);
    }

    private static GameEntry generateGameEntry() {
        // TODO: Populate GameEntry from persistent storage
        return generateMockGameEntry(); // TODO: For testing only
    }

    // TODO: For testing only
    private static GameEntry generateMockGameEntry() {
        Player player = new Player();
        player.setId(1);
        player.setAccountId(1);
        Set<Player> players = new HashSet<>();
        players.add(player);
        Space space = new Space();
        space.setWidth(8);
        space.setHeight(8);
        Character character = new Character();
        character.setPlayerId(1);
        character.setWidth(1);
        character.setHeight(1);
        character.setMovementSpeed(1);
        Map<Long, Character> characters = new HashMap<>();
        characters.put(player.getId(), character);
        GameState gameState = new GameState();
        gameState.setPlayers(players);
        gameState.setSpace(space);
        gameState.setCharacters(characters);
        return new GameEntry(gameState);
    }

    private static class GameEntry {
        private static final double DIAGONAL_MOVEMENT_SCALING = 0.707107;
        private static final double SPEED_SCALING = 0.005;
        private static final long MOVEMENT_DURATION_PER_INPUT = 25;
        private long lastProcessTime;
        private final List<GameInput> inputQueue = new ArrayList<>();
        private final Lock inputQueueLock = new ReentrantLock();
        private final GameState gameState;
        private final Lock gameStateReadLock;
        private final Lock gameStateWriteLock;

        public GameEntry(GameState gameState) {
            this.gameState = gameState;
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

        public GameState snapshot() {
            gameStateReadLock.lock();
            try {
                return cloneGameState(gameState);
            }
            finally {
                gameStateReadLock.unlock();
            }
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

        private static GameState cloneGameState(GameState gameState) {
            if (gameState == null) {
                return null;
            }
            GameState clone = new GameState();
            Set<Player> players = new HashSet<>();
            for (Player player : gameState.getPlayers()) {
                Player playerClone = new Player();
                playerClone.setId(player.getId());
                playerClone.setAccountId(player.getAccountId());;
                players.add(playerClone);
            }
            clone.setPlayers(players);
            Space space = new Space();
            space.setWidth(gameState.getSpace().getWidth());
            space.setHeight(gameState.getSpace().getHeight());
            clone.setSpace(space);
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
            clone.setCharacters(characters);
            return clone;
        }
    }
}
