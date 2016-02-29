package io.ucoin.app.model;

public interface UcoinContact extends SqlRow {
    Long currencyId();

    String name();

    String uid();

    String publicKey();
}