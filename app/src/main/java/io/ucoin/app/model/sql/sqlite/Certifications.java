package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.CertificationState;
import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.model.UcoinCertification;
import io.ucoin.app.model.UcoinCertifications;
import io.ucoin.app.model.UcoinMember;
import io.ucoin.app.model.http_api.WotCertification;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Certifications extends Table
        implements UcoinCertifications {

    private Long mIdentityId;

    public Certifications(Context context, Long identityId) {
        this(context, identityId, SQLiteTable.Certification.IDENTITY_ID + "=?", new String[]{identityId.toString()});
    }

    private Certifications(Context context, Long identityId, String selection, String[] selectionArgs) {
        this(context, identityId, selection, selectionArgs, null);
    }

    private Certifications(Context context, Long identityId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.CERTIFICATION_URI, selection, selectionArgs, sortOrder);
        mIdentityId = identityId;
    }

    @Override
    public UcoinCertification add(UcoinMember member, CertificationType type, WotCertification.Certification certification) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Certification.IDENTITY_ID, mIdentityId);
        values.put(SQLiteTable.Certification.MEMBER_ID, member.id());
        values.put(SQLiteTable.Certification.TYPE, type.name());
        values.put(SQLiteTable.Certification.BLOCK, certification.cert_time.block);
        values.put(SQLiteTable.Certification.MEDIAN_TIME, certification.cert_time.medianTime);
        values.put(SQLiteTable.Certification.SIGNATURE, certification.signature);
        values.put(SQLiteTable.Certification.STATE, CertificationState.WRITTEN.name());

        Uri uri = insert(values);
        return new Certification(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinCertification getById(Long id) {
        return new Certification(mContext, id);
    }

    @Override
    public UcoinCertification getBySignature(String signature) {
        String selection = SQLiteTable.Certification.SIGNATURE + "=?";
        String[] selectionArgs = new String[]{signature};

        UcoinCertifications certifications = new Certifications(mContext, mIdentityId, selection, selectionArgs, null);
        if (certifications.iterator().hasNext()) {
            return certifications.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinCertifications getByType(CertificationType type) {
        String selection = SQLiteTable.Certification.IDENTITY_ID + "=? AND " +
                SQLiteTable.Certification.TYPE + "=?";
        String[] selectionArgs = new String[]{mIdentityId.toString(), type.name()};
        return new Certifications(mContext, mIdentityId, selection, selectionArgs);
    }

    @Override
    public Iterator<UcoinCertification> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinCertification> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Certification(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}