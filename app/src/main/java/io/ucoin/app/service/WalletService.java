package io.ucoin.app.service;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;

/**
 * Created by eis on 07/02/15.
 */
public class WalletService extends BaseService {

    /**
     * Logger.
     */
    private static final String TAG = "WalletService";

    // a cache instance of the wallet Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mWalletUri = null;

    public WalletService() {
        super();
    }


    public Wallet save(final Context context, final Wallet wallet) throws DuplicatePubkeyException {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());
        ObjectUtils.checkNotNull(wallet.getAccountId());
        ObjectUtils.checkNotNull(wallet.getName());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(wallet.getPubKeyHash()));
        ObjectUtils.checkNotNull(wallet.getIsMember());
        ObjectUtils.checkNotNull(wallet.getCredit());

        // Make sure public key is unique
        checkPubKeyUnique(context, wallet);

        // create if not exists
        if (wallet.getId() == null) {

            insert(context.getContentResolver(), wallet);
        }

        // or update
        else {
            update(context.getContentResolver(), wallet);
        }

        // return the updated wallet (id could have change)
        return wallet;
    }

    public List<Wallet> getWallets(Activity activity) {
        return getWallets(activity.getApplication());
    }

    public List<Wallet> getWallets(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getWalletsByAccountId(application.getContentResolver(), Long.parseLong(accountId));
    }

    /* -- internal methods-- */

    private List<Wallet> getWalletsByAccountId(ContentResolver resolver, long accountId) {

        String selection = Contract.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        Cursor cursor = resolver.query(getContentUri(), new String[]{}, selection,
                selectionArgs, null);

        List<Wallet> result = new ArrayList<Wallet>();
        while (cursor.moveToNext()) {
            Wallet wallet = toWallet(cursor);
            result.add(wallet);
        }
        cursor.close();

        return result;
    }

    private void checkPubKeyUnique(
            final Context context,
            final Wallet wallet) throws DuplicatePubkeyException {
        if (!isDuplicatePubKeyExists(
                context.getContentResolver(),
                wallet.getPubKeyHash(),
                wallet.getAccountId(),
                wallet.getId())) {
            throw new DuplicatePubkeyException(context.getString(R.string.duplicate_pubkey,
                    wallet.getPubKeyHash()));
        }
    }

    private boolean isDuplicatePubKeyExists(
            final ContentResolver resolver,
            final String pubkey,
            final long accountId,
            final Long excludedWalletId) {
        String[] projection = new String[]{BaseColumns._ID};

        String selection;
        String[] selectionArgs;
        if (excludedWalletId != null) {
            selection = String.format("%s=? and %s=? and %s<>?",
                    Contract.Wallet.ACCOUNT_ID,
                    Contract.Wallet.PUBLIC_KEY,
                    Contract.Wallet._ID
            );
            selectionArgs = new String[] {
                    String.valueOf(accountId),
                    String.valueOf(pubkey),
                    String.valueOf(excludedWalletId)
            };
        }
        else {
            selection = String.format("%s=? and %s=?",
                    Contract.Wallet.ACCOUNT_ID,
                    Contract.Wallet.PUBLIC_KEY
            );
            selectionArgs = new String[] {
                    String.valueOf(accountId),
                    String.valueOf(pubkey)
            };
        }
        Cursor cursor = resolver.query(getContentUri(),
                projection,
                selection,
                selectionArgs,
                null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public void insert(final ContentResolver resolver, final Wallet source) {

        ContentValues target = toContentValues(source);

        Uri uri = resolver.insert(getContentUri(), target);
        Long walletId = ContentUris.parseId(uri);
        if (walletId < 0) {
            throw new UCoinTechnicalException("Error while inserting wallet");
        }

        // Refresh the inserted account
        source.setId(walletId);
    }

    public void update(final ContentResolver resolver, final Wallet source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        Uri uri = ContentUris.withAppendedId(getContentUri(), source.getId());
        int rowsUpdated = resolver.update(uri, target, null, null);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating wallet. %s rows updated.", rowsUpdated));
        }
    }

    private ContentValues toContentValues(final Wallet source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(Contract.Wallet.ACCOUNT_ID, source.getAccountId());
        target.put(Contract.Wallet.CURRENCY_ID, source.getCurrencyId());
        target.put(Contract.Wallet.NAME, source.getName());
        target.put(Contract.Wallet.PUBLIC_KEY, source.getPubKeyHash());
        if (source.getSecKey() != null) {
            target.put(Contract.Wallet.SECRET_KEY, CryptoUtils.encodeBase58(source.getSecKey()));
        }
        target.put(Contract.Wallet.IS_MEMBER, source.getIsMember().booleanValue() ? 1 : 0);
        target.put(Contract.Wallet.CREDIT, source.getCredit());

        return target;
    }


    private Wallet toWallet(final Cursor cursor) {
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

    private Uri getContentUri() {
        if (mWalletUri != null){
            return mWalletUri;
        }
        mWalletUri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        return mWalletUri;
    }
}
