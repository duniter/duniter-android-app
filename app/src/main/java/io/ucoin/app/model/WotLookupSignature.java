package io.ucoin.app.model;

import java.util.Map;

public class WotLookupSignature {

    private String pubkey;
    
    private Map<String, String> meta;
    
    private String signature;

    private String isMember;

    private String wasMember;

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String isMember() {
        return isMember;
    }

    public void setIsMember(String isMember) {
        this.isMember = isMember;
    }

    public String wasMember() {
        return wasMember;
    }

    public void setWasMember(String wasMember) {
        this.wasMember = wasMember;
    }
}
