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
        root: document.getElementById('overlay_friends_page'),
        friendsList: document.getElementById('overlay_friends_page_friends_list'),
        friendEntryTemplate: document.getElementsByClassName('friend_entry')[0],
        addFriendSection: {
          root: document.getElementById('overlay_friends_page_add_friend_section'),
          idInput: document.getElementById('overlay_friends_page_add_friend_section_id_input'),
          addFriendButton: document.getElementById('overlay_friends_page_add_friend_section_add_friend_button')
        }
      },
      accountPage: {
        root: document.getElementById('overlay_account_page'),
        id: document.getElementById('overlay_account_page_id'),
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
    blocker: {
      root: document.getElementById('blocker'),
      indicator: document.getElementById('blocker_indicator')
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

  clearContent: () => {
    ui.hide(ui.elements.content.loginModule.root);
    ui.hide(ui.elements.content.lobbyModule.root);
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

  notify: (message) => {
    clearTimeout(ui.state.notificationTimeoutId);
    ui.elements.notification.message.innerHTML = message;
    ui.show(ui.elements.notification.root);
    ui.state.notificationTimeoutId = setTimeout(() => {
      ui.hide(ui.elements.notification.root);
      ui.elements.notification.message.innerHTML = null;
    }, 2000);
  },

  hideBlocker: () => {
    ui.hide(ui.elements.blocker.root);
  },

  showBlocker: () => {
    ui.show(ui.elements.blocker.root);
  },

  showLoginModule: () => {
    ui.clearContent();
    ui.show(ui.elements.content.loginModule.root);
  },

  showLobbyModule: () => {
    ui.clearContent();
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

  addActiveGameEntry: (gameEntry) => {
    // TODO: Implement
  },

  addInvitationEntry: (gameEntry) => {
    // TODO: Implement
  },

  addPendingGameEntry: (gameEntry) => {
    // TODO: Implement
  },

  addFriendEntry: (friendEntry) => {
    const entry = ui.elements.overlay.friendsPage.friendEntryTemplate.cloneNode(true);
    entry.getElementsByClassName('friend_entry_name')[0].innerHTML = friendEntry.friendAccount.publicName;
    ui.elements.overlay.friendsPage.friendsList.insertBefore(entry, ui.elements.overlay.friendsPage.friendsList.firstElementChild);
  },

  refreshFriendsPage: () => {
    ui.elements.overlay.friendsPage.friendsList.innerHTML = null;
    const friends = api.getFriends();
    if (friends.incomingRequests != null) {
      for (const friend of friends.incomingRequests) {
        ui.addFriendEntry(friend);
      }
    }
    if (friends.confirmedFriendships != null) {
      for (const friend of friends.confirmedFriendships) {
        ui.addFriendEntry(friend);
      }
    }
    if (friends.outgoingRequests != null) {
      for (const friend of friends.outgoingRequests) {
        ui.addFriendEntry(friend);
      }
    }
  },

  refreshModifyAccountPage: () => {
    const account = api.getLoggedInAccount();
    if (account != null) {
      ui.elements.overlay.modifyAccountPage.usernameInput.value = account.loginName;
      ui.elements.overlay.modifyAccountPage.passwordInput.value = null;
      ui.elements.overlay.modifyAccountPage.publicNameInput.value = account.publicName;
    }
    ui.elements.overlay.modifyAccountPage.usernameInput.focus();
  },

  parseInputAndLogin: async () => {
    const username = ui.elements.content.loginModule.pages.loginPage.usernameInput.value;
    const password = ui.elements.content.loginModule.pages.loginPage.passwordInput.value;
    if (username === '' || password === '') {
      return;
    }
    try {
      await api.login(username, password);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          ui.notify('Failed to login');
          break;
        case '401':
          ui.notify('Invalid credentials');
          break;
      }
      return;
    }
    ui.handleLogin();
  },

  parseInputAndCreateAccount: async () => {
    const username = ui.elements.content.loginModule.pages.createAccountPage.usernameInput.value;
    const password = ui.elements.content.loginModule.pages.createAccountPage.passwordInput.value;
    const publicName = ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.value;
    let account;
    try {
      account = await api.createAccount(username, password, publicName);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          ui.notify('Rules not met');
          break;
        case '409':
          ui.notify('Username already taken');
          break;
      }
      return;
    }
    ui.notify('Successfully created account');
    ui.showLoginPage();
    ui.elements.content.loginModule.pages.loginPage.usernameInput.value = account.loginName;
    ui.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
    ui.elements.content.loginModule.pages.loginPage.passwordInput.focus();
    ui.elements.content.loginModule.pages.createAccountPage.usernameInput.value = null;
    ui.elements.content.loginModule.pages.createAccountPage.passwordInput.value = null;
    ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.value = null;
  },

  parseInputAndUpdateAccount: async () => {
    const username = ui.elements.overlay.modifyAccountPage.usernameInput.value;
    const password = ui.elements.overlay.modifyAccountPage.passwordInput.value;
    const publicName = ui.elements.overlay.modifyAccountPage.publicNameInput.value;
    try {
      await api.updateAccount(null, username, password, null, publicName);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          ui.notify('Rules not met');
          break;
        case '401':
          ui.notify('Not authenticated');
          break;
        case '403':
          ui.notify('Not authorized');
          break;
        case '404':
          ui.notify('No such account');
          break;
        case '409':
          ui.notify('Username already taken');
          break;
      }
      return;
    }
    try {
      await api.logout();
    }
    catch { }
    ui.handleLogout();
  },

  handleIncomingSocketMessage: (message) => {
    // TODO
  },

  handleLogout: () => {
    ui.hideOverlay();
    ui.hideTopBarButtons();
    ui.showLoginPage();
    ui.showLoginModule();
    ui.elements.content.loginModule.pages.loginPage.usernameInput.focus();
    ui.elements.content.lobbyModule.activeGamesList.innerHTML = null;
    ui.elements.content.lobbyModule.invitationsList.innerHTML = null;
    ui.elements.content.lobbyModule.pendingGamesList.innerHTML = null;
    ui.elements.overlay.friendsPage.friendsList.innerHTML = null;
    ui.elements.overlay.accountPage.id.innerHTML = null;
    ui.elements.overlay.accountPage.username.innerHTML = null;
    ui.elements.overlay.accountPage.publicName.innerHTML = null;
    ui.elements.overlay.modifyAccountPage.usernameInput.value = null;
    ui.elements.overlay.modifyAccountPage.passwordInput.value = null;
    ui.elements.overlay.modifyAccountPage.publicNameInput.value = null;
  },

  handleLogin: () => {
    ui.showLobbyModule();
    ui.showTopBarButtons();
    const account = api.getLoggedInAccount();
    if (account != null) {
      ui.elements.overlay.accountPage.id.innerHTML = account.id;
      ui.elements.overlay.accountPage.username.innerHTML = account.loginName;
      ui.elements.overlay.accountPage.publicName.innerHTML = account.publicName;
    }
    ui.elements.content.loginModule.pages.loginPage.usernameInput.value = null;
    ui.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
    ui.elements.content.loginModule.pages.createAccountPage.usernameInput.value = null;
    ui.elements.content.loginModule.pages.createAccountPage.passwordInput.value = null;
    ui.elements.content.loginModule.pages.createAccountPage.publicNameInput.value = null;
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
    await ui.parseInputAndLogin();
  }
});
ui.elements.content.loginModule.pages.loginPage.loginButton.addEventListener('click', async () => {
  await ui.parseInputAndLogin();
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
    await ui.parseInputAndCreateAccount();
  }
});
ui.elements.content.loginModule.pages.createAccountPage.createAccountButton.addEventListener('click', async () => {
  await ui.parseInputAndCreateAccount();
});
ui.elements.topBar.createGameButton.addEventListener('click', () => {
  ui.showCreateGamePage();
  ui.showOverlay();
});
ui.elements.topBar.friendsButton.addEventListener('click', () => {
  ui.refreshFriendsPage();
  ui.showFriendsPage();
  ui.showOverlay();
});
ui.elements.topBar.accountButton.addEventListener('click', () => {
  ui.showAccountPage();
  ui.showOverlay();
});
ui.elements.topBar.logoutButton.addEventListener('click', async () => {
  try {
    await api.logout();
  }
  catch { }
  ui.handleLogout();
});
ui.elements.overlay.root.addEventListener('click', () => {
  ui.hideOverlay();
});
ui.elements.overlay.window.addEventListener('click', e => {
  e.stopPropagation();
});
ui.elements.overlay.accountPage.modifyButton.addEventListener('click', () => {
  ui.refreshModifyAccountPage();
  ui.showModifyAccountPage();
});
ui.elements.overlay.modifyAccountPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    ui.elements.overlay.modifyAccountPage.passwordInput.focus();
  }
});
ui.elements.overlay.modifyAccountPage.passwordInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    ui.elements.overlay.modifyAccountPage.publicNameInput.focus();
  }
});
ui.elements.overlay.modifyAccountPage.publicNameInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    await ui.parseInputAndUpdateAccount();
  }
});
ui.elements.overlay.modifyAccountPage.cancelButton.addEventListener('click', () => {
  ui.showAccountPage();
});
ui.elements.overlay.modifyAccountPage.deleteButton.addEventListener('click', async () => {
  try {
    await api.deleteAccount();
  }
  catch {
    ui.notify('Failed to delete account');
    return;
  }
  try {
    await api.logout();
  }
  catch { }
  ui.handleLogout();
});
ui.elements.overlay.modifyAccountPage.saveButton.addEventListener('click', async () => {
  await ui.parseInputAndUpdateAccount();
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
ui.elements.overlay.friendsPage.friendEntryTemplate.remove();
ui.hide(ui.elements.overlay.accountPage.root);
ui.hide(ui.elements.overlay.modifyAccountPage.root);
ui.hide(ui.elements.notification.root);

(async () => {
  await api.initialize([ui.handleIncomingSocketMessage]);
  if (api.getLoggedInAccount() != null) {
    ui.handleLogin();
  } else {
    ui.handleLogout();
  }
  ui.hideBlocker();
})();