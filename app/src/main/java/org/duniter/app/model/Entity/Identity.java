package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 04/04/16.
 */
public class Identity implements Serializable{
    private Currency currency;
    private Wallet wallet;
    private String publicKey;
    private String uid;
    private String selfBlockUid;
    private long walletId;
    private long currencyId;
    private long id;
    private long sigDate;
    private long syncBlock;

    public Identity(long id) {
        this.id = id;
    }
    public Identity(){}

    public long getSyncBlock() {
        return syncBlock;
    }

    public void setSyncBlock(long numberBlock) {
        this.syncBlock = numberBlock;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
        this.walletId = wallet.getId();
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        this.currencyId = currency.getId();

    }

    public String getSelfBlockUid() {
        return selfBlockUid;
    }

    public void setSelfBlockUid(String selfBlockUid) {
        this.selfBlockUid = selfBlockUid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSigDate() {
        return sigDate;
    }

    public void setSigDate(long sigDate) {
        this.sigDate = sigDate;
    }
}
