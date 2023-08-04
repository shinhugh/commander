package org.dev.pixels.service.internal;

import org.dev.pixels.model.Account;

public interface AccountEventHandler {
    void handleCreateAccount(Account newAccount);
    void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount);
    void handleDeleteAccount(Account deletedAccount);
}
