let sessionToken;

// ------------------------------------------------------------

const callApi = async (path, method, contentType, body) => {
  const url = 'http://localhost:8080' + path;
  const headers = { };
  if (sessionToken != null) {
    headers.authorization = 'Bearer ' + sessionToken;
  }
  if (contentType != null) {
    headers['content-type'] = contentType;
  }
  const options = {
    method: method,
    headers: headers
  };
  if (body != null) {
    options.body = body;
  }
  return await fetch(url, options);
};

const login = async (username, password) => {
  const response = await callApi('/api/auth', 'POST', 'application/json', JSON.stringify({
    username: username,
    password, password
  }));
  if (response.ok) {
    sessionToken = await response.text();
  }
};

const logout = async () => {
  await callApi('/api/auth', 'DELETE', null, null);
  sessionToken = null;
};

const createAccount = async (username, password, publicName) => {
  const response = await callApi('/api/account', 'POST', 'application/json', JSON.stringify({
    loginName: username,
    password: password,
    publicName: publicName
  }));
  if (response.ok) {
    return await response.json();
  }
  throw new Error();
};

// ------------------------------------------------------------

const topBarRoot = document.getElementById('top_bar_root');
const logoutButton = document.getElementById('logout_button');
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

// ------------------------------------------------------------

const showLoginModule = () => {
  contentAreaRoot.innerHTML = '';
  contentAreaRoot.appendChild(loginModuleRoot);
};

const tabToLogin = () => {
  loginModulePages.innerHTML = '';
  loginModulePages.appendChild(loginPage);
  accountCreationTab.classList.remove('selected');
  loginTab.classList.add('selected');
};

const tabToAccountCreation = () => {
  loginModulePages.innerHTML = '';
  loginModulePages.appendChild(accountCreationPage);
  loginTab.classList.remove('selected');
  accountCreationTab.classList.add('selected');
};

const loginFromInput = async () => {
  const username = loginUsernameInput.value;
  const password = loginPasswordInput.value;
  await login(username, password);
};

const createAccountFromInput = async () => {
  const username = accountCreationUsernameInput.value;
  const password = accountCreationPasswordInput.value;
  const publicName = accountCreationPublicNameInput.value;
  await createAccount(username, password, publicName);
};

// ------------------------------------------------------------

// loginModuleRoot.remove();
lobbyModuleRoot.remove();
loginPage.remove();
accountCreationPage.remove();

logoutButton.addEventListener('click', logout);

loginTab.addEventListener('click', tabToLogin);
accountCreationTab.addEventListener('click', tabToAccountCreation);

loginButton.addEventListener('click', loginFromInput);
accountCreationButton.addEventListener('click', createAccountFromInput);