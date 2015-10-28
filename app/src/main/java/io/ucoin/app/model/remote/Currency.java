package io.ucoin.app.model.remote;

import java.io.Serializable;

import io.ucoin.app.model.local.Account;
import io.ucoin.app.model.local.Peer;

/**
 * Created by eis on 05/02/15.
 */
public class Currency implements Serializable {

    private Peer peers[];

    private Long id;
    private String currencyName;
    private Integer membersCount;
    private String firstBlockSignature;
    private Account account;
    private Long accountId;
    private Long lastUD;
    private BlockchainParameters parameters;

    public Currency() {
    }

    public Currency(String currencyName,
                    String firstBlockSignature,
                    int membersCount,
                    Peer[] peers,
                    BlockchainParameters parameters) {
        this.currencyName = currencyName;
        this.firstBlockSignature = firstBlockSignature;
        this.membersCount = membersCount;
        this.peers = peers;
        this.parameters = parameters;
    }

    public Long getId() {
        return id;
    }

    public String getCurrencyName()
    {
        return currencyName;
    }

    public Integer getMembersCount() {
        return membersCount;
    }

    public String getFirstBlockSignature() {
        return firstBlockSignature;
    }

    public Peer[] getPeers() {
        return peers;
    }

    public void setPeers(Peer[] peers) {
        this.peers = peers;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }

    public void setFirstBlockSignature(String firstBlockSignature) {
        this.firstBlockSignature = firstBlockSignature;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getLastUD() {
        return lastUD;
    }

    public void setLastUD(Long lastUD) {
        this.lastUD = lastUD;
    }

    public BlockchainParameters getParameters() {
        return parameters;
    }

    public void setParameters(BlockchainParameters parameters) {
        this.parameters = parameters;
    }

    public String toString() {
        return currencyName;
    }
}