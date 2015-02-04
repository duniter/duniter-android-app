package io.ucoin.app;

import android.accounts.Account;
import android.accounts.AccountManager;

public class Application extends android.app.Application{

    private Account mAccount;

    public Application() {
        super();
        mAccount = null;
    }

    public void setAccount(Account account) {
        mAccount = account;
    }

    public String getAccountId() {
        return AccountManager.get(this).getUserData(mAccount, "_id");
    }
}
