package io.ucoin.app.service;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.UnitType;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.remote.TxHistoryResults;
import io.ucoin.app.model.remote.TxSourceResults;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.AsyncTaskListener;
import io.ucoin.app.technical.task.NullProgressModel;
import io.ucoin.app.technical.task.ProgressModel;

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
    private BlockchainRemoteService blockchainRemoteService;
    private MovementService movementService;

    public WalletService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        currencyService = ServiceLocator.instance().getCurrencyService();
        transactionRemoteService = ServiceLocator.instance().getTransactionRemoteService();
        peerService = ServiceLocator.instance().getPeerService();
        movementService = ServiceLocator.instance().getMovementService();
        blockchainRemoteService = ServiceLocator.instance().getBlockchainRemoteService();
    }

    public Wallet save(final Context context, final Wallet wallet, final boolean checkUniquePubKey) throws DuplicatePubkeyException {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());
        ObjectUtils.checkNotNull(wallet.getAccountId());
        ObjectUtils.checkNotNull(wallet.getName());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(wallet.getPubKeyHash()));
        ObjectUtils.checkNotNull(wallet.getIsMember());
        ObjectUtils.checkNotNull(wallet.getCredit());

        // Make sure public key is unique
        if (checkUniquePubKey) {
            checkPubKeyUnique(context, wallet);
        }

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

    public Wallet save(final Context context, final Wallet wallet) throws DuplicatePubkeyException {
        return save(context, wallet, true);
    }

    /**
     * Get wallets form database, and update it from remote node if ask
     *
     * @param accountId account Id
     * @param updateRemotely Should wallet must be update from remote nodes ?
     */
    public List<Wallet> getWalletsByAccountId(final Context context,
                                      final long accountId,
                                      final boolean updateRemotely,
                                      ProgressModel progressModel) {

        // Make sure the progress model is not null
        if (progressModel == null) {
            progressModel = new NullProgressModel();
        }

        boolean needComputeUD = displayCreditAsUD(context);

        // Loading wallets from database
        progressModel.setMessage(context.getString(R.string.loading_wallets));
        List<Wallet> result = getWalletsByAccountId(context, accountId, needComputeUD);
        progressModel.increment();

        // Check if cancelled
        if (progressModel.isCancelled()) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(result) && updateRemotely) {
            // Update progress message and max
            progressModel.setMessage(context.getString(R.string.updating_balances));
            progressModel.setMax(result.size() + 1);

            for (Wallet wallet : result) {

                updateWalletRemotly(context, wallet);

                progressModel.increment();

                // Check if cancelled
                if (progressModel.isCancelled()) {
                    return null;
                }
            }
        }

        return result;
    }

    /**
     * Get wallets form database, and update it from remote node if ask
     *
     * @param accountId account Id
     * @param updateRemotely Should wallet must be update from remote nodes ?
     */
    public void getWalletsByAccountId(final long accountId,
                                      final boolean updateRemotely,
                                      final AsyncTaskListener<List<Wallet>> listener) {

        LoadWalletsTask task = new LoadWalletsTask(listener, updateRemotely);
        task.execute(accountId);
    }

    /**
     * Update wallets from remote nodes (balance)
     *
     * @param wallets
     * @param listener
     */
    public void updateWalletsRemotely(final List<? extends Wallet> wallets, final AsyncTaskListener<List<? extends Wallet>> listener) {
        ObjectUtils.checkNotNull(wallets);
        ObjectUtils.checkNotNull(listener);

        // If empty, do nothing
        if (CollectionUtils.isEmpty(wallets)) {
            listener.onSuccess(null);
            return;
        }

        // Run the async task
        UpdateWalletsRemotelyTask task = new UpdateWalletsRemotelyTask(listener);
        task.execute(wallets.toArray(new Wallet[wallets.size()]));
    }



    /**
     * Update wallet from remote nodes (from the blockchain)
     * @param context
     * @param wallet
     */
    protected void updateWalletRemotly(final Context context, final Wallet wallet) {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());

        Log.d(TAG, String.format("Updating wallet [%s]", wallet.toString()));

        long currencyId = wallet.getCurrencyId();
        boolean computeUD = displayCreditAsUD(context);

        // Get the current block number (using cache)
        // This should be done BEFORE the call of getSources (in case a new block occur)
        BlockchainBlock currentBlock = blockchainRemoteService.getCurrentBlock(currencyId, true /*use cache*/);
        long currentBlockNumber = currentBlock == null ? 0 :currentBlock.getNumber();
        boolean blockNumberHasChanged = wallet.getBlockNumber() != currentBlockNumber;

        // Stop if no new block
        if (!blockNumberHasChanged) {
            return;
        }

        // Get sources and credit, from remote nodes
        TxSourceResults txSourcesAndCredit = transactionRemoteService.getSourcesAndCredit(currencyId, wallet.getPubKeyHash());
        long credit = txSourcesAndCredit.getCredit() == null ? 0 : txSourcesAndCredit.getCredit().longValue();
        double creditAsUD = 0;
        if (computeUD) {
            creditAsUD = getCreditAsUD(context, currencyId, credit);
        }

        // Check if credit has been updated or not
        boolean creditHasChanged = wallet.getCredit() == null
                || wallet.getCredit().longValue() != credit;
        boolean creditAsUDHasChanged = computeUD
                && (wallet.getCreditAsUD() == null
                || wallet.getCreditAsUD().doubleValue() != creditAsUD);


        // If new block since the last update
        if (blockNumberHasChanged) {
            TxHistoryResults history = transactionRemoteService.getHistory(currencyId, wallet.getPubKeyHash(),
                    wallet.getBlockNumber() == -1 ? 0 : wallet.getBlockNumber() + 1,
                    currentBlockNumber);
            // Process wallet's sources
            /*movementService.updateMovementsFromHistory(context,
                    wallet.getId(),
                    wallet.getBlockNumber(),
                    txSourcesAndCredit.getSources(),
                    history);*/
        }

        // If credits has changed
        if (creditHasChanged || creditAsUDHasChanged || blockNumberHasChanged) {

            // Set new credits
            wallet.setCredit(credit);
            wallet.setCreditAsUD(creditAsUD);
            wallet.setBlockNumber(currentBlockNumber);

            // Save the wallet
            try {
                save(context, wallet);
            } catch (DuplicatePubkeyException e) {
                // Should never happend
            }

            // Mark as dirty (let's the UI known that something changed)
            wallet.setDirty(true);
        }

    }

    /**
     * Return wallets that have a uid (e.g. that could be used to sign another identity)
     * @param context
     * @param accountId
     * @return
     */
    public List<Wallet> getUidWalletsByAccountId(Context context, long accountId) {
        List<Wallet> allWallets = getWalletsByAccountId(
                context,
                accountId,
                displayCreditAsUD(context));

        List<Wallet> result = new ArrayList<Wallet>();
        for (Wallet wallet: allWallets) {
            if (StringUtils.isNotBlank(wallet.getUid())) {
                result.add(wallet);
            }
        }

        return result;
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

        long certTimestamp = DateUtils.getCurrentTimestampSeconds();

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
        Log.d(TAG, "Deleting wallet id=" + walletId);

        ContentResolver resolver = context.getContentResolver();

        // First, delete movements
        {
            String whereClause = Contract.Movement.WALLET_ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(walletId)};
            int rowsDeleted = resolver.delete(Uri.parse(Provider.CONTENT_URI + "/movement/"), whereClause, whereArgs);
            Log.d(TAG, " deleted movement count: " + rowsDeleted);
        }

        // Then delete the wallet
        {
            String whereClause = "_id=?";
            String[] whereArgs = new String[]{String.valueOf(walletId)};
            int rowsDeleted = resolver.delete(getContentUri(), whereClause, whereArgs);
            if (rowsDeleted != 1) {
                throw new UCoinTechnicalException(String.format("Error while deleting wallet [id=%s]. %s rows updated.", walletId, rowsDeleted));
            }
        }
    }

    /* -- internal methods-- */

    protected List<Wallet> getWallets(Context context, long accountId) {
        return getWalletsByAccountId(
                context,
                accountId,
                displayCreditAsUD(context));
    }

    private List<Wallet> getWalletsByAccountId(Context context, long accountId, boolean computeUD) {

        String selection = Contract.Wallet.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        String orderBy = Contract.Wallet.ALIAS + " ASC";

        Cursor cursor = context.getContentResolver()
                .query(getContentUri(),
                        new String[]{}, selection, selectionArgs, orderBy);

        List<Wallet> result = new ArrayList<Wallet>();
        while (cursor.moveToNext()) {
            Wallet wallet = toWallet(cursor);
            result.add(wallet);

            // Update the wallet UD
            if (computeUD) {
                updateCreditUD(context, wallet);
            }
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
        target.put(Contract.Wallet.ALIAS, source.getName());
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
        target.put(Contract.Wallet.BLOCK_NUMBER, source.getBlockNumber());
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
        result.setCredit(cursor.getLong(mSelectHolder.creditIndex));
        result.setMember(cursor.getInt(mSelectHolder.isMemberIndex) == 1 ? true : false);
        result.setCertTimestamp(cursor.getLong(mSelectHolder.certTimestampIndex));
        result.setSalt(cursor.getString(mSelectHolder.saltIndex));
        result.setBlockNumber(cursor.getLong(mSelectHolder.blockNumberIndex));

        return result;
    }

    private void updateCreditUD(Context context, Wallet wallet) {

        long lastUD = currencyService.getLastUD(context, wallet.getCurrencyId());
        double ud = CurrencyUtils.convertToUD(wallet.getCredit(), lastUD);
        wallet.setCreditAsUD(ud);
    }

    private double getCreditAsUD(Context context, long currencyId, long credit) {
        if (credit == 0) {
            return 0;
        }

        long lastUD = currencyService.getLastUD(context, currencyId);
        return CurrencyUtils.convertToUD(credit, lastUD);
    }

    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        return mContentUri;
    }

    private boolean displayCreditAsUD(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return SettingsActivity.PREF_UNIT_UD.equals(preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN));
    }

    private long getAccountId(Activity activity) {
        return ((io.ucoin.app.Application) activity.getApplication()).getAccountId();
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
        int blockNumberIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Wallet._ID);
            accountIdIndex = cursor.getColumnIndex(Contract.Wallet.ACCOUNT_ID);
            currencyIdIndex = cursor.getColumnIndex(Contract.Wallet.CURRENCY_ID);
            nameIndex = cursor.getColumnIndex(Contract.Wallet.ALIAS);
            pubKeyIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
            uidIndex= cursor.getColumnIndex(Contract.Wallet.UID);
            certTimestampIndex= cursor.getColumnIndex(Contract.Wallet.CERT_TS);
            secKeyIndex = cursor.getColumnIndex(Contract.Wallet.SECRET_KEY);
            isMemberIndex = cursor.getColumnIndex(Contract.Wallet.IS_MEMBER);
            creditIndex = cursor.getColumnIndex(Contract.Wallet.CREDIT);
            saltIndex = cursor.getColumnIndex(Contract.Wallet.SALT);
            blockNumberIndex = cursor.getColumnIndex(Contract.Wallet.BLOCK_NUMBER);
        }
    }

    private class LoadWalletsTask extends AsyncTaskHandleException<Long, Void, List<Wallet>> {

        private boolean mUpdateRemotely;

        public LoadWalletsTask(AsyncTaskListener<List<Wallet>> listener, boolean updateRemotely) {
            super(listener);
            this.mUpdateRemotely = updateRemotely;
        }

        @Override
        protected List<Wallet> doInBackgroundHandleException(Long... accountIds) {
            Long accountId = accountIds[0];
            Context context = getContext();
            boolean computeUD = displayCreditAsUD(context);

            setMax(100);
            setProgress(0);

            // Loading wallets from database
            setMessage(getContext().getString(R.string.loading_wallets));
            List<Wallet> result = getWalletsByAccountId(context, accountId, computeUD);
            increment();

            // Check if cancelled
            if (isCancelled()) {
                return null;
            }

            if (CollectionUtils.isNotEmpty(result) && mUpdateRemotely) {
                // Update progress message and max
                setMessage(getContext().getString(R.string.updating_balances));
                setMax(result.size() + 1);

                for (Wallet wallet : result) {

                    updateWalletRemotly(getContext(), wallet);

                    increment();

                    // Check if cancelled
                    if (isCancelled()) {
                        return null;
                    }
                }
            }

            return result;
        }

    }

    private class UpdateWalletsRemotelyTask extends AsyncTaskHandleException<Wallet, Void, List<Wallet>> {

        public UpdateWalletsRemotelyTask(AsyncTaskListener<List<? extends Wallet>> listener) {
            super(listener);
        }

        @Override
        protected List<Wallet> doInBackgroundHandleException(final Wallet... wallets) {
            int i=0;
            int count = wallets.length;

            setMax(count);
            setProgress(0);
            setMessage(getString(R.string.updating_balances));

            List<Wallet> result = new ArrayList<Wallet>(count);
            while (i < count) {

                Wallet wallet = wallets[i++];

                updateWalletRemotly(getContext(), wallet);

                result.add(wallet);

                increment();

                // Check if cancel
                if (isCancelled()) {
                    return null;
                }
            }

            return result;
        }

    }
}
