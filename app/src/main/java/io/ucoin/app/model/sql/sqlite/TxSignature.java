package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxSignature;
import io.ucoin.app.sqlite.SQLiteTable;

public class TxSignature extends Row
        implements UcoinTxSignature {

    public TxSignature(Context context, Long signatureId) {
        super(context, UcoinUris.TX_SIGNATURE_URI, signatureId);
    }

    @Override
    public Long txId() {
        return getLong(SQLiteTable.TxSignature.TX_ID);
    }

    @Override
    public String value() {
        return getString(SQLiteTable.TxSignature.VALUE);
    }

    @Override
    public Integer issuerOrder() {
        return getInt(SQLiteTable.TxSignature.ISSUER_ORDER);
    }

    @Override
    public String toString() {
        return "TxSignature id=" + id() + "\n" +
                "tx_id=" + txId() + "\n" +
                "value=" + value() + "\n" +
                "issuer_order=" + issuerOrder();
    }
}