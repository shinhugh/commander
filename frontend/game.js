/* Requires:
 * - api.js
 */

const game = {

  internal: {

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
      const character = game.internal.gameState.characters[game.internal.gameState.clientPlayerId];
      const spaceWidth = game.internal.gameState.space.width;
      const spaceHeight = game.internal.gameState.space.height;
      const currentTime = Date.now();
      const duration = currentTime - game.internal.lastGameStateProcessTime;
      let distance = character.movementSpeed * duration * 0.002;
      const diagonalScaling = 0.707107;
      switch (game.internal.directionInput) {
        case 'up':
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY - distance));
          break;
        case 'up_right':
          distance *= diagonalScaling;
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX + distance));
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY - distance));
          break;
        case 'right':
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX + distance));
          break;
        case 'down_right':
          distance *= diagonalScaling;
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX + distance));
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY + distance));
          break;
        case 'down':
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY + distance));
          break;
        case 'down_left':
          distance *= diagonalScaling;
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX - distance));
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY + distance));
          break;
        case 'left':
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX - distance));
          break;
        case 'up_left':
          distance *= diagonalScaling;
          character.posX = Math.max(0, Math.min(spaceWidth - character.width, character.posX - distance));
          character.posY = Math.max(0, Math.min(spaceHeight - character.height, character.posY - distance));
          break;
      }
      game.internal.lastGameStateProcessTime = currentTime;
      game.internal.invokeGameStateChangeHandlers();
      api.sendGameInput({
        type: 'position',
        posX: character.posX,
        posY: character.posY,
        orientation: game.internal.directionInput
      });
    },

    startProcessing: () => {
      if (game.internal.gameStateProcessingInterval != null) {
        return;
      }
      game.internal.lastGameStateProcessTime = Date.now();
      game.internal.process();
      game.internal.gameStateProcessingInterval = setInterval(game.internal.process, 15);
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