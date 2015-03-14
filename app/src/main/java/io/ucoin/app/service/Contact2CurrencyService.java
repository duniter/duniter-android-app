package io.ucoin.app.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Identity;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class Contact2CurrencyService extends BaseService {

    /**
     * Logger.
     */
    private static final String TAG = "Contact2CurrencyService";

    // a cache instance of the contact Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    public Contact2CurrencyService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public List<Identity> saveAll(final Context context, long contactId, List<Identity> identities) {

        // TODO : load existing identities, to delete identities not present in the given list

        for (Identity identity: identities) {
            // create if not exists
            if (identity.getId() == null) {

                insert(context.getContentResolver(), contactId, identity);
            }

            // or update
            else {
                update(context.getContentResolver(), contactId, identity);

            }
        }

        return identities;
    }

    /* -- internal methods-- */

    public void insert(final ContentResolver resolver,final long contactId, final Identity source) {

        ContentValues target = toContentValues(contactId, source);

        Uri uri = resolver.insert(getContentUri(), target);
        Long contact2currencyId = ContentUris.parseId(uri);
        if (contact2currencyId < 0) {
            throw new UCoinTechnicalException("Error while inserting contact2currency");
        }

        // Refresh the inserted account
        source.setId(contactId);
    }

    public void update(final ContentResolver resolver,final long contactId, final Identity source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(contactId, source);

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(getContentUri(), target, whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating contact2currency. %s rows updated.", rowsUpdated));
        }
    }

    private ContentValues toContentValues(final long contactId, final Identity source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(Contract.Contact2Currency.CONTACT_ID, contactId);
        target.put(Contract.Contact2Currency.UID, source.getUid());
        target.put(Contract.Contact2Currency.PUBLIC_KEY, source.getPubkey());
        target.put(Contract.Contact2Currency.CURRENCY_ID, source.getCurrencyId());
        return target;
    }

    private Identity toIdentity(final Cursor cursor) {
        // Init the holder is need
        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }

        Identity result = new Identity();
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setCurrencyId(cursor.getLong(mSelectHolder.currencyIdIndex));
        result.setUid(cursor.getString(mSelectHolder.uidIdIndex));
        result.setPubkey(cursor.getString(mSelectHolder.pubkeyIdIndex));

        return result;
    }

    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/contact2currency/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int contactIdIndex;
        int currencyIdIndex;
        int uidIdIndex;
        int pubkeyIdIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Contact2Currency._ID);
            contactIdIndex = cursor.getColumnIndex(Contract.Contact2Currency.CONTACT_ID);
            currencyIdIndex = cursor.getColumnIndex(Contract.Contact2Currency.CURRENCY_ID);
            uidIdIndex = cursor.getColumnIndex(Contract.Contact2Currency.UID);
            pubkeyIdIndex = cursor.getColumnIndex(Contract.Contact2Currency.PUBLIC_KEY);
        }
    }

}
