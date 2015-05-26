package io.ucoin.app.service;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.ModelUtils;
import io.ucoin.app.model.Movement;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.remote.TxHistoryMovement;
import io.ucoin.app.model.remote.TxHistoryResults;
import io.ucoin.app.model.remote.TxSource;
import io.ucoin.app.model.remote.TxSourceResults;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.CollectionUtils;
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

    // a cache instance of the movement Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private WalletService walletService;


    public MovementService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        walletService = ServiceLocator.instance().getWalletService();
    }

    public Movement save(final Context context, final Movement movement) throws DuplicatePubkeyException {
        ObjectUtils.checkNotNull(movement);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(movement.getFingerprint()));

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
        ServiceLocator serviceLocator = ServiceLocator.instance();

        if (progressModel == null) {
            progressModel = new NullProgressModel();
        }

        // Get wallet from database
        Wallet wallet = walletService.getWalletById(context, walletId);
        long currencyId = wallet.getCurrencyId();

        // Get the current block number
        BlockchainBlock currentBlock = serviceLocator.getBlockchainRemoteService().getCurrentBlock(currencyId, true);
        long currentBlockNumber = currentBlock.getNumber();
        long syncTxBlockNumber = wallet.getTxBlockNumber();

        progressModel.setMax(4);
        progressModel.setMessage(context.getString(R.string.resync_started));

        // Delete existing movement if need
        if (completeRefresh) {
            deleteByWalletId(context, walletId);
            syncTxBlockNumber = -1;
        }

        // If current block has NOT changed: exit
        else if (syncTxBlockNumber == currentBlockNumber) {
            return 0;
        }

        progressModel.increment();
        TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
        TxSourceResults sourceResults = txService.getSourcesAndCredit(wallet.getCurrencyId(), wallet.getPubKeyHash());

        // Load movement that waiting block
        progressModel.increment();
        List<Movement> waitingMovements = getWaitingMovementsByWalletId(context.getContentResolver(), walletId);
        Map<String, Movement> waitingMovementByFingerprint = ModelUtils.movementsToFingerprintMap(waitingMovements);


        progressModel.increment();
        long start = syncTxBlockNumber == -1 ? 0 : syncTxBlockNumber + 1;
        long end = Math.min(start + TX_BLOCK_BATCH_SIZE, currentBlockNumber);

        long txCount = 0;

        while(start < currentBlockNumber) {
            TxHistoryResults history = txService.getHistory(wallet.getCurrencyId(),
                    wallet.getPubKeyHash(),
                    start, end);

            txCount += updateMovementsFromHistory(context, walletId, start - 1,
                    waitingMovementByFingerprint, sourceResults.getSources(), history);

            start += TX_BLOCK_BATCH_SIZE;
            end = Math.min(end + TX_BLOCK_BATCH_SIZE, currentBlockNumber);
        }

        // Update the wallet with the new TX block number
        progressModel.increment();
        wallet.setTxBlockNumber(currentBlockNumber);
        walletService.update(context.getContentResolver(), wallet);

        return txCount;
    }

    /**
     * Update wallet's movements from tx history
     * @param context
     * @param walletId
     * @param lastUpdateBlockNumber
     * @param sources
     */
    public long updateMovementsFromHistory(final Context context,
                                           long walletId,
                                           long lastUpdateBlockNumber,
                                           Map<String, Movement> waitingMovementByFingerprint,
                                           List<TxSource> sources,
                                           TxHistoryResults historyResults) {

        List<Movement> movementsToUpdate = new ArrayList<Movement>();
        List<Movement> movementsToInsert = new ArrayList<Movement>();

        String pubkey = historyResults.getPubkey();

        // Workaround for ucoin issue #71
        // TODO remove this map when fixed in ucoin
        Set<String> processedFringerprints = new HashSet<String>();

        // Transfer Received
        if (historyResults.getHistory() != null && CollectionUtils.isNotEmpty(historyResults.getHistory().getReceived())) {
            for (TxHistoryMovement txHistoryMovement : historyResults.getHistory().getReceived()) {
                if (!processedFringerprints.contains(txHistoryMovement.getFingerprint())) {
                    Movement waitingMovement = waitingMovementByFingerprint.get(txHistoryMovement.getFingerprint());

                    // Movement was existing, so update it
                    if (waitingMovement != null) {
                        waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                        waitingMovement.setTime(txHistoryMovement.getTime());
                        movementsToUpdate.add(waitingMovement);
                    }

                    // Movement was not exists, so insert it
                    else {
                        Movement newMovement = toMovement(txHistoryMovement, walletId, pubkey);

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
                    Movement waitingMovement = waitingMovementByFingerprint.get(txHistoryMovement.getFingerprint());

                    // Movement was existing, so update it
                    if (waitingMovement != null) {
                        waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                        waitingMovement.setTime(txHistoryMovement.getTime());
                        movementsToUpdate.add(waitingMovement);
                    }

                    // Movement was not exists, so insert it
                    else {
                        Movement newMovement = toMovement(txHistoryMovement, walletId, pubkey);
                        if (newMovement != null) {
                            movementsToInsert.add(newMovement);
                        }
                    }

                    // Remember to not process it again (workaround for ucoin issue #71)
                    processedFringerprints.add(txHistoryMovement.getFingerprint());
                }
            }
        }

        // UD received
        /*for (TxSource source: sources) {
            // If UD
            if (TxSource.SOURCE_TYPE_UD.equals(source.getType())) {
                // If not processed block
                if (source.getNumber() > lastUpdateBlockNumber) {
                    Log.d(TAG, "Detected a received UD : " + source.toString() + ". Should be processed ???");
                    // TODO : check the block number to known if already processed or not
                    Movement udMovement = new Movement();
                    udMovement.setWalletId(walletId);
                    udMovement.setFingerprint(source.getFingerprint());
                    udMovement.setComment(context.getString(R.string.movement_ud));
                    udMovement.setAmount(source.getAmount());
                    udMovement.setUD(true);
                    udMovement.setBlockNumber(source.getNumber());
                    // TODO : udMovement.setTime();
                    movementsToInsert.add(udMovement);
                }
            }
        }*/

        long count = 0;

        // Update existing movements to update
        if (CollectionUtils.isNotEmpty(movementsToUpdate)) {
            // bulk updates
            update(context.getContentResolver(), movementsToUpdate);
            count += movementsToUpdate.size();
        }

        // Insert new movements
        if (CollectionUtils.isNotEmpty(movementsToInsert)) {
            // bulk insert
            insert(context.getContentResolver(), movementsToInsert, false);
            count += movementsToInsert.size();
        }

        return count;
    }

    /* -- internal methods-- */
    private Movement toMovement(TxHistoryMovement source, long walletId, String pubkey) {
        // Read the amount
        long amount = computeAmount(source, pubkey);
        if (amount == 0) {
            Log.w(TAG, String.format("Invalid TX (amount=0) with fingerprint [%s].", source.getFingerprint()));
            amount = computeAmount(source, pubkey);
            return null;
        }

        // Read issuers pubkeys
        String pubkeys = computeOtherPubkeys(source, pubkey);

        Movement target = new Movement();
        target.setWalletId(walletId);
        target.setFingerprint(source.getFingerprint());
        target.setComment(source.getComment());
        target.setAmount(amount);
        target.setUD(false);
        target.setBlockNumber(source.getBlockNumber());
        target.setTime(source.getTime());
        target.setPubkey(pubkeys);
        return target;
    }

    private long getAmountFromTxInput(String version, String inlineTxSource) {
        int lastIndex = inlineTxSource.lastIndexOf(':');
        return Long.parseLong(inlineTxSource.substring(lastIndex + 1));
    }

    private long getAmountFromTxOutput(String version, String inlineTxSource) {
        int lastIndex = inlineTxSource.lastIndexOf(':');
        return Long.parseLong(inlineTxSource.substring(lastIndex+1));
    }

    private long computeAmount(TxHistoryMovement movement, String pubkey) {
        long result = 0;

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
                    result -= getAmountFromTxInput(movement.getVersion(), input);
                }
            }
        }

        for (String output : movement.getOutputs()) {
            if (output.startsWith(pubkey + ":")) {
                result += getAmountFromTxOutput(movement.getVersion(), output);
            }
        }

        return result;
    }

    private String computeOtherPubkeys(TxHistoryMovement movement, String pubkey) {
        Set<String> pubKeys = new HashSet<String>();

        for (String issuer: movement.getIssuers()) {
            if (!pubkey.equals(issuer)) {
                pubKeys.add(issuer);
            }
        }
        if (pubKeys.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String pubKey: pubKeys) {
            sb.append(", ").append(pubKey);
        }
        return sb.substring(2);
    }

    private List<Movement> getMovementsByWalletId(final ContentResolver resolver, final long walletId) {

        String selection = Contract.Movement.WALLET_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(walletId)
        };
        return getMovements(resolver, selection, selectionArgs);
    }

    private List<Movement> getWaitingMovementsByWalletId(final ContentResolver resolver, final long walletId) {

        String selection = String.format("%s=? AND %s is null",
                Contract.Movement.WALLET_ID,
                Contract.Movement.BLOCK
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

        String whereClause = Contract.Movement.WALLET_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(walletId)};
        int rowsDeleted = resolver.delete(Uri.parse(Provider.CONTENT_URI + "/movement/"), whereClause, whereArgs);
        Log.d(TAG, " deleted movement count: " + rowsDeleted);
    }

    private ContentValues toContentValues(final Movement source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(Contract.Movement.WALLET_ID, source.getWalletId());
        target.put(Contract.Movement.IS_UD, source.isUD() ? 1 : 0);
        target.put(Contract.Movement.AMOUNT, source.getAmount());
        target.put(Contract.Movement.FINGERPRINT, source.getFingerprint());
        target.put(Contract.Movement.BLOCK, source.getBlockNumber());
        target.put(Contract.Movement.TIME, source.getTime());
        if (StringUtils.isNotBlank(source.getComment())) {
            target.put(Contract.Movement.COMMENT, source.getComment());
        }
        if (StringUtils.isNotBlank(source.getPubkey())) {
            target.put(Contract.Movement.PUBLIC_KEY, source.getPubkey());
        }
        return target;
    }

    private Movement toMovement(final Cursor cursor) {
        // Init the holder is need
        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }

        Movement result = new Movement();
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setWalletId(cursor.getLong(mSelectHolder.walletIdIndex));
        result.setUD(cursor.getInt(mSelectHolder.isUDIndex) == 0 ? false : true);
        result.setAmount(cursor.getLong(mSelectHolder.amountIndex));
        result.setFingerprint(cursor.getString(mSelectHolder.fingerprint));
        result.setBlockNumber(cursor.getLong(mSelectHolder.blockIndex));
        result.setTime(cursor.getLong(mSelectHolder.timeIndex));
        result.setComment(cursor.getString(mSelectHolder.commentIndex));
        result.setPubkey(cursor.getString(mSelectHolder.pubkeyIndex));

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
        int isUDIndex;
        int fingerprint;
        int blockIndex;
        int timeIndex;
        int commentIndex;
        int pubkeyIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Movement._ID);
            walletIdIndex = cursor.getColumnIndex(Contract.Movement.WALLET_ID);
            amountIndex = cursor.getColumnIndex(Contract.Movement.AMOUNT);
            isUDIndex = cursor.getColumnIndex(Contract.Movement.IS_UD);
            fingerprint = cursor.getColumnIndex(Contract.Movement.FINGERPRINT);
            blockIndex = cursor.getColumnIndex(Contract.Movement.BLOCK);
            timeIndex = cursor.getColumnIndex(Contract.Movement.TIME);
            commentIndex = cursor.getColumnIndex(Contract.Movement.COMMENT);
            pubkeyIndex = cursor.getColumnIndex(Contract.Movement.PUBLIC_KEY);
        }
    }

    class RefreshMovementTask extends AsyncTaskHandleException<Void, Void, Long> {

        private final long walletId;
        private final boolean doCompleteRefresh;

        public RefreshMovementTask(long walletId, boolean doCompleteRefresh, AsyncTaskListener<Long> listener) {
            super(listener);
            this.walletId = walletId;
            this.doCompleteRefresh = doCompleteRefresh;
        }

        @Override
        protected Long doInBackgroundHandleException(Void... params) {
            long nbUpdates = refreshMovements(getContext(),
                    walletId, doCompleteRefresh, RefreshMovementTask.this);
            return nbUpdates;
        }
    }
}
