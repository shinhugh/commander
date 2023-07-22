const api = {

  internal: {

    endpoint: window.location.hostname + ':' + window.location.port,

    socket: null,

    incomingSocketMessageHandlers: [ ],

    session: null,

    account: null,

    friends: null,

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

    registerIncomingSocketMessageHandler: (handler) => {
      api.internal.incomingSocketMessageHandlers.push(handler);
    },

    sendObjectOverSocket: (obj) => {
      if (api.internal.socket != null) {
        api.internal.socket.send(JSON.stringify(obj));
      }
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
    },

    requestLogin: async (username, password) => {
      const response = await api.internal.makeRequest('/api/auth', 'POST', null, 'application/json', JSON.stringify({
        username: username,
        password, password
      }));
      if (response.ok) {
        return await response.json();
      }
      throw new Error(response.status);
    },

    requestLogout: async () => {
      await api.internal.makeRequest('/api/auth', 'DELETE', null, null, null);
    },

    requestReadAccount: async (accountId) => {
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

    requestCreateAccount: async (username, password, publicName) => {
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

    requestUpdateAccount: async (accountId, username, password, authorities, publicName) => {
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

    requestDeleteAccount: async (accountId) => {
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

    requestListFriendships: async () => {
      const response = await api.internal.makeRequest('/api/friendship', 'GET', null, null, null);
      if (response.ok) {
        return await response.json();
      }
      throw new Error(response.status);
    },

    requestRequestFriendship: async (accountId) => {
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

    requestTerminateFriendship: async (accountId) => {
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
    },

    logout: async () => {
      if (api.internal.session == null) {
        return;
      }
      await api.internal.requestLogout();
      api.internal.session = null;
      api.internal.account = null;
    },

    login: async (username, password) => {
      if (api.internal.session != null) {
        return;
      }
      api.internal.session = await api.internal.requestLogin(username, password);
      api.internal.account = await api.internal.requestReadAccount(null);
      // TODO: Connect WebSocket
      await api.internal.updateFriends();
    },

    createAccount: async (username, password, publicName) => {
      return await api.internal.requestCreateAccount(username, password, publicName);
    },

    updateAccount: async (accountId, username, password, authorities, publicName) => {
      return await api.internal.requestUpdateAccount(accountId, username, password, authorities, publicName);
    },

    deleteAccount: async (accountId) => {
      await api.internal.requestDeleteAccount(accountId);
    },

    updateFriends: async () => {
      if (api.internal.session != null) {
        try {
          api.internal.friends = await api.internal.requestListFriendships();
        }
        catch {
          api.internal.friends = null;
        }
      } else {
        api.internal.friends = null;
      }
    },

    initialize: async () => {
      try {
        await api.internal.login(null);
      }
      catch { }
    }

  },

  getLoggedInAccount: () => {
    return api.internal.account;
  },

  getFriends: () => {
    return api.internal.friends;
  },

  logout: async () => {
    await api.internal.logout();
  },

  login: async (username, password) => {
    await api.internal.login(username, password);
  },

  createAccount: async (username, password, publicName) => {
    return await api.internal.createAccount(username, password, publicName);
  },

  updateAccount: async (accountId, username, password, authorities, publicName) => {
    return await api.internal.updateAccount(accountId, username, password, authorities, publicName);
  },

  deleteAccount: async (accountId) => {
    await api.internal.deleteAccount(accountId);
  },

  initialize: async () => {
    await api.internal.initialize();
  }

};