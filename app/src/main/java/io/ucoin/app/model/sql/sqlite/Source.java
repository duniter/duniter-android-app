package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import java.math.BigInteger;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.enumeration.SourceType;
import io.ucoin.app.model.UcoinSource;
import io.ucoin.app.sqlite.SQLiteTable;

public class Source extends Row
        implements UcoinSource {

    protected Source(Context context, Long sourceId) {
        super(context, UcoinUris.SOURCE_URI, sourceId);
    }

    @Override
    public Long walletId() {
        return getLong(SQLiteTable.Source.WALLET_ID);
    }

    @Override
    public Long number() {
        return getLong(SQLiteTable.Source.NUMBER);
    }
    @Override
    public SourceType type() {
        return SourceType.valueOf(getString(SQLiteTable.Source.TYPE));
    }

    @Override
    public String fingerprint() {
        return getString(SQLiteTable.Source.FINGERPRINT);
    }
    @Override
    public BigInteger amount() {
        return new BigInteger(getString(SQLiteTable.Source.AMOUNT));
    }

    @Override
    public SourceState state() {
        return SourceState.valueOf(getString(SQLiteTable.Source.STATE));
    }

    @Override
    public void setState(SourceState state) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Source.STATE, state.name());
        update(values);
    }

    @Override
    public String toString() {
        return "SOURCE id=" + ((id() == null) ? "not in database" : id()) + "\n" +
                "wallet_id=" + walletId() + "\n" +
                "number=" + number() + "\n" +
                "type=" + type().toString() + "\n" +
                "fingerprint=" + fingerprint() + "\n" +
                "amount=" + amount();
    }
}