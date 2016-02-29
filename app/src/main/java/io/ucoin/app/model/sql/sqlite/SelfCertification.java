package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.SelfCertificationState;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.sqlite.SQLiteTable;

public class SelfCertification extends Row
        implements UcoinSelfCertification {

    public SelfCertification(Context context, Long certificationId) {
        super(context, UcoinUris.SELF_CERTIFICATION_URI, certificationId);
    }

    @Override
    public Long identityId() {
        return getLong(SQLiteTable.Certification.IDENTITY_ID);
    }

    @Override
    public Long timestamp() {
        return getLong(SQLiteTable.SelfCertification.TIMESTAMP);
    }

    @Override
    public String self() {
        return getString(SQLiteTable.SelfCertification.SELF);
    }

    @Override
    public SelfCertificationState state() {
        return SelfCertificationState.valueOf(getString(SQLiteTable.SelfCertification.STATE));
    }

    @Override
    public void setState(SelfCertificationState state) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.SelfCertification.STATE, state.name());
        update(values);
    }

    @Override
    public UcoinIdentity identity() {
        return new Identity(mContext, identityId());
    }

    @Override
    public String toString() {
        String s = "SELF_CERTIFICATION id=" + id() + "\n" +
                "identity_id=" +  identityId() + "\n" +
                "timestamp=" + timestamp() + "\n" +
                "self=" + self();
        return s;
    }
}