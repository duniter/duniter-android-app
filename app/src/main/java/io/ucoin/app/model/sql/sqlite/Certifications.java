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

    public UcoinCertification add(WotCertification.Certification certification, CertificationType type){
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Certification.IDENTITY_ID, mIdentityId);
        values.put(SQLiteTable.Certification.UID, certification.uid);
        values.put(SQLiteTable.Certification.PUBLIC_KEY, certification.pubkey);
        values.put(SQLiteTable.Certification.IS_MEMBER, certification.isMember.toString());
        values.put(SQLiteTable.Certification.WAS_MEMBER, certification.wasMember.toString());
        values.put(SQLiteTable.Certification.SIG_DATE, certification.sigDate);
        if(certification.written != null) {
            values.put(SQLiteTable.Certification.NUMBER, certification.written.number);
            values.put(SQLiteTable.Certification.HASH, certification.written.hash);
        }
        values.put(SQLiteTable.Certification.TYPE, type.name());
        values.put(SQLiteTable.Certification.BLOCK, certification.cert_time.block);
        values.put(SQLiteTable.Certification.MEDIAN_TIME, certification.cert_time.medianTime);
        values.put(SQLiteTable.Certification.SIGNATURE, certification.signature);
        values.put(SQLiteTable.Certification.STATE, CertificationState.WRITTEN.name());

        Uri uri = insert(values);
        return new Certification(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinCertifications add(Long currencyId, WotCertification wotCertification, CertificationType type) {
        remove(type);
        for(WotCertification.Certification certification : wotCertification.certifications){
            add(certification,type);
        }
        return new Certifications(mContext,mIdentityId);
    }

    public void remove(CertificationType type) {
        ArrayList<UcoinCertification> list = list();
        if(list!=null) {
            for (UcoinCertification certification : list) {
                if(certification.type().name().equals(type.name())) {
                    certification.delete();
                }
            }
        }
    }

    public ArrayList<UcoinCertification> list() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinCertification> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Certification(mContext, id));
            }
            cursor.close();

            return data;
        }
        return null;
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