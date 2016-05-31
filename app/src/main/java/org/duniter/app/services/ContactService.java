package org.duniter.app.services;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.duniter.app.Format;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class ContactService extends AsyncTask<ContentResolver, Void, String> {

    private Context context;

    public ContactService(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(ContentResolver... param) {
        retrieveContacts(param[0]);
        return null;
    }

    private void retrieveContacts(ContentResolver contentResolver){
        String where = ContactsContract.Data.MIMETYPE + " = ? AND "
                + ContactsContract.CommonDataKinds.Website.URL + " LIKE ?";
        String[] whereParams = new String[]{
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                Format.CONTACT_PATH+"%"};

        final Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);


        if (cursor == null){
            Log.i("TAG", "Cannot retrieve the contacts");
            return ;
        }

        String currencyName ="";
        Currency currency = null;

        if (cursor.moveToFirst()){
            do{
                //final long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID)));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String webSite = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                Map<String, String > data = Format.parseUri(webSite);

                String uid = Format.isNull(data.get(Format.UID));
                String pubkey = Format.isNull(data.get(Format.PUBLICKEY));
                String cName = Format.isNull(data.get(Format.CURRENCY));

                if(!currencyName.equals(cName)){
                    currencyName=cName;
                    currency = SqlService.getCurrencySql(context).getByName(currencyName);
                }
                if (currency!=null && currency.getId()!=null) {
                    try {
                        addContact(currency, pubkey, name, uid);
                    }catch (SQLiteConstraintException e){
                        Log.d("CONTACT","contact : "+name+" alredy exist");
                    }
                }
            }
            while (cursor.moveToNext());
        }

        if (!cursor.isClosed()){
            cursor.close();
        }

        return ;
    }

    private void addContact(Currency currency,String pubkey,String name,String uid){
        Contact contact = new Contact();
        contact.setCurrency(currency);
        contact.setPublicKey(pubkey);
        contact.setAlias(name);
        contact.setUid(uid);
        contact.setContact(true);
        contact.setId(SqlService.getContactSql(context).insert(contact));
    }

    private Bitmap getPhoto(ContentResolver contentResolver, long contactId){
        Bitmap photo = null;
        final Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        final Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        final Cursor cursor = contentResolver.query(photoUri, new String[] { ContactsContract.Contacts.Photo.DATA15 }, null, null, null);

        if (cursor == null)
        {
            return null;
        }

        if (cursor.moveToFirst() == true)
        {
            final byte[] data = cursor.getBlob(0);

            if (data != null)
            {
                photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
            }
        }

        if (cursor.isClosed() == false)
        {
            cursor.close();
        }

        return photo;
    }
}