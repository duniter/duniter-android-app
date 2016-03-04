package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import java.math.BigInteger;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;
import io.ucoin.app.model.UcoinTx;
import io.ucoin.app.model.UcoinTxInputs;
import io.ucoin.app.model.UcoinTxIssuers;
import io.ucoin.app.model.UcoinTxOutputs;
import io.ucoin.app.model.UcoinTxSignatures;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;

public class Tx extends Row
        implements UcoinTx {

    public Tx(Context context, Long txId) {
        super(context, UcoinUris.TX_URI, txId);
    }

    @Override
    public Long walletId() {
        return getLong(SQLiteView.Tx.WALLET_ID);
    }

    @Override
    public Integer version() {
        return getInt(SQLiteView.Tx.VERSION);
    }

    @Override
    public String comment() {
        return getString(SQLiteView.Tx.COMMENT);
    }

    @Override
    public String hash() {
        return getString(SQLiteView.Tx.HASH);
    }

    @Override
    public Long block() {
        return getLong(SQLiteView.Tx.BLOCK);
    }

    @Override
    public Long time() {
        return getLong(SQLiteView.Tx.TIME);
    }

    @Override
    public TxDirection direction() {
        return TxDirection.valueOf(getString(SQLiteView.Tx.DIRECTION));
    }

    @Override
    public TxState state() {
        return TxState.valueOf(getString(SQLiteView.Tx.STATE));
    }

    @Override
    public String currencyName() {
        return getString(SQLiteView.Tx.CURRENCY_NAME);
    }

    @Override
    public BigInteger amount() {
        return new BigInteger(getString(SQLiteView.Tx.AMOUNT));
    }

    @Override
    public void setComment(String comment) {

    }

    @Override
    public void setState(TxState state) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.STATE, state.name());
        update(values);
    }

    @Override
    public void setHash(String hash) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.HASH, hash);
        update(values);
    }

    @Override
    public void setTime(Long time) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.TIME, time);
        update(values);
    }

    @Override
    public void setBlock(Long block) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.BLOCK, block);
        update(values);
    }

    @Override
    public void setDirection(TxDirection direction) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.DIRECTION, direction.name());
        update(values);
    }

    @Override
    public UcoinTxIssuers issuers() {
        return new TxIssuers(mContext, mId);
    }

    @Override
    public UcoinTxInputs inputs() {
        return new TxInputs(mContext, mId);
    }

    @Override
    public UcoinTxOutputs outputs() {
        return new TxOutputs(mContext, mId);
    }

    @Override
    public UcoinTxSignatures signatures() {
        return new TxSignatures(mContext, mId);
    }

    @Override
    public UcoinWallet wallet() {
        return new Wallet(mContext, walletId());
    }

    @Override
    public String toString() {
        return "Tx id=" + id() + "\n" +
                "wallet_id=" + walletId() + "\n" +
                "comment=" + comment() + "\n" +
                "hash=" + hash() + "\n" +
                "block=" + block() + "\n" +
                "time=" + time();
    }
}