let notificationTimeoutId;

// ------------------------------------------------------------

const contentAreaRoot = document.getElementById('content_area_root');

const loginModuleRoot = document.getElementById('login_module_root');
const loginModuleTabsContainer = document.getElementById('login_module_tabs_container');
const loginModuleLoginTab = document.getElementById('login_module_login_tab');
const loginModuleCreateAccountTab = document.getElementById('login_module_create_account_tab');
const loginModulePagesContainer = document.getElementById('login_module_pages_container');
const loginModuleLoginPage = document.getElementById('login_module_login_page');
const loginModuleLoginPageUsernameInput = document.getElementById('login_module_login_page_username_input');
const loginModuleLoginPagePasswordInput = document.getElementById('login_module_login_page_password_input');
const loginModuleLoginPageLoginButton = document.getElementById('login_module_login_page_login_button');
const loginModuleCreateAccountPage = document.getElementById('login_module_create_account_page');
const loginModuleCreateAccountPageUsernameInput = document.getElementById('login_module_create_account_page_username_input');
const loginModuleCreateAccountPagePasswordInput = document.getElementById('login_module_create_account_page_password_input');
const loginModuleCreateAccountPagePublicNameInput = document.getElementById('login_module_create_account_page_public_name_input');
const loginModuleCreateAccountPageCreateAccountButton = document.getElementById('login_module_create_account_page_create_account_button');

const lobbyModuleRoot = document.getElementById('lobby_module_root');
const lobbyModuleActiveGamesList = document.getElementById('lobby_module_active_games_list');
const lobbyModuleInvitationsList = document.getElementById('lobby_module_invitations_list');
const lobbyModulePendingGamesList = document.getElementById('lobby_module_pending_games_list');
const lobbyModuleGameEntryTemplate = document.getElementsByClassName('game_entry')[0];

const topBarRoot = document.getElementById('top_bar_root');
const topBarCreateGameButton = document.getElementById('top_bar_create_game_button');
const topBarFriendsButton = document.getElementById('top_bar_friends_button');
const topBarAccountButton = document.getElementById('top_bar_account_button');
const topBarLogoutButton = document.getElementById('top_bar_logout_button');

const overlayBackdrop = document.getElementById('overlay_backdrop');
const overlayWindow = document.getElementById('overlay_window');
const overlayActiveGameEntryPage = document.getElementById('overlay_active_game_entry_page');
const overlayInvitationEntryPage = document.getElementById('overlay_invitation_entry_page');
const overlayPendingGameEntryPage = document.getElementById('overlay_pending_game_entry_page');
const overlayCreateGamePage = document.getElementById('overlay_create_game_page');
const overlayFriendsPage = document.getElementById('overlay_friends_page');
const overlayAccountPage = document.getElementById('overlay_account_page');
const overlayAccountPageUsername = document.getElementById('overlay_account_page_username');
const overlayAccountPagePublicName = document.getElementById('overlay_account_page_public_name');
const overlayAccountPageModifyButton = document.getElementById('overlay_account_page_modify_button');
const overlayModifyAccountPage = document.getElementById('overlay_modify_account_page');
const overlayModifyAccountPageUsernameInput = document.getElementById('overlay_modify_account_page_username_input');
const overlayModifyAccountPagePasswordInput = document.getElementById('overlay_modify_account_page_password_input');
const overlayModifyAccountPagePublicNameInput = document.getElementById('overlay_modify_account_page_public_name_input');
const overlayModifyAccountPageCancelButton = document.getElementById('overlay_modify_account_page_cancel_button');
const overlayModifyAccountPageDeleteButton = document.getElementById('overlay_modify_account_page_delete_button');
const overlayModifyAccountPageSaveButton = document.getElementById('overlay_modify_account_page_save_button');

const notificationRoot = document.getElementById('notification_root');
const notificationMessage = document.getElementById('notification_message');

// ------------------------------------------------------------