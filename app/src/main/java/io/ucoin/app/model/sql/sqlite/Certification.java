package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.CertificationState;
import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.model.UcoinCertification;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinMember;
import io.ucoin.app.sqlite.SQLiteTable;

public class Certification extends Row
        implements UcoinCertification {

    public Certification(Context context, Long certificationId) {
        super(context, UcoinUris.CERTIFICATION_URI, certificationId);
    }

    @Override
    public Long identityId() {
        return getLong(SQLiteTable.Certification.IDENTITY_ID);
    }

    @Override
    public String uid() {
        return getString(SQLiteTable.Certification.UID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.Certification.PUBLIC_KEY);
    }

    @Override
    public Boolean isMember() {
        return getString(SQLiteTable.Certification.IS_MEMBER).equals("true");
    }

    @Override
    public Boolean wasMember() {
        return getString(SQLiteTable.Certification.WAS_MEMBER).equals("true");
    }

    @Override
    public Long block() {
        return getLong(SQLiteTable.Certification.BLOCK);
    }

    @Override
    public Long medianTime() {
        return getLong(SQLiteTable.Certification.MEDIAN_TIME);
    }

    @Override
    public Long sigDate() {
        return getLong(SQLiteTable.Certification.SIG_DATE);
    }

    @Override
    public String signature() {
        return getString(SQLiteTable.Certification.SIGNATURE);
    }

    @Override
    public Long number() {
        return getLong(SQLiteTable.Certification.NUMBER);
    }

    @Override
    public String hash() {
        return getString(SQLiteTable.Certification.HASH);
    }

    @Override
    public CertificationState state() {
        return CertificationState.valueOf(getString(SQLiteTable.Certification.STATE));
    }

    @Override
    public void setState(CertificationState state) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Certification.STATE, state.name());
        update(values);
    }


    @Override
    public UcoinIdentity identity() {
        return new Identity(mContext, identityId());
    }

    @Override
    public CertificationType type() {
        return CertificationType.valueOf(getString(SQLiteTable.Certification.TYPE));
    }

    @Override
    public String toString() {
        String s = "\nCERTIFICATION id=" + ((id() == null) ? "not in database" : id()) + "\n";
        s += "\nidentityId=" + identityId();
        s += "\ntype=" + type().name();
        s += "\nblock=" + block();
        s += "\nmedianTime=" + medianTime();
        s += "\nsignature=" + signature();

        return s;
    }
}