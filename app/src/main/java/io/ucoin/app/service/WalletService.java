package io.ucoin.app.service;

import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;

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
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());
        ObjectUtils.checkNotNull(wallet.getAccountId());
        ObjectUtils.checkNotNull(wallet.getName());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(wallet.getPubKeyHash()));
        ObjectUtils.checkNotNull(wallet.getSecKey());
        ObjectUtils.checkNotNull(wallet.getIsMember());
        ObjectUtils.checkNotNull(wallet.getCredit());

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
        ContentValues values = new ContentValues();
        values.put(Contract.Wallet.ACCOUNT_ID, wallet.getAccountId());
        values.put(Contract.Wallet.CURRENCY_ID, wallet.getCurrencyId());
        values.put(Contract.Wallet.NAME, wallet.getName());
        values.put(Contract.Wallet.PUBLIC_KEY, wallet.getPubKeyHash());
        values.put(Contract.Wallet.SECRET_KEY, CryptoUtils.encodeBase58(wallet.getSecKey()));
        values.put(Contract.Wallet.IS_MEMBER, wallet.getIsMember().booleanValue() ? 1 : 0);
        values.put(Contract.Wallet.CREDIT, wallet.getCredit());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        uri = context.getContentResolver().insert(uri, values);
        Long walletId = ContentUris.parseId(uri);
        if (walletId < 0) {
            throw new UCoinTechnicalException("Error while inserting wallet");
        }

        // Refresh the inserted account
        wallet.setId(walletId);

        return wallet;
    }

}
