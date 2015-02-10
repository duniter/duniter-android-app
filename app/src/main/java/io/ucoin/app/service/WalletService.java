package io.ucoin.app.service;

import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

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
        ObjectUtils.checkNotNull(wallet.getIsMember());
        ObjectUtils.checkNotNull(wallet.getCredit());

        // Create
        if (wallet.getId() == null) {
            return insert(context, wallet);
        }

        // TODO : update
        return null;
    }

    public List<Wallet> getWallets(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getWalletsByAccountId(application, Long.parseLong(accountId));
    }

    public List<Wallet> getWalletsByAccountId(Context context, long accountId) {

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        String selection = Contract.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        Cursor cursor = context.getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, null);

        List<Wallet> result = new ArrayList<Wallet>();
        while (cursor.moveToNext()) {
            Wallet wallet = read(cursor);
            result.add(wallet);
        }

        return result;
    }

    public Wallet read(final Cursor cursor) {
        // TODO kimamila: use holder for index
        int idIndex = cursor.getColumnIndex(Contract.Wallet._ID);
        Long id = cursor.getLong(idIndex);

        int pubKeyIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
        String pubKey = cursor.getString(pubKeyIndex);

        int secKeyIndex = cursor.getColumnIndex(Contract.Wallet.SECRET_KEY);
        String secKey = cursor.getString(secKeyIndex);

        int nameIndex = cursor.getColumnIndex(Contract.Wallet.NAME);
        String name = cursor.getString(nameIndex);

        int isMemberIndex = cursor.getColumnIndex(Contract.Wallet.IS_MEMBER);
        boolean isMember = cursor.getInt(isMemberIndex) == 1 ? true : false;

        int creditIndex = cursor.getColumnIndex(Contract.Wallet.CREDIT);
        int credit = cursor.getInt(creditIndex);

        // TODO get the currency name from DB (table currency)
        Wallet result = new Wallet("meta_brouzouf", name, pubKey, secKey);
        result.setId(id);
        result.setName(name);
        result.setCredit(credit);
        result.getIdentity().setMember(isMember);

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
        if (wallet.getSecKey() != null) {
            values.put(Contract.Wallet.SECRET_KEY, CryptoUtils.encodeBase58(wallet.getSecKey()));
        }
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
