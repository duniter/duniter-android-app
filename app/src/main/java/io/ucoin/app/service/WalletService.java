package io.ucoin.app.service;

import android.content.Context;
import android.database.Cursor;

import io.ucoin.app.model.Wallet;

/**
 * Created by eis on 07/02/15.
 */
public class WalletService extends BaseService {

    /** Logger. */
    private static final String TAG = "WalletService";

    public WalletService() {
        super();
    }

    public Wallet save(final Context context, final Wallet wallet) {
        // Create
        if (wallet.getId() == null) {
            return insert(context, wallet);
        }

        // TODO : update
        return null;
    }

    public Wallet read(final Cursor cursor) {
        Wallet result = new Wallet();
/*
        // TODO kimamila: use holder for index
        int idIndex = cursor.getColumnIndex(Contract.Currency._ID);
        result.setId(cursor.getLong(idIndex));

        int currencyNameIndex = cursor.getColumnIndex(Contract.Wallet.CURRENCY_NAME);
        result.setCurrencyName(cursor.getString(currencyNameIndex));

        int membersCountIndex = cursor.getColumnIndex(Contract.Wallet.MEMBERS_COUNT);
        result.setMembersCount(cursor.getInt(membersCountIndex));

        int firstBlockSignatureIndex = cursor
                .getColumnIndex(Contract.Currency.FIRST_BLOCK_SIGNATURE);
        result.setFirstBlockSignature(cursor.getString(firstBlockSignatureIndex));
*/
        return result;
    }

    /* -- internal methods-- */

    public Wallet insert(final Context context, final Wallet wallet) {

        //Create account in database
        /*ContentValues values = new ContentValues();
        values.put(Contract.Currency.UID, currency.getCurrencyName());
        values.put(Contract.Account.PUBLIC_KEY, currency.getPubkey());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = context.getContentResolver().insert(uri, values);
        Long accountId = ContentUris.parseId(uri);

        //create account in android framework
        Bundle data = new Bundle();
        data.putString(Contract.Account._ID, accountId.toString());
        data.putString(Contract.Account.PUBLIC_KEY, account.getPubkey());
        android.accounts.Account androidAccount = new android.accounts.Account(account.getUid(), getString(R.string.ACCOUNT_TYPE));
        AccountManager.get(context).addAccountExplicitly(androidAccount, null, data);

        //keep a reference to the last account used
        SharedPreferences.Editor editor =
                getSharedPreferences("account", Context.MODE_PRIVATE).edit();
        editor.putString("_id", accountId.toString());
        editor.apply();

        // Refresh the inserted account
        account.setId(accountId);
*/
        return wallet;
    }

}
