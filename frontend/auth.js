/* Requires:
 * - api.js
 */

const auth = {

  internal: {

    session: null,

    loginHandlers: [ ],

    logoutHandlers: [ ],

    invokeLoginHandlers: () => {
      for (const handler of auth.internal.loginHandlers) {
        handler();
      }
    },

    invokeLogoutHandlers: () => {
      for (const handler of auth.internal.logoutHandlers) {
        handler();
      }
    }

  },

  initialize: async () => {
    try {
      await auth.login(null, null);
    }
    catch { }
  },

  registerLoginHandler: (handler) => {
    auth.internal.loginHandlers.push(handler);
  },

  registerLogoutHandler: (handler) => {
    auth.internal.logoutHandlers.push(handler);
  },

  isLoggedIn: () => {
    return auth.internal.session != null;
  },

  login: async (username, password) => {
    if (auth.internal.session != null) {
      return;
    }
    auth.internal.session = await api.requestLogin(username, password);
    api.setAuthorizationToken(auth.internal.session.token);
    await api.connectSocket();
    auth.internal.invokeLoginHandlers();
  },

  logout: async () => {
    if (auth.internal.session == null) {
      return;
    }
    auth.internal.session = null;
    api.disconnectSocket();
    await api.requestLogout();
    api.setAuthorizationToken(null);
    auth.internal.invokeLogoutHandlers();
  }

};

auth.initialize();