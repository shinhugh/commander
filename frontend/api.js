const api = {

  internal: {

    endpoint: window.location.hostname + ':' + window.location.port,

    authorizationToken: null,

    socket: null,

    establishedConnectionHandlers: [ ],

    closedConnectionHandlers: [ ],

    incomingMessageHandlers: [ ],

    invokeEstablishedConnectionHandlers: () => {
      for (const handler of api.internal.establishedConnectionHandlers) {
        handler();
      }
    },

    invokeClosedConnectionHandlers: () => {
      for (const handler of api.internal.closedConnectionHandlers) {
        handler();
      }
    },

    invokeIncomingMessageHandlers: (message) => {
      for (const handler of api.internal.incomingMessageHandlers) {
        handler(message);
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
      if (api.internal.authorizationToken != null) {
        headers.authorization = 'Bearer ' + api.internal.authorizationToken;
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

    sendObjectOverSocket: (obj) => {
      if (api.internal.socket == null) {
        throw new Error('No socket connection');
      }
      api.internal.socket.send(JSON.stringify(obj));
    }

  },

  registerEstablishedConnectionHandler: (handler) => {
    api.internal.establishedConnectionHandlers.push(handler);
  },

  registerClosedConnectionHandler: (handler) => {
    api.internal.closedConnectionHandlers.push(handler);
  },

  registerIncomingMessageHandler: (handler) => {
    api.internal.incomingMessageHandlers.push(handler);
  },

  isConnected: () => {
    return api.internal.socket != null;
  },

  setAuthorizationToken: (token) => {
    api.internal.authorizationToken = token;
  },

  connectSocket: async () => {
    const url = 'ws://' + api.internal.endpoint + '/api/ws';
    const socket = await new Promise((resolve, reject) => {
      const errorEventHandler = () => {
        reject();
      };
      const socket = new WebSocket(url);
      socket.addEventListener('error', errorEventHandler);
      socket.addEventListener('open', () => {
        socket.removeEventListener('error', errorEventHandler);
        resolve(socket);
      });
    });
    socket.addEventListener('close', () => {
      api.internal.socket = null;
      api.internal.invokeClosedConnectionHandlers();
    });
    socket.addEventListener('error', () => {
      api.internal.socket = null;
      api.internal.invokeClosedConnectionHandlers();
    });
    socket.addEventListener('message', e => {
      api.internal.invokeIncomingMessageHandlers(JSON.parse(e.data));
    });
    api.internal.socket = socket;
    api.internal.invokeEstablishedConnectionHandlers();
  },

  disconnectSocket: () => {
    if (api.internal.socket == null) {
      return;
    }
    api.internal.socket.close();
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

  requestReadAccounts: async (id) => {
    let parameters = null;
    if (id != null) {
      parameters = {
        id: id
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

  requestUpdateAccount: async (id, username, password, authorities, publicName) => {
    let parameters = null;
    if (id != null) {
      parameters = {
        id: id
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

  requestDeleteAccount: async (id) => {
    let parameters = null;
    if (id != null) {
      parameters = {
        id: id
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

  requestRequestFriendship: async (id) => {
    let parameters = null;
    if (id != null) {
      parameters = {
        id: id
      };
    }
    const response = await api.internal.makeRequest('/api/friendship', 'POST', parameters, null, null);
    if (response.ok) {
      return;
    }
    throw new Error(response.status);
  },

  requestTerminateFriendship: async (id) => {
    let parameters = null;
    if (id != null) {
      parameters = {
        id: id
      };
    }
    const response = await api.internal.makeRequest('/api/friendship', 'DELETE', parameters, null, null);
    if (response.ok) {
      return;
    }
    throw new Error(response.status);
  },

  sendGameJoin: () => {
    api.internal.sendObjectOverSocket({
      type: 'game_join'
    });
  },

  sendGameInput: (input) => {
    api.internal.sendObjectOverSocket({
      type: 'game_input',
      payload: input
    });
  }

};