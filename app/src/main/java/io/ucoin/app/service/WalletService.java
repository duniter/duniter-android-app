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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.DateUtils;
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
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private CurrencyService currencyService;
    private PeerService peerService;
    private TransactionRemoteService transactionRemoteService;

    public WalletService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        currencyService = ServiceLocator.instance().getCurrencyService();
        transactionRemoteService = ServiceLocator.instance().getTransactionRemoteService();
        peerService = ServiceLocator.instance().getPeerService();
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

    /**
     * Return wallets that have a uid (e.g. that could be used to sign another identity)
     * @param application
     * @return
     */
    public List<Wallet> getWalletsWithUid(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        List<Wallet> allWallets = getWalletsByAccountId(application.getContentResolver(), Long.parseLong(accountId));

        List<Wallet> result = new ArrayList<Wallet>();
        for (Wallet wallet: allWallets) {
            if (StringUtils.isNotBlank(wallet.getUid())) {
                result.add(wallet);
            }
        }

        return result;
    }

    public void updateWallet(Context context, Wallet wallet) {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());

        Log.d(TAG, String.format("updating wallet [%s]", wallet.toString()));

        Long currencyId = wallet.getCurrencyId();
        boolean dirty = false;

        Long creditObj = transactionRemoteService.getCredit(wallet.getCurrencyId(), wallet.getPubKeyHash());
        int updatedCredit = creditObj == null ? 0 : creditObj.intValue();
        if (wallet.getCredit() == null || updatedCredit != wallet.getCredit().intValue()) {
            wallet.setCredit(updatedCredit);
            dirty = true;
        }

        if (dirty) {
            // Save updated wallet to DB
            try {
                save(context, wallet);
            } catch (DuplicatePubkeyException e) {
                // Should never happen
            }

            // Mark as dirty (let's the UI known that something changed)
            wallet.setDirty(true);
        }
    }

    /**
     * Send a self certification, from an wallet
     * @param wallet
     * @return the updated wallet (with a new cert timestamp)
     */
    public Wallet sendSelfAndSave(final Context context, final Wallet wallet) {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getId());
        ObjectUtils.checkNotNull(wallet.getCurrencyId());
        ObjectUtils.checkNotNull(wallet.getPubKey());
        ObjectUtils.checkNotNull(wallet.getSecKey());
        ObjectUtils.checkNotNull(wallet.getUid());

        long certTimestamp = DateUtils.getCurrentTimestamp();

        // Send self to node
        ServiceLocator.instance().getWotRemoteService().sendSelf(
                wallet.getCurrencyId(),
                wallet.getPubKey(),
                wallet.getSecKey(),
                wallet.getUid(),
                certTimestamp);

        // Then save the wallet
        wallet.setCertTimestamp(certTimestamp);
        update(context.getContentResolver(), wallet);

        return wallet;
    }

    public void delete(final Context context, final long walletId) {

        ContentResolver resolver = context.getContentResolver();

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(walletId)};
        int rowsUpdated = resolver.delete(getContentUri(), whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while deleting wallet [id=%s]. %s rows updated.", walletId, rowsUpdated));
        }
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
        if (isDuplicatePubKeyExists(
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

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(getContentUri(), target, whereClause, whereArgs);
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
        target.put(Contract.Wallet.UID, source.getUid());
        if (source.getSalt() != null) {
            target.put(Contract.Wallet.SALT, source.getSalt());
        }
        target.put(Contract.Wallet.PUBLIC_KEY, source.getPubKeyHash());
        if (source.getCertTimestamp() != -1) {
            target.put(Contract.Wallet.CERT_TS, source.getCertTimestamp());
        }
        if (source.getSecKey() != null) {
            target.put(Contract.Wallet.SECRET_KEY, CryptoUtils.encodeBase58(source.getSecKey()));
        }
        target.put(Contract.Wallet.IS_MEMBER, source.getIsMember().booleanValue() ? 1 : 0);
        target.put(Contract.Wallet.CREDIT, source.getCredit());

        return target;
    }

    private Wallet toWallet(final Cursor cursor) {
        // Init the holder is need
        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }

        long currencyId = cursor.getLong(mSelectHolder.currencyIdIndex);
        String currencyName = currencyService.getCurrencyNameById(currencyId);

        String pubKey = cursor.getString(mSelectHolder.pubKeyIndex);
        String secKey = cursor.getString(mSelectHolder.secKeyIndex);
        String uid = cursor.getString(mSelectHolder.uidIndex);


        Wallet result = new Wallet(currencyName, uid, pubKey, secKey);
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setAccountId(cursor.getLong(mSelectHolder.accountIdIndex));
        result.setCurrencyId(currencyId);
        result.setName(cursor.getString(mSelectHolder.nameIndex));
        result.setCredit(cursor.getInt(mSelectHolder.creditIndex));
        result.setMember(cursor.getInt(mSelectHolder.isMemberIndex) == 1 ? true : false);
        result.setCertTimestamp(cursor.getLong(mSelectHolder.certTimestampIndex));
        result.setSalt(cursor.getString(mSelectHolder.saltIndex));

        return result;
    }

    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int currencyIdIndex;
        int accountIdIndex;
        int pubKeyIndex;
        int secKeyIndex;
        int nameIndex;
        int isMemberIndex;
        int creditIndex;
        int uidIndex;
        int certTimestampIndex;
        int saltIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Wallet._ID);
            accountIdIndex = cursor.getColumnIndex(Contract.Wallet.ACCOUNT_ID);
            currencyIdIndex = cursor.getColumnIndex(Contract.Wallet.CURRENCY_ID);
            nameIndex = cursor.getColumnIndex(Contract.Wallet.NAME);
            pubKeyIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
            uidIndex= cursor.getColumnIndex(Contract.Wallet.UID);
            certTimestampIndex= cursor.getColumnIndex(Contract.Wallet.CERT_TS);
            secKeyIndex = cursor.getColumnIndex(Contract.Wallet.SECRET_KEY);
            isMemberIndex = cursor.getColumnIndex(Contract.Wallet.IS_MEMBER);
            creditIndex = cursor.getColumnIndex(Contract.Wallet.CREDIT);
            saltIndex = cursor.getColumnIndex(Contract.Wallet.SALT);
        }
    }
}
