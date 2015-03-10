package io.ucoin.app.service;

import android.app.Activity;
import android.app.Application;
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
import io.ucoin.app.model.Contact;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class ContactService extends BaseService {

    /**
     * Logger.
     */
    private static final String TAG = "ContactService";

    // a cache instance of the contact Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    public ContactService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public Contact save(final Context context, final Contact contact) throws DuplicatePubkeyException {
        ObjectUtils.checkNotNull(contact);

        // create if not exists
        if (contact.getId() == null) {

            insert(context.getContentResolver(), contact);
        }

        // or update
        else {
            update(context.getContentResolver(), contact);
        }

        // return the updated contact (id could have change)
        return contact;
    }

    public List<Contact> getContacts(Activity activity) {
        return getContacts(activity.getApplication());
    }

    public List<Contact> getContacts(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getContactsByAccountId(application.getContentResolver(), Long.parseLong(accountId));
    }

    public void delete(final Context context, final long contactId) {

        ContentResolver resolver = context.getContentResolver();

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(contactId)};
        int rowsUpdated = resolver.delete(getContentUri(), whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while deleting contact [id=%s]. %s rows updated.", contactId, rowsUpdated));
        }
    }

    /* -- internal methods-- */

    private List<Contact> getContactsByAccountId(ContentResolver resolver, long accountId) {

        String selection = Contract.Contact.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        Cursor cursor = resolver.query(getContentUri(), new String[]{}, selection,
                selectionArgs, null);

        List<Contact> result = new ArrayList<Contact>();
        while (cursor.moveToNext()) {
            Contact contact = toContact(cursor);
            result.add(contact);
        }
        cursor.close();

        return result;
    }

    public void insert(final ContentResolver resolver, final Contact source) {

        ContentValues target = toContentValues(source);

        Uri uri = resolver.insert(getContentUri(), target);
        Long contactId = ContentUris.parseId(uri);
        if (contactId < 0) {
            throw new UCoinTechnicalException("Error while inserting contact");
        }

        // Refresh the inserted account
        source.setId(contactId);
    }

    public void update(final ContentResolver resolver, final Contact source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(getContentUri(), target, whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating contact. %s rows updated.", rowsUpdated));
        }
    }

    private ContentValues toContentValues(final Contact source) {
        //Create account in database
        ContentValues target = new ContentValues();
        target.put(Contract.Contact.ACCOUNT_ID, source.getAccountId());
        return target;
    }

    private Contact toContact(final Cursor cursor) {
        // Init the holder is need
        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }

        Contact result = new Contact();
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setAccountId(cursor.getLong(mSelectHolder.accountIdIndex));

        return result;
    }

    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/contact/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int accountIdIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Contact._ID);
            accountIdIndex = cursor.getColumnIndex(Contract.Contact.ACCOUNT_ID);
        }
    }
}
