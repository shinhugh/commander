const ui = {

  state: {
    notificationTimeoutId: null
  },

  elements: {
    content: {
      root: document.getElementById('content'),
      loginModule: {
        root: document.getElementById('content_login_module'),
        tabs: {
          root: document.getElementById('content_login_module_tabs'),
          loginTab: document.getElementById('content_login_module_tabs_login_tab'),
          createAccountTab: document.getElementById('content_login_module_tabs_create_account_tab')
        },
        pages: {
          root: document.getElementById('content_login_module_pages'),
          loginPage: {
            root: document.getElementById('content_login_module_pages_login_page'),
            usernameInput: document.getElementById('content_login_module_pages_login_page_username_input'),
            passwordInput: document.getElementById('content_login_module_pages_login_page_password_input'),
            loginButton: document.getElementById('content_login_module_pages_login_page_login_button'),
          },
          createAccountPage: {
            root: document.getElementById('content_login_module_pages_create_account_page'),
            usernameInput: document.getElementById('content_login_module_pages_create_account_page_username_input'),
            passwordInput: document.getElementById('content_login_module_pages_create_account_page_password_input'),
            publicNameInput: document.getElementById('content_login_module_pages_create_account_page_public_name_input'),
            createAccountButton: document.getElementById('content_login_module_pages_create_account_page_create_account_button')
          }
        }
      },
      lobbyModule: {
        root: document.getElementById('content_lobby_module'),
        activeGamesList: document.getElementById('content_lobby_module_active_games_list'),
        invitationsList: document.getElementById('content_lobby_module_invitations_list'),
        pendingGamesList: document.getElementById('content_lobby_module_pending_games_list'),
        gameEntryTemplate: document.getElementsByClassName('game_entry')[0]
      }
    },
    topBar: {
      root: document.getElementById('top_bar'),
      createGameButton: document.getElementById('top_bar_create_game_button'),
      friendsButton: document.getElementById('top_bar_friends_button'),
      accountButton: document.getElementById('top_bar_account_button'),
      logoutButton: document.getElementById('top_bar_logout_button')
    },
    overlay: {
      root: document.getElementById('overlay'),
      window: document.getElementById('overlay_window'),
      activeGameEntryPage: {
        root: document.getElementById('overlay_active_game_entry_page')
      },
      invitationEntryPage: {
        root: document.getElementById('overlay_invitation_entry_page')
      },
      pendingGameEntryPage: {
        root: document.getElementById('overlay_pending_game_entry_page')
      },
      createGamePage: {
        root: document.getElementById('overlay_create_game_page')
      },
      friendsPage: {
        root: document.getElementById('overlay_friends_page')
      },
      accountPage: {
        root: document.getElementById('overlay_account_page'),
        username: document.getElementById('overlay_account_page_username'),
        publicName: document.getElementById('overlay_account_page_public_name'),
        modifyButton: document.getElementById('overlay_account_page_modify_button'),
      },
      modifyAccountPage: {
        root: document.getElementById('overlay_modify_account_page'),
        usernameInput: document.getElementById('overlay_modify_account_page_username_input'),
        passwordInput: document.getElementById('overlay_modify_account_page_password_input'),
        publicNameInput: document.getElementById('overlay_modify_account_page_public_name_input'),
        cancelButton: document.getElementById('overlay_modify_account_page_cancel_button'),
        deleteButton: document.getElementById('overlay_modify_account_page_delete_button'),
        saveButton: document.getElementById('overlay_modify_account_page_save_button')
      }
    },
    notification: {
      root: document.getElementById('notification'),
      message: document.getElementById('notification_message')
    }
  },

  hide: (element) => {
    element.style.display = 'none';
  },

  show: (element) => {
    element.style.removeProperty('display');
  },

  select: (element) => {
    element.classList.add('selected');
  },

  unselect: (element) => {
    element.classList.remove('selected');
  },

  notify: (message) => {
    clearTimeout(ui.state.notificationTimeoutId);
    ui.elements.notification.message.innerHTML = message;
    ui.show(ui.elements.notification.root);
    ui.state.notificationTimeoutId = setTimeout(() => {
      ui.hide(ui.elements.notification.root);
    }, 2000);
  },

  hideOverlay: () => {
    ui.hide(ui.elements.overlay.root);
  },

  showOverlay: () => {
    ui.show(ui.elements.overlay.root);
  },

  clearOverlay: () => {
    ui.hide(ui.elements.overlay.activeGameEntryPage.root);
    ui.hide(ui.elements.overlay.invitationEntryPage.root);
    ui.hide(ui.elements.overlay.pendingGameEntryPage.root);
    ui.hide(ui.elements.overlay.createGamePage.root);
    ui.hide(ui.elements.overlay.friendsPage.root);
    ui.hide(ui.elements.overlay.accountPage.root);
    ui.hide(ui.elements.overlay.modifyAccountPage.root);
  },

  showLoginModule: () => {
    ui.hide(ui.elements.content.lobbyModule.root);
    ui.show(ui.elements.content.loginModule.root);
  },

  showLobbyModule: () => {
    ui.hide(ui.elements.content.loginModule.root);
    ui.show(ui.elements.content.lobbyModule.root);
  },

  hideTopBarButtons: () => {
    ui.hide(ui.elements.topBar.createGameButton);
    ui.hide(ui.elements.topBar.friendsButton);
    ui.hide(ui.elements.topBar.accountButton);
    ui.hide(ui.elements.topBar.logoutButton);
  },

  showTopBarButtons: () => {
    ui.show(ui.elements.topBar.createGameButton);
    ui.show(ui.elements.topBar.friendsButton);
    ui.show(ui.elements.topBar.accountButton);
    ui.show(ui.elements.topBar.logoutButton);
  },

  showLoginPage: () => {
    ui.hide(ui.elements.content.loginModule.pages.createAccountPage.root);
    ui.show(ui.elements.content.loginModule.pages.loginPage.root);
    ui.unselect(ui.elements.content.loginModule.tabs.createAccountTab);
    ui.select(ui.elements.content.loginModule.tabs.loginTab);
  },

  showCreateAccountPage: () => {
    ui.hide(ui.elements.content.loginModule.pages.loginPage.root);
    ui.show(ui.elements.content.loginModule.pages.createAccountPage.root);
    ui.unselect(ui.elements.content.loginModule.tabs.loginTab);
    ui.select(ui.elements.content.loginModule.tabs.createAccountTab);
  },

  addActiveGameEntry: (gameEntry) => {
    // TODO: Implement
  },

  addInvitationEntry: (gameEntry) => {
    // TODO: Implement
  },

  addPendingGameEntry: (gameEntry) => {
    // TODO: Implement
  },

  showActiveGameEntryPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.activeGameEntryPage.root);
  },

  showInvitationEntryPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.invitationEntryPage.root);
  },

  showPendingGameEntryPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.pendingGameEntryPage.root);
  },

  showCreateGamePage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.createGamePage.root);
  },

  showFriendsPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.friendsPage.root);
  },

  showAccountPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.accountPage.root);
  },

  showModifyAccountPage: () => {
    ui.clearOverlay();
    ui.show(ui.elements.overlay.modifyAccountPage.root);
  },

  handleLogout: () => {
    ui.hideOverlay();
    ui.hideTopBarButtons();
    ui.showLoginPage();
    ui.showLoginModule();
    ui.elements.content.loginModule.pages.loginPage.usernameInput.focus();
  },

  handleLogin: () => {
    ui.showLobbyModule();
    ui.showTopBarButtons();
  }

};

