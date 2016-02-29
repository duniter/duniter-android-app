package io.ucoin.app.model;

import java.io.Serializable;

import io.ucoin.app.model.http_api.WotRequirements;

/**
 * Created by naivalf27 on 05/01/16.
 */
public class IdentityContact implements Serializable{
    private boolean isContact;
    private String name;
    private String publicKey;
    private String currencyName;
    private String uid;
    private Long currencyId;
    private WotRequirements requirements;

    public IdentityContact(boolean isContact, String name, String uid, String publicKey, String currencyName, Long currencyId) {
        this.isContact = isContact;
        this.name = name;
        this.uid = uid;
        this.publicKey = publicKey;
        this.currencyName = currencyName;
        this.currencyId = currencyId;
    }

    @Override
    public boolean equals(Object o) {
        return this.uid.equals(((IdentityContact) o).uid) &&
                this.publicKey.equals(((IdentityContact) o).publicKey) &&
                this.currencyName.equals(((IdentityContact) o).currencyName);
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getCurrency() {
        return currencyName;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public String getUid() {
        return uid;
    }

    public WotRequirements getRequirements() {
        return requirements;
    }

    public void setRequirements(WotRequirements requirements) {
        this.requirements = requirements;
    }

    public boolean isContact() {
        return isContact;
    }

    public boolean filter(String query, boolean findByPubKey){
        boolean result;
        if(!findByPubKey){
            result = (uid.substring(0,query.length()).equals(query)) || (uid.substring(0,query.length()).equals(query.toLowerCase()));
        }else{
            result = publicKey.contains(query);
        }
        return result;
    }

    @Override
    public String toString() {
        String result;
        if(isContact()){
            result = getName().concat(" (").concat(getUid()).concat(")");
        }else{
            result = getUid();
        }

        return result;
    }
}