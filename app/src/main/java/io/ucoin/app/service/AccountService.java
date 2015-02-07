package io.ucoin.app.service;

import android.accounts.AccountManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Account;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class AccountService extends BaseService {

    /** Logger. */
    private static final String TAG = "AccountService";

    public AccountService() {
        super();
    }

    public Account save(final Context context, final Account account) {
        ObjectUtils.checkNotNull(account);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getUid()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getPubkey()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getSalt()));

        // Create
        if (account.getId() == null) {
            return insert(context, account);
        }

        // TODO : update
        return null;
    }

    /* -- internal methods-- */


    public Account insert(final Context context, final Account account) {

        //Create account in database
        ContentValues values = new ContentValues();
        values.put(Contract.Account.UID, account.getUid());
        values.put(Contract.Account.PUBLIC_KEY, account.getPubkey());
        values.put(Contract.Account.SALT, account.getSalt());
        values.put(Contract.Account.CRYPT_PIN, account.getCryptPin());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = context.getContentResolver().insert(uri, values);
        Long accountId = ContentUris.parseId(uri);
        if (accountId < 0) {
            throw new UCoinTechnicalException("Could not insert account: " + account.toString());
        }

        //create account in android framework
        Bundle data = new Bundle();
        data.putString(Contract.Account._ID, accountId.toString());
        data.putString(Contract.Account.PUBLIC_KEY, account.getPubkey());
        android.accounts.Account androidAccount = new android.accounts.Account(account.getUid(), context.getString(R.string.ACCOUNT_TYPE));
        AccountManager.get(context).addAccountExplicitly(androidAccount, null, data);

        //keep a reference to the last account used
        SharedPreferences.Editor editor =
                context.getSharedPreferences("account", Context.MODE_PRIVATE).edit();
        editor.putString("_id", accountId.toString());
        editor.apply();

        // Refresh the inserted account
        account.setId(accountId);

        return account;
    }

}
