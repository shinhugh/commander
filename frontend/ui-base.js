/* Requires:
 * - auth.js
 * - accounts.js
 * - friendships.js
 * - ui-api.js
 */

const uiBase = {

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
        game: document.getElementById('content_game_module_game')
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

  state: {
    notificationTimeoutId: null,
    addFriendButtonHandler: null
  },

  clearContent: () => {
    for (const child of uiBase.elements.content.root.children) {
      uiApi.hide(child);
    }
  },

  showLoginModule: () => {
    uiBase.clearContent();
    uiApi.show(uiBase.elements.content.loginModule.root);
  },

  showLoginPage: () => {
    uiApi.hide(uiBase.elements.content.loginModule.pages.createAccountPage.root);
    uiApi.show(uiBase.elements.content.loginModule.pages.loginPage.root);
    uiApi.unselect(uiBase.elements.content.loginModule.tabs.createAccountTab);
    uiApi.select(uiBase.elements.content.loginModule.tabs.loginTab);
  },

  showCreateAccountPage: () => {
    uiApi.hide(uiBase.elements.content.loginModule.pages.loginPage.root);
    uiApi.show(uiBase.elements.content.loginModule.pages.createAccountPage.root);
    uiApi.unselect(uiBase.elements.content.loginModule.tabs.loginTab);
    uiApi.select(uiBase.elements.content.loginModule.tabs.createAccountTab);
  },

  showGameModule: () => {
    uiBase.clearContent();
    uiApi.show(uiBase.elements.content.gameModule.root);
  },

  hideTopBarTitle: () => {
    uiApi.hide(uiBase.elements.topBar.title);
  },

  showTopBarTitle: () => {
    uiApi.show(uiBase.elements.topBar.title);
  },

  hideTopBarButtons: () => {
    uiApi.hide(uiBase.elements.topBar.friendsButton);
    uiApi.hide(uiBase.elements.topBar.accountButton);
    uiApi.hide(uiBase.elements.topBar.logoutButton);
  },

  showTopBarButtons: () => {
    uiApi.show(uiBase.elements.topBar.friendsButton);
    uiApi.show(uiBase.elements.topBar.accountButton);
    uiApi.show(uiBase.elements.topBar.logoutButton);
  },

  hideOverlay: () => {
    uiApi.hide(uiBase.elements.overlay.root);
  },

  showOverlay: () => {
    uiApi.show(uiBase.elements.overlay.root);
  },

  clearOverlay: () => {
    for (const child of uiBase.elements.overlay.root.children) {
      uiApi.hide(child);
    }
  },

  showFriendsPage: () => {
    uiBase.clearOverlay();
    uiApi.show(uiBase.elements.overlay.friendsPage.root);
  },

  addIncomingFriendshipRequestEntry: (friendEntry) => {
    const entry = uiBase.elements.overlay.friendsPage.incomingFriendshipRequestEntryTemplate.cloneNode(true);
    entry.getElementsByClassName('friend_entry_name')[0].innerHTML = friendEntry.friendAccount.publicName;
    entry.getElementsByClassName('friend_entry_accept_button')[0].addEventListener('click', async () => {
      try {
        await friendships.requestFriendship(friendEntry.friendAccount.id);
      }
      catch {
        uiBase.notify('Failed to accept friend request');
        return;
      }
      uiBase.notify('Accepted friend request');
    });
    entry.getElementsByClassName('friend_entry_deny_button')[0].addEventListener('click', async () => {
      try {
        await friendships.terminateFriendship(friendEntry.friendAccount.id);
      }
      catch {
        uiBase.notify('Failed to deny friend request');
        return;
      }
      uiBase.notify('Denied friend request');
    });
    uiBase.elements.overlay.friendsPage.friendsList.insertBefore(entry, uiBase.elements.overlay.friendsPage.friendsList.firstElementChild);
  },

  addConfirmedFriendshipEntry: (friendEntry) => {
    const entry = uiBase.elements.overlay.friendsPage.confirmedFriendshipEntryTemplate.cloneNode(true);
    entry.getElementsByClassName('friend_entry_name')[0].innerHTML = friendEntry.friendAccount.publicName;
    entry.getElementsByClassName('friend_entry_remove_button')[0].addEventListener('click', async () => {
      try {
        await friendships.terminateFriendship(friendEntry.friendAccount.id);
      }
      catch {
        uiBase.notify('Failed to remove friend');
        return;
      }
      uiBase.notify('Removed friend');
    });
    uiBase.elements.overlay.friendsPage.friendsList.insertBefore(entry, uiBase.elements.overlay.friendsPage.friendsList.firstElementChild);
  },

  addOutgoingFriendshipRequestEntry: (friendEntry) => {
    const entry = uiBase.elements.overlay.friendsPage.outgoingFriendshipRequestEntryTemplate.cloneNode(true);
    entry.getElementsByClassName('friend_entry_name')[0].innerHTML = friendEntry.friendAccount.publicName;
    entry.getElementsByClassName('friend_entry_cancel_button')[0].addEventListener('click', async () => {
      try {
        await friendships.terminateFriendship(friendEntry.friendAccount.id);
      }
      catch {
        uiBase.notify('Failed to cancel friend request');
        return;
      }
      uiBase.notify('Canceled friend request');
    });
    uiBase.elements.overlay.friendsPage.friendsList.insertBefore(entry, uiBase.elements.overlay.friendsPage.friendsList.firstElementChild);
  },

  refreshFriendsList: () => {
    uiBase.elements.overlay.friendsPage.friendsList.innerHTML = null;
    const friends = friendships.getFriendships();
    if (friends == null) {
      return;
    }
    if (friends.outgoingRequests != null) {
      for (const friendEntry of friends.outgoingRequests) {
        uiBase.addOutgoingFriendshipRequestEntry(friendEntry);
      }
    }
    if (friends.confirmedFriendships != null) {
      for (const friendEntry of friends.confirmedFriendships) {
        uiBase.addConfirmedFriendshipEntry(friendEntry);
      }
    }
    if (friends.incomingRequests != null) {
      for (const friendEntry of friends.incomingRequests) {
        uiBase.addIncomingFriendshipRequestEntry(friendEntry);
      }
    }
  },

  clearFriendshipSearchResult: () => {
    uiBase.elements.overlay.friendsPage.addFriendSection.result.name.innerHTML = null;
    uiBase.elements.overlay.friendsPage.addFriendSection.result.addFriendButton.removeEventListener('click', uiBase.state.addFriendButtonHandler);
    uiBase.state.addFriendButtonHandler = null;
    uiApi.cloak(uiBase.elements.overlay.friendsPage.addFriendSection.result.root);
  },

  showAccountPage: () => {
    uiBase.clearOverlay();
    uiApi.show(uiBase.elements.overlay.accountPage.root);
  },

  refreshAccountPage: () => {
    const account = accounts.getSelf();
    if (account == null) {
      uiBase.elements.overlay.accountPage.id.innerHTML = null;
      uiBase.elements.overlay.accountPage.username.innerHTML = null;
      uiBase.elements.overlay.accountPage.publicName.innerHTML = null;
    } else {
      uiBase.elements.overlay.accountPage.id.innerHTML = account.id;
      uiBase.elements.overlay.accountPage.username.innerHTML = account.loginName;
      uiBase.elements.overlay.accountPage.publicName.innerHTML = account.publicName;
    }
  },

  showModifyAccountPage: () => {
    uiBase.clearOverlay();
    uiApi.show(uiBase.elements.overlay.modifyAccountPage.root);
  },

  refreshModifyAccountPage: () => {
    const account = accounts.getSelf();
    if (account == null) {
      uiBase.elements.overlay.modifyAccountPage.usernameInput.value = null;
      uiBase.elements.overlay.modifyAccountPage.passwordInput.value = null;
      uiBase.elements.overlay.modifyAccountPage.publicNameInput.value = null;
    } else {
      uiBase.elements.overlay.modifyAccountPage.usernameInput.value = account.loginName;
      uiBase.elements.overlay.modifyAccountPage.passwordInput.value = null;
      uiBase.elements.overlay.modifyAccountPage.publicNameInput.value = account.publicName;
    }
  },

  notify: (message) => {
    clearTimeout(uiBase.state.notificationTimeoutId);
    uiBase.elements.notification.message.innerHTML = message;
    uiApi.show(uiBase.elements.notification.root);
    uiBase.state.notificationTimeoutId = setTimeout(() => {
      uiApi.hide(uiBase.elements.notification.root);
      uiBase.elements.notification.message.innerHTML = null;
    }, 2000);
  },

  hideBlocker: () => {
    uiApi.hide(uiBase.elements.blocker.root);
  },

  showBlocker: () => {
    uiApi.show(uiBase.elements.blocker.root);
  },

  parseInputAndLogin: async () => {
    const username = uiBase.elements.content.loginModule.pages.loginPage.usernameInput.value;
    const password = uiBase.elements.content.loginModule.pages.loginPage.passwordInput.value;
    if (username === '' || password === '') {
      return;
    }
    try {
      await auth.login(username, password);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          uiBase.notify('Failed to login');
          break;
        case '401':
          uiBase.notify('Invalid credentials');
          break;
      }
      return;
    }
  },

  parseInputAndCreateAccount: async () => {
    const username = uiBase.elements.content.loginModule.pages.createAccountPage.usernameInput.value;
    const password = uiBase.elements.content.loginModule.pages.createAccountPage.passwordInput.value;
    const publicName = uiBase.elements.content.loginModule.pages.createAccountPage.publicNameInput.value;
    let account;
    try {
      account = await accounts.createAccount(username, password, publicName);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          uiBase.notify('Rules not met');
          break;
        case '409':
          uiBase.notify('Username already taken');
          break;
      }
      return;
    }
    uiBase.notify('Successfully created account');
    uiBase.showLoginPage();
    uiBase.elements.content.loginModule.pages.loginPage.usernameInput.value = account.loginName;
    uiBase.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
    uiBase.elements.content.loginModule.pages.loginPage.passwordInput.focus();
    uiBase.elements.content.loginModule.pages.createAccountPage.usernameInput.value = null;
    uiBase.elements.content.loginModule.pages.createAccountPage.passwordInput.value = null;
    uiBase.elements.content.loginModule.pages.createAccountPage.publicNameInput.value = null;
  },

  parseInputAndShowFriendshipSearchResult: async () => {
    const accountId = parseInt(uiBase.elements.overlay.friendsPage.addFriendSection.idInput.value);
    if (isNaN(accountId)) {
      return;
    }
    if (accountId === accounts.getSelf().id) {
      uiBase.notify('Cannot add self as friend');
      return;
    }
    let matches;
    try {
      matches = await accounts.readAccounts(accountId);
    }
    catch (e) {
      uiBase.notify('Failed to search for user');
      return;
    }
    if (matches.length == 0) {
      uiBase.notify('User not found');
      return;
    }
    uiBase.elements.overlay.friendsPage.addFriendSection.result.name.innerHTML = matches[0].publicName;
    uiBase.elements.overlay.friendsPage.addFriendSection.result.addFriendButton.removeEventListener('click', uiBase.state.addFriendButtonHandler);
    uiBase.state.addFriendButtonHandler = async () => {
      try {
        await friendships.requestFriendship(accountId);
      }
      catch (e) {
        switch (e.message) {
          case '409':
            uiBase.notify('User is already a confirmed or requested friend');
            break;
          default:
            uiBase.notify('Failed to add friend');
            break;
        }
        return;
      }
      uiApi.cloak(uiBase.elements.overlay.friendsPage.addFriendSection.result.root);
      uiBase.elements.overlay.friendsPage.addFriendSection.result.name.innerHTML = null;
      uiBase.elements.overlay.friendsPage.addFriendSection.result.addFriendButton.removeEventListener('click', uiBase.state.addFriendButtonHandler);
      uiBase.state.addFriendButtonHandler = null;
      uiBase.elements.overlay.friendsPage.addFriendSection.idInput.value = null;
      uiBase.notify('Friendship request sent');
    };
    uiBase.elements.overlay.friendsPage.addFriendSection.result.addFriendButton.addEventListener('click', uiBase.state.addFriendButtonHandler);
    uiApi.uncloak(uiBase.elements.overlay.friendsPage.addFriendSection.result.root);
  },

  parseInputAndUpdateAccount: async () => {
    let username = uiBase.elements.overlay.modifyAccountPage.usernameInput.value;
    username = username.length > 0 ? username : null;
    let password = uiBase.elements.overlay.modifyAccountPage.passwordInput.value;
    password = password.length > 0 ? password : null;
    let publicName = uiBase.elements.overlay.modifyAccountPage.publicNameInput.value;
    publicName = publicName.length > 0 ? publicName : null;
    try {
      await accounts.updateAccount(null, username, password, null, publicName);
    }
    catch (e) {
      switch (e.message) {
        case '400':
          uiBase.notify('Rules not met');
          break;
        case '401':
          uiBase.notify('Not authenticated');
          break;
        case '403':
          uiBase.notify('Not authorized');
          break;
        case '404':
          uiBase.notify('No such account');
          break;
        case '409':
          uiBase.notify('Username already taken');
          break;
      }
      return;
    }
    uiBase.notify('Successfully updated account');
    try {
      await auth.logout();
    }
    catch { }
  },

  handleLogin: () => {
    uiBase.showBlocker();
    uiBase.showGameModule();
    uiBase.hideTopBarTitle();
    uiBase.showTopBarButtons();
    uiBase.elements.content.loginModule.pages.loginPage.usernameInput.value = null;
    uiBase.elements.content.loginModule.pages.loginPage.passwordInput.value = null;
    uiBase.elements.content.loginModule.pages.createAccountPage.usernameInput.value = null;
    uiBase.elements.content.loginModule.pages.createAccountPage.passwordInput.value = null;
    uiBase.elements.content.loginModule.pages.createAccountPage.publicNameInput.value = null;
    uiBase.hideBlocker();
  },

  handleLogout: () => {
    uiBase.showBlocker();
    uiBase.hideOverlay();
    uiBase.hideTopBarButtons();
    uiBase.showTopBarTitle();
    uiBase.showLoginPage();
    uiBase.showLoginModule();
    uiBase.elements.content.loginModule.pages.loginPage.usernameInput.focus();
    uiBase.clearFriendshipSearchResult();
    uiBase.elements.overlay.friendsPage.addFriendSection.idInput.value = null;
    uiBase.hideBlocker();
  },

  handleSelfChange: () => {
    uiBase.refreshAccountPage();
    uiBase.refreshModifyAccountPage();
  },

  handleFriendshipsChange: () => {
    uiBase.refreshFriendsList();
  }

};

