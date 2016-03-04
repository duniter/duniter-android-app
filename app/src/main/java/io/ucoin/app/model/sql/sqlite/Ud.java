package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import java.math.BigInteger;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinUd;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.sqlite.SQLiteView;

public class Ud extends Row
        implements UcoinUd {

    public Ud(Context context, Long udId) {
        super(context, UcoinUris.UD_URI, udId);
    }

    @Override
    public Long walletId() {
        return getLong(SQLiteView.Ud.WALLET_ID);
    }

    @Override
    public Long block() {
        return getLong(SQLiteView.Ud.BLOCK);
    }

    @Override
    public Boolean consumed() {
        return getBoolean(SQLiteView.Ud.CONSUMED);
    }
    @Override
    public Long time() {
        return getLong(SQLiteView.Ud.TIME);
    }

    @Override
    public String currencyName() {
        return getString(SQLiteView.Ud.CURRENCY_NAME);
    }

    @Override
    public BigInteger quantitativeAmount() {
        return new BigInteger(getString(SQLiteView.Ud.QUANTITATIVE_AMOUNT));
    }

    @Override
    public UcoinWallet wallet() {
        return new Wallet(mContext, walletId());
    }

    @Override
    public String toString() {
        return "Ud id=" + id() + "\n" +
                "wallet_id=" + walletId() + "\n" +
                "block=" + block() + "\n" +
                "consumed=" + consumed() + "\n" +
                "time=" + time() + "\n" +
                "amount=" + quantitativeAmount();
    }
}