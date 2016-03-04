package io.ucoin.app.model;

import java.math.BigInteger;

import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;

public interface UcoinTx extends SqlRow {
    Long walletId();

    Integer version();

    String comment();

    String hash();

    Long block();

    Long time();

    TxDirection direction();

    TxState state();

    String currencyName();

    BigInteger amount();

    void setComment(String comment);

    void setState(TxState state);

    void setHash(String hash);

    void setTime(Long time);

    void setBlock(Long block);

    void setDirection(TxDirection direction);

    UcoinTxIssuers issuers();

    UcoinTxInputs inputs();

    UcoinTxOutputs outputs();

    UcoinTxSignatures signatures();

    UcoinWallet wallet();
}

