package io.ucoin.app.model;

import java.math.BigInteger;

public interface UcoinCurrency extends SqlRow {

    String name();

    Float c();

    Integer dt();

    Integer ud0();

    Integer sigDelay();

    Integer sigValidity();

    Integer sigQty();

    Integer sigWoT();

    Integer msValidity();

    Integer stepMax();

    Integer medianTimeBlocks();

    Integer avgGenTime();

    Integer dtDiffEval();

    Integer blocksRot();

    Float percentRot();

    Long membersCount();

    BigInteger monetaryMass();

    UcoinIdentity identity();

    UcoinBlocks blocks();

    UcoinWallets wallets();

    UcoinPeers peers();

    UcoinContacts contacts();

    UcoinIdentity addIdentity(String uid, String publicKey);

    //UcoinIdentity addIdentity(String uid, String publicKey) throws AddressFormatException;
}
