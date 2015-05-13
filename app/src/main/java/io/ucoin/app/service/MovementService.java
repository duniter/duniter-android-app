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
import java.util.List;
import java.util.Map;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.ModelUtils;
import io.ucoin.app.model.Movement;
import io.ucoin.app.model.remote.TxHistoryMovement;
import io.ucoin.app.model.remote.TxHistoryResults;
import io.ucoin.app.model.remote.TxSource;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class MovementService extends BaseService {

    /**
     * Logger.
     */
    private static final String TAG = "MovementService";

    // a cache instance of the movement Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;


    public MovementService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
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
     * Update wallet's movements from tx history
     * @param context
     * @param walletId
     * @param sources
     */
    public void updateMovementsFromHistory(final Context context,
                                           long walletId,
                                           long bockNumber,
                                           List<TxSource> sources,
                                           TxHistoryResults historyResults) {

        // Load movement that waiting block
        List<Movement> waitingMovements = getWaitingMovementsByWalletId(context.getContentResolver(), walletId);

        Map<String, Movement> waitingMovementByFingerprint = ModelUtils.movementsToFingerprintMap(waitingMovements);

        List<Movement> movementsToUpdate = new ArrayList<>();
        List<Movement> movementsToInsert = new ArrayList<>();

        // Transfer Received
        if (historyResults.getHistory() != null && CollectionUtils.isNotEmpty(historyResults.getHistory().getReceived())) {
            for (TxHistoryMovement txHistoryMovement : historyResults.getHistory().getReceived()) {
                Movement waitingMovement = waitingMovementByFingerprint.get(txHistoryMovement.getFingerprint());

                // Movement was existing, so update it
                if (waitingMovement != null) {
                    waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                    waitingMovement.setTime(txHistoryMovement.getTime());
                    movementsToUpdate.add(waitingMovement);
                }

                // Movement was not exists, so insert it
                else {
                    Movement newMovement = toMovement(txHistoryMovement, walletId, historyResults.getPubkey());

                    movementsToInsert.add(newMovement);
                }
            }
        }

        // Transfer sent
        if (historyResults.getHistory() != null && CollectionUtils.isNotEmpty(historyResults.getHistory().getReceived())) {
            for (TxHistoryMovement txHistoryMovement : historyResults.getHistory().getSent()) {
                Movement waitingMovement = waitingMovementByFingerprint.get(txHistoryMovement.getFingerprint());

                // Movement was existing, so update it
                if (waitingMovement != null) {
                    waitingMovement.setBlockNumber(txHistoryMovement.getBlockNumber());
                    waitingMovement.setTime(txHistoryMovement.getTime());
                    movementsToUpdate.add(waitingMovement);
                }

                // Movement was not exists, so insert it
                else {
                    Movement newMovement = toMovement(txHistoryMovement, walletId, historyResults.getPubkey());

                    movementsToInsert.add(newMovement);
                }
            }
        }

        // UD received
        for (TxSource source: sources) {
            // If UD
            if (TxSource.SOURCE_TYPE_UD.equals(source.getType())) {
                // If not processed block
                if (source.getNumber() > bockNumber) {
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
        }

        // Update existing movements to update
        if (CollectionUtils.isNotEmpty(movementsToUpdate)) {
            // bulk updates
            update(context.getContentResolver(), movementsToUpdate);
        }

        // Insert new movements
        if (CollectionUtils.isNotEmpty(movementsToInsert)) {
            // bulk insert
            insert(context.getContentResolver(), movementsToInsert, false);
        }
    }

    /* -- internal methods-- */
    private Movement toMovement(TxHistoryMovement source, long walletId, String pubkey) {
        long amount = computeAmount(source, pubkey);

        Movement target = new Movement();
        target.setWalletId(walletId);
        target.setFingerprint(source.getFingerprint());
        target.setComment(source.getComment());
        target.setAmount(amount);
        target.setUD(false);
        target.setBlockNumber(source.getBlockNumber());
        target.setTime(source.getTime());

        return target;
    }

    private long getAmountFromTxInput(String version, String inlineTxSource) {
        int lastIndex = inlineTxSource.lastIndexOf(':');
        return Long.parseLong(inlineTxSource.substring(lastIndex+1));
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

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Movement._ID);
            walletIdIndex = cursor.getColumnIndex(Contract.Movement.WALLET_ID);
            amountIndex = cursor.getColumnIndex(Contract.Movement.AMOUNT);
            isUDIndex = cursor.getColumnIndex(Contract.Movement.IS_UD);
            fingerprint = cursor.getColumnIndex(Contract.Movement.FINGERPRINT);
            blockIndex = cursor.getColumnIndex(Contract.Movement.BLOCK);
            timeIndex = cursor.getColumnIndex(Contract.Movement.TIME);
            commentIndex = cursor.getColumnIndex(Contract.Movement.COMMENT);
        }
    }
}
