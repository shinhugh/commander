const api = {

  internal: {

    endpoint: window.location.hostname + ':' + window.location.port,

    socket: null,

    friendshipChangeHandler: null,

    gameSnapshotHandler: null,

    session: null,

    account: null,

    friends: null,

    gameState: null,

    connectSocket: async () => {
      const url = 'ws://' + api.internal.endpoint + '/api/ws';
      const socket = new WebSocket(url);
      await new Promise((resolve, reject) => {
        const errorEventHandler = () => {
          reject();
        };
        socket.addEventListener('error', errorEventHandler);
        socket.addEventListener('open', () => {
          socket.removeEventListener('error', errorEventHandler);
          resolve();
        });
      });
      socket.addEventListener('close', () => {
        api.internal.socket = null;
      });
      socket.addEventListener('error', () => {
        api.internal.socket = null;
      });
      socket.addEventListener('message', async e => {
        await api.internal.handleSocketEvent(JSON.parse(e.data));
      });
      api.internal.socket = socket;
    },

    disconnectSocket: () => {
      if (api.internal.socket == null) {
        return;
      }
      api.internal.socket.close();
      api.internal.socket = null;
    },

    sendObjectOverSocket: (obj) => {
      if (api.internal.socket == null) {
        throw new Error('No socket connection');
      }
      api.internal.socket.send(JSON.stringify(obj));
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

    requestLogout: async () => {
      await api.internal.makeRequest('/api/auth', 'DELETE', null, null, null);
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

    requestReadAccounts: async (accountId) => {
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

    sendJoinGame: () => {
      api.internal.sendObjectOverSocket({
        type: 'game_join'
      });
    },

    logout: async () => {
      if (api.internal.session == null) {
        return;
      }
      api.internal.disconnectSocket();
      await api.internal.requestLogout();
      api.internal.session = null;
      api.internal.account = null;
    },

    login: async (username, password) => {
      if (api.internal.session != null) {
        return;
      }
      api.internal.session = await api.internal.requestLogin(username, password);
      const accounts = await api.internal.readAccounts(null);
      api.internal.account = accounts.length > 0 ? accounts[0] : null;
      await Promise.allSettled([
        (async () => {
          await api.internal.connectSocket();
          api.internal.joinGame();
        })(),
        api.internal.updateFriends()
      ]);
    },

    readAccounts: async (accountId) => {
      return await api.internal.requestReadAccounts(accountId);
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

    listFriendships: async () => {
      await api.internal.requestListFriendships();
    },

    requestFriendship: async (accountId) => {
      await api.internal.requestRequestFriendship(accountId);
    },

    terminateFriendship: async (accountId) => {
      await api.internal.requestTerminateFriendship(accountId);
    },

    joinGame: () => {
      api.internal.sendJoinGame();
    },

    updateFriends: async () => {
      if (api.internal.session != null) {
        try {
          api.internal.friends = await api.internal.listFriendships();
        }
        catch {
          api.internal.friends = null;
        }
      } else {
        api.internal.friends = null;
      }
    },

    handleFriendshipChange: async () => {
      await api.internal.updateFriends();
      api.internal.friendshipChangeHandler?.();
    },

    handleGameSnapshot: (snapshot) => {
      api.internal.gameState = snapshot;
      api.internal.gameSnapshotHandler?.(snapshot);
    },

    handleSocketEvent: async e => {
      switch (e.type) {
        case 'friendships_change':
          await api.internal.handleFriendshipChange();
          break;
        case 'game_snapshot':
          api.internal.handleGameSnapshot(e.payload);
          break;
      }
    },

    initialize: async (friendshipChangeHandler, gameSnapshotHandler) => {
      api.internal.friendshipChangeHandler = friendshipChangeHandler;
      api.internal.gameSnapshotHandler = gameSnapshotHandler;
      try {
        await api.internal.login(null, null);
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

  readAccounts: async (accountId) => {
    return await api.internal.readAccounts(accountId);
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

  requestFriendship: async (accountId) => {
    await api.internal.requestFriendship(accountId);
  },

  terminateFriendship: async (accountId) => {
    await api.internal.terminateFriendship(accountId);
  },

  joinGame: () => {
    api.internal.joinGame();
  },

  initialize: async (friendshipChangeHandler, gameSnapshotHandler) => {
    await api.internal.initialize(friendshipChangeHandler, gameSnapshotHandler);
  }

};