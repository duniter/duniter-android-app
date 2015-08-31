package io.ucoin.app.model.local;

import java.io.Serializable;

/**
 * A wallet's movement (DU or transfer)
 * @author
 */
public class Movement implements LocalEntity, Serializable {

    private Long id;
    private long walletId;
    private long amount;
    private Long time;
    private Integer blockNumber;
    private long dividend;
    private boolean isUD = false;
    private String fingerprint;
    private String comment;
    private String issuers;
    private String receivers;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public boolean isUD() {
        return isUD;
    }

    public void setUD(boolean isUD) {
        this.isUD = isUD;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isValidate() {
        return blockNumber != null;
    }

    public String getIssuers() {
        return issuers;
    }

    public void setIssuers(String issuers) {
        this.issuers = issuers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceivers() {
        return receivers;
    }

    public long getDividend() {
        return dividend;
    }

    public void setDividend(long dividend) {
        this.dividend = dividend;
    }
}
