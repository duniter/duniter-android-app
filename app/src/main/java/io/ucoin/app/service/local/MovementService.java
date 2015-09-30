package io.ucoin.app.service.local;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainBlock;
import io.ucoin.app.model.remote.TxHistoryMovement;
import io.ucoin.app.model.remote.TxHistoryResults;
import io.ucoin.app.model.remote.UdHistoryMovement;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.service.remote.UdRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.AsyncTaskListener;
import io.ucoin.app.technical.task.NullProgressModel;
import io.ucoin.app.technical.task.ProgressModel;

/**
 * Created by eis on 07/02/15.
 */
public class MovementService extends BaseService {

    /**
     * Logger.
     */
    private static final String TAG = "MovementService";

    private static final int TX_BLOCK_BATCH_SIZE = 500;
    private static final String MOVEMENT_ISSUER_SEPARATOR = ",";
    private static final String MOVEMENT_RECEIVERS_SEPARATOR = ",";

    // a cache instance of the movement Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private WalletService walletService;
    private CurrencyService currencyService;


    public MovementService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        walletService = ServiceLocator.instance().getWalletService();
        currencyService = ServiceLocator.instance().getCurrencyService();
    }

    public Movement save(final Context context, final Movement movement) throws DuplicatePubkeyException {
        ObjectUtils.checkNotNull(movement);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(movement.getFingerprint()));
        ObjectUtils.checkNotNull(movement.getTime());

        // create if not exists
        if (movement.getId() == null) {

            insert(context.getContentResolver(), movement);
        }

        // or update
        else {
            update(context.getContentResolver(), movement);
        }

        // return the updated movement (id could have change)
        return movement;
    }

    public List<Movement> getMovementsByWalletId(Context context, long walletId) {
        return getMovementsByWalletId(context.getContentResolver(), walletId);
    }


    public void delete(final Context context, final long movementId) {

        ContentResolver resolver = context.getContentResolver();

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(movementId)};
        int rowsUpdated = resolver.delete(getContentUri(), whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while deleting movement [id=%s]. %s rows updated.", movementId, rowsUpdated));
        }
    }


    /**
     * Update movements from remote node (async mode)
     *
     * @param walletId Wallet to refresh
     * @param doCompleteRefresh if true, do a full sync (after a delete of all transactions)
     * @param listener
     */
    public void refreshMovements(final long walletId,
                                 final boolean doCompleteRefresh,
                                 final AsyncTaskListener<Long> listener) {

        new RefreshMovementTask(walletId, doCompleteRefresh, listener).execute();
    }


    /**
     * Update movements from remote node
     *
     * @param context
     * @param walletId
     * @param completeRefresh
     * @return number of movements updated or inserted
     */
    public long refreshMovements(Context context, long walletId, boolean completeRefresh, ProgressModel progressModel) {
        //ObjectUtils.checkNotNull(unitType);

        ServiceLocator serviceLocator = ServiceLocator.instance();

        if (progressModel == null) {
            progressModel = new NullProgressModel();
        }

        // Get wallet from database
        Wallet wallet = walletService.getWalletById(context, walletId);
        long currencyId = wallet.getCurrencyId();



        // Get the current block number
        BlockchainRemoteService blockchainRemoteService = serviceLocator.getBlockchainRemoteService();
        BlockchainBlock currentBlock = blockchainRemoteService.getCurrentBlock(currencyId, true);
        long currentBlockNumber = currentBlock.getNumber();
        long syncTxBlockNumber = wallet.getTxBlockNumber();
        long nbBlockToRead = !completeRefresh && syncTxBlockNumber != -1
                ? currentBlockNumber - syncTxBlockNumber
                : currentBlockNumber;

        progressModel.setMax((int)(nbBlockToRead + 5) /*delete movements + refreshAndGetUD + pendingsMovements + ud history + save wallet */);

        // Delete existing movement if need
        if (completeRefresh) {
            progressModel.setMessage(context.getString(R.string.resync_delete_movements));
            deleteByWalletId(context, walletId);
            syncTxBlockNumber = -1;
        }

        // If current block has NOT changed: exit
        else if (syncTxBlockNumber == currentBlockNumber) {
            return 0;
        }

        // Load UD
        progressModel.increment(context.getString(R.string.resync_get_UD));
        Map<Integer, Long> udMap = currencyService.refreshAndGetUD(context, currencyId, syncTxBlockNumber);
        progressModel.increment();

        // Load pending movements
        List<Movement> pendingsMovements = getPendingMovementsByWalletId(context.getContentResolver(), walletId);
        Map<String, Movement> pendingMovementsByFingerprint = ModelUtils.movementsToFingerprintMap(pendingsMovements);

        // Read TX history, by N blocks
        long nbUpdatedMovements = 0;
        {
            progressModel.increment(context.getString(R.string.resync_started));
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
            long start = syncTxBlockNumber == -1 ? 0 : syncTxBlockNumber + 1;
            long end = Math.min(start + TX_BLOCK_BATCH_SIZE, currentBlockNumber);
            while (start < currentBlockNumber) {
                TxHistoryResults txPartialHistory = txService.getTxHistory(
                        wallet.getCurrencyId(),
                        wallet.getPubKeyHash(),
                        start, end);

                nbUpdatedMovements += updateMovementsFromTxHistory(context,
                        walletId,
                        pendingMovementsByFingerprint,
                        udMap,
                        txPartialHistory);

                progressModel.increment((int)(end - start));

                start += TX_BLOCK_BATCH_SIZE;
                end = Math.min(end + TX_BLOCK_BATCH_SIZE, currentBlockNumber);
            }
        }

        // Load UD history
        {
            UdRemoteService udRemoteService = ServiceLocator.instance().getUdRemoteService();
            List<UdHistoryMovement> udHistoryMovements = udRemoteService.getUdHistory(wallet.getCurrencyId(),
                wallet.getPubKeyHash());
            nbUpdatedMovements += updateMovementsFromUdHistory(context, walletId, syncTxBlockNumber, udHistoryMovements);
        }

        // Update the wallet with the new TX block number
        progressModel.increment(context.getString(R.string.saving_wallet));
        wallet.setTxBlockNumber(currentBlockNumber);
        walletService.update(context.getContentResolver(), wallet);
        progressModel.increment();

        return nbUpdatedMovements;
    }


    /* -- internal methods-- */

    /**
     * Update wallet's movements from tx history
     * @param context
     * @param walletId
     * @param pendingMovementsByFingerprint
     * @param historyResults
     */
    protected long updateMovementsFromTxHistory(final Context context,
                                             final long walletId,
                                             final Map<String, Movement> pendingMovementsByFingerprint,
                                             final Map<Integer, Long> udMap,
                                             final TxHistoryResults historyResults) {

        List<Movement> movementsToUpdate = new ArrayList<Movement>();
        List<Movement> movementsToInsert = new ArrayList<Movement>();

        String pubkey = historyResults.getPubkey();

        // Workaround for ucoin issue #71
        // TODO remove this map when fixed in ucoin
        Set<String> processedFringerprints = new HashSet<String>();

        Iterator<Map.Entry<Integer, Long>> iteUD = udMap.entrySet().iterator();

        // Transfer Received
        if (historyResults.getHistory() != null && CollectionUtils.isNotEmpty(historyResults.getHistory().getReceived())) {
            for (TxHistoryMovement txHistoryMovement : historyResults.getHistory().getReceived()) {
                if (!processedFringerprints.contains(txHistoryMovement.getFingerprint())) {
                    Movement waitingMovement = pendingMovementsByFingerprint.get(txHistoryMovement.getFingerprint());

                    Long dividend = getUD(udMap, txHistoryMovement.getBlockNumber());

                    // Movement was existing, so update it
                    if (waitingMovement != null) {
                        waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                        waitingMovement.setTime(txHistoryMovement.getTime());
                        waitingMovement.setDividend(dividend);
                        movementsToUpdate.add(waitingMovement);

                        // Remove it from pending movement list
                        pendingMovementsByFingerprint.remove(txHistoryMovement.getFingerprint());
                    }

                    // Movement was not exists, so insert it
                    else {
                        Movement newMovement = toMovement(txHistoryMovement, walletId, pubkey, dividend);

                        if (newMovement != null) {
                            movementsToInsert.add(newMovement);
                        }
                    }

                    // Remember to not process it again (workaround for ucoin issue #71)
                    processedFringerprints.add(txHistoryMovement.getFingerprint());
                }
            }
        }

        // Transfer sent
        if (historyResults.getHistory() != null && CollectionUtils.isNotEmpty(historyResults.getHistory().getReceived())) {
            for (TxHistoryMovement txHistoryMovement : historyResults.getHistory().getSent()) {
                if (!processedFringerprints.contains(txHistoryMovement.getFingerprint())) {
                    Movement waitingMovement = pendingMovementsByFingerprint.get(txHistoryMovement.getFingerprint());

                    Long dividend = getUD(udMap, txHistoryMovement.getBlockNumber());

                    // Movement was existing, so update it
                    if (waitingMovement != null) {
                        waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                        waitingMovement.setTime(txHistoryMovement.getTime());
                        waitingMovement.setDividend(dividend);
                        movementsToUpdate.add(waitingMovement);

                        // Remove it from pending movement list
                        pendingMovementsByFingerprint.remove(txHistoryMovement.getFingerprint());
                    }

                    // Movement was not exists, so insert it
                    else {
                        Movement newMovement = toMovement(txHistoryMovement, walletId, pubkey, dividend);
                        if (newMovement != null) {
                            movementsToInsert.add(newMovement);
                        }
                    }

                    // Remember to not process it again (workaround for ucoin issue #71)
                    processedFringerprints.add(txHistoryMovement.getFingerprint());
                }
            }
        }

        long nbInsertOrUpdate = 0;

        // Update existing movements to update
        if (CollectionUtils.isNotEmpty(movementsToUpdate)) {
            // bulk updates
            update(context.getContentResolver(), movementsToUpdate);
            nbInsertOrUpdate += movementsToUpdate.size();
        }

        // Insert new movements
        if (CollectionUtils.isNotEmpty(movementsToInsert)) {
            // bulk insert
            insert(context.getContentResolver(), movementsToInsert, false);
            nbInsertOrUpdate += movementsToInsert.size();
        }

        return nbInsertOrUpdate;
    }

    /**
     * Update wallet's movements from UD history
     * @param context
     * @param walletId
     * @param udHistoryMovements UD history
     */
    public long updateMovementsFromUdHistory(final Context context,
                                             long walletId,
                                             long syncBlockNumber,
                                             List<UdHistoryMovement> udHistoryMovements) {
        List<Movement> movementsToInsert = new ArrayList<Movement>();

        for (UdHistoryMovement udHistoryMovement: udHistoryMovements) {
            if (udHistoryMovement.getBlockNumber() > syncBlockNumber) {
                Movement udMovement = toMovement(udHistoryMovement, walletId);
                movementsToInsert.add(udMovement);
            }
        }

        long nbInsertOrUpdate = 0;

        // Insert new movements
        if (CollectionUtils.isNotEmpty(movementsToInsert)) {
            // bulk insert
            insert(context.getContentResolver(), movementsToInsert, false);
            nbInsertOrUpdate += movementsToInsert.size();
        }

        return nbInsertOrUpdate;
    }


    private Movement toMovement(TxHistoryMovement source, long walletId, String pubkey, long dividend) {
        // Read the amount
        long amount = computeAmount(source, pubkey);
        if (amount == 0) {
            Log.w(TAG, String.format("Invalid TX (amount=0) with fingerprint [%s].", source.getFingerprint()));
            return null;
        }

        // Read issuers
        String issuers = computeIssuers(source, pubkey);

        // Read receivers
        String receivers = computeReceivers(source, pubkey);

        Movement target = new Movement();
        target.setWalletId(walletId);
        target.setFingerprint(source.getFingerprint());
        target.setComment(source.getComment());
        target.setAmount(amount);
        target.setDividend(dividend);
        target.setUD(false);
        target.setBlockNumber(source.getBlockNumber());
        target.setTime(source.getTime());
        target.setIssuers(issuers);
        target.setReceivers(receivers);
        return target;
    }

    private Movement toMovement(UdHistoryMovement source, long walletId) {
        // Check the amount
        if (source.getAmount() <= 0) {
            Log.w(TAG, String.format("Invalid UD (amount<=0) from block %s", source.getBlockNumber()));
            return null;
        }

        // Compute a fake fingerprint (should be unique)
        String fakeFingerPrint = new StringBuilder().append(walletId).append("~~")
                .append(source.getBlockNumber()).toString();

        Movement target = new Movement();
        target.setWalletId(walletId);
        target.setAmount(source.getAmount());
        target.setDividend(source.getAmount()); // same as amount
        target.setUD(true);
        target.setBlockNumber(source.getBlockNumber());
        target.setTime(source.getTime());
        target.setFingerprint(fakeFingerPrint);
        return target;
    }

    private long getAmountFromTxInput(String version, String inlineTxInput) {
        int lastIndex = inlineTxInput.lastIndexOf(':');
        return Long.parseLong(inlineTxInput.substring(lastIndex + 1));
    }

    private long getAmountFromTxOutput(String version, String inlineTxOutput) {
        int lastIndex = inlineTxOutput.lastIndexOf(':');
        return Long.parseLong(inlineTxOutput.substring(lastIndex + 1));
    }

    private String getPubkeyFromTxOutput(String version, String inlineTxOutput) {
        int lastIndex = inlineTxOutput.lastIndexOf(':');
        return inlineTxOutput.substring(0, lastIndex);
    }

    private long computeAmount(TxHistoryMovement movement, String pubkey) {
        long result = 0;
        String txVersion = movement.getVersion();

        int issuerIndex = -1;
        int i=0;
        for (String issuer: movement.getIssuers()) {
            if (pubkey.equals(issuer)) {
                issuerIndex = i;
                break;
            }
            i++;
        }

        if (issuerIndex != -1) {
            for (String input : movement.getInputs()) {
                if (input.startsWith(issuerIndex + ":")) {
                    result -= getAmountFromTxInput(txVersion, input);
                }
            }
        }

        for (String output : movement.getOutputs()) {
            if (output.startsWith(pubkey + ":")) {
                result += getAmountFromTxOutput(txVersion, output);
            }
        }

        return result;
    }

    private String computeIssuers(TxHistoryMovement movement, String pubkey) {
        Set<String> issuerPubKeys = new HashSet<String>();

        for (String issuer: movement.getIssuers()) {
            if (!pubkey.equals(issuer)) {
                issuerPubKeys.add(issuer);
            }
        }
        if (issuerPubKeys.size() == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (String pubKey: issuerPubKeys) {
            result.append(MOVEMENT_ISSUER_SEPARATOR).append(pubKey);
        }
        return result.substring(MOVEMENT_ISSUER_SEPARATOR.length()); // skip the first separator
    }

    private String computeReceivers(TxHistoryMovement movement, String pubkey) {
        StringBuilder result = new StringBuilder();

        String txVersion = movement.getVersion();

        for (String output: movement.getOutputs()) {
            String receiverPubkey = getPubkeyFromTxOutput(txVersion, output);
            if (!pubkey.equals(receiverPubkey)) {
                result.append(MOVEMENT_RECEIVERS_SEPARATOR)
                        .append(receiverPubkey);
            }
        }
        if (result.length() == 0) {
            return null;
        }
        return result.substring(MOVEMENT_ISSUER_SEPARATOR.length()); // skip the first separator
    }

    private List<Movement> getMovementsByWalletId(final ContentResolver resolver, final long walletId) {

        String selection = SQLiteTable.Movement.WALLET_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(walletId)
        };
        return getMovements(resolver, selection, selectionArgs);
    }

    private List<Movement> getPendingMovementsByWalletId(final ContentResolver resolver, final long walletId) {

        String selection = String.format("%s=? AND %s is null",
                SQLiteTable.Movement.WALLET_ID,
                SQLiteTable.Movement.BLOCK
        );
        String[] selectionArgs = {
                String.valueOf(walletId)
        };
        return getMovements(resolver, selection, selectionArgs);
    }

    private List<Movement> getMovements(final ContentResolver resolver, final String selection, final String[] selectionArgs) {
        Cursor cursor = resolver.query(getContentUri(), new String[]{}, selection,
                selectionArgs, null);

        List<Movement> result = new ArrayList<Movement>();
        while (cursor.moveToNext()) {
            Movement movement = toMovement(cursor);
            result.add(movement);
        }
        cursor.close();

        return result;
    }

    public void insert(final ContentResolver resolver, final Movement source) {

        ContentValues target = toContentValues(source);

        Uri uri = resolver.insert(getContentUri(), target);
        Long movementId = ContentUris.parseId(uri);
        if (movementId < 0) {
            throw new UCoinTechnicalException("Error while inserting movement");
        }

        // Refresh the inserted account
        source.setId(movementId);
    }

    public void update(final ContentResolver resolver, final Movement source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(getContentUri(), target, whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating movement. %s rows updated.", rowsUpdated));
        }
    }

    public void update(final ContentResolver resolver, final List<Movement> movements) {

        Uri contentUri = getContentUri();
        String whereClause = "_id=?";
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (Movement movement: movements) {
            String[] whereArgs = new String[]{String.valueOf(movement.getId())};

            ops.add(ContentProviderOperation.newUpdate(contentUri)
                    .withSelection(whereClause, whereArgs)
                    .withValues(toContentValues(movement))
                    .withYieldAllowed(true)
                    .build());
        }

        try {
            resolver.applyBatch(contentUri.getAuthority(), ops);
        }
        catch(RemoteException e1) {
            throw new UCoinTechnicalException("Error while inserting movements in batch mode: "
                    + e1.getMessage(),
                    e1);
        }
        catch(OperationApplicationException e2) {
            throw new UCoinTechnicalException("Error while inserting movements in batch mode: "
                    + e2.getMessage(),
                    e2);
        }
    }

    public void insert(final ContentResolver resolver, final List<Movement> movements, boolean mustSetId) {

        Uri contentUri = getContentUri();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (Movement movement: movements) {
            ops.add(ContentProviderOperation.newInsert(contentUri)
                    .withValues(toContentValues(movement))
                    .withYieldAllowed(true)
                    .build());
        }

        try {
            // Execute the batch
            ContentProviderResult[] opResults = resolver.applyBatch(contentUri.getAuthority(), ops);

            // Set movement's ids
            if (mustSetId) {
                int i = 0;
                for (Movement movement : movements) {
                    ContentProviderResult opResult = opResults[i++];
                    Long movementId = ContentUris.parseId(opResult.uri);
                    movement.setId(movementId);
                }
            }
        }
        catch(RemoteException e1) {
            throw new UCoinTechnicalException("Error while inserting movements in batch mode: "
                    + e1.getMessage(),
                    e1);
        }
        catch(OperationApplicationException e2) {
            throw new UCoinTechnicalException("Error while inserting movements in batch mode: "
                    + e2.getMessage(),
                    e2);
        }
    }

    public void deleteByWalletId(final Context context, final long walletId) {
        Log.d(TAG, "Deleting all movements for wallet id=" + walletId);

        ContentResolver resolver = context.getContentResolver();

        String whereClause = SQLiteTable.Movement.WALLET_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(walletId)};
        int rowsDeleted = resolver.delete(Uri.parse(Provider.CONTENT_URI + "/movement/"), whereClause, whereArgs);
        Log.d(TAG, " deleted movement count: " + rowsDeleted);
    }

    private ContentValues toContentValues(final Movement source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(SQLiteTable.Movement.WALLET_ID, source.getWalletId());
        target.put(SQLiteTable.Movement.IS_UD, source.isUD() ? 1 : 0);
        target.put(SQLiteTable.Movement.AMOUNT, source.getAmount());
        target.put(SQLiteTable.Movement.DIVIDEND, source.getDividend());
        target.put(SQLiteTable.Movement.FINGERPRINT, source.getFingerprint());
        target.put(SQLiteTable.Movement.BLOCK, source.getBlockNumber());
        target.put(SQLiteTable.Movement.TIME, source.getTime());
        if (StringUtils.isNotBlank(source.getComment())) {
            target.put(SQLiteTable.Movement.COMMENT, source.getComment());
        }
        if (StringUtils.isNotBlank(source.getIssuers())) {
            target.put(SQLiteTable.Movement.ISSUERS, source.getIssuers());
        }
        if (StringUtils.isNotBlank(source.getReceivers())) {
            target.put(SQLiteTable.Movement.RECEIVERS, source.getReceivers());
        }
        return target;
    }

    public Movement toMovement(final Cursor cursor) {
        // Init the holder is need
        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }

        Movement result = new Movement();
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setWalletId(cursor.getInt(mSelectHolder.walletIdIndex));
        result.setUD(cursor.getInt(mSelectHolder.isUDIndex) == 0 ? false : true);
        result.setAmount(cursor.getLong(mSelectHolder.amountIndex));
        result.setDividend(cursor.getLong(mSelectHolder.dividendIndex));
        result.setFingerprint(cursor.getString(mSelectHolder.fingerprint));
        result.setBlockNumber(cursor.getInt(mSelectHolder.blockIndex));
        result.setTime(cursor.getLong(mSelectHolder.timeIndex));
        result.setComment(cursor.getString(mSelectHolder.commentIndex));
        result.setIssuers(cursor.getString(mSelectHolder.issuersIndex));
        result.setReceivers(cursor.getString(mSelectHolder.receiversIndex));

        return result;
    }

    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/movement/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int walletIdIndex;
        int amountIndex;
        int dividendIndex;
        int isUDIndex;
        int fingerprint;
        int blockIndex;
        int timeIndex;
        int commentIndex;
        int issuersIndex;
        int receiversIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(SQLiteTable.Movement._ID);
            walletIdIndex = cursor.getColumnIndex(SQLiteTable.Movement.WALLET_ID);
            amountIndex = cursor.getColumnIndex(SQLiteTable.Movement.AMOUNT);
            dividendIndex = cursor.getColumnIndex(SQLiteTable.Movement.DIVIDEND);
            isUDIndex = cursor.getColumnIndex(SQLiteTable.Movement.IS_UD);
            fingerprint = cursor.getColumnIndex(SQLiteTable.Movement.FINGERPRINT);
            blockIndex = cursor.getColumnIndex(SQLiteTable.Movement.BLOCK);
            timeIndex = cursor.getColumnIndex(SQLiteTable.Movement.TIME);
            commentIndex = cursor.getColumnIndex(SQLiteTable.Movement.COMMENT);
            issuersIndex = cursor.getColumnIndex(SQLiteTable.Movement.ISSUERS);
            receiversIndex = cursor.getColumnIndex(SQLiteTable.Movement.RECEIVERS);
        }
    }

    class RefreshMovementTask extends AsyncTaskHandleException<Void, Void, Long> {

        private final long walletId;
        private final boolean doCompleteRefresh;
        //private final String unitType;

        public RefreshMovementTask(long walletId, boolean doCompleteRefresh, AsyncTaskListener<Long> listener) {
            super(listener);
            this.walletId = walletId;
            this.doCompleteRefresh = doCompleteRefresh;

            // Read unit type from preferences
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            //unitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
        }

        @Override
        protected Long doInBackgroundHandleException(Void... params) {
            long nbUpdates = refreshMovements(getContext(),
                    walletId, doCompleteRefresh, RefreshMovementTask.this);
            return nbUpdates;
        }
    }

    /**
     * Retrieve the UD from a given block number
     * @param udMap
     * @param blockNumber
     * @return
     */
    protected Long getUD(Map<Integer, Long> udMap, int blockNumber) {

        for (Integer udBlockNumber: udMap.keySet()) {
            if (blockNumber >= udBlockNumber.intValue()) {
                return udMap.get(udBlockNumber);
            }
        }

        return null;
    }
}
