package org.duniter.app.model.Entity;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class BlockUd implements Serializable{

    private Currency currency;
    private long currencyId;
    private long number;
    private long id;
    private long medianTime;
    private long membersCount;
    private BigInteger monetaryMass;
    private String hash;
    private BigInteger dividend;
    private long powMin;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        this.currencyId = currency.getId();
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
    }


    public void setNumber(long number) {
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setMedianTime(long medianTime) {
        this.medianTime = medianTime;
    }

    public long getMedianTime() {
        return medianTime;
    }

    public void setMembersCount(long membersCount) {
        this.membersCount = membersCount;
    }

    public long getMembersCount() {
        return membersCount;
    }

    public void setMonetaryMass(BigInteger monetaryMass) {
        this.monetaryMass = monetaryMass;
    }

    public BigInteger getMonetaryMass() {
        return monetaryMass;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setDividend(BigInteger dividend) {
        this.dividend = dividend;
    }

    public BigInteger getDividend() {
        return dividend;
    }

    public String getUid() {
        return number + "-" + hash;
    }

    public void setPowMin(long powMin) {
        this.powMin = powMin;
    }

    public long getPowMin() {
        return powMin;
    }
}
