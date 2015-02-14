package io.ucoin.app.model;

import java.util.Map;

public class WotLookupSignature {

    public static final String META_KEY_TS = "timestamp";
    public static final String META_KEY_BLOCK_NUMBER = "block_number";

    private String pubkey;
    
    private Map<String, Integer> meta;
    
    private String signature;

    private boolean isMember;

    private boolean wasMember;

    private String[] uids;

    private String uid;

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public Map<String, Integer> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Integer> meta) {
        this.meta = meta;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }

    public boolean wasMember() {
        return wasMember;
    }

    public void setWasMember(boolean wasMember) {
        this.wasMember = wasMember;
    }

    public String[] getUids() {
        return uids;
    }

    public void setUids(String[] uids) {
        this.uids = uids;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
