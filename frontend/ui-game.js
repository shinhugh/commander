/* Requires:
 * - auth.js
 * - game.js
 * - ui-base.js
 */

const uiGame = {

  internal: {

    elements: {
      root: document.getElementById('content_game_module_game'),
      map: document.getElementById('content_game_module_game_map')
    },

    state: {
      zoom: 1.1,
      keyWPressed: false,
      keyWPressTime: null,
      keyAPressed: false,
      keyAPressTime: null,
      keySPressed: false,
      keySPressTime: null,
      keyDPressed: false,
      keyDPressTime: null,
      characterElements: { },
      obstacleElements: { }
    },

    registerApiHandlers: () => {
      auth.registerLogoutHandler(uiGame.internal.handleLogout);
      game.registerGameStateChangeHandler(uiGame.internal.handleGameStateChange);
      uiBase.registerModuleChangeHandler(uiGame.internal.handleModuleChange);
      uiBase.registerOverlayAppearanceHandler(uiGame.internal.handleOverlayAppearance);
      uiBase.registerOverlayDisappearanceHandler(uiGame.internal.handleOverlayDisappearance);
    },

    updateDirectionInput: () => {
      let vertical = 0;
      if (uiGame.internal.state.keyWPressed && uiGame.internal.state.keySPressed) {
        if (uiGame.internal.state.keyWPressTime <= uiGame.internal.state.keySPressTime) {
          vertical = 1;
        } else {
          vertical = -1;
        }
      } else if (uiGame.internal.state.keyWPressed) {
        vertical = -1;
      } else if (uiGame.internal.state.keySPressed) {
        vertical = 1;
      }
      let horizontal = 0;
      if (uiGame.internal.state.keyAPressed && uiGame.internal.state.keyDPressed) {
        if (uiGame.internal.state.keyAPressTime <= uiGame.internal.state.keyDPressTime) {
          horizontal = 1;
        } else {
          horizontal = -1;
        }
      } else if (uiGame.internal.state.keyAPressed) {
        horizontal = -1;
      } else if (uiGame.internal.state.keyDPressed) {
        horizontal = 1;
      }
      let direction = null;
      if (vertical < 0) {
        if (horizontal < 0) {
          direction = 'up_left';
        } else if (horizontal > 0) {
          direction = 'up_right';
        } else {
          direction = 'up';
        }
      } else if (vertical > 0) {
        if (horizontal < 0) {
          direction = 'down_left';
        } else if (horizontal > 0) {
          direction = 'down_right';
        } else {
          direction = 'down';
        }
      } else {
        if (horizontal < 0) {
          direction = 'left';
        } else if (horizontal > 0) {
          direction = 'right';
        }
      }
      game.setDirectionInput(direction);
    },

    resetDirectionInput: () => {
      uiGame.internal.state.keyWPressed = false;
      uiGame.internal.state.keyWPressTime = null;
      uiGame.internal.state.keyAPressed = false;
      uiGame.internal.state.keyAPressTime = null;
      uiGame.internal.state.keySPressed = false;
      uiGame.internal.state.keySPressTime = null;
      uiGame.internal.state.keyDPressed = false;
      uiGame.internal.state.keyDPressTime = null;
      game.setDirectionInput(null);
    },

    updateMapElement: (spaceModel, clientCharacterModel) => {
      const rootHeight = uiGame.internal.elements.root.offsetHeight;
      const rootWidth = uiGame.internal.elements.root.offsetWidth;
      const spaceHeight = spaceModel.height;
      const spaceWidth = spaceModel.width;
      const scale = uiGame.internal.state.zoom * Math.max((rootHeight / spaceHeight), (rootWidth / spaceWidth));
      const mapHeight = scale * spaceHeight;
      const mapWidth = scale * spaceWidth;
      const mapTop = ((rootHeight - mapHeight) / (spaceHeight - clientCharacterModel.height)) * clientCharacterModel.posY;
      const mapLeft = ((rootWidth - mapWidth) / (spaceWidth - clientCharacterModel.width)) * clientCharacterModel.posX;
      uiGame.internal.elements.map.style.height = mapHeight + 'px';
      uiGame.internal.elements.map.style.width = mapWidth + 'px';
      uiGame.internal.elements.map.style.top = mapTop + 'px';
      uiGame.internal.elements.map.style.left = mapLeft + 'px';
      return scale;
    },

    updateCharacterElements: (characterModels, scale) => {
      let cachedCharacterIds = new Set(Object.keys(uiGame.internal.state.characterElements));
      for (const characterModel of characterModels) {
        let characterElement;
        if (cachedCharacterIds.has(characterModel.id.toString())) {
          characterElement = uiGame.internal.state.characterElements[characterModel.id];
          cachedCharacterIds.delete(characterModel.id.toString());
        } else {
          const spriteElement = document.createElement('div');
          spriteElement.classList.add('game_character_sprite');
          characterElement = document.createElement('div');
          characterElement.classList.add('game_character');
          characterElement.appendChild(spriteElement);
          uiGame.internal.elements.map.appendChild(characterElement);
          uiGame.internal.state.characterElements[characterModel.id] = characterElement;
        }
        characterElement.style.top = (scale * characterModel.posY) + 'px';
        characterElement.style.left = (scale * characterModel.posX) + 'px';
        characterElement.style.height = (scale * characterModel.height) + 'px';
        characterElement.style.width = (scale * characterModel.width) + 'px';
        switch (characterModel.orientation) {
          case 'up_right':
          case 'right':
          case 'down_right':
            characterElement.dataset.right = null;
            break;
          case 'down_left':
          case 'left':
          case 'up_left':
            delete characterElement.dataset.right;
            break;
        }
        // TODO: Set 'moving' data attribute on characterElement if character is moving
      }
      cachedCharacterIds.forEach(id => {
        uiGame.internal.state.characterElements[id].remove();
        delete uiGame.internal.state.characterElements[id];
      });
    },

    updateObstacleElements: (obstacleModels, scale) => {
      let cachedObstacleIds = new Set(Object.keys(uiGame.internal.state.obstacleElements));
      for (const obstacleModel of obstacleModels) {
        let obstacleElement;
        if (cachedObstacleIds.has(obstacleModel.id.toString())) {
          obstacleElement = uiGame.internal.state.obstacleElements[obstacleModel.id];
          cachedObstacleIds.delete(obstacleModel.id.toString());
        } else {
          obstacleElement = document.createElement('div');
          obstacleElement.classList.add('game_obstacle');
          uiGame.internal.elements.map.appendChild(obstacleElement);
          uiGame.internal.state.obstacleElements[obstacleModel.id] = obstacleElement;
        }
        obstacleElement.style.top = (scale * obstacleModel.posY) + 'px';
        obstacleElement.style.left = (scale * obstacleModel.posX) + 'px';
        obstacleElement.style.height = (scale * obstacleModel.height) + 'px';
        obstacleElement.style.width = (scale * obstacleModel.width) + 'px';
        cachedObstacleIds.forEach(id => {
          uiGame.internal.state.obstacleElements[id].remove();
          delete uiGame.internal.state.obstacleElements[id];
        });
      }
    },

    handleKeyDown: (e) => {
      switch (e.key) {
        case 'w':
          if (!uiGame.internal.state.keyWPressed) {
            uiGame.internal.state.keyWPressTime = Date.now();
            uiGame.internal.state.keyWPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          break;
        case 'a':
          if (!uiGame.internal.state.keyAPressed) {
            uiGame.internal.state.keyAPressTime = Date.now();
            uiGame.internal.state.keyAPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          break;
        case 's':
          if (!uiGame.internal.state.keySPressed) {
            uiGame.internal.state.keySPressTime = Date.now();
            uiGame.internal.state.keySPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          break;
        case 'd':
          if (!uiGame.internal.state.keyDPressed) {
            uiGame.internal.state.keyDPressTime = Date.now();
            uiGame.internal.state.keyDPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          break;
      }
    },

    handleKeyUp: (e) => {
      switch (e.key) {
        case 'w':
          uiGame.internal.state.keyWPressed = false;
          uiGame.internal.state.keyWPressTime = null;
          uiGame.internal.updateDirectionInput();
          break;
        case 'a':
          uiGame.internal.state.keyAPressed = false;
          uiGame.internal.state.keyAPressTime = null;
          uiGame.internal.updateDirectionInput();
          break;
        case 's':
          uiGame.internal.state.keySPressed = false;
          uiGame.internal.state.keySPressTime = null;
          uiGame.internal.updateDirectionInput();
          break;
        case 'd':
          uiGame.internal.state.keyDPressed = false;
          uiGame.internal.state.keyDPressTime = null;
          uiGame.internal.updateDirectionInput();
          break;
      }
    },

    handleLogout: () => {
      uiGame.internal.state.keyWPressed = false;
      uiGame.internal.state.keyWPressTime = null;
      uiGame.internal.state.keyAPressed = false;
      uiGame.internal.state.keyAPressTime = null;
      uiGame.internal.state.keySPressed = false;
      uiGame.internal.state.keySPressTime = null;
      uiGame.internal.state.keyDPressed = false;
      uiGame.internal.state.keyDPressTime = null;
      uiGame.internal.state.characterElements = { };
    },

    handleGameStateChange: () => {
      const snapshot = game.getGameState();
      if (snapshot == null) {
        uiGame.internal.elements.map.innerHTML = null;
        return;
      }
      const clientCharacter = snapshot.characters[snapshot.clientPlayerId];
      if (clientCharacter == null) {
        return;
      }
      const scale = uiGame.internal.updateMapElement(snapshot.space, clientCharacter);
      uiGame.internal.updateCharacterElements(Object.values(snapshot.characters), scale);
      uiGame.internal.updateObstacleElements(snapshot.obstacles, scale);
    },

    handleModuleChange: () => {
      if (uiBase.getCurrentModule() === 'game') {
        game.joinGame();
        document.addEventListener('keydown', uiGame.internal.handleKeyDown);
        document.addEventListener('keyup', uiGame.internal.handleKeyUp);
      } else {
        document.removeEventListener('keydown', uiGame.internal.handleKeyDown);
        document.removeEventListener('keyup', uiGame.internal.handleKeyUp);
        uiGame.internal.resetDirectionInput();
      }
    },

    handleOverlayAppearance: () => {
      document.removeEventListener('keydown', uiGame.internal.handleKeyDown);
      document.removeEventListener('keyup', uiGame.internal.handleKeyUp);
      uiGame.internal.resetDirectionInput();
    },

    handleOverlayDisappearance: () => {
      if (uiBase.getCurrentModule() !== 'game') {
        return;
      }
      document.addEventListener('keydown', uiGame.internal.handleKeyDown);
      document.addEventListener('keyup', uiGame.internal.handleKeyUp);
    }

  },

  initialize: () => {
    uiGame.internal.registerApiHandlers();
  }

};

uiGame.initialize();