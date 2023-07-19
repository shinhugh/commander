const body = document.getElementsByTagName('body')[0];
const contentAreaRoot = document.getElementById('content_area_root');
const loginModuleRoot = document.getElementById('login_module_root');
const loginModuleTabs = document.getElementById('login_module_tabs');
const loginTab = document.getElementById('login_tab');
const accountCreationTab = document.getElementById('account_creation_tab');
const loginModulePages = document.getElementById('login_module_pages');
const loginPage = document.getElementById('login_page');
const loginUsernameInput = document.getElementById('login_username_input');
const loginPasswordInput = document.getElementById('login_password_input');
const loginButton = document.getElementById('login_button');
const accountCreationPage = document.getElementById('account_creation_page');
const accountCreationUsernameInput = document.getElementById('account_creation_username_input');
const accountCreationPasswordInput = document.getElementById('account_creation_password_input');
const accountCreationPublicNameInput = document.getElementById('account_creation_public_name_input');
const accountCreationButton = document.getElementById('account_creation_button');
const lobbyModuleRoot = document.getElementById('lobby_module_root');
const activeGamesList = document.getElementById('active_games_list');
const invitationsList = document.getElementById('invitations_list');
const pendingGamesList = document.getElementById('pending_games_list');
const gameEntryTemplate = document.getElementsByClassName('game_entry')[0];
const topBarRoot = document.getElementById('top_bar_root');
const topBarLeftStrip = document.getElementById('top_bar_left_strip');
const topBarRightStrip = document.getElementById('top_bar_right_strip');
const createGameButton = document.getElementById('create_game_button');
const friendsButton = document.getElementById('friends_button');
const accountButton = document.getElementById('account_button');
const logoutButton = document.getElementById('logout_button');
const overlayBackdrop = document.getElementById('overlay_backdrop');
const overlayDialog = document.getElementById('overlay_dialog');
const activeGameEntryPage = document.getElementById('active_game_entry_page');
const invitationEntryPage = document.getElementById('invitation_entry_page');
const pendingGameEntryPage = document.getElementById('pending_game_entry_page');
const createGamePage = document.getElementById('create_game_page');
const friendsPage = document.getElementById('friends_page');
const accountPage = document.getElementById('account_page');
const accountPageUsername = document.getElementById('account_page_username');
const accountPagePublicName = document.getElementById('account_page_public_name');
const accountPageModifyButton = document.getElementById('account_page_modify_button');
const modifyAccountPage = document.getElementById('modify_account_page');
const modifyAccountPageUsername = document.getElementById('modify_account_page_username');
const modifyAccountPagePassword = document.getElementById('modify_account_page_password');
const modifyAccountPagePublicName = document.getElementById('modify_account_page_public_name');
const modifyAccountPageCancelButton = document.getElementById('modify_account_page_cancel_button');
const modifyAccountPageDeleteButton = document.getElementById('modify_account_page_delete_button');
const modifyAccountPageSaveButton = document.getElementById('modify_account_page_save_button');
const notificationRoot = document.getElementById('notification_root');
const notificationMessage = document.getElementById('notification_message');

// ------------------------------------------------------------

let notificationTimeoutId;

// ------------------------------------------------------------

const showOverlay = () => {
  overlayBackdrop.style.removeProperty('display');
};

const hideOverlay = () => {
  overlayBackdrop.style.display = 'none';
};

const clearOverlay = () => {
  activeGameEntryPage.style.display = 'none';
  invitationEntryPage.style.display = 'none';
  pendingGameEntryPage.style.display = 'none';
  createGamePage.style.display = 'none';
  friendsPage.style.display = 'none';
  accountPage.style.display = 'none';
  modifyAccountPage.style.display = 'none';
};

const showActiveGameEntryPage = (gameEntryId) => { // TODO
  clearOverlay();
  activeGameEntryPage.style.removeProperty('display');
  showOverlay();
};

const showInvitationEntryPage = (gameEntryId) => { // TODO
  clearOverlay();
  invitationEntryPage.style.removeProperty('display');
  showOverlay();
};

const showPendingGameEntryPage = (gameEntryId) => { // TODO
  clearOverlay();
  pendingGameEntryPage.style.removeProperty('display');
  showOverlay();
};

const showCreateGamePage = () => {
  clearOverlay();
  createGamePage.style.removeProperty('display');
  showOverlay();
};

