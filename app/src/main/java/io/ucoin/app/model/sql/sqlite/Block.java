package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import java.math.BigInteger;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.sqlite.SQLiteTable;

public class Block extends Row
        implements UcoinBlock {

    public Block(Context context, Long blockId) {
        super(context, UcoinUris.BLOCK_URI, blockId);
    }

    @Override
    public Long currencyId() {
        return getLong(SQLiteTable.Block.CURRENCY_ID);
    }

    @Override
    public Integer version() {
        return getInt(SQLiteTable.Block.VERSION);
    }

    @Override
    public Long nonce() {
        return getLong(SQLiteTable.Block.NONCE);
    }

    @Override
    public BigInteger monetaryMass() {
        return new BigInteger(getString(SQLiteTable.Block.MONETARY_MASS));
    }

    @Override
    public String issuer() {
        return getString(SQLiteTable.Block.ISSUER);
    }

    @Override
    public String previousHash() {
        return getString(SQLiteTable.Block.PREVIOUS_HASH);
    }

    @Override
    public String previousIssuer() {
        return getString(SQLiteTable.Block.PREVIOUS_ISSUER);
    }

    @Override
    public String signature() {
        return getString(SQLiteTable.Block.SIGNATURE);
    }

    @Override
    public void setIsMembership(Boolean isMembership) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Block.IS_MEMBERSHIP, isMembership.toString());
        update(values);
    }

    @Override
    public boolean remove() {
        if(!isMembership() && dividend() == null) {
            delete();
            return true;
        }
        return false;
    }

    @Override
    public UcoinCurrency currency() {
        return new Currency(mContext, currencyId());
    }

    @Override
    public Long membersCount() {
        return getLong(SQLiteTable.Block.MEMBERS_COUNT);
    }

    @Override
    public Boolean isMembership() {
        return getBoolean(SQLiteTable.Block.IS_MEMBERSHIP);
    }

    @Override
    public String hash() {
        return getString(SQLiteTable.Block.HASH);
    }

    @Override
    public Long number() {
        return getLong(SQLiteTable.Block.NUMBER);
    }

    @Override
    public Long powMin() {
        return getLong(SQLiteTable.Block.POWMIN);
    }

    @Override
    public Long time() {
        return getLong(SQLiteTable.Block.TIME);
    }
    @Override

    public Long medianTime() {
        return getLong(SQLiteTable.Block.MEDIAN_TIME);
    }

    @Override
    public BigInteger dividend() {
        return new BigInteger(getString(SQLiteTable.Block.DIVIDEND));
    }

    @Override
    public String toString() {
        return "BLOCK id=" + ((id() == null) ? "not in database" : id()) + "\n" +
                "currency_id=" + currencyId() + "\n" +
                "number=" + number() + "\n" +
                "monetary_mass=" + monetaryMass() +
                "signature=" + signature();
    }
}