// ------------------------------------------------------------

uiApi.hide(uiBase.elements.content.loginModule.root);
uiApi.hide(uiBase.elements.content.loginModule.pages.loginPage.root);
uiApi.hide(uiBase.elements.content.loginModule.pages.createAccountPage.root);
uiApi.hide(uiBase.elements.content.gameModule.root);
uiApi.hide(uiBase.elements.topBar.title);
uiApi.hide(uiBase.elements.topBar.friendsButton);
uiApi.hide(uiBase.elements.topBar.accountButton);
uiApi.hide(uiBase.elements.topBar.logoutButton);
uiApi.hide(uiBase.elements.overlay.root);
uiApi.hide(uiBase.elements.overlay.friendsPage.root);
uiBase.elements.overlay.friendsPage.incomingFriendshipRequestEntryTemplate.remove();
uiBase.elements.overlay.friendsPage.confirmedFriendshipEntryTemplate.remove();
uiBase.elements.overlay.friendsPage.outgoingFriendshipRequestEntryTemplate.remove();
uiApi.cloak(uiBase.elements.overlay.friendsPage.addFriendSection.result.root);
uiApi.hide(uiBase.elements.overlay.accountPage.root);
uiApi.hide(uiBase.elements.overlay.modifyAccountPage.root);
uiApi.hide(uiBase.elements.notification.root);

