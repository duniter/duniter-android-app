package io.ucoin.app.model;

import java.math.BigInteger;

public interface UcoinUd extends SqlRow {
    Long walletId();

    Long block();

    Boolean consumed();

    Long time();

    BigInteger quantitativeAmount();

    String currencyName();

    UcoinWallet wallet();
}

