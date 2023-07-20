let notificationTimeoutId;

// ------------------------------------------------------------

const contentAreaRoot = document.getElementById('content_area_root');

const loginModuleRoot = document.getElementById('login_module_root');
const loginTabsContainer = document.getElementById('login_tabs_container');
const loginTab = document.getElementById('login_tab');
const createAccountTab = document.getElementById('create_account_tab');
const loginPagesContainer = document.getElementById('login_pages_container');
const loginPage = document.getElementById('login_page');
const loginUsernameInput = document.getElementById('login_username_input');
const loginPasswordInput = document.getElementById('login_password_input');
const loginButton = document.getElementById('login_button');
const createAccountPage = document.getElementById('create_account_page');
const createAccountUsernameInput = document.getElementById('create_account_username_input');
const createAccountPasswordInput = document.getElementById('create_account_password_input');
const createAccountPublicNameInput = document.getElementById('create_account_public_name_input');
const createAccountButton = document.getElementById('create_account_button');

const lobbyModuleRoot = document.getElementById('lobby_module_root');
const activeGamesList = document.getElementById('active_games_list');
const invitationsList = document.getElementById('invitations_list');
const pendingGamesList = document.getElementById('pending_games_list');
const gameEntryTemplate = document.getElementsByClassName('game_entry')[0];

const topBarRoot = document.getElementById('top_bar_root');
const createGameButton = document.getElementById('create_game_button');
const friendsButton = document.getElementById('friends_button');
const accountButton = document.getElementById('account_button');
const logoutButton = document.getElementById('logout_button');

const overlayBackdrop = document.getElementById('overlay_backdrop');
const overlayWindow = document.getElementById('overlay_window');
const activeGameEntryPage = document.getElementById('active_game_entry_page');
const invitationEntryPage = document.getElementById('invitation_entry_page');
const pendingGameEntryPage = document.getElementById('pending_game_entry_page');
const createGamePage = document.getElementById('create_game_page');
const friendsPage = document.getElementById('friends_page');
const accountPage = document.getElementById('account_page');
const accountPageUsername = document.getElementById('account_page_username');
const accountPagePublicName = document.getElementById('account_page_public_name');
const accountPageModifyButton = document.getElementById('account_page_modify_button');
const modifyAccountPage = document.getElementById('modify_account_page');
const modifyAccountUsernameInput = document.getElementById('modify_account_username_input');
const modifyAccountPasswordInput = document.getElementById('modify_account_password_input');
const modifyAccountPublicNameInput = document.getElementById('modify_account_public_name_input');
const modifyAccountCancelButton = document.getElementById('modify_account_cancel_button');
const modifyAccountDeleteButton = document.getElementById('modify_account_delete_button');
const modifyAccountSaveButton = document.getElementById('modify_account_save_button');

const notificationRoot = document.getElementById('notification_root');
const notificationMessage = document.getElementById('notification_message');

// ------------------------------------------------------------