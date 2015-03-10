package io.ucoin.app.model;

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
    private Long blockNumber;
    private boolean isUD;
    private String fingerprint;

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

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
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
}
