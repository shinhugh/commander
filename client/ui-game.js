/* Requires:
 * - api.js
 * - game.js
 * - ui-api.js
 * - ui-base.js
 * - 3js
 */

import { api } from './api';
import { game } from './game';
import { uiApi } from './ui-api';
import { uiBase } from './ui-base';
import * as THREE from 'three';
import { CSS3DRenderer, CSS3DObject } from 'three/addons/renderers/CSS3DRenderer.js';

 // TODO: Resize 3js elements if their game entities change size?
const uiGame = {

  internal: {

    elements: {
      root: document.getElementById('content_game_module_game'),
      scene: document.getElementById('content_game_module_game_scene'),
      chatbox: {
        root: document.getElementById('content_game_module_game_chatbox'),
        composer: document.getElementById('content_game_module_game_chatbox_composer'),
        sendButton: document.getElementById('content_game_module_game_chatbox_send_button')
      },
      overlay: {
        root: document.getElementById('content_game_module_game_overlay'),
        disconnectPage: {
          root: document.getElementById('content_game_module_game_overlay_disconnect_page')
        },
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
      directionalLightIntensity: 0.8,
      ambientLightIntensity: 0.4,
      characterZLength: 1.5,
      chatBubbleCssScaling: 0.005,
      chatBubbleOffset: 0.1,
      chatBubbleDuration: 3000,
      obstacleZLength: 1,
      obstacleOpacity: 1,
      keyWPressed: false,
      keyWPressTime: null,
      keyAPressed: false,
      keyAPressTime: null,
      keySPressed: false,
      keySPressTime: null,
      keyDPressed: false,
      keyDPressTime: null,
      renderer: null,
      cssRenderer: null,
      scene: null,
      camera: null,
      fieldMesh: null,
      characterMeshes: { },
      chatBubbleMeshes: { },
      chatBubbleHideTimeouts: { },
      obstacleMeshes: { }
    },

    registerApiHandlers: () => {
      api.registerEstablishedConnectionHandler(uiGame.internal.handleEstablishedConnection);
      api.registerClosedConnectionHandler(uiGame.internal.handleClosedConnection);
      game.registerGameStateChangeHandler(uiGame.internal.handleGameStateChange);
      game.registerGameChatHandler(uiGame.internal.handleGameChat);
      game.registerGameSeatLossHandler(uiGame.internal.handleGameSeatLoss);
      game.registerGameIntegrityViolationHandler(uiGame.internal.handleGameIntegrityViolation);
      uiBase.registerModuleChangeHandler(uiGame.internal.handleModuleChange);
      uiBase.registerOverlayAppearanceHandler(uiGame.internal.handleOverlayAppearance);
      uiBase.registerOverlayDisappearanceHandler(uiGame.internal.handleOverlayDisappearance);
    },

    registerUiHandlers: () => {
      new ResizeObserver(uiGame.internal.resizeScene).observe(uiGame.internal.elements.scene);
      uiGame.internal.elements.scene.addEventListener('click', () => {
        uiGame.internal.disableChatControls();
        uiGame.internal.enableGameControls();
        uiGame.internal.hideChatbox();
      });
      uiGame.internal.elements.chatbox.sendButton.addEventListener('click', () => {
        uiGame.internal.parseChatboxComposerAndSendChat();
      });
      uiGame.internal.elements.overlay.seatLossPage.reconnectButton.addEventListener('click', () => {
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
        game.joinGame();
        uiGame.internal.enableGameControls();
      });
      uiGame.internal.elements.overlay.integrityViolationPage.reconnectButton.addEventListener('click', () => {
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
        game.joinGame();
        uiGame.internal.enableGameControls();
      });
    },

    clearUi: () => {
      uiApi.hide(uiGame.internal.elements.chatbox.root);
      uiApi.hide(uiGame.internal.elements.overlay.root);
      uiApi.hide(uiGame.internal.elements.overlay.disconnectPage.root);
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

    hideChatbox: () => {
      uiApi.hide(uiGame.internal.elements.chatbox.root);
    },

    showChatbox: () => {
      uiApi.show(uiGame.internal.elements.chatbox.root);
    },

    showDisconnectPage: () => {
      uiGame.internal.clearGameOverlay();
      uiApi.show(uiGame.internal.elements.overlay.disconnectPage.root);
    },

    showGameSeatLossPage: () => {
      uiGame.internal.clearGameOverlay();
      uiApi.show(uiGame.internal.elements.overlay.seatLossPage.root);
    },

    showGameIntegrityViolationPage: () => {
      uiGame.internal.clearGameOverlay();
      uiApi.show(uiGame.internal.elements.overlay.integrityViolationPage.root);
    },

    parseChatboxComposerAndSendChat: () => {
      const content = uiGame.internal.elements.chatbox.composer.value;
      if (content.length === 0) {
        return;
      }
      game.sendChat(null, true, content);
      uiGame.internal.elements.chatbox.composer.value = null;
    },

    removeAllChildrenOfMesh: (mesh) => {
      for (let i = mesh.children.length - 1; i >= 0; i--) {
        mesh.remove(mesh.children[i]);
      }
    },

    createCharacterTexture: (xLength, zLength) => {
      const imageHeight = 280;
      const imageWidth = 198;
      const xToYScale = (zLength / xLength) / (imageHeight / imageWidth);
      const xScale = 0.95; // smaller enlarges
      const offsetX = 0; // smaller moves right
      const offsetY = 0; // smaller moves up
      const characterTexture = new THREE.TextureLoader().load('assets/paul_bunyan.png');
      characterTexture.wrapS = THREE.ClampToEdgeWrapping;
      characterTexture.wrapT = THREE.ClampToEdgeWrapping;
      characterTexture.repeat.set(xScale, xScale * xToYScale);
      characterTexture.offset.set(offsetX, offsetY);
      return characterTexture;
    },

    tearDownScene: () => {
      uiGame.internal.elements.scene.innerHTML = null;
      uiGame.internal.state.renderer = null;
      uiGame.internal.state.cssRenderer = null;
      uiGame.internal.state.scene = null;
      uiGame.internal.state.camera = null;
      uiGame.internal.state.fieldMesh = null;
      uiGame.internal.state.characterMeshes = { };
      uiGame.internal.state.chatBubbleMeshes = { };
      for (const timeout of Object.values(uiGame.internal.state.chatBubbleHideTimeouts)) {
        clearTimeout(timeout);
      }
      uiGame.internal.state.chatBubbleHideTimeouts = { };
      uiGame.internal.state.obstacleMeshes = { };
    },

    setUpScene: () => {
      if (uiGame.internal.state.renderer != null) {
        return;
      }
      const renderer = new THREE.WebGLRenderer();
      uiGame.internal.state.renderer = renderer;
      renderer.setPixelRatio(window.devicePixelRatio);
      renderer.setSize(uiGame.internal.elements.scene.offsetWidth, uiGame.internal.elements.scene.offsetHeight);
      uiGame.internal.elements.scene.appendChild(renderer.domElement);
      const cssRenderer = new CSS3DRenderer();
      uiGame.internal.state.cssRenderer = cssRenderer;
      cssRenderer.setSize(uiGame.internal.elements.scene.offsetWidth, uiGame.internal.elements.scene.offsetHeight);
      cssRenderer.domElement.style.position = 'absolute';
      cssRenderer.domElement.style.top = '0';
      cssRenderer.domElement.style.left = '0';
      uiGame.internal.elements.scene.appendChild(cssRenderer.domElement);
      const scene = new THREE.Scene();
      scene.background = new THREE.Color(0xd9c880);
      uiGame.internal.state.scene = scene;
      const camera = new THREE.PerspectiveCamera(45, uiGame.internal.elements.scene.offsetWidth / uiGame.internal.elements.scene.offsetHeight, 1, 1000);
      uiGame.internal.state.camera = camera;
      camera.position.set(0, -16, 6);
      camera.lookAt(0, 0, 0);
      const directionalLight = new THREE.DirectionalLight(0xffffff, uiGame.internal.state.directionalLightIntensity);
      directionalLight.position.set(0, -40, 40);
      scene.add(directionalLight);
      const ambientLight = new THREE.AmbientLight(0xffffff, uiGame.internal.state.ambientLightIntensity);
      scene.add(ambientLight);
      const animate = () => {
        requestAnimationFrame(animate);
        renderer.render(scene, camera);
        cssRenderer.render(scene, camera);
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
      uiGame.internal.state.cssRenderer.setSize(width, height);
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
          const characterGeometry = new THREE.BoxGeometry(characterModel.width, characterModel.height, uiGame.internal.state.characterZLength);
          const characterTexture = uiGame.internal.createCharacterTexture(characterModel.width, uiGame.internal.state.characterZLength);
          const characterMaterial = new THREE.MeshBasicMaterial({
            map: characterTexture,
            transparent: true
          });
          const transparentMaterial = new THREE.MeshBasicMaterial({
            transparent: true,
            opacity: 0
          });
          characterMesh = new THREE.Mesh(characterGeometry, [transparentMaterial, transparentMaterial, transparentMaterial, characterMaterial, transparentMaterial, transparentMaterial]);
          characterMesh.position.z = uiGame.internal.state.characterZLength / 2;
          uiGame.internal.state.scene.add(characterMesh);
          uiGame.internal.state.characterMeshes[characterModel.id] = characterMesh;
        }
        characterMesh.position.x = characterModel.posX + characterModel.width / 2;
        characterMesh.position.y = -1 * (characterModel.posY + characterModel.height / 2);
        switch (characterModel.orientation) {
          case 'up_right':
          case 'right':
          case 'down_right':
            characterMesh.scale.x = -1;
            break;
          case 'down_left':
          case 'left':
          case 'up_left':
            characterMesh.scale.x = 1;
            break;
        }
      }
      for (const staleCharacterId of staleCharacterIds) {
        uiGame.internal.state.scene.remove(uiGame.internal.state.characterMeshes[staleCharacterId]);
        delete uiGame.internal.state.characterMeshes[staleCharacterId];
      }
    },

    updateChatBubbleMeshes: (characterModels) => {
      const staleChatBubbleCharacterIds = new Set(Object.keys(uiGame.internal.state.chatBubbleMeshes));
      for (const characterModel of characterModels) {
        let chatBubbleMesh;
        if (staleChatBubbleCharacterIds.has(characterModel.id.toString())) {
          chatBubbleMesh = uiGame.internal.state.chatBubbleMeshes[characterModel.id];
          staleChatBubbleCharacterIds.delete(characterModel.id.toString());
        } else {
          const chatBubbleGeometry = new THREE.PlaneGeometry(1, 1);
          const chatBubbleMaterial = new THREE.MeshBasicMaterial({ color: 0x000000 });
          chatBubbleMaterial.transparent = true;
          chatBubbleMaterial.opacity = 0;
          chatBubbleMesh = new THREE.Mesh(chatBubbleGeometry, chatBubbleMaterial);
          chatBubbleMesh.rotation.x = 1.5708;
          chatBubbleMesh.position.z = uiGame.internal.state.characterZLength + uiGame.internal.state.chatBubbleOffset;
          uiGame.internal.state.chatBubbleMeshes[characterModel.id] = chatBubbleMesh;
        }
        chatBubbleMesh.position.x = characterModel.posX + characterModel.width / 2;
        chatBubbleMesh.position.y = -1 * (characterModel.posY + characterModel.height / 2);
      }
      for (const staleCharacterId of staleChatBubbleCharacterIds) {
        uiGame.internal.state.scene.remove(uiGame.internal.state.chatBubbleMeshes[staleCharacterId]);
        delete uiGame.internal.state.chatBubbleMeshes[staleCharacterId];
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
          const obstacleGeometry = new THREE.BoxGeometry(obstacleModel.width, obstacleModel.height, uiGame.internal.state.obstacleZLength);
          const obstacleMaterial = new THREE.MeshLambertMaterial({
            color: 0x7cb06d,
            transparent: true,
            opacity: uiGame.internal.state.obstacleOpacity
          });
          obstacleMesh = new THREE.Mesh(obstacleGeometry, obstacleMaterial);
          obstacleMesh.position.z = uiGame.internal.state.obstacleZLength / 2;
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

    updateCamera: (space, clientCharacterModel) => {
      uiGame.internal.state.camera.position.x = clientCharacterModel.posX + clientCharacterModel.width / 2;
    },

    showChatBubble: (characterId, content) => {
      const chatBubbleMesh = uiGame.internal.state.chatBubbleMeshes[characterId];
      if (chatBubbleMesh == null) {
        return;
      }
      clearTimeout(uiGame.internal.state.chatBubbleHideTimeouts[characterId]);
      uiGame.internal.removeAllChildrenOfMesh(chatBubbleMesh);
      const textElement = document.createElement('div');
      textElement.innerHTML = content;
      textElement.classList.add('chat_text');
      const textWrapperElement = document.createElement('div');
      textWrapperElement.classList.add('chat_text_wrapper');
      textWrapperElement.appendChild(textElement);
      const textObject = new CSS3DObject(textWrapperElement);
      textObject.scale.set(uiGame.internal.state.chatBubbleCssScaling, uiGame.internal.state.chatBubbleCssScaling, uiGame.internal.state.chatBubbleCssScaling);
      chatBubbleMesh.add(textObject);
      uiGame.internal.state.scene.add(chatBubbleMesh);
      uiGame.internal.state.chatBubbleHideTimeouts[characterId] = setTimeout(() => {
        uiGame.internal.state.chatBubbleHideTimeouts[characterId] = null;
        uiGame.internal.state.scene.remove(chatBubbleMesh);
        uiGame.internal.removeAllChildrenOfMesh(chatBubbleMesh);
      }, uiGame.internal.state.chatBubbleDuration);
    },

    enableGameControls: () => {
      document.addEventListener('keydown', uiGame.internal.handleKeyDownGame);
      document.addEventListener('keyup', uiGame.internal.handleKeyUpGame);
    },

    disableGameControls: () => {
      document.removeEventListener('keydown', uiGame.internal.handleKeyDownGame);
      document.removeEventListener('keyup', uiGame.internal.handleKeyUpGame);
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

    enableChatControls: () => {
      document.addEventListener('keydown', uiGame.internal.handleKeyDownChat);
    },

    disableChatControls: () => {
      document.removeEventListener('keydown', uiGame.internal.handleKeyDownChat);
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

    handleKeyDownGame: (e) => {
      switch (e.key) {
        case 'w':
          if (!uiGame.internal.state.keyWPressed) {
            uiGame.internal.state.keyWPressTime = Date.now();
            uiGame.internal.state.keyWPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          return;
        case 'a':
          if (!uiGame.internal.state.keyAPressed) {
            uiGame.internal.state.keyAPressTime = Date.now();
            uiGame.internal.state.keyAPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          return;
        case 's':
          if (!uiGame.internal.state.keySPressed) {
            uiGame.internal.state.keySPressTime = Date.now();
            uiGame.internal.state.keySPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          return;
        case 'd':
          if (!uiGame.internal.state.keyDPressed) {
            uiGame.internal.state.keyDPressTime = Date.now();
            uiGame.internal.state.keyDPressed = true;
          }
          uiGame.internal.updateDirectionInput();
          return;
        case 'Enter':
          uiGame.internal.disableGameControls();
          uiGame.internal.enableChatControls();
          uiGame.internal.showChatbox();
          uiGame.internal.elements.chatbox.composer.focus();
          return;
      }
    },

    handleKeyUpGame: (e) => {
      switch (e.key) {
        case 'w':
          uiGame.internal.state.keyWPressed = false;
          uiGame.internal.state.keyWPressTime = null;
          uiGame.internal.updateDirectionInput();
          return;
        case 'a':
          uiGame.internal.state.keyAPressed = false;
          uiGame.internal.state.keyAPressTime = null;
          uiGame.internal.updateDirectionInput();
          return;
        case 's':
          uiGame.internal.state.keySPressed = false;
          uiGame.internal.state.keySPressTime = null;
          uiGame.internal.updateDirectionInput();
          return;
        case 'd':
          uiGame.internal.state.keyDPressed = false;
          uiGame.internal.state.keyDPressTime = null;
          uiGame.internal.updateDirectionInput();
          return;
      }
    },

    handleKeyDownChat: (e) => {
      uiGame.internal.elements.chatbox.composer.focus();
      switch (e.key) {
        case 'Enter':
          if (uiGame.internal.elements.chatbox.composer.value.length > 0) {
            uiGame.internal.parseChatboxComposerAndSendChat();
            return;
          }
        case 'Escape':
          uiGame.internal.disableChatControls();
          uiGame.internal.enableGameControls();
          uiGame.internal.hideChatbox();
          uiGame.internal.elements.scene.focus();
          return;
      }
    },

    handleEstablishedConnection: () => {
      uiGame.internal.hideGameOverlay();
      uiGame.internal.clearGameOverlay();
    },

    handleClosedConnection: () => {
      uiGame.internal.showDisconnectPage();
      uiGame.internal.showGameOverlay();
    },

    handleGameStateChange: () => {
      const snapshot = game.getGameState();
      if (snapshot == null) {
        if (uiGame.internal.state.renderer != null) {
          uiGame.internal.state.scene.remove(uiGame.internal.state.fieldMesh);
          for (const characterMesh of Object.values(uiGame.internal.state.characterMeshes)) {
            uiGame.internal.state.scene.remove(characterMesh);
          }
          for (const chatBubbleMesh of Object.values(uiGame.internal.state.chatBubbleMeshes)) {
            uiGame.internal.state.scene.remove(chatBubbleMesh);
          }
          for (const obstacleMesh of Object.values(uiGame.internal.state.obstacleMeshes)) {
            uiGame.internal.state.scene.remove(obstacleMesh);
          }
        }
        uiGame.internal.state.fieldMesh = null;
        uiGame.internal.state.characterMeshes = { };
        uiGame.internal.state.chatBubbleMeshes = { };
        for (const timeout of Object.values(uiGame.internal.state.chatBubbleHideTimeouts)) {
          clearTimeout(timeout);
        }
        uiGame.internal.state.chatBubbleHideTimeouts = { };
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
      uiGame.internal.updateChatBubbleMeshes(Object.values(snapshot.characters));
      uiGame.internal.updateObstacleMeshes(snapshot.obstacles);
      uiGame.internal.updateCamera(snapshot.space, clientCharacter);
    },

    handleGameChat: (chat) => {
      const characterId = game.getGameState()?.characters[chat.srcPlayerId]?.id;
      if (characterId != null) {
        uiGame.internal.showChatBubble(characterId, chat.content);
      }
    },

    handleGameSeatLoss: () => {
      uiGame.internal.disableGameControls();
      uiGame.internal.disableChatControls();
      uiGame.internal.hideChatbox();
      uiGame.internal.showGameSeatLossPage();
      uiGame.internal.showGameOverlay();
    },

    handleGameIntegrityViolation: () => {
      uiGame.internal.disableGameControls();
      uiGame.internal.disableChatControls();
      uiGame.internal.hideChatbox();
      uiGame.internal.showGameIntegrityViolationPage();
      uiGame.internal.showGameOverlay();
    },

    handleModuleChange: () => {
      if (uiBase.getCurrentModule() === 'game') {
        game.joinGame();
        uiGame.internal.setUpScene();
        uiGame.internal.enableGameControls();
      } else {
        uiGame.internal.disableGameControls();
        uiGame.internal.disableChatControls();
        uiGame.internal.tearDownScene();
        uiGame.internal.hideChatbox();
        uiGame.internal.hideGameOverlay();
        uiGame.internal.clearGameOverlay();
      }
    },

    handleOverlayAppearance: () => {
      uiGame.internal.disableGameControls();
      uiGame.internal.disableChatControls();
      uiGame.internal.hideChatbox();
    },

    handleOverlayDisappearance: () => {
      if (uiBase.getCurrentModule() !== 'game') {
        return;
      }
      uiGame.internal.enableGameControls();
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