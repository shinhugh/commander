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
      if (matterAXUpper > matterBXLower - game.internal.collisionMargin && matterAXLower < matterBXUpper + game.internal.collisionMargin) {
        return matterAYUpper > matterBYLower - game.internal.collisionMargin && matterAYLower < matterBYUpper + game.internal.collisionMargin;
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
      const proposedState = {
        width: clientCharacter.width,
        height: clientCharacter.height,
        posX: clientCharacter.posX,
        posY: clientCharacter.posY,
      };
      switch (game.internal.directionInput) {
        case 'up':
          proposedState.posY -= distance;
          break;
        case 'up_right':
          distance *= game.internal.diagonalScaling;
          proposedState.posX += distance;
          proposedState.posY -= distance;
          break;
        case 'right':
          proposedState.posX += distance;
          break;
        case 'down_right':
          distance *= game.internal.diagonalScaling;
          proposedState.posX += distance;
          proposedState.posY += distance;
          break;
        case 'down':
          proposedState.posY += distance;
          break;
        case 'down_left':
          distance *= game.internal.diagonalScaling;
          proposedState.posX -= distance;
          proposedState.posY += distance;
          break;
        case 'left':
          proposedState.posX -= distance;
          break;
        case 'up_left':
          distance *= game.internal.diagonalScaling;
          proposedState.posX -= distance;
          proposedState.posY -= distance;
          break;
      }
      for (const obstacle of obstacles) { // TODO: Consider case where there are multiple collisions
        if (game.internal.testForOverlap(proposedState, obstacle)) {
          let gapX, gapY;
          switch (game.internal.directionInput) { // TODO: Instead of movement direction, use which corners are contained within the obstacle
            case 'up':
              proposedState.posY = obstacle.posY + obstacle.height + game.internal.collisionMargin;
              break;
            case 'up_right':
              gapX = proposedState.posX + proposedState.width - obstacle.posX;
              gapY = obstacle.posY + obstacle.height - proposedState.posY;
              if (gapX >= gapY) {
                proposedState.posY = obstacle.posY + obstacle.height + game.internal.collisionMargin;
              } else {
                proposedState.posX = obstacle.posX - proposedState.width - game.internal.collisionMargin;
              }
              break;
            case 'right':
              proposedState.posX = obstacle.posX - proposedState.width - game.internal.collisionMargin;
              break;
            case 'down_right':
              gapX = proposedState.posX + proposedState.width - obstacle.posX;
              gapY = proposedState.posY + proposedState.height - obstacle.posY;
              if (gapX >= gapY) {
                proposedState.posY = obstacle.posY - proposedState.height - game.internal.collisionMargin;
              } else {
                proposedState.posX = obstacle.posX - proposedState.width - game.internal.collisionMargin;
              }
              break;
            case 'down':
              proposedState.posY = obstacle.posY - proposedState.height - game.internal.collisionMargin;
              break;
            case 'down_left':
              gapX = obstacle.posX + obstacle.width - proposedState.posX;
              gapY = proposedState.posY + proposedState.height - obstacle.posY;
              if (gapX >= gapY) {
                proposedState.posY = obstacle.posY - proposedState.height - game.internal.collisionMargin;
              } else {
                proposedState.posX = obstacle.posX + obstacle.width + game.internal.collisionMargin;
              }
              break;
            case 'left':
              proposedState.posX = obstacle.posX + obstacle.width + game.internal.collisionMargin;
              break;
            case 'up_left':
              gapX = obstacle.posX + obstacle.width - proposedState.posX;
              gapY = obstacle.posY + obstacle.height - proposedState.posY;
              if (gapX >= gapY) {
                proposedState.posY = obstacle.posY + obstacle.height + game.internal.collisionMargin;
              } else {
                proposedState.posX = obstacle.posX + obstacle.width + game.internal.collisionMargin;
              }
              break;
          }
        }
      }
      proposedState.posX = Math.max(game.internal.collisionMargin, Math.min(spaceWidth - proposedState.width - game.internal.collisionMargin, proposedState.posX));
      proposedState.posY = Math.max(game.internal.collisionMargin, Math.min(spaceHeight - proposedState.height - game.internal.collisionMargin, proposedState.posY));
      const clientCharacterPositionChanged = clientCharacter.posX !== proposedState.posX || clientCharacter.posY !== proposedState.posY;
      if (clientCharacterPositionChanged) {
        clientCharacter.posX = proposedState.posX;
        clientCharacter.posY = proposedState.posY;
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