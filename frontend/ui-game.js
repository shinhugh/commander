/* Requires:
 * - game.js
 * - ui-api.js
 * - ui-base.js
 * - 3js
 */

import { game } from './game';
import { uiApi } from './ui-api';
import { uiBase } from './ui-base';
import * as THREE from 'three';

 // TODO: Scaling from game units to 3js units? Currently 1:1
 // TODO: Resizing elements?
const uiGame = {

  internal: {

    elements: {
      root: document.getElementById('content_game_module_game'),
      scene: document.getElementById('content_game_module_game_scene'),
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
      keyWPressed: false,
      keyWPressTime: null,
      keyAPressed: false,
      keyAPressTime: null,
      keySPressed: false,
      keySPressTime: null,
      keyDPressed: false,
      keyDPressTime: null,
      renderer: null,
      scene: null,
      camera: null,
      fieldMesh: null,
      characterMeshes: { },
      obstacleMeshes: { }
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

    tearDownScene: () => {
      // TODO: Clean up 3js? dispose()?
      uiGame.internal.elements.scene.innerHTML = null;
      uiGame.internal.state.renderer = null;
      uiGame.internal.state.scene = null;
      uiGame.internal.state.camera = null;
      uiGame.internal.state.fieldMesh = null;
      uiGame.internal.state.characterMeshes = { };
      uiGame.internal.state.obstacleMeshes = { };
    },

    setUpScene: () => {
      if (uiGame.internal.state.renderer != null) {
        return;
      }
      const renderer = new THREE.WebGLRenderer();
      uiGame.internal.state.renderer = renderer;
      renderer.setSize(uiGame.internal.elements.scene.offsetWidth, uiGame.internal.elements.scene.offsetHeight);
      renderer.shadowMap.enabled = true;
      uiGame.internal.elements.scene.appendChild(renderer.domElement);

      const scene = new THREE.Scene();
      uiGame.internal.state.scene = scene;

      const camera = new THREE.PerspectiveCamera(45, uiGame.internal.elements.scene.offsetWidth / uiGame.internal.elements.scene.offsetHeight, 1, 1000);
      uiGame.internal.state.camera = camera;
      camera.position.set(0, 0, 30);
      camera.lookAt(0, 0, 0);

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
      directionalLight.position.set(0, 0, 300);
      directionalLight.castShadow = true;
      scene.add(directionalLight);

      const animate = () => {
        requestAnimationFrame(animate);
        renderer.render(scene, camera);
      };
      animate();
    },

    updateFieldElement: (spaceModel) => {
      if (uiGame.internal.state.fieldMesh != null) {
        return;
      }
      const fieldGeometry = new THREE.PlaneGeometry(spaceModel.width, spaceModel.height);
      const fieldMaterial = new THREE.MeshPhongMaterial({
        color: 0xffffff,
        shininess: 100
      });
      const fieldMesh = new THREE.Mesh(fieldGeometry, fieldMaterial);
      fieldMesh.position.x = spaceModel.width / 2;
      fieldMesh.position.y = spaceModel.height / -2;
      fieldMesh.receiveShadow = true;
      uiGame.internal.state.scene.add(fieldMesh);
      uiGame.internal.state.fieldMesh = fieldMesh;
    },

    updateCharacterMeshes: (characterModels) => {
      const staleCharacterIds = new Set(Object.keys(uiGame.internal.state.characterMeshes));
      for (const characterModel of characterModels) {
        let characterMesh;
        if (staleCharacterIds.has(characterModel.id.toString())) {
          characterMesh = uiGame.internal.state.characterMeshes[characterModel.id];
          staleCharacterIds.delete(characterModel.id.toString());
        } else {
          const characterGeometry = new THREE.BoxGeometry(characterModel.width, characterModel.height, 3); // TODO: Character z-length?
          const characterMaterial = new THREE.MeshLambertMaterial({ color: 0x00ff00 });
          characterMesh = new THREE.Mesh(characterGeometry, characterMaterial);
          uiGame.internal.state.scene.add(characterMesh);
          uiGame.internal.state.characterMeshes[characterModel.id] = characterMesh;
        }
        characterMesh.position.x = characterModel.posX + characterModel.width / 2;
        characterMesh.position.y = -1 * (characterModel.posY + characterModel.height / 2);
      }
      for (const staleCharacterId of staleCharacterIds) {
        uiGame.internal.state.scene.remove(uiGame.internal.state.characterMeshes[staleCharacterId]);
        delete uiGame.internal.state.characterMeshes[staleCharacterId];
      }
    },

    updateObstacleMeshes: (obstacleModels) => {
      // TODO: Implement
    },

    updateCamera:(space, clientCharacterModel) => {
      // TODO: Implement
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
        // TODO: Remove field, characters, and obstacles; dispose()?
        uiGame.internal.state.characterMeshes = { };
        uiGame.internal.state.obstacleMeshes = { };
        return;
      }
      if (uiGame.internal.state.renderer == null) {
        return;
      }
      const clientCharacter = snapshot.characters[snapshot.clientPlayerId];
      if (clientCharacter == null) {
        return;
      }
      uiGame.internal.updateFieldElement(snapshot.space);
      uiGame.internal.updateCharacterMeshes(Object.values(snapshot.characters));
      uiGame.internal.updateObstacleMeshes(snapshot.obstacles);
      uiGame.internal.updateCamera(snapshot.space, clientCharacter);
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
        uiGame.internal.setUpScene();
        uiGame.internal.enableControls();
      } else {
        uiGame.internal.disableControls();
        uiGame.internal.tearDownScene();
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

window.addEventListener('DOMContentLoaded', () => {
  uiGame.initialize();
});

export { uiGame };