package io.ucoin.app;

import android.accounts.Account;
import android.accounts.AccountManager;

import io.ucoin.app.technical.StringUtils;

public class Application extends android.app.Application{

    private Account mAccount;
    private Long accountId;
    private Long id;
    //private TaskManager taskManager;

    public Application() {
        super();
        mAccount = null;
        accountId = null;
        //taskManager = new TaskManager(this);
    }

    public void setAccount(Account account) {
        mAccount = account;
        accountId = getAccountId(account);
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccountIdAsString() {
        if (accountId != null) {
            return accountId.toString();
        }
        return null;
    }

    /*public TaskManager getTaskManager() {
        return taskManager;
    }*/

    /* -- Internal methods -- */

    protected Long getAccountId(Account account) {
        Long result = null;
        if (mAccount != null) {
            String accountIdStr = AccountManager.get(this).getUserData(mAccount, "_id");
            if (StringUtils.isNotBlank(accountIdStr)) {
                result = Long.parseLong(accountIdStr);
            }
        }
        return result;
    }
}
