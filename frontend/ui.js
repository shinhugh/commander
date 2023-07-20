const ui = {

  state: {
    notificationTimeoutId: null
  },

  elements: {
    content: {
      root: document.getElementById('content'),
      loginModule: {
        root: document.getElementById('content_login_module'),
        tabsContainer: document.getElementById('content_login_module_tabs_container'),
        loginTab: document.getElementById('content_login_module_login_tab'),
        createAccountTab: document.getElementById('content_login_module_create_account_tab'),
        pagesContainer: document.getElementById('content_login_module_pages_container'),
        loginPage: {
          root: document.getElementById('content_login_module_login_page'),
          usernameInput: document.getElementById('content_login_module_login_page_username_input'),
          passwordInput: document.getElementById('content_login_module_login_page_password_input'),
          loginButton: document.getElementById('content_login_module_login_page_login_button'),
        },
        createAccountPage: {
          root: document.getElementById('content_login_module_create_account_page'),
          usernameInput: document.getElementById('content_login_module_create_account_page_username_input'),
          passwordInput: document.getElementById('content_login_module_create_account_page_password_input'),
          publicNameInput: document.getElementById('content_login_module_create_account_page_public_name_input'),
          createAccountButton: document.getElementById('content_login_module_create_account_page_create_account_button')
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

  navigateToLoginModule: () => {
    ui.hide(ui.elements.lobbyModule.root);
    ui.show(ui.elements.loginModule.root);
  },

  navigateToLobbyModule: () => {
    ui.hide(ui.elements.loginModule.root);
    ui.show(ui.elements.lobbyModule.root);
  }

};

// ------------------------------------------------------------

// TODO: Add event handlers

// ------------------------------------------------------------

ui.hide(ui.elements.content.loginModule.root);
ui.hide(ui.elements.content.loginModule.loginPage.root);
ui.hide(ui.elements.content.loginModule.createAccountPage.root);
ui.hide(ui.elements.content.lobbyModule.root);
ui.hide(ui.elements.topBar.createGameButton);
ui.hide(ui.elements.topBar.friendsButton);
ui.hide(ui.elements.topBar.accountButton);
ui.hide(ui.elements.topBar.logoutButton);
ui.hide(ui.elements.overlay.root);

ui.navigateToLoginModule();