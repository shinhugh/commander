/* Requires:
 * - auth.js
 * - accounts.js
 * - friendships.js
 * - game.js
 */

const uiApi = {

  hide: (element) => {
    element.style.display = 'none';
  },

  show: (element) => {
    element.style.removeProperty('display');
  },

  cloak: (element) => {
    element.style.visibility = 'hidden';
  },

  uncloak: (element) => {
    element.style.removeProperty('visibility');
  },

  select: (element) => {
    element.classList.add('selected');
  },

  unselect: (element) => {
    element.classList.remove('selected');
  }

};

// ------------------------------------------------------------

auth.initialize();
accounts.initialize();
friendships.initialize();
game.initialize();