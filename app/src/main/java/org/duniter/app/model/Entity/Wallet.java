package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 04/04/16.
 */
public class Wallet implements Serializable{
    private long syncBlock;
    private Identity identity;
    private Currency currency;
    private String publicKey;
    private String salt;
    private String alias;
    private String privateKey;
    private long id;
    private long currencyId;
    private long amount;
    private int base;
    private boolean haveIdentity = false;
    private long amountWithoutUd;
    private long amountTimeWithOblivion;
    private long amountTimeWithoutOblivion;

    public Wallet(){
        amount = 0;
    }

    public Wallet(long id) {
        this.id = id;
        amount = 0;
    }

    public long getSyncBlock() {
        return syncBlock;
    }

    public void setSyncBlock(long syncBlock) {
        this.syncBlock = syncBlock;
    }

    public boolean asIdentity() {
        return identity == null;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
        this.haveIdentity = true;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        this.currencyId = currency.getId();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
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

    public boolean getHaveIdentity() {
        return haveIdentity;
    }

    public void setHaveIdentity(boolean haveIdentity) {
        this.haveIdentity = haveIdentity;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(alias);
        return sb.toString();
    }

    public void setAmountWithoutUd(long amountWithoutUd) {
        this.amountWithoutUd = amountWithoutUd;
    }

    public long getAmountWithoutUd() {
        return amountWithoutUd;
    }

    public void setAmountTimeWithOblivion(long amountTimeWithOblivion) {
        this.amountTimeWithOblivion = amountTimeWithOblivion;
    }

    public long getAmountTimeWithOblivion() {
        return amountTimeWithOblivion;
    }

    public void setAmountTimeWithoutOblivion(long amountTimeWithoutOblivion) {
        this.amountTimeWithoutOblivion = amountTimeWithoutOblivion;
    }

    public long getAmountTimeWithoutOblivion() {
        return amountTimeWithoutOblivion;
    }
}
