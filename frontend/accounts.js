/* Requires:
 * - api.js
 * - auth.js
 */

const accounts = {

  internal: {

    self: null,

    selfChangeHandlers: [ ],

    invokeSelfChangeHandlers: () => {
      for (const handler of accounts.internal.selfChangeHandlers) {
        handler();
      }
    },

    handleLogin: async () => {
      accounts.internal.self = (await api.requestReadAccounts(null))[0];
      accounts.internal.invokeSelfChangeHandlers();
    },

    handleLogout: () => {
      accounts.internal.self = null;
      accounts.internal.invokeSelfChangeHandlers();
    }

  },

  initialize: async () => {
    auth.registerLoginHandler(accounts.internal.handleLogin);
    auth.registerLogoutHandler(accounts.internal.handleLogout);
    if (auth.isLoggedIn()) {
      await accounts.internal.handleLogin();
    }
  },

  registerSelfChangeHandler: (handler) => {
    accounts.internal.selfChangeHandlers.push(handler);
  },

  getSelf: () => {
    return accounts.internal.self;
  },

  readAccounts: async (id) => {
    return await api.requestReadAccounts(id);
  },

  createAccount: async (username, password, publicName) => {
    return await api.requestCreateAccount(username, password, publicName);
  },

  updateAccount: async (id, username, password, authorities, publicName) => {
    return await api.requestUpdateAccount(id, username, password, authorities, publicName);
  },

  deleteAccount: async (id) => {
    await api.requestDeleteAccount(id);
  }

};

accounts.initialize();