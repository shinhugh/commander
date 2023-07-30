package org.dev.commander.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.commander.model.IncomingMessage;
import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.model.game.GameInput;
import org.dev.commander.model.game.GameState;
import org.dev.commander.model.game.Space;
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
    private static final long PROCESS_INTERVAL = 5000; // TODO: Set to ~50
    private static final long BROADCAST_INTERVAL = 8000; // TODO: Set to ~100
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
        for (String sessionToken : sessionTokens) {
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
        Long playerId = input.getPlayerId();
        if (playerId == null || playerId <= 0) {
            return;
        }
        // TODO: Verify that authentication matches with playerId
        game.input(input);
    }

    private static GameEntry generateGameEntry() {
        Space space = new Space();
        space.setWidth(25);
        space.setHeight(25);
        GameState gameState = new GameState();
        // TODO: Initialize game state
        gameState.setPlayers(new HashSet<>());
        gameState.setSpace(space);
        gameState.setCharacters(new HashSet<>());
        return new GameEntry(gameState);
    }

    private static class GameEntry {
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
            // TODO: Process `gameState` according to the passage of `duration` ms
        }

        private static void processInput(GameState gameState, GameInput input) {
            // TODO: Apply changes to `gameState` as described by `input`
        }

        private static GameState cloneGameState(GameState gameState) {
            if (gameState == null) {
                return null;
            }
            GameState clone = new GameState();
            // TODO: Deep copy `gameState`
            return clone;
        }
    }
}
