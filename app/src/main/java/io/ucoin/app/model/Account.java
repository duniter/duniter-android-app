package io.ucoin.app.model;

/**
 * Created by eis on 07/02/15.
 */
public class Account implements LocalEntity {

    private Long id;
    private String uid;
    private String pubkey;
    private String salt;
    private String cryptPin;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCryptPin() {
        return cryptPin;
    }

    public void setCryptPin(String cryptPin) {
        this.cryptPin = cryptPin;
    }
}
