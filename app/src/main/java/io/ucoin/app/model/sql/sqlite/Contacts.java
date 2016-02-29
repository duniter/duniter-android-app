package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinContact;
import io.ucoin.app.model.UcoinContacts;
import io.ucoin.app.sqlite.SQLiteTable;

public class Contacts extends Table
        implements UcoinContacts {

    private Long mCurrencyId;

    public Contacts(Context context, Long currencyId) {
        this(context, currencyId, SQLiteTable.Contact.CURRENCY_ID + "=?", new String[]{currencyId.toString()});
        mCurrencyId = currencyId;
    }

    private Contacts(Context context, Long currencyId, String selection, String[] selectionArgs) {
        this(context, currencyId, selection, selectionArgs, null);
        mCurrencyId = currencyId;
    }

    private Contacts(Context context, Long currencyId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.CONTACT_URI, selection, selectionArgs, sortOrder);
        mCurrencyId = currencyId;
    }

    @Override
    public UcoinContact add(String name, String uid, String publicKey) {
        ContentValues values = new ContentValues();
        UcoinContact contact;

        contact = getByName(name);
        if(contact != null){
            delete(contact.id());
        }
        contact = getByPublicKey(publicKey);
        if(contact != null){
            delete(contact.id());
        }
        values.put(SQLiteTable.Contact.CURRENCY_ID, mCurrencyId);
        values.put(SQLiteTable.Contact.NAME, name);
        values.put(SQLiteTable.Contact.UID, uid);
        values.put(SQLiteTable.Contact.PUBLIC_KEY, publicKey);
        Uri uri = insert(values);

        return new Contact(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinContact getById(Long id) {
        return new Contact(mContext, id);
    }

    @Override
    public UcoinContact getByName(String name) {
        String selection = SQLiteTable.Contact.CURRENCY_ID + "=? AND " + SQLiteTable.Contact.NAME + " LIKE ?";
        String[] selectionArgs = new String[]{mCurrencyId.toString(), name};
        UcoinContacts contacts = new Contacts(mContext, mCurrencyId, selection, selectionArgs);
        if (contacts.iterator().hasNext()) {
            return contacts.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinContact getByPublicKey(String publicKey) {
        String selection = SQLiteTable.Contact.CURRENCY_ID + "=? AND " + SQLiteTable.Contact.PUBLIC_KEY + " LIKE ?";
        String[] selectionArgs = new String[]{mCurrencyId.toString(), publicKey};
        UcoinContacts contacts = new Contacts(mContext, mCurrencyId, selection, selectionArgs);
        if (contacts.iterator().hasNext()) {
            return contacts.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Cursor getbyCurrency() {
        String selection = SQLiteTable.Contact.CURRENCY_ID + "=?";
        String[] selectionArgs = new String[]{mCurrencyId.toString()};
        UcoinContacts contacts;
        if (mCurrencyId.equals(Long.valueOf(-1))){
            contacts = new Contacts(mContext, mCurrencyId, null, null);
        }else {
            contacts = new Contacts(mContext, mCurrencyId, selection, selectionArgs);
        }
        return ((Table)contacts).fetch();
    }

    @Override
    public Iterator<UcoinContact> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinContact> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Contact(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}