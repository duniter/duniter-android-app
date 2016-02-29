package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import java.math.BigInteger;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxOutput;
import io.ucoin.app.sqlite.SQLiteTable;

public class TxOutput extends Row
        implements UcoinTxOutput {

    public TxOutput(Context context, Long outputId) {
        super(context, UcoinUris.TX_OUTPUT_URI, outputId);
    }

    @Override
    public Long txId() {
        return getLong(SQLiteTable.TxOutput.TX_ID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.TxOutput.PUBLIC_KEY);
    }

    @Override
    public BigInteger amount() {
        return new BigInteger(getString(SQLiteTable.TxOutput.AMOUNT));
    }


    @Override
    public String toString() {
        return "TxOutput id=" + id() + "\n" +
                "tx_id=" + txId() + "\n" +
                "public_key=" + publicKey() + "\n" +
                "amount=" + amount();
    }
}