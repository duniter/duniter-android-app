package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinIdentities;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.technical.crypto.AddressFormatException;

final public class Identities extends Table
        implements UcoinIdentities {

    private Long mCurrencyId;

    public Identities(Context context, Long currencyId) {
        this(context, currencyId, SQLiteTable.Identity.CURRENCY_ID + "=?",new String[]{currencyId.toString()});
    }

    private Identities(Context context, Long currencyId, String selection, String[] selectionArgs) {
        this(context, currencyId, selection, selectionArgs, null);
    }

    private Identities(Context context, Long currencyId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.IDENTITY_URI, selection, selectionArgs, sortOrder);
        mCurrencyId = currencyId;
    }

    @Override
    public UcoinIdentity getById(Long id) {
        return new Identity(mContext, id);
    }

    @Override
    public UcoinIdentity getIdentity() {
        UcoinIdentity result = null;
        Cursor c = fetch();
        if (c.moveToFirst()) {
            Long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
            result = new Identity(mContext, id);
        }
        if(!c.isClosed()){
            c.close();
        }
        return result;
    }

    @Override
    public UcoinIdentity getIdentityByWallet(Long id) {
        String selection = SQLiteTable.Identity.WALLET_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        UcoinIdentities identities = new Identities(mContext, mCurrencyId, selection, selectionArgs, null);
        if (identities.iterator().hasNext()) {
            return identities.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinIdentity add(String uid, String publicKey) throws AddressFormatException {
        UcoinIdentity result = getIdentityByWallet((long) -5);

        if(result==null || !getIdentity().publicKey().equals(publicKey)){
            if(result!=null) {
                delete(result.id());
            }
            ContentValues values = new ContentValues();
            values.put(SQLiteTable.Identity.CURRENCY_ID, mCurrencyId);
            values.put(SQLiteTable.Identity.PUBLIC_KEY, publicKey);
            values.put(SQLiteTable.Identity.UID, uid);
            values.put(SQLiteTable.Identity.WALLET_ID, -5);
            Uri uri = insert(values);
            result = new Identity(mContext, Long.parseLong(uri.getLastPathSegment()));
        }

        return result;
    }

    @Override
    public UcoinIdentity addWallet(String uid, String publicKey, Long walletId) throws AddressFormatException {
        UcoinIdentity result = getIdentityByWallet(walletId);

        if(result==null || !result.publicKey().equals(publicKey)){
            if(result!=null) {
                delete(result.id());
            }
            ContentValues values = new ContentValues();
            values.put(SQLiteTable.Identity.CURRENCY_ID, mCurrencyId);
            values.put(SQLiteTable.Identity.PUBLIC_KEY, publicKey);
            values.put(SQLiteTable.Identity.UID, uid);
            values.put(SQLiteTable.Identity.WALLET_ID, walletId);
            Uri uri = insert(values);
            result = new Identity(mContext, Long.parseLong(uri.getLastPathSegment()));
        }

        return result;
    }

    @Override
    public Iterator<UcoinIdentity> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinIdentity> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Identity(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}