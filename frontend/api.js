let sessionToken;
let connection;
const loginCallbacks = [];
const logoutCallbacks = [];
const accountCreationCallbacks = [];

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

const openSocketConnection = async () => {
  // TODO: Cannot send authorization header; find workaround
  await new Promise(resolve => {
    const socket = new WebSocket('ws://localhost:8080/api/ws');
    socket.addEventListener('open', () => {
      resolve();
    });
  });
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

const getCookie = (key) => {
  const cookieString = RegExp(key + "=[^;]+").exec(document.cookie);
  // Return everything after the equal sign, or an empty string if the cookie name not found
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
    sessionToken = await response.text();
    document.cookie = 'token=' + sessionToken;
  }
  for (callback of loginCallbacks) {
    callback(response.ok);
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
    callback(response.status, username);
  }
  if (response.ok) {
    return responseBody;
  }
  throw new Error();
};

const registerAccountCreationCallback = (callback) => {
  accountCreationCallbacks.push(callback);
};