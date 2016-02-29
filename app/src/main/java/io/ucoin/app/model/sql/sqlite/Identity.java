package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.model.UcoinCertifications;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinMembers;
import io.ucoin.app.model.UcoinMemberships;
import io.ucoin.app.model.UcoinSelfCertifications;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;

public class Identity extends Row
        implements UcoinIdentity{


    public Identity(Context context, Long identityId) {
        super(context, UcoinUris.IDENTITY_URI, identityId);
    }

    @Override
    public Long currencyId() {
        return getLong(SQLiteView.Identity.CURRENCY_ID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteView.Identity.PUBLIC_KEY);
    }

    @Override
    public String uid() {
        return getString(SQLiteView.Identity.UID);
    }

    @Override
    public Long sigDate() {
        return getLong(SQLiteView.Identity.SIG_DATE);
    }

    @Override
    public Long selfCount() {
        return getLong(SQLiteView.Identity.SELF_COUNT);
    }

    @Override
    public Boolean isMember() {
        return getBoolean(SQLiteView.Identity.IS_MEMBER);
    }

    @Override
    public Boolean wasMember() {
        return getBoolean(SQLiteView.Identity.WAS_MEMBER);
    }

    @Override
    public MembershipType lastMembership() {
        return MembershipType.valueOf(getString(SQLiteView.Identity.LAST_MEMBERSHIP));
    }

    @Override
    public Long expirationTime() {
        return getLong(SQLiteView.Identity.EXPIRATION_TIME);
    }

    @Override
    public Integer expirationYear() {
        return getInt(SQLiteView.Identity.EXPIRATION_YEAR);
    }

    @Override
    public Integer expirationMonth() {
        return getInt(SQLiteView.Identity.EXPIRATION_MONTH);
    }

    @Override
    public Integer expirationDay() {
        return getInt(SQLiteView.Identity.EXPIRATION_DAY);
    }

    @Override
    public DayOfWeek expirationDayOfWeek() {
        return DayOfWeek.valueOf(getString(SQLiteView.Identity.EXPIRATION_DAY_OF_WEEK));
    }

    @Override
    public String expirationHour() {
        return getString(SQLiteView.Identity.EXPIRATION_HOUR);
    }

    @Override
    public Long syncBlock() {
        return getLong(SQLiteView.Identity.SYNC_BLOCK);
    }

    @Override
    public void setSigDate(Long sigDate) {
        ContentValues values= new ContentValues();
        values.put(SQLiteTable.Identity.SIG_DATE, sigDate);
        update(values);
    }

    @Override
    public void setSyncBlock(Long block) {
        ContentValues values= new ContentValues();
        values.put(SQLiteTable.Identity.SYNC_BLOCK, block);
        update(values);
    }

    @Override
    public UcoinCurrency currency() {
        return new Currency(mContext, currencyId());
    }

    @Override
    public UcoinMemberships memberships() {
        return new Memberships(mContext, mId);
    }

    @Override
    public UcoinMembers members() {
        return new Members(mContext, mId);
    }

    @Override
    public UcoinCertifications certifications() {
        return new Certifications(mContext, mId);
    }

    @Override
    public UcoinSelfCertifications selfCertifications() {
        return new SelfCertifications(mContext, mId);
    }

    @Override
    public String toString() {
        String s = "\nIDENTITY id=" + ((id() == null) ? "not in database" : id()) + "\n";
        s += "\nsigDate=" + sigDate();
        s += "\ncurrencyId=" + currencyId();
        s += "\npublicKey=" + publicKey();
        s += "\nuid=" + uid();

        return s;
    }
}