package io.ucoin.app.model;

import java.math.BigInteger;

import io.ucoin.app.enumeration.SourceType;

public interface UcoinTxInput extends SqlRow {
    Long txId();

    Integer index();

    SourceType type();

    Long number();

    String fingerprint();

    BigInteger amount();
}

