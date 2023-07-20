let sessionToken;
let connection;
const socketCallbacks = [];
const loginCallbacks = [];
const logoutCallbacks = [];
const accountCreationCallbacks = [];
const accountUpdateCallbacks = [];
const accountDeleteCallbacks = [];

// ------------------------------------------------------------

const callApi = async (path, method, contentType, body) => {
  const url = 'http://127.0.0.1:8080' + path;
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

const openSocketConnection = async () => {
  // TODO: Test and verify that X-Authorization cookie gets sent
  await new Promise(resolve => {
    const socket = new WebSocket('ws://127.0.0.1:8080/api/ws');
    socket.addEventListener('open', () => {
      resolve();
    });
  });
  // TODO: Add event handler to invoke socket callbacks
  connection = socket;
};

const closeSocketConnection = async () => {
  const socket = connection;
  connection = null;
  await new Promise(resolve => {
    socket.addEventListener('close', () => {
      resolve();
    });
    socket.close();
  });
};

const sendSocketMessage = (obj) => {
  connection.send(JSON.stringify(obj));
};

const registerSocketCallback = (callback) => {
  socketCallbacks.push(callback);
};

const getCookie = (key) => {
  const cookieString = RegExp(key + "=[^;]+").exec(document.cookie);
  return decodeURIComponent(!!cookieString ? cookieString.toString().replace(/^[^=]+./,"") : "");
};

const checkLoggedIn = () => {
  return getCookie('token') !== '';
};

const login = async (username, password) => {
  const response = await callApi('/api/auth', 'POST', 'application/json', JSON.stringify({
    username: username,
    password, password
  }));
  if (response.ok) {
    const session = await response.json();
    sessionToken = session.token;
  }
  for (callback of loginCallbacks) {
    callback(response.status);
  }
};

const registerLoginCallback = (callback) => {
  loginCallbacks.push(callback);
};

const logout = async () => {
  await callApi('/api/auth', 'DELETE', null, null);
  sessionToken = null;
  for (callback of logoutCallbacks) {
    callback();
  }
};

const registerLogoutCallback = (callback) => {
  logoutCallbacks.push(callback);
};

const getAccount = async (accountId) => {
  let url = '/api/account';
  if (accountId != null) {
    url += '?id=' + accountId;
  }
  const response = await callApi(url, 'GET', null, null);
  if (response.ok) {
    return await response.json();
  }
  return null;
};

const createAccount = async (username, password, publicName) => {
  const response = await callApi('/api/account', 'POST', 'application/json', JSON.stringify({
    loginName: username,
    password: password,
    publicName: publicName
  }));
  let responseBody;
  if (response.ok) {
    responseBody = await response.json();
  }
  for (callback of accountCreationCallbacks) {
    callback(response.status, responseBody);
  }
  if (response.ok) {
    return responseBody;
  }
  return null;
};

const registerAccountCreationCallback = (callback) => {
  accountCreationCallbacks.push(callback);
};

const updateAccount = async (accountId, username, password, publicName) => {
  let url = '/api/account';
  if (accountId != null) {
    url += '?id=' + accountId;
  }
  const response = await callApi(url, 'PUT', 'application/json', JSON.stringify({
    loginName: username,
    password: password,
    publicName: publicName
  }));
  let responseBody;
  if (response.ok) {
    responseBody = await response.json();
  }
  for (callback of accountUpdateCallbacks) {
    callback(response.status, responseBody);
  }
  if (response.ok) {
    return responseBody;
  }
  return null;
};

const registerAccountUpdateCallback = (callback) => {
  accountUpdateCallbacks.push(callback);
};

const deleteAccount = async (accountId) => {
  let url = '/api/account';
  if (accountId != null) {
    url += '?id=' + accountId;
  }
  const response = await callApi(url, 'DELETE', null, null);
  for (callback of accountDeleteCallbacks) {
    callback(response.status);
  }
};

const registerAccountDeleteCallback = (callback) => {
  accountDeleteCallbacks.push(callback);
};