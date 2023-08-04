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

 // TODO: Resize 3js elements if their game entities change size?
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
      new ResizeObserver(uiGame.internal.resizeScene).observe(uiGame.internal.elements.scene);
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
      uiGame.internal.elements.scene.appendChild(renderer.domElement);
      const scene = new THREE.Scene();
      scene.background = new THREE.Color(0xd9c880);
      uiGame.internal.state.scene = scene;
      const camera = new THREE.PerspectiveCamera(45, uiGame.internal.elements.scene.offsetWidth / uiGame.internal.elements.scene.offsetHeight, 1, 1000);
      uiGame.internal.state.camera = camera;
      camera.position.set(0, -16, 6);
      camera.lookAt(0, 0, 0);
      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
      directionalLight.position.set(0, -40, 40);
      scene.add(directionalLight);
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.4);
      scene.add(ambientLight);
      const animate = () => {
        requestAnimationFrame(animate);
        renderer.render(scene, camera);
      };
      animate();
    },

    resizeScene: () => {
      if (uiGame.internal.state.renderer == null) {
        return;
      }
      const width = uiGame.internal.elements.scene.offsetWidth;
      const height = uiGame.internal.elements.scene.offsetHeight;
      uiGame.internal.state.camera.aspect = width / height;
      uiGame.internal.state.camera.updateProjectionMatrix();
      uiGame.internal.state.renderer.setSize(width, height);
    },

    updateFieldElement: (spaceModel) => {
      if (uiGame.internal.state.fieldMesh != null) {
        return;
      }
      const fieldGeometry = new THREE.PlaneGeometry(spaceModel.width, spaceModel.height);
      const fieldTexture = new THREE.TextureLoader().load('assets/texture-grass.jpg');
      fieldTexture.wrapS = THREE.RepeatWrapping;
      fieldTexture.wrapT = THREE.RepeatWrapping;
      fieldTexture.repeat.set(5, 1);
      const fieldMaterial = new THREE.MeshPhongMaterial({
        map: fieldTexture
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
          const characterGeometry = new THREE.BoxGeometry(characterModel.width, characterModel.height, 2); // TODO: Character z-length?
          const characterMaterial = new THREE.MeshPhongMaterial({ color: 0x00ff00 });
          characterMaterial.transparent = true;
          characterMaterial.opacity = 0.8;
          characterMesh = new THREE.Mesh(characterGeometry, characterMaterial);
          characterMesh.position.z = 1;
          characterMesh.castShadow = true;
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
      const staleObstacleIds = new Set(Object.keys(uiGame.internal.state.obstacleMeshes));
      for (const obstacleModel of obstacleModels) {
        let obstacleMesh;
        if (staleObstacleIds.has(obstacleModel.id.toString())) {
          obstacleMesh = uiGame.internal.state.obstacleMeshes[obstacleModel.id];
          staleObstacleIds.delete(obstacleModel.id.toString());
        } else {
          const obstacleGeometry = new THREE.BoxGeometry(obstacleModel.width, obstacleModel.height, 3); // TODO: Obstacle z-length?
          const obstacleMaterial = new THREE.MeshLambertMaterial({ color: 0x0000ff });
          obstacleMaterial.transparent = true;
          obstacleMaterial.opacity = 0.5;
          obstacleMesh = new THREE.Mesh(obstacleGeometry, obstacleMaterial);
          obstacleMesh.position.z = 1.5;
          obstacleMesh.castShadow = true;
          uiGame.internal.state.scene.add(obstacleMesh);
          uiGame.internal.state.obstacleMeshes[obstacleModel.id] = obstacleMesh;
        }
        obstacleMesh.position.x = obstacleModel.posX + obstacleModel.width / 2;
        obstacleMesh.position.y = -1 * (obstacleModel.posY + obstacleModel.height / 2);
      }
      for (const staleObstacleId of staleObstacleIds) {
        uiGame.internal.state.scene.remove(uiGame.internal.state.obstacleMeshes[staleObstacleId]);
        delete uiGame.internal.state.obstacleMeshes[staleObstacleId];
      }
    },

    updateCamera:(space, clientCharacterModel) => {
      uiGame.internal.state.camera.position.x = clientCharacterModel.posX + clientCharacterModel.width / 2;
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
        // TODO: dispose()?
        if (uiGame.internal.state.renderer != null) {
          uiGame.internal.state.scene.remove(uiGame.internal.state.fieldMesh);
          for (const characterMesh of uiGame.internal.state.characterMeshes) {
            uiGame.internal.state.scene.remove(characterMesh);
          }
          for (const obstacleMesh of uiGame.internal.state.obstacleMeshes) {
            uiGame.internal.state.scene.remove(obstacleMesh);
          }
        }
        uiGame.internal.state.fieldMesh = null;
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