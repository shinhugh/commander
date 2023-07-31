/* Requires:
 * - api.js,
 * - auth.js
 */

const friendships = {

  internal: {

    friendships: null,

    friendshipsChangeHandlers: [ ],

    invokeFriendshipsChangeHandlers: () => {
      for (const handler of friendships.internal.friendshipsChangeHandlers) {
        handler();
      }
    },

    handleIncomingMessage: async (message) => {
      if (message.type !== 'friendships_change') {
        return;
      }
      friendships.internal.friendships = await api.requestListFriendships();
      friendships.internal.invokeFriendshipsChangeHandlers();
    },

    handleLogin: async () => {
      friendships.internal.friendships = await api.requestListFriendships();
      friendships.internal.invokeFriendshipsChangeHandlers();
    },

    handleLogout: () => {
      friendships.internal.friendships = null;
      friendships.internal.invokeFriendshipsChangeHandlers();
    }

  },

  initialize: async () => {
    api.registerIncomingMessageHandler(friendships.internal.handleIncomingMessage);
    auth.registerLoginHandler(friendships.internal.handleLogin);
    auth.registerLogoutHandler(friendships.internal.handleLogout);
    if (auth.isLoggedIn()) {
      await friendships.internal.handleLogin();
    }
  },

  registerFriendshipsChangeHandler: (handler) => {
    friendships.internal.friendshipsChangeHandlers.push(handler);
  },

  getFriendships: () => {
    return friendships.internal.friendships;
  },

  requestFriendship: async (id) => {
    await api.requestRequestFriendship(id);
  },

  terminateFriendship: async (id) => {
    await api.requestTerminateFriendship(id);
  }

};