package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class Requirement implements Serializable{
    private Identity identity;
    private long identityId;
    private String publicKey;
    private String uid;
    private String selfBlockUid;
    private boolean outDistanced;
    private int numberCertification;
    private long membershipPendingExpiresIn;
    private long membershipExpiresIn;
    private long id;

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setSelfBlockUid(String selfBlockUid) {
        this.selfBlockUid = selfBlockUid;
    }

    public String getSelfBlockUid() {
        return selfBlockUid;
    }

    public void setOutDistanced(boolean outDistanced) {
        this.outDistanced = outDistanced;
    }

    public boolean isOutDistanced() {
        return outDistanced;
    }

    public void setNumberCertification(int numberCertification) {
        this.numberCertification = numberCertification;
    }

    public int getNumberCertification() {
        return numberCertification;
    }

    public void setMembershipPendingExpiresIn(long membershipPendingExpiresIn) {
        this.membershipPendingExpiresIn = membershipPendingExpiresIn;
    }

    public long getMembershipPendingExpiresIn() {
        return membershipPendingExpiresIn;
    }

    public void setMembershipExpiresIn(long membershipExpiresIn) {
        this.membershipExpiresIn = membershipExpiresIn;
    }

    public long getMembershipExpiresIn() {
        return membershipExpiresIn;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
        this.identityId = identity.getId();
        this.publicKey = identity.getPublicKey();
        this.uid = identity.getUid();
    }

    public long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(long identityId) {
        this.identityId = identityId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
