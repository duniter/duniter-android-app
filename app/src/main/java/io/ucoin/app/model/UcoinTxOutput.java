package io.ucoin.app.model;

import java.math.BigInteger;

public interface UcoinTxOutput extends SqlRow {
    Long txId();

    String publicKey();

    BigInteger amount();
}

