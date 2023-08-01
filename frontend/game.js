/* Requires:
 * - api.js
 */

const game = {

  internal: {

    gameState: null,

    gameStateChangeHandlers: [ ],

    lastDirectionalGameInput: null,

    directionalGameInputInterval: null,

    invokeGameStateChangeHandlers: () => {
      for (const handler of game.internal.gameStateChangeHandlers) {
        handler();
      }
    },

    handleIncomingMessage: (message) => {
      if (message.type !== 'game_snapshot') {
        return;
      }
      game.internal.gameState = message.payload;
      game.internal.invokeGameStateChangeHandlers();
    },

    handleClosedConnection: () => {
      game.internal.gameState = null;
      game.internal.lastDirectionalGameInput = null;
      clearInterval(game.internal.directionalGameInputInterval);
      game.internal.directionalGameInputInterval = null;
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
  },

  setCharacterMovement: (direction) => {
    if (game.internal.gameState == null) {
      return;
    }
    if (direction === game.internal.lastDirectionalGameInput) {
      return;
    }
    clearInterval(game.internal.directionalGameInputInterval);
    game.internal.lastDirectionalGameInput = direction;
    if (game.internal.lastDirectionalGameInput != null) {
      const gameInput = {
        type: 'move',
        movementDirection: game.internal.lastDirectionalGameInput
      };
      api.sendGameInput(gameInput);
      game.internal.directionalGameInputInterval = setInterval(() => {
        api.sendGameInput(gameInput);
      }, 15);
    }
  }

};

game.initialize();