package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinMember;
import io.ucoin.app.model.UcoinMembers;
import io.ucoin.app.model.http_api.WotCertification;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.sqlite.SQLiteTable;

public class Members extends Table
        implements UcoinMembers {

    private Long mCurrencyId;

    public Members(Context context, Long currencyId) {
        this(context, currencyId, SQLiteTable.Member.IDENTITY_ID + "=?", new String[]{currencyId.toString()});
    }

    private Members(Context context, Long currencyId, String selection, String[] selectionArgs) {
        this(context, currencyId, selection, selectionArgs, null);
    }

    private Members(Context context, Long currencyId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.MEMBER_URI, selection, selectionArgs, sortOrder);
        mCurrencyId = currencyId;
    }

    @Override
    public UcoinMember add(WotLookup.Result result) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Member.IDENTITY_ID, mCurrencyId);
        values.put(SQLiteTable.Member.UID, result.uids[0].uid);
        values.put(SQLiteTable.Member.PUBLIC_KEY, result.pubkey);
        values.put(SQLiteTable.Member.SELF, result.uids[0].self);
        values.put(SQLiteTable.Member.TIMESTAMP, result.uids[0].meta.timestamp);

        Uri uri = insert(values);
        return new Member(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinMember add(WotCertification.Certification certification) {

        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Member.IDENTITY_ID, mCurrencyId);
        values.put(SQLiteTable.Member.UID, certification.uid);
        values.put(SQLiteTable.Member.PUBLIC_KEY, certification.pubkey);

        Uri uri = insert(values);
        return new Member(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinMember getById(Long id) {
        return new Member(mContext, id);
    }

    @Override
    public UcoinMember getByPublicKey(String publicKey) {
        String selection = SQLiteTable.Member.IDENTITY_ID + "=? AND " + SQLiteTable.Member.PUBLIC_KEY + " LIKE ?";
        String[] selectionArgs = new String[]{mCurrencyId.toString(), publicKey};
        UcoinMembers members = new Members(mContext, mCurrencyId, selection, selectionArgs);
        if (members.iterator().hasNext()) {
            return members.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinMember getBySelf(String self) {
        String selection = SQLiteTable.Member.IDENTITY_ID + "=? AND " + SQLiteTable.Member.SELF + " LIKE ?";
        String[] selectionArgs = new String[]{mCurrencyId.toString(), self};
        UcoinMembers members = new Members(mContext, mCurrencyId, selection, selectionArgs);
        if (members.iterator().hasNext()) {
            return members.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Iterator<UcoinMember> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinMember> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Member(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}