package io.ucoin.app.model.remote;

import java.util.Map;

public class WotRequirements {

    private String uid;
    
    private Map<String, Long> meta;
    
    private String outdistanced;

    private int certifications;

    private boolean membershipMissing;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Long> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Long> meta) {
        this.meta = meta;
    }

    public String getOutdistanced() {
        return outdistanced;
    }

    public void setOutdistanced(String outdistanced) {
        this.outdistanced = outdistanced;
    }

    public int getCertifications() {
        return certifications;
    }

    public void setCertifications(int certifications) {
        this.certifications = certifications;
    }

    public boolean isMembershipMissing() {
        return membershipMissing;
    }

    public void setMembershipMissing(boolean membershipMissing) {
        this.membershipMissing = membershipMissing;
    }
}
