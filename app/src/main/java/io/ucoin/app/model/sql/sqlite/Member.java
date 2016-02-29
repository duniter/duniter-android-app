package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinMember;
import io.ucoin.app.sqlite.SQLiteTable;

public class Member extends Row
        implements UcoinMember {

    public Member(Context context, Long memberId) {
        super(context, UcoinUris.MEMBER_URI, memberId);
    }

    @Override
    public Long identityId() {
        return getLong(SQLiteTable.Member.IDENTITY_ID);
    }

    @Override
    public String uid() {
        return getString(SQLiteTable.Member.UID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.Member.PUBLIC_KEY);
    }

    @Override
    public String self() {
        return getString(SQLiteTable.Member.SELF);
    }

    @Override
    public Long timestamp() {
        return getLong(SQLiteTable.Member.TIMESTAMP);
    }

    @Override
    public void setSelf(String self) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Member.SELF, self);
        update(values);
    }

    @Override
    public void setTimestamp(Long timestamp) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Member.TIMESTAMP, timestamp);
        update(values);
    }

    @Override
    public UcoinIdentity identity() {
        return new Identity(mContext, identityId());
    }

    @Override
    public String toString() {
        String s = "MEMBER id=" + ((id() == null) ? "not in database" : id()) + "\n";
        s += "\nidentityId=" + identityId();
        s += "\nuid=" + uid();
        s += "\npublicKey=" + publicKey();
        s += "\nself=" + self();
        s += "\ntimestamp=" + timestamp();

        return s;
    }
}