// ------------------------------------------------------------

ui.elements.content.loginModule.tabs.loginTab.addEventListener('click', () => {
  ui.showLoginPage();
  ui.elements.content.loginModule.pages.loginPage.usernameInput.focus();
});
ui.elements.content.loginModule.tabs.createAccountTab.addEventListener('click', () => {
  ui.showCreateAccountPage();
  ui.elements.content.loginModule.pages.createAccountPage.usernameInput.focus();
});
ui.elements.content.loginModule.pages.loginPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    ui.elements.content.loginModule.pages.loginPage.passwordInput.focus();
  }
});
ui.elements.content.loginModule.pages.loginPage.passwordInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    const username = ui.elements.content.loginModule.pages.loginPage.usernameInput.value;
    const password = ui.elements.content.loginModule.pages.loginPage.passwordInput.value;
    try {
      await api.login(username, password);
    }
    catch {
      ui.notify('Failed to login');
      return;
    }
    ui.handleLogin();
  }
});
ui.elements.content.loginModule.pages.loginPage.loginButton.addEventListener('click', async () => {
  const username = ui.elements.content.loginModule.pages.loginPage.usernameInput.value;
  const password = ui.elements.content.loginModule.pages.loginPage.passwordInput.value;
  try {
    await api.login(username, password);
  }
  catch {
    ui.notify('Failed to login');
    return;
  }
  ui.handleLogin();
});
ui.elements.content.loginModule.pages.createAccountPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    ui.elements.content.loginModule.pages.createAccountPage.passwordInput.focus();
  }
});
ui.elements.content.loginModule.pages.createAccountPage.passwordInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.focus();
  }
});
ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    const username = ui.elements.content.loginModule.pages.createAccountPage.usernameInput.value;
    const password = ui.elements.content.loginModule.pages.createAccountPage.passwordInput.value;
    const publicName = ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.value;
    let account;
    try {
      account = await api.createAccount(username, password, publicName);
    }
    catch {
      ui.notify('Failed to create account');
      return;
    }
    ui.notify('Successfully created account');
    ui.showLoginPage();
    ui.elements.content.loginModule.pages.loginPage.usernameInput.value = account.loginName;
    ui.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
    ui.elements.content.loginModule.pages.loginPage.passwordInput.focus();
  }
});
ui.elements.content.loginModule.pages.createAccountPage.createAccountButton.addEventListener('click', async () => {
  const username = ui.elements.content.loginModule.pages.createAccountPage.usernameInput.value;
  const password = ui.elements.content.loginModule.pages.createAccountPage.passwordInput.value;
  const publicName = ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.value;
  let account;
  try {
    account = await api.createAccount(username, password, publicName);
  }
  catch { // TODO: Use error to describe why operation failed
    ui.notify('Failed to create account');
    return;
  }
  ui.notify('Successfully created account');
  ui.showLoginPage();
  ui.elements.content.loginModule.pages.loginPage.usernameInput.value = account.loginName;
  ui.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
  ui.elements.content.loginModule.pages.loginPage.passwordInput.focus();
});

// ------------------------------------------------------------

ui.hide(ui.elements.content.loginModule.root);
ui.hide(ui.elements.content.loginModule.pages.loginPage.root);
ui.hide(ui.elements.content.loginModule.pages.createAccountPage.root);
ui.hide(ui.elements.content.lobbyModule.root);
ui.elements.content.lobbyModule.gameEntryTemplate.remove();
ui.hide(ui.elements.topBar.createGameButton);
ui.hide(ui.elements.topBar.friendsButton);
ui.hide(ui.elements.topBar.accountButton);
ui.hide(ui.elements.topBar.logoutButton);
ui.hide(ui.elements.overlay.root);
ui.hide(ui.elements.overlay.activeGameEntryPage.root);
ui.hide(ui.elements.overlay.invitationEntryPage.root);
ui.hide(ui.elements.overlay.pendingGameEntryPage.root);
ui.hide(ui.elements.overlay.createGamePage.root);
ui.hide(ui.elements.overlay.friendsPage.root);
ui.hide(ui.elements.overlay.accountPage.root);
ui.hide(ui.elements.overlay.modifyAccountPage.root);
ui.hide(ui.elements.notification.root);

ui.handleLogout();