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
  }

};