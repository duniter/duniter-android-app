package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class Contact implements Serializable {
    private Currency currency;
    private String alias;
    private String uid;
    private String publicKey;
    private long id;
    private String currencyName;
    private boolean contact;
    private String timestamp;
    private String signature;
    private Boolean revoke;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        this.currencyName = currency.getName();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public boolean isContact() {
        return contact;
    }

    public void setContact(boolean contact) {
        this.contact = contact;
    }

    public boolean filter(String query, boolean findByPubKey){
        boolean result;
        if(!findByPubKey){
            result = (uid.toLowerCase().substring(0,query.length()).equals(query)) || (uid.toLowerCase().substring(0,query.length()).equals(query));
        }else{
            result = publicKey.toLowerCase().contains(query);
        }
        return result;
    }

    public boolean certify() {
        return !revoke &&
                uid!=null && !uid.equals("") &&
                timestamp!=null && !timestamp.equals("") &&
                signature!=null && !signature.equals("");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;

        if (!uid.equals(contact.uid)) return false;
        return publicKey.equals(contact.publicKey);

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setRevoke(Boolean revoke) {
        this.revoke = revoke;
    }

    public Boolean getRevoke() {
        return revoke;
    }
}
