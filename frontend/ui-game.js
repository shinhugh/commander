/* Requires:
 * - game.js
 * - ui-api.js
 * - ui-base.js
 */

const uiGame = {

  internal: {

    elements: {
      root: document.getElementById('content_game_module_game'),
      scene: {
        root: document.getElementById('content_game_module_game_scene'),
        field: document.getElementById('content_game_module_game_scene_field')
      },
      overlay: {
        root: document.getElementById('content_game_module_game_overlay'),
        seatLossPage: {
          root: document.getElementById('content_game_module_game_overlay_seat_loss_page'),
          reconnectButton: document.getElementById('content_game_module_game_overlay_seat_loss_page_reconnect_button')
        },
        integrityViolationPage: {
          root: document.getElementById('content_game_module_game_overlay_integrity_violation_page'),
          reconnectButton: document.getElementById('content_game_module_game_overlay_integrity_violation_page_reconnect_button')
        }
      }
    },

    state: {
      zoom: 1.1,
      fieldRotation: 70,
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
      game.registerGameStateChangeHandler(uiGame.internal.handleGameStateChange);
      game.registerGameSeatLossHandler(uiGame.internal.handleGameSeatLoss);
      game.registerGameIntegrityViolationHandler(uiGame.internal.handleGameIntegrityViolation);
      uiBase.registerModuleChangeHandler(uiGame.internal.handleModuleChange);
      uiBase.registerOverlayAppearanceHandler(uiGame.internal.handleOverlayAppearance);
      uiBase.registerOverlayDisappearanceHandler(uiGame.internal.handleOverlayDisappearance);
    },

    registerUiHandlers: () => {
      uiGame.internal.elements.overlay.seatLossPage.reconnectButton.addEventListener('click', () => {
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
        game.joinGame();
        uiGame.internal.enableControls();
      });
      uiGame.internal.elements.overlay.integrityViolationPage.reconnectButton.addEventListener('click', () => {
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
        game.joinGame();
        uiGame.internal.enableControls();
      });
    },

    clearUi: () => {
      uiApi.hide(uiGame.internal.elements.overlay.root);
      uiApi.hide(uiGame.internal.elements.overlay.seatLossPage.root);
      uiApi.hide(uiGame.internal.elements.overlay.integrityViolationPage.root);
    },

    hideGameOverlay: () => {
      uiApi.hide(uiGame.internal.elements.overlay.root);
    },

    showGameOverlay: () => {
      uiApi.show(uiGame.internal.elements.overlay.root);
    },

    clearGameOverlay: () => {
      for (const child of uiGame.internal.elements.overlay.root.children) {
        uiApi.hide(child);
      }
    },

    showGameSeatLossPage: () => {
      uiGame.internal.clearGameOverlay();
      uiApi.show(uiGame.internal.elements.overlay.seatLossPage.root);
    },

    showGameIntegrityViolationPage: () => {
      uiGame.internal.clearGameOverlay();
      uiApi.show(uiGame.internal.elements.overlay.integrityViolationPage.root);
    },

    updateFieldElement: (spaceModel, clientCharacterModel) => {
      const fieldElement = uiGame.internal.elements.scene.field;
      const gameUnitToPixelMultiplier = fieldElement.offsetHeight / spaceModel.height;
      const sceneElementWidth = uiGame.internal.elements.scene.root.offsetWidth;
      const fieldElementWidth = gameUnitToPixelMultiplier * spaceModel.width;
      const clientCharacterElementWidth = gameUnitToPixelMultiplier * clientCharacterModel.width;
      const translateX = (sceneElementWidth - clientCharacterElementWidth) / 2 + (clientCharacterElementWidth - fieldElementWidth) / (spaceModel.width - clientCharacterModel.width) * clientCharacterModel.posX;
      fieldElement.style.width = fieldElementWidth + 'px';
      fieldElement.style.transform = 'translateX(' + translateX + 'px) translateY(40%) rotateX(' + uiGame.internal.state.fieldRotation + 'deg) translateY(50%)';
      return gameUnitToPixelMultiplier;
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
          uiGame.internal.elements.scene.field.appendChild(characterElement);
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
        if (characterModel.moving) {
          characterElement.dataset.moving = null;
        } else {
          delete characterElement.dataset.moving;
        }
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
          uiGame.internal.elements.scene.field.appendChild(obstacleElement);
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

    enableControls: () => {
      document.addEventListener('keydown', uiGame.internal.handleKeyDown);
      document.addEventListener('keyup', uiGame.internal.handleKeyUp);
    },

    disableControls: () => {
      document.removeEventListener('keydown', uiGame.internal.handleKeyDown);
      document.removeEventListener('keyup', uiGame.internal.handleKeyUp);
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

    handleGameStateChange: () => {
      const snapshot = game.getGameState();
      if (snapshot == null) {
        uiGame.internal.elements.scene.field.innerHTML = null;
        uiGame.internal.state.characterElements = { };
        uiGame.internal.state.obstacleElements = { };
        return;
      }
      const clientCharacter = snapshot.characters[snapshot.clientPlayerId];
      if (clientCharacter == null) {
        return;
      }
      const scale = uiGame.internal.updateFieldElement(snapshot.space, clientCharacter);
      uiGame.internal.updateCharacterElements(Object.values(snapshot.characters), scale);
      uiGame.internal.updateObstacleElements(snapshot.obstacles, scale);
    },

    handleGameSeatLoss: () => {
      uiGame.internal.disableControls();
      uiGame.internal.showGameSeatLossPage();
      uiGame.internal.showGameOverlay();
    },

    handleGameIntegrityViolation: () => {
      uiGame.internal.disableControls();
      uiGame.internal.showGameIntegrityViolationPage();
      uiGame.internal.showGameOverlay();
    },

    handleModuleChange: () => {
      if (uiBase.getCurrentModule() === 'game') {
        game.joinGame();
        uiGame.internal.enableControls();
      } else {
        uiGame.internal.disableControls();
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
      }
    },

    handleOverlayAppearance: () => {
      uiGame.internal.disableControls();
    },

    handleOverlayDisappearance: () => {
      if (uiBase.getCurrentModule() !== 'game') {
        return;
      }
      uiGame.internal.enableControls();
    }

  },

  initialize: () => {
    uiGame.internal.clearUi();
    uiGame.internal.registerApiHandlers();
    uiGame.internal.registerUiHandlers();
  }

};

uiGame.initialize();