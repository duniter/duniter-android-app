package io.ucoin.app.model;

public interface UcoinTxIssuer extends SqlRow {
    Long txId();

    String publicKey();

    Integer issuerOrder();
}