const showAddFriendPage = () => {
  clearOverlay();
  friendsPage.style.removeProperty('display');
  showOverlay();
};

const showAccountPage = () => {
  clearOverlay();
  accountPage.style.removeProperty('display');
  showOverlay();
};

const showModifyAccountPage = () => {
  clearOverlay();
  modifyAccountPage.style.removeProperty('display');
  showOverlay();
  modifyAccountPageUsername.focus();
};

const notify = (message) => {
  clearTimeout(notificationTimeoutId);
  notificationMessage.innerHTML = message;
  notificationRoot.style.removeProperty('display');
  notificationTimeoutId = setTimeout(() => {
    notificationRoot.style.display = 'none';
  }, 2000);
};

const showLoginModule = () => {
  lobbyModuleRoot.style.display = 'none';
  loginModuleRoot.style.removeProperty('display');
};

const showLobbyModule = () => {
  loginModuleRoot.style.display = 'none';
  lobbyModuleRoot.style.removeProperty('display');
};

const tabToLogin = () => {
  accountCreationPage.style.display = 'none';
  loginPage.style.removeProperty('display');
  accountCreationTab.classList.remove('selected');
  loginTab.classList.add('selected');
  loginUsernameInput.focus();
};

const tabToAccountCreation = () => {
  loginPage.style.display = 'none';
  accountCreationPage.style.removeProperty('display');
  loginTab.classList.remove('selected');
  accountCreationTab.classList.add('selected');
  accountCreationUsernameInput.focus();
};

const loginFromInput = async () => {
  const username = loginUsernameInput.value;
  const password = loginPasswordInput.value;
  await login(username, password);
  await openSocketConnection();
};

const createAccountFromInput = async () => {
  const username = accountCreationUsernameInput.value;
  const password = accountCreationPasswordInput.value;
  const publicName = accountCreationPublicNameInput.value;
  await createAccount(username, password, publicName);
};

const modifyAccountFromInput = async () => {
  const username = modifyAccountPageUsername.value;
  const password = modifyAccountPagePassword.value;
  const publicName = modifyAccountPagePublicName.value;
  await updateAccount(null, username, password, publicName);
};

const addActiveGameEntry = (players, nextTurn, timestamp) => {
  const entry = gameEntryTemplate.cloneNode(true);
  let playersString = 'test'; // TODO
  let nextTurnString = 'next'; // TODO
  let timestampString = 'timestamp'; // TODO
  entry.getElementsByClassName('game_entry_players')[0].innerHTML = playersString;
  entry.getElementsByClassName('game_entry_next_turn')[0].innerHTML = nextTurnString;
  entry.getElementsByClassName('game_entry_time')[0].innerHTML = timestampString;
  entry.addEventListener('click', () => {
    showActiveGameEntryPage(0); // TODO
  });
  activeGamesList.appendChild(entry);
};

const addInvitationEntry = (players, timestamp) => {
  const entry = gameEntryTemplate.cloneNode(true);
  let playersString = 'test'; // TODO
  let timestampString = 'timestamp'; // TODO
  entry.getElementsByClassName('game_entry_players')[0].innerHTML = playersString;
  entry.getElementsByClassName('game_entry_next_turn')[0].remove();
  entry.getElementsByClassName('game_entry_time')[0].innerHTML = timestampString;
  entry.addEventListener('click', () => {
    showInvitationEntryPage(0); // TODO
  });
  invitationsList.appendChild(entry);
};

const addPendingGameEntry = (players, timestamp) => {
  const entry = gameEntryTemplate.cloneNode(true);
  let playersString = 'test'; // TODO
  let timestampString = 'timestamp'; // TODO
  entry.getElementsByClassName('game_entry_players')[0].innerHTML = playersString;
  entry.getElementsByClassName('game_entry_next_turn')[0].remove();
  entry.getElementsByClassName('game_entry_time')[0].innerHTML = timestampString;
  entry.addEventListener('click', () => {
    showPendingGameEntryPage(0); // TODO
  });
  pendingGamesList.appendChild(entry);
};

// ------------------------------------------------------------

overlayBackdrop.style.display = 'none';
notificationRoot.style.display = 'none';
showLoginModule();
tabToLogin();
gameEntryTemplate.remove();
createGameButton.style.display = 'none';
friendsButton.style.display = 'none';
accountButton.style.display = 'none';
logoutButton.style.display = 'none';
clearOverlay();

