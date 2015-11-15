package io.ucoin.app.service.local;

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
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainBlock;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.cache.SimpleCache;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.crypto.KeyPair;
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

    private SimpleCache<Long, Wallet> mWalletCache;

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

    /**
     * Create a new wallet (using an async task)
     * @param currency
     * @param alias
     * @param uid
     * @param salt
     * @param password
     * @param listener
     */
    public void create(Currency currency,
                       String alias,
                       String uid,
                       String salt,
                       String password,
                       AsyncTaskListener<Wallet> listener
    ) {
        ObjectUtils.checkNotNull(currency);
        ObjectUtils.checkNotNull(currency.getAccountId());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(alias) || StringUtils.isNotBlank(uid));
        ObjectUtils.checkNotNull(salt);
        ObjectUtils.checkNotNull(password);
        ObjectUtils.checkNotNull(listener);
        ObjectUtils.checkNotNull(listener.getContext());

        new AddWalletTask(listener).execute(currency, alias, uid, salt, password);
    }

    /**
     * Create a new wallet
     * @param context
     * @param currency
     * @param alias
     * @param uid
     * @param salt
     * @param password
     */
    public Wallet create(Context context,
                         Currency currency,
                         String alias,
                         String uid,
                         String salt,
                         String password
    ) throws UidMatchAnotherPubkeyException, DuplicatePubkeyException{

        ObjectUtils.checkNotNull(currency);
        ObjectUtils.checkNotNull(currency.getAccountId());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(alias) || StringUtils.isNotBlank(uid));
        ObjectUtils.checkNotNull(salt);
        ObjectUtils.checkNotNull(password);

        // Compute a alias is not set
        if (StringUtils.isBlank(alias)) {
            alias = uid;
        }

        long accountId = currency.getAccountId();

        // Create a seed from salt and password
        KeyPair keyPair = ServiceLocator.instance().getCryptoService().getKeyPair(salt, password);

        // Create a new wallet
        Wallet wallet = new Wallet(currency.getCurrencyName(), uid, keyPair.publicKey, keyPair.secretKey);
        wallet.setCurrencyId(currency.getId());
        wallet.setSalt(salt);
        wallet.setAccountId(accountId);
        wallet.setName(alias);

        // Load membership
        blockchainRemoteService.loadMembership(currency.getId(), wallet.getIdentity(), true);
        // If isMember is null, the UID is already used by another pubkey !
        if (wallet.getIsMember() == null) {
            throw new UidMatchAnotherPubkeyException();
        }

        // Get credit
        Long credit = transactionRemoteService.getCredit(currency.getId(), wallet.getPubKeyHash());
        wallet.setCredit(credit == null ? 0 : credit);

        // Save the wallet in DB
        // (reset private key first)
        wallet.setSecKey(null);
        save(context, wallet, true);

        return wallet;
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
        for (Wallet wallet : result) {
            mWalletCache.put(wallet.getId(), wallet);
        }

        return result;
    }

    /**
     * Get wallets from database, and update it from remote node if ask
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

        // Get credit, from remote nodes
        long credit = transactionRemoteService.getCreditOrZero(currencyId, wallet.getPubKeyHash());
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

        // If credits has changed
        if (creditHasChanged || creditAsUDHasChanged) {

            // Set new credits
            wallet.setCredit(credit);
            wallet.setCreditAsUD(creditAsUD);
            wallet.setBlockNumber(currentBlockNumber);

            // Save the wallet
            try {
                save(context, wallet);
            } catch (DuplicatePubkeyException e) {
                // Should never occur
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
        return getUidWalletsByAccountId(context, accountId, displayCreditAsUD(context));
    }

    /**
     * Return wallets that have a uid (e.g. that could be used to sign another identity)
     * @param context
     * @param accountId
     * @param updateCreditAsUD
     * @return
     */
    public List<Wallet> getUidWalletsByAccountId(Context context, long accountId, boolean updateCreditAsUD) {
        List<Wallet> allWallets = getWalletsByAccountId(
                context,
                accountId,
                updateCreditAsUD);

        List<Wallet> result = new ArrayList<Wallet>();
        for (Wallet wallet: allWallets) {
            if (StringUtils.isNotBlank(wallet.getUid())) {
                result.add(wallet);
            }
        }

        return result;
    }

    /**
     * Get wallet by id, from database
     *
     * @param walletId wallet Id
     */
    public Wallet getWalletById(final Context context,
                                final long walletId) {


        String selection = SQLiteTable.Wallet._ID + "=?";
        String[] selectionArgs = {
                String.valueOf(walletId)
        };

        Cursor cursor = context.getContentResolver()
                .query(getContentUri(),
                        new String[]{}, selection, selectionArgs, null);

        try {
            if (!cursor.moveToNext()) {
                throw new UCoinTechnicalException(String.format("Could not retrieve wallet with id [%s]", walletId));
            }

            Wallet result = toWallet(cursor);
            return result;
        }
        finally {
            cursor.close();
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
            String whereClause = SQLiteTable.Movement.WALLET_ID + "=?";
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

        String selection = SQLiteTable.Wallet.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        String orderBy = SQLiteTable.Wallet.ALIAS + " ASC";

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
                    SQLiteTable.Wallet.ACCOUNT_ID,
                    SQLiteTable.Wallet.PUBLIC_KEY,
                    SQLiteTable.Wallet._ID
            );
            selectionArgs = new String[] {
                    String.valueOf(accountId),
                    String.valueOf(pubkey),
                    String.valueOf(excludedWalletId)
            };
        }
        else {
            selection = String.format("%s=? and %s=?",
                    SQLiteTable.Wallet.ACCOUNT_ID,
                    SQLiteTable.Wallet.PUBLIC_KEY
            );
            selectionArgs = new String[] {
                    String.valueOf(accountId),
                    String.valueOf(pubkey)
            };
        }
        Cursor cursor = resolver.query(getContentUri(), projection, selection, selectionArgs, null);
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
        mWalletCache.put(source.getId(), source);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating wallet. %s rows updated.", rowsUpdated));
        }
    }

    public void loadCache(Context context, long accountId) {
        if (mWalletCache == null) {
            // Create and fill the currency cache
            List<Wallet> wallets = getUidWalletsByAccountId(context, accountId);
            mWalletCache = new SimpleCache<Long, Wallet>() {
                @Override
                public Wallet load(Context context, Long currencyId) {
                    return getWalletById(context, currencyId);
                }
            };

            // Fill the cache
            for (Wallet wallet : wallets) {
                mWalletCache.put(wallet.getId(), wallet);
            }
        }
    }

    public List<Wallet> getAllCacheWallet(Context context){
                List<Wallet> result = new ArrayList<>();
        for( long key :mWalletCache.keySet()){
            result.add(mWalletCache.get(context, key));
        }
        return result;
    }

    private ContentValues toContentValues(final Wallet source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(SQLiteTable.Wallet.ACCOUNT_ID, source.getAccountId());
        target.put(SQLiteTable.Wallet.CURRENCY_ID, source.getCurrencyId());
        target.put(SQLiteTable.Wallet.ALIAS, source.getName());
        target.put(SQLiteTable.Wallet.UID, source.getUid());
        if (source.getSalt() != null) {
            target.put(SQLiteTable.Wallet.SALT, source.getSalt());
        }
        target.put(SQLiteTable.Wallet.PUBLIC_KEY, source.getPubKeyHash());
        if (source.getCertTimestamp() != -1) {
            target.put(SQLiteTable.Wallet.CERT_TS, source.getCertTimestamp());
        }
        if (source.getSecKey() != null) {
            target.put(SQLiteTable.Wallet.SECRET_KEY, CryptoUtils.encodeBase58(source.getSecKey()));
        }
        target.put(SQLiteTable.Wallet.IS_MEMBER, source.getIsMember().booleanValue() ? 1 : 0);
        target.put(SQLiteTable.Wallet.CREDIT, source.getCredit());
        target.put(SQLiteTable.Wallet.BLOCK_NUMBER, source.getBlockNumber());
        target.put(SQLiteTable.Wallet.TX_BLOCK_NUMBER, source.getTxBlockNumber());
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
        result.setTxBlockNumber(cursor.getLong(mSelectHolder.txBlockNumberIndex));

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
        int txBlockNumberIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(SQLiteTable.Wallet._ID);
            accountIdIndex = cursor.getColumnIndex(SQLiteTable.Wallet.ACCOUNT_ID);
            currencyIdIndex = cursor.getColumnIndex(SQLiteTable.Wallet.CURRENCY_ID);
            nameIndex = cursor.getColumnIndex(SQLiteTable.Wallet.ALIAS);
            pubKeyIndex = cursor.getColumnIndex(SQLiteTable.Wallet.PUBLIC_KEY);
            uidIndex= cursor.getColumnIndex(SQLiteTable.Wallet.UID);
            certTimestampIndex= cursor.getColumnIndex(SQLiteTable.Wallet.CERT_TS);
            secKeyIndex = cursor.getColumnIndex(SQLiteTable.Wallet.SECRET_KEY);
            isMemberIndex = cursor.getColumnIndex(SQLiteTable.Wallet.IS_MEMBER);
            creditIndex = cursor.getColumnIndex(SQLiteTable.Wallet.CREDIT);
            saltIndex = cursor.getColumnIndex(SQLiteTable.Wallet.SALT);
            blockNumberIndex = cursor.getColumnIndex(SQLiteTable.Wallet.BLOCK_NUMBER);
            txBlockNumberIndex = cursor.getColumnIndex(SQLiteTable.Wallet.TX_BLOCK_NUMBER);
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

    private class UpdateWalletsRemotelyTask extends AsyncTaskHandleException<Wallet, Void, List<? extends Wallet>> {

        public UpdateWalletsRemotelyTask(AsyncTaskListener<List<? extends Wallet>> listener) {
            super(listener);
        }

        @Override
        protected List<? extends Wallet> doInBackgroundHandleException(final Wallet... wallets) {
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

    public class AddWalletTask extends AsyncTaskHandleException<Object, Void, Wallet> {

        public AddWalletTask(AsyncTaskListener<Wallet> listener) {
            super(listener);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Object... args) throws Exception {
            ObjectUtils.checkNotNull(args);
            ObjectUtils.checkArgument(args.length == 5);

            Currency currency = (Currency)args[0];
            String name = (String)args[1];
            String uid = (String)args[2];
            String salt = (String)args[3];
            String password = (String)args[4];

            // Run the wallet creation
            Wallet result = create(getContext(), currency, name, uid, salt, password);

            return result;
        }
    }
}
