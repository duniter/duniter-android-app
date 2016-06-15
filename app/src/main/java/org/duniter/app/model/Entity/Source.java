package org.duniter.app.model.Entity;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by naivalf27 on 04/04/16.
 */
public class Source implements Serializable{
    private long id;
    private Currency currency;
    private Wallet wallet;
    private String type;
    private int noffset;
    private String identifier;
    private long amount;
    private String state;
    private int base;

    public Source(long id) {
        this.id =id;
    }

    public Source(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setNoffset(int noffset) {
        this.noffset = noffset;
    }

    public int getNoffset() {
        return noffset;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        if (noffset != source.noffset) return false;
        if (amount != source.amount) return false;
        if (base != source.base) return false;
        if (type != null ? !type.equals(source.type) : source.type != null) return false;
        return identifier != null ? identifier.equals(source.identifier) : source.identifier == null;

    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getBase() {
        return base;
    }
}