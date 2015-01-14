package io.ucoin.app.model;

import io.ucoin.app.technical.crypto.CryptoUtils;

/**
 * A wallet is a user account
 * Created by eis on 13/01/15.
 */
public class Wallet {

    private byte[] pubKey;
    private byte[] secKey;
    private Identity identity;

    public Wallet() {
        super();
        this.identity = new Identity();
    }

    public Wallet(byte[] pubKey, byte[] secKey) {
        super();
        this.pubKey = pubKey;
        this.secKey = secKey;
        this.identity = new Identity();
        this.identity.setPubkey(CryptoUtils.encodeBase58(pubKey));
    }


    public Wallet(byte[] secKey, Identity identity) {
        super();
        this.secKey = secKey;
        this.pubKey = CryptoUtils.decodeBase58(identity.getPubkey());
        this.identity = identity;
    }

    public byte[] getSecKey() {
        return secKey;
    }

    public void setSecKey(byte[] secKey) {
        this.secKey = secKey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}
