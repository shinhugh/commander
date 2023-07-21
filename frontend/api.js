const api = {

  internal: {

    endpoint: window.location.hostname + ':' + window.location.port,

    socket: null,

    incomingSocketMessageHandlers: [],

    session: null,

    account: null,

    friends: [],

    connectSocket: async () => {
      const url = 'ws://' + api.internal.endpoint + '/api/ws';
      const socket = new WebSocket(url);
      await new Promise((resolve, reject) => {
        socket.addEventListener('open', () => {
          resolve();
        });
        socket.addEventListener('error', () => {
          reject();
        });
      });
      socket.addEventListener('message', e => {
        for (const handler of api.internal.incomingSocketMessageHandlers) {
          handler(e.data); // TODO: Is e.data correct? Expecting string
        }
      });
      api.internal.socket = socket;
    },

    disconnectSocket: async () => {
      if (api.internal.socket == null) {
        return;
      }
      const socket = api.internal.socket;
      api.internal.socket = null;
      await new Promise(resolve => {
        socket.addEventListener('close', () => {
          resolve();
        });
        socket.addEventListener('error', () => {
          resolve();
        });
        socket.close();
      });
    },

    makeRequest: async (path, method, parameters, contentType, body) => {
      let parameterString = '';
      if (parameters != null && Object.keys(parameters).length > 0) {
        parameterString += '?';
        for (const key of Object.keys(parameters)) {
          parameterString += key + '=' + parameters[key] + '&';
        }
        parameterString = parameterString.substring(0, parameterString.length - 1);
      }
      const url = 'http://' + api.internal.endpoint + path + parameterString;
      const headers = { };
      if (api.internal.session != null) {
        headers.authorization = 'Bearer ' + api.internal.session.token;
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
    }

  },

  initialize: async () => {
    try {
      await api.login();
    }
    catch { }
  },

  sendObjectOverSocket: (obj) => {
    if (api.internal.socket != null) {
      api.internal.socket.send(JSON.stringify(obj));
    }
  },

  registerIncomingSocketMessageHandler: (handler) => {
    api.internal.incomingSocketMessageHandlers.push(handler);
  },

  getLoggedInAccount: () => {
    return api.internal.account;
  },

  login: async (username, password) => {
    if (api.internal.session != null) {
      return;
    }
    const response = await api.internal.makeRequest('/api/auth', 'POST', null, 'application/json', JSON.stringify({
      username: username,
      password, password
    }));
    if (response.ok) {
      api.internal.session = await response.json();
      api.internal.account = await api.readAccount(null);
      return;
    }
    throw new Error(response.status);
  },

  logout: async () => {
    if (api.internal.session == null) {
      return;
    }
    await api.internal.makeRequest('/api/auth', 'DELETE', null, null, null);
    api.internal.session = null;
    api.internal.account = null;
  },

  readAccount: async (accountId) => {
    let parameters = null;
    if (accountId != null) {
      parameters = {
        id: accountId
      };
    }
    const response = await api.internal.makeRequest('/api/account', 'GET', parameters, null, null);
    if (response.ok) {
      return await response.json();
    }
    throw new Error(response.status);
  },

  createAccount: async (username, password, publicName) => {
    const response = await api.internal.makeRequest('/api/account', 'POST', null, 'application/json', JSON.stringify({
      loginName: username,
      password: password,
      publicName: publicName
    }));
    if (response.ok) {
      return await response.json();
    }
    throw new Error(response.status);
  },

  updateAccount: async (accountId, username, password, authorities, publicName) => {
    let parameters = null;
    if (accountId != null) {
      parameters = {
        id: accountId
      };
    }
    const response = await api.internal.makeRequest('/api/account', 'PUT', parameters, 'application/json', JSON.stringify({
      loginName: username,
      password: password,
      authorities: authorities,
      publicName: publicName
    }));
    if (response.ok) {
      return await response.json();
    }
    throw new Error(response.status);
  },

  deleteAccount: async (accountId) => {
    let parameters = null;
    if (accountId != null) {
      parameters = {
        id: accountId
      };
    }
    const response = await api.internal.makeRequest('/api/account', 'DELETE', parameters, null, null);
    if (response.ok) {
      return;
    }
    throw new Error(response.status);
  },

  getFriends: async () => {
    const response = await api.internal.makeRequest('/api/friendship', 'GET', null, null, null);
    if (response.ok) {
      return await response.json();
    }
    throw new Error(response.status);
  },

  addFriend: async (accountId) => {
    let parameters = null;
    if (accountId != null) {
      parameters = {
        id: accountId
      };
    }
    const response = await api.internal.makeRequest('/api/friendship', 'POST', parameters, null, null);
    if (response.ok) {
      return;
    }
    throw new Error(response.status);
  },

  removeFriend: async (accountId) => {
    let parameters = null;
    if (accountId != null) {
      parameters = {
        id: accountId
      };
    }
    const response = await api.internal.makeRequest('/api/friendship', 'DELETE', parameters, null, null);
    if (response.ok) {
      return;
    }
    throw new Error(response.status);
  }

};