package io.ucoin.app.model;

import java.math.BigInteger;

import io.ucoin.app.technical.crypto.AddressFormatException;

public interface UcoinWallet extends SqlRow {
    Long currencyId();

    String salt();

    String publicKey();

    String privateKey();

    String alias();

    BigInteger amount();

    BigInteger udValue();

    Long syncBlock();

    UcoinSources sources();

    UcoinTxs txs();

    UcoinUds uds();

    UcoinCurrency currency();

    UcoinIdentity identity();

    UcoinIdentity addIdentity(String uid, String publicKey) throws AddressFormatException;

    void setSyncBlock(Long number);

    void setAmount(String amount);

    void substractAmount(String amount);
}