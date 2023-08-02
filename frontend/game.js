/* Requires:
 * - api.js
 */

const game = {

  internal: {

    diagonalScaling: 0.707107,

    characterSpeedScaling: 0.005,

    collisionMargin: 0.0001,

    distanceMargin: 0.06,

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

    calculateDistance: (pointA, pointB) => {
      return Math.sqrt(Math.pow(pointA.posX - pointB.posX, 2) + Math.pow(pointA.posY - pointB.posY, 2));
    },

    verifyInBounds: (matter, space, margin) => {
      return matter.posX >= margin && matter.posX <= space.width - margin && matter.posY >= margin && matter.posY <= space.height - margin;
    },

    resolveCollision: (unmovableMatter, movableMatter) => {
      const unmovableMatterXLower = unmovableMatter.posX - game.internal.collisionMargin;
      const unmovableMatterXUpper = unmovableMatter.posX + unmovableMatter.width + game.internal.collisionMargin;
      const unmovableMatterYLower = unmovableMatter.posY - game.internal.collisionMargin;
      const unmovableMatterYUpper = unmovableMatter.posY + unmovableMatter.height + game.internal.collisionMargin;
      const movableMatterXLower = movableMatter.posX;
      const movableMatterXUpper = movableMatter.posX + movableMatter.width;
      const movableMatterYLower = movableMatter.posY;
      const movableMatterYUpper = movableMatter.posY + movableMatter.height;
      const overlap = movableMatterXUpper > unmovableMatterXLower && movableMatterXLower < unmovableMatterXUpper && movableMatterYUpper > unmovableMatterYLower && movableMatterYLower < unmovableMatterYUpper;
      if (!overlap) {
        return;
      }
      const distanceUp = movableMatterYUpper - unmovableMatterYLower;
      const distanceRight = unmovableMatterXUpper - movableMatterXLower;
      const distanceDown = unmovableMatterYUpper - movableMatterYLower;
      const distanceLeft = movableMatterXUpper - unmovableMatterXLower;
      const distanceMin = Math.min(distanceUp, distanceRight, distanceDown, distanceLeft);
      if (distanceMin === distanceUp) {
        movableMatter.posY -= distanceMin;
      } else if (distanceMin === distanceRight) {
        movableMatter.posX += distanceMin;
      } else if (distanceMin === distanceDown) {
        movableMatter.posY += distanceMin;
      } else {
        movableMatter.posX -= distanceMin;
      }
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
      proposedState.posX = Math.max(game.internal.collisionMargin, Math.min(spaceWidth - proposedState.width - game.internal.collisionMargin, proposedState.posX));
      proposedState.posY = Math.max(game.internal.collisionMargin, Math.min(spaceHeight - proposedState.height - game.internal.collisionMargin, proposedState.posY));
      for (const obstacle of obstacles) {
        game.internal.resolveCollision(obstacle, proposedState);
      }
      if (!game.internal.verifyInBounds(proposedState, game.internal.gameState.space, game.internal.collisionMargin) || game.internal.calculateDistance(clientCharacter, proposedState) - distance > game.internal.distanceMargin) {
        proposedState.posX = clientCharacter.posX;
        proposedState.posY = clientCharacter.posY;
      }
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