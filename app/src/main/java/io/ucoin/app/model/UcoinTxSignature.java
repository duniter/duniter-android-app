package io.ucoin.app.model;

public interface UcoinTxSignature extends SqlRow {
    Long txId();

    String value();

    Integer issuerOrder();
}

