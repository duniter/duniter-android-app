package io.ucoin.app.model;

public interface UcoinPeer extends SqlRow {
    Long currencyId();

    String publicKey();

    String signature();

    UcoinEndpoints endpoints();
}
