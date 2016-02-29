package io.ucoin.app.model;

import java.math.BigInteger;

public interface UcoinBlock extends SqlRow {
    Long currencyId();

    Integer version();

    Long nonce();

    Long number();

    Long powMin();

    Long time();

    Long medianTime();

    BigInteger dividend();

    BigInteger monetaryMass();

    String issuer();

    String previousHash();

    String previousIssuer();

    Long membersCount();

    Boolean isMembership();

    String hash();

    String signature();

    void setIsMembership(Boolean isMembership);

    boolean remove();

    UcoinCurrency currency();
}