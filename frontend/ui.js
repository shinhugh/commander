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
const topBarRoot = document.getElementById('top_bar_root');
const topBarLeftStrip = document.getElementById('top_bar_left_strip');
const topBarRightStrip = document.getElementById('top_bar_right_strip');
const createGameButton = document.getElementById('create_game_button');
const addFriendButton = document.getElementById('add_friend_button');
const accountButton = document.getElementById('account_button');
const logoutButton = document.getElementById('logout_button');
const notificationRoot = document.getElementById('notification_root');
const notificationMessage = document.getElementById('notification_message');

// ------------------------------------------------------------

let notificationTimeoutId;

// ------------------------------------------------------------

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

// ------------------------------------------------------------

notificationRoot.style.display = 'none';
showLoginModule();
tabToLogin();
createGameButton.style.display = 'none';
addFriendButton.style.display = 'none';
accountButton.style.display = 'none';
logoutButton.style.display = 'none';

loginTab.addEventListener('click', tabToLogin);
accountCreationTab.addEventListener('click', tabToAccountCreation);
loginButton.addEventListener('click', loginFromInput);
accountCreationButton.addEventListener('click', createAccountFromInput);
createGameButton.addEventListener('click', () => {
  notify('Not implemented');
});
addFriendButton.addEventListener('click', () => {
  notify('Not implemented');
});
accountButton.addEventListener('click', () => {
  notify('Not implemented');
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

registerLoginCallback((success) => {
  if (success) {
    createGameButton.style.removeProperty('display');
    addFriendButton.style.removeProperty('display');
    accountButton.style.removeProperty('display');
    logoutButton.style.removeProperty('display');
    showLobbyModule();
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
  addFriendButton.style.display = 'none';
  accountButton.style.display = 'none';
  logoutButton.style.display = 'none';
  showLoginModule();
  tabToLogin();
});

registerAccountCreationCallback((status, username) => {
  if (status === 200) {
    notify('Successfully created account');
    loginUsernameInput.value = username;
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

if (checkLoggedIn()) {
  createGameButton.style.removeProperty('display');
  addFriendButton.style.removeProperty('display');
  accountButton.style.removeProperty('display');
  logoutButton.style.removeProperty('display');
  showLobbyModule();
}