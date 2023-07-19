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
const topBarRoot = document.getElementById('top_bar_root');
const topBarLeftStrip = document.getElementById('top_bar_left_strip');
const topBarRightStrip = document.getElementById('top_bar_right_strip');
const logoutButton = document.getElementById('logout_button');
const notificationRoot = document.getElementById('notification_root');
const notificationMessage = document.getElementById('notification_message');

// ------------------------------------------------------------

let notificationTimeoutId;

// ------------------------------------------------------------

const notify = (message) => {
  clearTimeout(notificationTimeoutId);
  notificationMessage.innerHTML = message;
  body.appendChild(notificationRoot);
  notificationTimeoutId = setTimeout(() => {
    notificationRoot.remove();
  }, 2000);
};

const showLoginModule = () => {
  contentAreaRoot.innerHTML = '';
  contentAreaRoot.appendChild(loginModuleRoot);
};

const showLobbyModule = () => {
  contentAreaRoot.innerHTML = '';
  contentAreaRoot.appendChild(lobbyModuleRoot);
};

const tabToLogin = () => {
  loginModulePages.innerHTML = '';
  loginModulePages.appendChild(loginPage);
  accountCreationTab.classList.remove('selected');
  loginTab.classList.add('selected');
  loginUsernameInput.focus();
};

const tabToAccountCreation = () => {
  loginModulePages.innerHTML = '';
  loginModulePages.appendChild(accountCreationPage);
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

notificationRoot.remove();
showLoginModule();
tabToLogin();
logoutButton.remove();

loginTab.addEventListener('click', tabToLogin);
accountCreationTab.addEventListener('click', tabToAccountCreation);
loginButton.addEventListener('click', loginFromInput);
accountCreationButton.addEventListener('click', createAccountFromInput);
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
    topBarRightStrip.appendChild(logoutButton);
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
  logoutButton.remove();
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
  topBarRightStrip.appendChild(logoutButton);
  showLobbyModule();
}