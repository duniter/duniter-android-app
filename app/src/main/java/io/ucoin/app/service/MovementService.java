package io.ucoin.app.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Movement;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
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

    /* -- internal methods-- */

    private List<Movement> getMovementsByWalletId(ContentResolver resolver, long walletId) {

        String selection = Contract.Movement.WALLET_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(walletId)
        };
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
