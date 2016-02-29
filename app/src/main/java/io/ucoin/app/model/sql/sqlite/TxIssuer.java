package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxIssuer;
import io.ucoin.app.sqlite.SQLiteTable;

public class TxIssuer extends Row
        implements UcoinTxIssuer {

    public TxIssuer(Context context, Long IssuerId) {
        super(context, UcoinUris.TX_ISSUER_URI, IssuerId);
    }

    @Override
    public Long txId() {
        return getLong(SQLiteTable.TxIssuer.TX_ID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.TxIssuer.PUBLIC_KEY);
    }

    @Override
    public Integer issuerOrder() {
        return getInt(SQLiteTable.TxIssuer.ISSUER_ORDER);
    }

    @Override
    public String toString() {
        return "TxIssuer id=" + id() + "\n" +
                "tx_id=" + txId() + "\n" +
                "public_key=" + publicKey() + "\n" +
                "issuer_order=" + issuerOrder();
    }
}