overlayBackdrop.addEventListener('click', hideOverlay);
overlayDialog.addEventListener('click', (e) => {
  e.stopPropagation();
});
loginTab.addEventListener('click', tabToLogin);
accountCreationTab.addEventListener('click', tabToAccountCreation);
loginButton.addEventListener('click', loginFromInput);
accountCreationButton.addEventListener('click', createAccountFromInput);
createGameButton.addEventListener('click', () => {
  showCreateGamePage();
});
friendsButton.addEventListener('click', () => {
  showAddFriendPage();
});
accountButton.addEventListener('click', () => {
  showAccountPage();
});
logoutButton.addEventListener('click', logout);
loginUsernameInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    loginPasswordInput.focus();
  }
});
loginPasswordInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    loginFromInput();
  }
});
accountCreationUsernameInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    accountCreationPasswordInput.focus();
  }
});
accountCreationPasswordInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    accountCreationPublicNameInput.focus();
  }
});
accountCreationPublicNameInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    createAccountFromInput();
  }
});
modifyAccountPageUsername.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    modifyAccountPagePassword.focus();
  }
});
modifyAccountPagePassword.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    modifyAccountPagePublicName.focus();
  }
});
modifyAccountPagePublicName.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    modifyAccountFromInput();
  }
});
accountPageModifyButton.addEventListener('click', () => {
  modifyAccountPageUsername.value = null; // TODO
  modifyAccountPagePassword.value = null;
  modifyAccountPagePublicName.value = null; // TODO
  showModifyAccountPage();
});
modifyAccountPageCancelButton.addEventListener('click', () => {
  showAccountPage();
  modifyAccountPageUsername.value = null;
  modifyAccountPagePassword.value = null;
  modifyAccountPagePublicName.value = null;
});
modifyAccountPageSaveButton.addEventListener('click', modifyAccountFromInput);
modifyAccountPageDeleteButton.addEventListener('click', async () => {
  await deleteAccount();
});

registerLoginCallback(async (status) => {
  if (status === 200) {
    createGameButton.style.removeProperty('display');
    friendsButton.style.removeProperty('display');
    accountButton.style.removeProperty('display');
    logoutButton.style.removeProperty('display');
    showLobbyModule();
    const account = await getAccount();
    accountPageUsername.innerHTML = account.loginName;
    accountPagePublicName.innerHTML = account.publicName;
    loginUsernameInput.value = null;
    loginPasswordInput.value = null;
    accountCreationUsernameInput.value = null;
    accountCreationPasswordInput.value = null;
    accountCreationPublicNameInput.value = null;
  } else {
    notify('Failed to login');
  }
});

registerLogoutCallback(() => {
  createGameButton.style.display = 'none';
  friendsButton.style.display = 'none';
  accountButton.style.display = 'none';
  logoutButton.style.display = 'none';
  showLoginModule();
  tabToLogin();
  accountPageUsername.innerHTML = '';
  accountPagePublicName.innerHTML = '';
});

registerAccountCreationCallback((status, account) => {
  if (status === 200) {
    notify('Successfully created account');
    loginUsernameInput.value = account.loginName;
    loginPasswordInput.value = null;
    tabToLogin();
    loginPasswordInput.focus();
    accountCreationUsernameInput.value = null;
    accountCreationPasswordInput.value = null;
    accountCreationPublicNameInput.value = null;
  } else if (status == 400) {
    notify('Provided values do not meet requirements');
  } else {
    notify('Failed to create account');
  }
});

registerAccountUpdateCallback(async (status, account) => {
  if (status === 200) {
    notify('Successfully modified account');
    hideOverlay();
    await logout();
    loginUsernameInput.value = account.loginName;
    loginPasswordInput.focus();
  } else if (status == 400) {
    notify('Provided values do not meet requirements');
  } else {
    notify('Failed to modify account');
  }
});

registerAccountDeleteCallback(async (status) => {
  if (status === 200) {
    notify('Successfully deleted account');
    await logout();
    hideOverlay();
  } else {
    notify('Failed to delete account');
  }
});

if (checkLoggedIn()) {
  createGameButton.style.removeProperty('display');
  friendsButton.style.removeProperty('display');
  accountButton.style.removeProperty('display');
  logoutButton.style.removeProperty('display');
  showLobbyModule();
}