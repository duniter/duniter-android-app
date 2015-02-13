package io.ucoin.app.model;

import io.ucoin.app.technical.DateUtils;

public class Identity extends BasicIdentity {

    private static final long serialVersionUID = -7451079677730158794L;

    private long timestamp = -1;

    private Boolean isMember = null;

    /**
     * The timestamp value of the signature date
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    public Boolean getIsMember() {
        return isMember;
    }

    public void setMember(Boolean isMember) {
        this.isMember = isMember;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString())
                .append(timestamp == -1 ? "" : ",timestamp=")
                .append(timestamp == -1 ? "" : DateUtils.format(getTimestamp()))
                .append(isMember == null ? "" : ",isMember=")
                .append(isMember == null ? "" : isMember.toString())
                .toString();
    }

    public void copy(Identity identity) {
        super.copy(identity);
        this.timestamp = identity.timestamp;
        this.isMember = identity.isMember;
    }
}
