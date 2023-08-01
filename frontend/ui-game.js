/* Requires:
 * - auth.js
 * - game.js
 * - ui-api.js
 */

const uiGame = {

  elements: {
    root: document.getElementById('content_game_module_game'),
    map: document.getElementById('content_game_module_game_map')
  },

  state: {
    keyWPressed: false,
    keyWPressTime: null,
    keyAPressed: false,
    keyAPressTime: null,
    keySPressed: false,
    keySPressTime: null,
    keyDPressed: false,
    keyDPressTime: null
  },

  sendDirectionalCommand: () => {
    let vertical = 0;
    if (uiGame.state.keyWPressed && uiGame.state.keySPressed) {
      if (uiGame.state.keyWPressTime <= uiGame.state.keySPressTime) {
        vertical = 1;
      } else {
        vertical = -1;
      }
    } else if (uiGame.state.keyWPressed) {
      vertical = -1;
    } else if (uiGame.state.keySPressed) {
      vertical = 1;
    }
    let horizontal = 0;
    if (uiGame.state.keyAPressed && uiGame.state.keyDPressed) {
      if (uiGame.state.keyAPressTime <= uiGame.state.keyDPressTime) {
        horizontal = 1;
      } else {
        horizontal = -1;
      }
    } else if (uiGame.state.keyAPressed) {
      horizontal = -1;
    } else if (uiGame.state.keyDPressed) {
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
    game.setCharacterMovement(direction);
  },

  handleKeyDown: (e) => {
    switch (e.key) {
      case 'w':
        if (!uiGame.state.keyWPressed) {
          uiGame.state.keyWPressTime = Date.now();
          uiGame.state.keyWPressed = true;
        }
        uiGame.sendDirectionalCommand();
        break;
      case 'a':
        if (!uiGame.state.keyAPressed) {
          uiGame.state.keyAPressTime = Date.now();
          uiGame.state.keyAPressed = true;
        }
        uiGame.sendDirectionalCommand();
        break;
      case 's':
        if (!uiGame.state.keySPressed) {
          uiGame.state.keySPressTime = Date.now();
          uiGame.state.keySPressed = true;
        }
        uiGame.sendDirectionalCommand();
        break;
      case 'd':
        if (!uiGame.state.keyDPressed) {
          uiGame.state.keyDPressTime = Date.now();
          uiGame.state.keyDPressed = true;
        }
        uiGame.sendDirectionalCommand();
        break;
    }
  },

  handleKeyUp: (e) => {
    switch (e.key) {
      case 'w':
        uiGame.state.keyWPressed = false;
        uiGame.state.keyWPressTime = null;
        uiGame.sendDirectionalCommand();
        break;
      case 'a':
        uiGame.state.keyAPressed = false;
        uiGame.state.keyAPressTime = null;
        uiGame.sendDirectionalCommand();
        break;
      case 's':
        uiGame.state.keySPressed = false;
        uiGame.state.keySPressTime = null;
        uiGame.sendDirectionalCommand();
        break;
      case 'd':
        uiGame.state.keyDPressed = false;
        uiGame.state.keyDPressTime = null;
        uiGame.sendDirectionalCommand();
        break;
    }
  },

  handleLogin: () => {
    game.joinGame();
  },

  handleGameStateChange: () => {
    const snapshot = game.getGameState();
    if (snapshot == null) {
      return;
    }
    const clientCharacter = snapshot.characters[snapshot.clientPlayerId];
    if (clientCharacter == null) {
      return;
    }
    const rootHeight = uiGame.elements.root.offsetHeight;
    const rootWidth = uiGame.elements.root.offsetWidth;
    const spaceHeight = snapshot.space.height;
    const spaceWidth = snapshot.space.width;
    const scale = 1.2 * Math.max((rootHeight / spaceHeight), (rootWidth / spaceWidth));
    const mapHeight = scale * spaceHeight;
    const mapWidth = scale * spaceWidth;
    uiGame.elements.map.style.height = mapHeight + 'px';
    uiGame.elements.map.style.width = mapWidth + 'px';
    const mapTop = ((rootHeight - mapHeight) / (spaceHeight - clientCharacter.height)) * clientCharacter.posY;
    const mapLeft = ((rootWidth - mapWidth) / (spaceWidth - clientCharacter.width)) * clientCharacter.posX;
    uiGame.elements.map.style.top = mapTop + 'px';
    uiGame.elements.map.style.left = mapLeft + 'px';
    uiGame.elements.map.innerHTML = null;
    for (const character of Object.values(snapshot.characters)) {
      const characterElement = document.createElement('div');
      characterElement.style.position = 'absolute';
      characterElement.style.top = (scale * character.posY) + 'px';
      characterElement.style.left = (scale * character.posX) + 'px';
      characterElement.style.height = (scale * character.height) + 'px';
      characterElement.style.width = (scale * character.width) + 'px';
      characterElement.classList.add('character_element');
      uiGame.elements.map.appendChild(characterElement);
    }
  }

};

// ------------------------------------------------------------

auth.registerLoginHandler(uiGame.handleLogin);
game.registerGameStateChangeHandler(uiGame.handleGameStateChange);

// ------------------------------------------------------------

// TODO: Element not detecting key events
uiGame.elements.root.addEventListener('keydown', uiGame.handleKeyDown);
uiGame.elements.root.addEventListener('keyup', uiGame.handleKeyUp);