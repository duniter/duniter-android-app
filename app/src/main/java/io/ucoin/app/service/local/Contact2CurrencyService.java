package io.ucoin.app.service.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

import io.ucoin.app.content.Provider;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.ServiceLocator;
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

        ContentResolver resolver = context.getContentResolver();

        String where =
                SQLiteTable.Contact2Currency.PUBLIC_KEY + "=? AND " +
                SQLiteTable.Contact2Currency.CONTACT_ID + "=?";
        String[] whereArgs = null;
        Cursor cur = null;

        for (Identity identity: identities) {
            whereArgs = new String[]{identity.getPubkey(),""+contactId};
            cur = resolver.query(Provider.CONTACT2CURRENCY_URI, null, where, whereArgs, null);
            // create if not exists
            if (!cur.moveToFirst()) {
                insert(context.getContentResolver(), contactId, identity);
            }
            // or update
            else {
                update(context.getContentResolver(), contactId, identity);
            }
            if(!cur.isClosed()){
                cur.close();
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

    public Contact isContact(final Context context, String pubkey, long currencyId){
        String where =
                SQLiteTable.Contact2Currency.PUBLIC_KEY + "=? AND "+
                SQLiteTable.Contact2Currency.CURRENCY_ID + "=?";
        String[] whereArgs = new String[]{pubkey,""+currencyId};

        Cursor cur = context.getContentResolver().query(Provider.CONTACT2CURRENCY_URI, new String[]{SQLiteTable.Contact2Currency.CONTACT_ID}, where, whereArgs, null);

        if(cur.moveToFirst()){
            long contactId = cur.getLong(cur.getColumnIndex(SQLiteTable.Contact2Currency.CONTACT_ID));
            cur.close();
            return ServiceLocator.instance().getContactService().getContactById(context,contactId);
        }
        return null;
    }

    public void update(final ContentResolver resolver,final long contactId, final Identity source) {
        ContentValues target = toContentValues(contactId, source);

        String where =
                SQLiteTable.Contact2Currency.PUBLIC_KEY + "=? AND " +
                        SQLiteTable.Contact2Currency.CONTACT_ID + "=?";
        String[] whereArgs = new String[]{source.getPubkey(),""+contactId};
        int rowsUpdated = resolver.update(Provider.CONTACT2CURRENCY_URI, target, where, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating contact2currency. %s rows updated.", rowsUpdated));
        }
    }

    private ContentValues toContentValues(final long contactId, final Identity source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(SQLiteTable.Contact2Currency.CONTACT_ID, contactId);
        target.put(SQLiteTable.Contact2Currency.UID, source.getUid());
        target.put(SQLiteTable.Contact2Currency.PUBLIC_KEY, source.getPubkey());
        target.put(SQLiteTable.Contact2Currency.CURRENCY_ID, source.getCurrencyId());
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

    public void delete(Context context, long contactId, long currencyId) {
        ContentResolver resolver = context.getContentResolver();

        String whereClause =
                SQLiteTable.Contact2Currency.CONTACT_ID + "=? AND "+
                SQLiteTable.Contact2Currency.CURRENCY_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(contactId),String.valueOf(currencyId)};
        int rowsUpdated = resolver.delete(Provider.CONTACT2CURRENCY_URI, whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while deleting contact [id=%s]. %s rows updated.", contactId, rowsUpdated));
        }
    }

    private class SelectCursorHolder {

        int idIndex;
        int contactIdIndex;
        int currencyIdIndex;
        int uidIdIndex;
        int pubkeyIdIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(SQLiteTable.Contact2Currency._ID);
            contactIdIndex = cursor.getColumnIndex(SQLiteTable.Contact2Currency.CONTACT_ID);
            currencyIdIndex = cursor.getColumnIndex(SQLiteTable.Contact2Currency.CURRENCY_ID);
            uidIdIndex = cursor.getColumnIndex(SQLiteTable.Contact2Currency.UID);
            pubkeyIdIndex = cursor.getColumnIndex(SQLiteTable.Contact2Currency.PUBLIC_KEY);
        }
    }

}
