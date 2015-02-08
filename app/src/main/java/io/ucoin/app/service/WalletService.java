package io.ucoin.app.service;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
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
        // TODO kimamila: use holder for index
        int idIndex = cursor.getColumnIndex(Contract.Wallet._ID);
        Long id = cursor.getLong(idIndex);

        int pubKeyIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
        String pubKey = cursor.getString(pubKeyIndex);

        int secKeyIndex = cursor.getColumnIndex(Contract.Wallet.SECRET_KEY);
        String secKey = cursor.getString(secKeyIndex);

        int uidIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
        String uid = cursor.getString(uidIndex);

        int isMemberKey = cursor.getColumnIndex(Contract.Wallet.IS_MEMBER);
        boolean isMember = cursor.getInt(isMemberKey) == 1 ? true : false;

        Wallet result = new Wallet("TODO currency", uid, pubKey, secKey);
        result.setId(id);
        result.getIdentity().setMember(isMember);

        return result;
    }

    public Wallet getDefaultWallet(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getDefaultWallet(application, Long.parseLong(accountId));
    }

    public Wallet getDefaultWallet(Context context, long accountId) {

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        String selection = Contract.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        Cursor cursor = context.getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, null);

        Wallet result = null;
        if (cursor.moveToNext()) {
            result = read(cursor);
        }

        if (result == null) {

            // FOR DEV ONLY
            result = new Wallet();
            result.getIdentity().setUid("kimamila");
            result.setSalt("benoit.lavenier@e-is.pro");
        }


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
