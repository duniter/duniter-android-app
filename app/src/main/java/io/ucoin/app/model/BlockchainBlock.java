package io.ucoin.app.model;

import java.io.Serializable;
import java.util.List;

/**
 * A block from the blockchain.
 *
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class BlockchainBlock implements Serializable {

    private static final long serialVersionUID = -5598140972293452669L;

    private String version;
    private Integer nonce;
    private Integer number;
    private Integer date;
    private Integer confirmedDate;
    private Integer membersCount;
    private String currency;
    private String issuer;
    private String signature;
    private String hash;
    private String parameters;
    private String previousHash;
    private String previousIssuer;
    private Integer dividend;
    //private int memberChanges;
    private List<Identity> identities;
    private List<Member> joiners;
    //            private int actives": [],
//            private int leavers": [],
//            private int excluded": [],
//            private int certifications": [],
//            private int transactions": [],
//            private int raw": "Version: 1\nType: Block\nCurrency: zeta_brouzouf\nNonce: 8233\nNumber: 1\nDate: 1416589860\nConfirmedDate: 1416589860\nIssuer: HnFcSms8jzwngtVomTTnzudZx7SHUQY8sVE1y8yBmULk\nPreviousHash: 00006CD96A01378465318E48310118AC6B2F3625\nPreviousIssuer: HnFcSms8jzwngtVomTTnzudZx7SHUQY8sVE1y8yBmULk\nMembersCount: 4\nIdentities:\nJoiners:\nActives:\nLeavers:\nExcluded:\nCertifications:\nTransactions:\n"

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public Integer getNonce() {
        return nonce;
    }
    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public Integer getDate() {
        return date;
    }
    public void setDate(Integer date) {
        this.date = date;
    }
    public Integer getConfirmedDate() {
        return confirmedDate;
    }
    public void setConfirmedDate(Integer confirmedDate) {
        this.confirmedDate = confirmedDate;
    }
    public Integer getMembersCount() {
        return membersCount;
    }
    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public String getParameters() {
        return parameters;
    }
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    public String getPreviousHash() {
        return previousHash;
    }
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    public String getPreviousIssuer() {
        return previousIssuer;
    }
    public void setPreviousIssuer(String previousIssuer) {
        this.previousIssuer = previousIssuer;
    }
    public Integer getDividend() {
        return dividend;
    }
    public void setDividend(Integer dividend) {
        this.dividend = dividend;
    }
    public List<Identity> getIdentities() {
        return identities;
    }
    public void setIdentities(List<Identity> identities) {
        this.identities = identities;
    }
    public List<Member> getJoiners() {
        return joiners;
    }
    public void setJoiners(List<Member> joiners) {
        this.joiners = joiners;
    }
}