// ------------------------------------------------------------

auth.registerLoginHandler(uiBase.handleLogin);
auth.registerLogoutHandler(uiBase.handleLogout);
accounts.registerSelfChangeHandler(uiBase.handleSelfChange);
friendships.registerFriendshipsChangeHandler(uiBase.handleFriendshipsChange);

// ------------------------------------------------------------

uiBase.elements.content.loginModule.tabs.loginTab.addEventListener('click', () => {
  uiBase.showLoginPage();
  uiBase.elements.content.loginModule.pages.loginPage.usernameInput.focus();
});
uiBase.elements.content.loginModule.tabs.createAccountTab.addEventListener('click', () => {
  uiBase.showCreateAccountPage();
  uiBase.elements.content.loginModule.pages.createAccountPage.usernameInput.focus();
});
uiBase.elements.content.loginModule.pages.loginPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    uiBase.elements.content.loginModule.pages.loginPage.passwordInput.focus();
  }
});
uiBase.elements.content.loginModule.pages.loginPage.passwordInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    await uiBase.parseInputAndLogin();
  }
});
uiBase.elements.content.loginModule.pages.loginPage.loginButton.addEventListener('click', async () => {
  await uiBase.parseInputAndLogin();
});
uiBase.elements.content.loginModule.pages.createAccountPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    uiBase.elements.content.loginModule.pages.createAccountPage.passwordInput.focus();
  }
});
uiBase.elements.content.loginModule.pages.createAccountPage.passwordInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    uiBase.elements.content.loginModule.pages.createAccountPage.publicNameInput.focus();
  }
});
uiBase.elements.content.loginModule.pages.createAccountPage.publicNameInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    await uiBase.parseInputAndCreateAccount();
  }
});
uiBase.elements.content.loginModule.pages.createAccountPage.createAccountButton.addEventListener('click', async () => {
  await uiBase.parseInputAndCreateAccount();
});
uiBase.elements.topBar.friendsButton.addEventListener('click', () => {
  uiBase.clearFriendshipSearchResult();
  uiBase.elements.overlay.friendsPage.addFriendSection.idInput.value = null;
  uiBase.showFriendsPage();
  uiBase.showOverlay();
});
uiBase.elements.topBar.accountButton.addEventListener('click', () => {
  uiBase.showAccountPage();
  uiBase.showOverlay();
});
uiBase.elements.topBar.logoutButton.addEventListener('click', async () => {
  try {
    await auth.logout();
  }
  catch { }
});
uiBase.elements.overlay.root.addEventListener('click', () => {
  uiBase.hideOverlay();
});
for (const child of uiBase.elements.overlay.root.children) {
  child.addEventListener('click', e => {
    e.stopPropagation();
  });
}
uiBase.elements.overlay.friendsPage.addFriendSection.idInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    await uiBase.parseInputAndShowFriendshipSearchResult();
  }
});
uiBase.elements.overlay.friendsPage.addFriendSection.searchButton.addEventListener('click', async () => {
  await uiBase.parseInputAndShowFriendshipSearchResult();
});
uiBase.elements.overlay.accountPage.modifyButton.addEventListener('click', () => {
  uiBase.refreshModifyAccountPage();
  uiBase.showModifyAccountPage();
  uiBase.elements.overlay.modifyAccountPage.usernameInput.focus();
});
uiBase.elements.overlay.modifyAccountPage.usernameInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    uiBase.elements.overlay.modifyAccountPage.passwordInput.focus();
  }
});
uiBase.elements.overlay.modifyAccountPage.passwordInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    uiBase.elements.overlay.modifyAccountPage.publicNameInput.focus();
  }
});
uiBase.elements.overlay.modifyAccountPage.publicNameInput.addEventListener('keydown', async e => {
  if (e.key === 'Enter') {
    await uiBase.parseInputAndUpdateAccount();
  }
});
uiBase.elements.overlay.modifyAccountPage.cancelButton.addEventListener('click', () => {
  uiBase.showAccountPage();
});
uiBase.elements.overlay.modifyAccountPage.deleteButton.addEventListener('click', async () => {
  try {
    await accounts.deleteAccount(null);
  }
  catch {
    uiBase.notify('Failed to delete account');
    return;
  }
  uiBase.notify('Successfully deleted account');
  try {
    await auth.logout();
  }
  catch { }
});
uiBase.elements.overlay.modifyAccountPage.saveButton.addEventListener('click', async () => {
  await uiBase.parseInputAndUpdateAccount();
});

// ------------------------------------------------------------

if (auth.isLoggedIn()) {
  uiBase.handleLogin();
} else {
  uiBase.handleLogout();
}