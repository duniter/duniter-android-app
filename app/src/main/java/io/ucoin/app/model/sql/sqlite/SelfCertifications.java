package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.SelfCertificationState;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.model.UcoinSelfCertifications;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.sqlite.SQLiteTable;

final public class SelfCertifications extends Table
        implements UcoinSelfCertifications {

    private Long mIdentityId;

    public SelfCertifications(Context context, Long identityId) {
        this(context, identityId, SQLiteTable.Certification.IDENTITY_ID + "=?", new String[]{identityId.toString()});
    }

    private SelfCertifications(Context context, Long identityId, String selection, String[] selectionArgs) {
        this(context, identityId, selection, selectionArgs, null);
    }

    private SelfCertifications(Context context, Long identityId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.SELF_CERTIFICATION_URI, selection, selectionArgs, sortOrder);
        mIdentityId = identityId;
    }

    @Override
    public UcoinSelfCertification add(WotLookup.Uid certification) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.SelfCertification.IDENTITY_ID, mIdentityId);
        values.put(SQLiteTable.SelfCertification.SELF, certification.self);
        values.put(SQLiteTable.SelfCertification.TIMESTAMP, certification.meta.timestamp);
        values.put(SQLiteTable.SelfCertification.STATE, SelfCertificationState.WRITTEN.name());

        Uri uri = insert(values);
        return new SelfCertification(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinSelfCertification getById(Long id) {
        return new SelfCertification(mContext, id);
    }

    @Override
    public UcoinSelfCertification getBySelf(String self) {
        String selection = SQLiteTable.SelfCertification.IDENTITY_ID + "=? AND " +
                SQLiteTable.SelfCertification.SELF + "=?";
        String[] selectionArgs = new String[]{mIdentityId.toString(), self};

        UcoinSelfCertifications selfCertifications = new SelfCertifications(mContext, mIdentityId, selection, selectionArgs);
        if (selfCertifications.iterator().hasNext()) {
            return selfCertifications.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinSelfCertifications getByState(SelfCertificationState state) {
        String selection = SQLiteTable.SelfCertification.IDENTITY_ID + "=? AND " +
                SQLiteTable.SelfCertification.STATE + "=?";
        String[] selectionArgs = new String[]{mIdentityId.toString(), state.name()};
        return new SelfCertifications(mContext, mIdentityId, selection, selectionArgs);
    }

    @Override
    public Iterator<UcoinSelfCertification> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinSelfCertification> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new SelfCertification(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}