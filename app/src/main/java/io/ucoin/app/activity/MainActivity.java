package io.ucoin.app.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.sql.sqlite.Currencies;
import io.ucoin.app.Format;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findActivity();
    }

    public void findActivity(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean connected = preferences.getBoolean(Application.CONNECTION,false);
        if(!connected) {
            startConnectionActivity();
        }else{
            LoadContactsTask loadContactsTask = new LoadContactsTask();
            loadContactsTask.execute();
            long currencyId = preferences.getLong(Application.EXTRA_CURRENCY_ID, -2);
            if(currencyId == -2){
                Toast.makeText(this,"Error MainActivity findActivity",Toast.LENGTH_LONG).show();
            }else {
                startCurrencyActivity(currencyId);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        Long currencyId;
        switch (requestCode){
//            case Application.ACTIVITY_CURRENCY_LIST:
//                currencyId = intent.getExtras().getLong(Application.EXTRA_CURRENCY_ID);
//                editor.putLong("currency_id", currencyId);
//                editor.apply();
//
//                startCurrencyActivity(currencyId);
//                break;
            case Application.ACTIVITY_CONNECTION:
                if(intent!=null) {
                    currencyId = intent.getExtras().getLong(Application.EXTRA_CURRENCY_ID);
                    editor.putLong(Application.EXTRA_CURRENCY_ID, currencyId);
                    editor.apply();
                }
                editor.putBoolean(Application.CONNECTION, true);
                editor.apply();
                findActivity();
                break;
        }
    }

    public void startConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivityForResult(intent, Application.ACTIVITY_CONNECTION);
    }

    public void startCurrencyActivity(Long currencyId) {
        Intent intent = new Intent(this, CurrencyActivity.class);
        intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
        startActivity(intent);
        finish();
    }


    public class LoadContactsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... param) {
            retrieveContacts(getContentResolver());
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

            if (cursor.moveToFirst()){
                do{
                    //final long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID)));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String webSite = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                    Map<String, String > data = Format.parseUri(webSite);

                    String uid = Format.isNull(data.get(Format.UID));
                    String pubkey = Format.isNull(data.get(Format.PUBLICKEY));
                    String currencyName = Format.isNull(data.get(Format.CURRENCY));

                    UcoinCurrency currency = new Currencies(getApplicationContext()).getByName(currencyName);
                    if(currency!=null) {
                        currency.contacts().add(name, uid, pubkey);
                    }
                }
                while (cursor.moveToNext());
            }

            if (cursor.isClosed()){
                cursor.close();
            }

            return ;
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

        private List<String> getWebSite(ContentResolver contentResolver, long contactId){
            List<String> result = new ArrayList<>();
            String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
                    + ContactsContract.Data.MIMETYPE + " = ? AND "
                    + ContactsContract.CommonDataKinds.Website.URL + " LIKE ?";
            String[] whereParams = new String[]{contactId+"",
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                    Format.CONTACT_PATH+"%"};
            Cursor webcur = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);
            if (webcur.moveToFirst()) {
                do {
                    result.add(webcur.getString(webcur.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)));
                }
                while (webcur.moveToNext());
            }
            webcur.close();

            return result;
        }
    }

}
