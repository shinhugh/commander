/* Requires:
 * - api.js
 */

const game = {

  internal: {

    diagonalScaling: 0.707107,

    characterSpeedScaling: 0.005,

    collisionMargin: 0.0001,

    processTickTime: 15,

    gameState: null,

    directionInput: null,

    gameStateProcessingInterval: null,

    lastGameStateProcessTime: null,

    gameStateChangeHandlers: [ ],

    gameSeatLossHandlers: [ ],

    gameIntegrityViolationHandlers: [ ],

    invokeGameStateChangeHandlers: () => {
      for (const handler of game.internal.gameStateChangeHandlers) {
        handler();
      }
    },

    invokeGameSeatLossHandlers: () => {
      for (const handler of game.internal.gameSeatLossHandlers) {
        handler();
      }
    },

    invokeGameIntegrityViolationHandlers: () => {
      for (const handler of game.internal.gameIntegrityViolationHandlers) {
        handler();
      }
    },

    testForOverlap: (matterA, matterB) => {
      const matterAXLower = matterA.posX;
      const matterAXUpper = matterAXLower + matterA.width;
      const matterAYLower = matterA.posY;
      const matterAYUpper = matterAYLower + matterA.height;
      const matterBXLower = matterB.posX;
      const matterBXUpper = matterBXLower + matterB.width;
      const matterBYLower = matterB.posY;
      const matterBYUpper = matterBYLower + matterB.height;
      if (matterAXUpper >= matterBXLower - game.internal.collisionMargin && matterAXLower <= matterBXUpper + game.internal.collisionMargin) {
        return matterAYUpper >= matterBYLower - game.internal.collisionMargin && matterAYLower <= matterBYUpper + game.internal.collisionMargin;
      }
      return false;
    },

    process: () => {
      if (game.internal.gameState == null) {
        return;
      }
      const clientCharacter = game.internal.gameState.characters[game.internal.gameState.clientPlayerId];
      const spaceWidth = game.internal.gameState.space.width;
      const spaceHeight = game.internal.gameState.space.height;
      const obstacles = game.internal.gameState.obstacles;
      const currentTime = Date.now();
      const duration = currentTime - game.internal.lastGameStateProcessTime;
      let distance = clientCharacter.movementSpeed * duration * game.internal.characterSpeedScaling;
      const proposedPosition = {
        width: clientCharacter.width,
        height: clientCharacter.height,
        posX: clientCharacter.posX,
        posY: clientCharacter.posY,
      };
      switch (game.internal.directionInput) {
        case 'up':
          proposedPosition.posY -= distance;
          break;
        case 'up_right':
          distance *= game.internal.diagonalScaling;
          proposedPosition.posX += distance;
          proposedPosition.posY -= distance;
          break;
        case 'right':
          proposedPosition.posX += distance;
          break;
        case 'down_right':
          distance *= game.internal.diagonalScaling;
          proposedPosition.posX += distance;
          proposedPosition.posY += distance;
          break;
        case 'down':
          proposedPosition.posY += distance;
          break;
        case 'down_left':
          distance *= game.internal.diagonalScaling;
          proposedPosition.posX -= distance;
          proposedPosition.posY += distance;
          break;
        case 'left':
          proposedPosition.posX -= distance;
          break;
        case 'up_left':
          distance *= game.internal.diagonalScaling;
          proposedPosition.posX -= distance;
          proposedPosition.posY -= distance;
          break;
      }
      for (const obstacle of obstacles) {
        if (game.internal.testForOverlap(proposedPosition, obstacle)) {
          // TODO: Adjust proposedPosition such that it is just touching the
          //       obstacle but not overlapping
          proposedPosition.posX = clientCharacter.posX;
          proposedPosition.posY = clientCharacter.posY;
        }
      }
      proposedPosition.posX = Math.max(game.internal.collisionMargin, Math.min(spaceWidth - clientCharacter.width - game.internal.collisionMargin, proposedPosition.posX));
      proposedPosition.posY = Math.max(game.internal.collisionMargin, Math.min(spaceHeight - clientCharacter.height - game.internal.collisionMargin, proposedPosition.posY));
      const clientCharacterPositionChanged = clientCharacter.posX !== proposedPosition.posX || clientCharacter.posY !== proposedPosition.posY;
      if (clientCharacterPositionChanged) {
        clientCharacter.posX = proposedPosition.posX;
        clientCharacter.posY = proposedPosition.posY;
      }
      clientCharacter.orientation = game.internal.directionInput;
      clientCharacter.moving = clientCharacterPositionChanged;
      game.internal.lastGameStateProcessTime = currentTime;
      game.internal.invokeGameStateChangeHandlers();
      if (clientCharacterPositionChanged) {
        api.sendGameInput({
          type: 'position',
          posX: clientCharacter.posX,
          posY: clientCharacter.posY
        });
      }
    },

    startProcessing: () => {
      if (game.internal.gameStateProcessingInterval != null) {
        return;
      }
      game.internal.lastGameStateProcessTime = Date.now();
      game.internal.process();
      game.internal.gameStateProcessingInterval = setInterval(game.internal.process, game.internal.processTickTime);
    },

    stopProcessing: () => {
      clearInterval(game.internal.gameStateProcessingInterval);
      game.internal.gameStateProcessingInterval = null;
      game.internal.lastGameStateProcessTime = null;
      game.internal.gameState = null;
      game.internal.directionInput = null;
      game.internal.invokeGameStateChangeHandlers();
    },

    handleIncomingMessage: (message) => {
      if (message.type === 'game_snapshot') {
        const clientCharacter = game.internal.gameState?.characters[game.internal.gameState.clientPlayerId];
        game.internal.gameState = message.payload;
        if (clientCharacter != null) {
          game.internal.gameState.characters[game.internal.gameState.clientPlayerId] = clientCharacter;
        }
      } else if (message.type === 'game_seat_usurped') {
        game.internal.stopProcessing();
        game.internal.invokeGameSeatLossHandlers();
      } else if (message.type === 'game_integrity_violation') {
        game.internal.stopProcessing();
        game.internal.invokeGameIntegrityViolationHandlers();
      }
    },

    handleClosedConnection: () => {
      game.internal.stopProcessing();
    }

  },

  initialize: () => {
    api.registerIncomingMessageHandler(game.internal.handleIncomingMessage);
    api.registerClosedConnectionHandler(game.internal.handleClosedConnection);
  },

  registerGameStateChangeHandler: (handler) => {
    game.internal.gameStateChangeHandlers.push(handler);
  },

  registerGameSeatLossHandler: (handler) => {
    game.internal.gameSeatLossHandlers.push(handler);
  },

  registerGameIntegrityViolationHandler: (handler) => {
    game.internal.gameIntegrityViolationHandlers.push(handler);
  },

  getGameState: () => {
    return game.internal.gameState;
  },

  joinGame: () => {
    if (game.internal.gameState != null) {
      return;
    }
    api.sendGameJoin();
    game.internal.startProcessing();
  },

  setDirectionInput: (direction) => {
    if (game.internal.gameState == null) {
      return;
    }
    game.internal.directionInput = direction;
  }

};

game.initialize();