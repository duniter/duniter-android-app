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
import io.ucoin.app.model.UcoinRequirement;
import io.ucoin.app.model.UcoinRequirements;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.technical.crypto.AddressFormatException;

final public class Requirements extends Table
        implements UcoinRequirements {

    long mIdentity;

    public Requirements(Context context, Long identityId) {
        this(context, SQLiteTable.Requirement.IDENTITY_ID + "=?",new String[]{identityId.toString()});
        mIdentity = identityId;
    }

    private Requirements(Context context, String selection, String[] selectionArgs) {
        this(context, selection, selectionArgs, null);
    }

    private Requirements(Context context, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.REQUIREMENT_URI, selection, selectionArgs, sortOrder);
    }

    @Override
    public UcoinRequirement getById(Long id) {
        return new Requirement(mContext, id);
    }

    @Override
    public UcoinRequirement add(long currencyId, long identityId, String publicKey, long expiresIn){
            ContentValues values = new ContentValues();
            values.put(SQLiteTable.Requirement.CURRENCY_ID, currencyId);
            values.put(SQLiteTable.Requirement.IDENTITY_ID, identityId);
            values.put(SQLiteTable.Requirement.PUBLIC_KEY, publicKey);
            values.put(SQLiteTable.Requirement.EXPIRES_IN, expiresIn);
            Uri uri = insert(values);

        return new Requirement(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinRequirements add(long currencyId, WotRequirements wotRequirements) {
        remove();
        for(WotRequirements.Certification c : wotRequirements.identities[0].certifications){
            add(currencyId, mIdentity, c.from, c.expiresIn);
        }
        return new Requirements(mContext,mIdentity);
    }

    @Override
    public void remove() {
        ArrayList<UcoinRequirement> list = list();
        if(list!=null) {
            for (UcoinRequirement requirement : list) {
                requirement.delete();
            }
        }
    }

    public ArrayList<UcoinRequirement> list() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinRequirement> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Requirement(mContext, id));
            }
            cursor.close();

            return data;
        }
        return null;
    }

    @Override
    public Iterator<UcoinRequirement> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinRequirement> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Requirement(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}