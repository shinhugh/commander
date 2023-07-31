/* Requires:
 * - auth.js
 * - friendships.js
 * - game.js
 */

const ui = {

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
      gameModule: {
        root: document.getElementById('content_game_module'),
        game: {
          root: document.getElementById('content_game_module_game'),
          map: document.getElementById('content_game_module_game_map')
        }
      }
    },
    topBar: {
      root: document.getElementById('top_bar'),
      title: document.getElementById('top_bar_title'),
      friendsButton: document.getElementById('top_bar_friends_button'),
      accountButton: document.getElementById('top_bar_account_button'),
      logoutButton: document.getElementById('top_bar_logout_button')
    },
    overlay: {
      root: document.getElementById('overlay'),
      backdrop: document.getElementById('overlay_backdrop'),
      window: document.getElementById('overlay_window'),
      friendsPage: {
        root: document.getElementById('overlay_friends_page'),
        friendsList: document.getElementById('overlay_friends_page_friends_list'),
        incomingFriendshipRequestEntryTemplate: document.getElementsByClassName('incoming_friendship_request_entry')[0],
        confirmedFriendshipEntryTemplate: document.getElementsByClassName('confirmed_friendship_entry')[0],
        outgoingFriendshipRequestEntryTemplate: document.getElementsByClassName('outgoing_friendship_request_entry')[0],
        addFriendSection: {
          root: document.getElementById('overlay_friends_page_add_friend_section'),
          result: {
            root: document.getElementById('overlay_friends_page_add_friend_section_result'),
            name: document.getElementById('overlay_friends_page_add_friend_section_result_name'),
            addFriendButton: document.getElementById('overlay_friends_page_add_friend_section_result_add_friend_button')
          },
          idInput: document.getElementById('overlay_friends_page_add_friend_section_id_input'),
          searchButton: document.getElementById('overlay_friends_page_add_friend_section_search_button')
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

  cloak: (element) => {
    element.style.visibility = 'hidden';
  },

  uncloak: (element) => {
    element.style.removeProperty('visibility');
  },

  select: (element) => {
    element.classList.add('selected');
  },

  unselect: (element) => {
    element.classList.remove('selected');
  },

  handleLogin: () => {
    // TODO: Implement
  },

  handleLogout: () => {
    // TODO: Implement
  },

  handleFriendshipsChange: () => {
    // TODO: Implement
  },

  handleGameStateChange: () => {
    // TODO: Implement
  }

};

// ------------------------------------------------------------

ui.hide(ui.elements.content.loginModule.root);
ui.hide(ui.elements.content.loginModule.pages.loginPage.root);
ui.hide(ui.elements.content.loginModule.pages.createAccountPage.root);
ui.hide(ui.elements.content.gameModule.root);
ui.hide(ui.elements.topBar.title);
ui.hide(ui.elements.topBar.friendsButton);
ui.hide(ui.elements.topBar.accountButton);
ui.hide(ui.elements.topBar.logoutButton);
ui.hide(ui.elements.overlay.root);
ui.hide(ui.elements.overlay.friendsPage.root);
ui.elements.overlay.friendsPage.incomingFriendshipRequestEntryTemplate.remove();
ui.elements.overlay.friendsPage.confirmedFriendshipEntryTemplate.remove();
ui.elements.overlay.friendsPage.outgoingFriendshipRequestEntryTemplate.remove();
ui.cloak(ui.elements.overlay.friendsPage.addFriendSection.result.root);
ui.hide(ui.elements.overlay.accountPage.root);
ui.hide(ui.elements.overlay.modifyAccountPage.root);
ui.hide(ui.elements.notification.root);

// ------------------------------------------------------------

auth.initialize();
friendships.initialize();
game.initialize();

// ------------------------------------------------------------

auth.registerLoginHandler(ui.handleLogin);
auth.registerLogoutHandler(ui.handleLogout);
friendships.registerFriendshipsChangeHandler(ui.handleFriendshipsChange);
game.registerGameStateChangeHandler(ui.handleGameStateChange);
// TODO: Register UI event handlers

// ------------------------------------------------------------

if (auth.isLoggedIn()) {
  ui.handleLogin();
}