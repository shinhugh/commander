/* Requires:
 * - api.js
 */

const auth = {

  internal: {

    session: null,

    account: null,

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

  getAccount: () => {
    return auth.internal.account;
  },

  login: async (username, password) => {
    if (auth.internal.session != null) {
      return;
    }
    auth.internal.session = await api.requestLogin(username, password);
    api.setAuthorizationToken(auth.internal.session.token);
    auth.internal.account = await api.requestReadAccounts(null)[0];
    await api.connectSocket();
    auth.internal.invokeLoginHandlers();
  },

  logout: async () => {
    if (auth.internal.session == null) {
      return;
    }
    auth.internal.account = null;
    auth.internal.session = null;
    api.disconnectSocket();
    await api.requestLogout();
    api.setAuthorizationToken(null);
    auth.internal.invokeLogoutHandlers();
  }

};