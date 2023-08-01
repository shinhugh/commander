/* Requires:
 * - api.js
 */

const game = {

  internal: {

    diagonalScaling: 0.707107,

    movementSpeedScaling: 0.005,

    processingInterval: 15,

    gameState: null,

    gameStateChangeHandlers: [ ],

    directionInput: null,

    gameStateProcessingInterval: null,

    lastGameStateProcessTime: null,

    invokeGameStateChangeHandlers: () => {
      for (const handler of game.internal.gameStateChangeHandlers) {
        handler();
      }
    },

    process: () => {
      if (game.internal.gameState == null) {
        return;
      }
      const clientCharacter = game.internal.gameState.characters[game.internal.gameState.clientPlayerId];
      const spaceWidth = game.internal.gameState.space.width;
      const spaceHeight = game.internal.gameState.space.height;
      const currentTime = Date.now();
      const duration = currentTime - game.internal.lastGameStateProcessTime;
      let distance = clientCharacter.movementSpeed * duration * game.internal.movementSpeedScaling;
      let clientCharacterPositionChanged = false;
      switch (game.internal.directionInput) {
        case 'up':
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY - distance));
          clientCharacterPositionChanged = true;
          break;
        case 'up_right':
          distance *= game.internal.diagonalScaling;
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX + distance));
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY - distance));
          clientCharacterPositionChanged = true;
          break;
        case 'right':
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX + distance));
          clientCharacterPositionChanged = true;
          break;
        case 'down_right':
          distance *= game.internal.diagonalScaling;
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX + distance));
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY + distance));
          clientCharacterPositionChanged = true;
          break;
        case 'down':
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY + distance));
          clientCharacterPositionChanged = true;
          break;
        case 'down_left':
          distance *= game.internal.diagonalScaling;
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX - distance));
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY + distance));
          clientCharacterPositionChanged = true;
          break;
        case 'left':
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX - distance));
          clientCharacterPositionChanged = true;
          break;
        case 'up_left':
          distance *= game.internal.diagonalScaling;
          clientCharacter.posX = Math.max(0, Math.min(spaceWidth - clientCharacter.width, clientCharacter.posX - distance));
          clientCharacter.posY = Math.max(0, Math.min(spaceHeight - clientCharacter.height, clientCharacter.posY - distance));
          clientCharacterPositionChanged = true;
          break;
      }
      clientCharacter.orientation = game.internal.directionInput;
      game.internal.lastGameStateProcessTime = currentTime;
      game.internal.invokeGameStateChangeHandlers();
      if (clientCharacterPositionChanged) {
        api.sendGameInput({
          type: 'position',
          posX: clientCharacter.posX,
          posY: clientCharacter.posY,
          orientation: clientCharacter.orientation
        });
      }
    },

    startProcessing: () => {
      if (game.internal.gameStateProcessingInterval != null) {
        return;
      }
      game.internal.lastGameStateProcessTime = Date.now();
      game.internal.process();
      game.internal.gameStateProcessingInterval = setInterval(game.internal.process, game.internal.processingInterval);
    },

    stopProcessing: () => {
      clearInterval(game.internal.gameStateProcessingInterval);
      game.internal.gameStateProcessingInterval = null;
      game.internal.lastGameStateProcessTime = null;
    },

    handleIncomingMessage: (message) => {
      if (message.type !== 'game_snapshot') {
        return;
      }
      let clientCharacter;
      if (game.internal.gameState != null) {
        clientCharacter = game.internal.gameState.characters[game.internal.gameState.clientPlayerId];
      }
      game.internal.gameState = message.payload;
      if (clientCharacter != null) {
        game.internal.gameState.characters[game.internal.gameState.clientPlayerId] = clientCharacter;
      }
    },

    handleClosedConnection: () => {
      game.internal.stopProcessing();
      game.internal.gameState = null;
      game.internal.directionInput = null;
      game.internal.invokeGameStateChangeHandlers();
    }

  },

  initialize: () => {
    api.registerIncomingMessageHandler(game.internal.handleIncomingMessage);
    api.registerClosedConnectionHandler(game.internal.handleClosedConnection);
  },

  registerGameStateChangeHandler: (handler) => {
    game.internal.gameStateChangeHandlers.push(handler);
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