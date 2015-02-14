package io.ucoin.app.model;

import java.util.List;
import java.util.Map;

public class WotLookupUId {

    private String uid;
    
    private Map<String, String> meta;
    
    private String self;

    private List<WotLookupSignature> others;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public List<WotLookupSignature> getOthers() {
        return others;
    }

    public void setOthers(List<WotLookupSignature> others) {
        this.others = others;
    }
}
