package org.dev.commander.service.internal;

import org.dev.commander.model.Account;

public interface AccountEventHandler {
    void handleCreateAccount(Account newAccount);
    void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount);
    void handleDeleteAccount(Account deletedAccount);
}
