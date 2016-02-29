package io.ucoin.app.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.Month;

public interface UcoinUd extends SqlRow {
    Long walletId();

    Long block();

    Boolean consumed();

    Long time();

    BigInteger quantitativeAmount();

    BigDecimal relativeAmountThen();

    BigDecimal relativeAmountNow();

    String currencyName();

    Integer year();

    Month month();

    DayOfWeek dayOfWeek();

    Integer day();

    String hour();

    UcoinWallet wallet();
}

