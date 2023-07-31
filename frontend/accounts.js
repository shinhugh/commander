/* Requires:
 * - api.js
 * - auth.js
 */

const accounts = {

  internal: {

    self: null,

    handleLogin: async () => {
      accounts.internal.self = await api.requestReadAccounts(null)[0];
    },

    handleLogout: () => {
      accounts.internal.self = null;
    }

  },

  initialize: async () => {
    auth.registerLoginHandler(accounts.internal.handleLogin);
    auth.registerLogoutHandler(accounts.internal.handleLogout);
    if (auth.isLoggedIn()) {
      await accounts.internal.handleLogin();
    }
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
    return await api.requestUpdateAccounts(id, username, password, authorities, publicName);
  },

  deleteAccount: async (id) => {
    await api.requestDeleteAccount(id);
  }

};