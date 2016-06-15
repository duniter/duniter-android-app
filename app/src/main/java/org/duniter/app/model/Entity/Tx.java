package org.duniter.app.model.Entity;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by naivalf27 on 28/04/16.
 */
public class Tx implements Serializable{
    public long id;
    public Wallet wallet;
    public Currency currency;
    private long amount;
    private int base;
    private long amountTimeOrigin;
    private double amountRelatifOrigin;
    private String publicKey;
    private long time;
    private long blockNumber;
    private String comment;
    private boolean enc;
    private String hash;
    private long locktime;
    private String state;
    private String uid;
    private Boolean isUd;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public long getAmountTimeOrigin() {
        return amountTimeOrigin;
    }

    public void setAmountTimeOrigin(long amountTimeOrigin) {
        this.amountTimeOrigin = amountTimeOrigin;
    }

    public double getAmountRelatifOrigin() {
        return amountRelatifOrigin;
    }

    public void setAmountRelatifOrigin(double amountRelatifOrigin) {
        this.amountRelatifOrigin = amountRelatifOrigin;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isEnc() {
        return enc;
    }

    public void setEnc(boolean enc) {
        this.enc = enc;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLocktime() {
        return locktime;
    }

    public void setLocktime(long locktime) {
        this.locktime = locktime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getUd() {
        return isUd;
    }

    public void setUd(Boolean ud) {
        isUd = ud;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tx tx = (Tx) o;
        if (hash != null ? !hash.equals(tx.hash) : tx.hash != null) return false;
        return state != null ? state.equals(tx.state) : tx.state == null;
    }
}
