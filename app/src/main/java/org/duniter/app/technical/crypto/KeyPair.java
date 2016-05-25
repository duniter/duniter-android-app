package org.duniter.app.technical.crypto;

/**
 * Created by eis on 28/01/15.
 */
public class KeyPair {

    public byte[] publicKey;
    public byte[] secretKey;

    public KeyPair(byte[] publicKey, byte[] secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }

    public byte[] getSecKey() {
        return secretKey;
    }

    public void setSecKey(byte[] secKey) {
        this.secretKey = secKey;
    }

    public byte[] getPubKey() {
        return publicKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.publicKey = pubKey;
    }
}
