package io.ucoin.app.model.remote;

import java.io.Serializable;
import java.util.List;

public class WotLookupResult implements Serializable{

    private static final long serialVersionUID = -39452685440482106L;

    private String pubkey;
    
    private List<WotLookupUId> uids;

    private List<WotLookupSignature> signed;

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public List<WotLookupUId> getUids() {
        return uids;
    }

    public void setUids(List<WotLookupUId> uids) {
        this.uids = uids;
    }

    public List<WotLookupSignature> getSigned() {
        return signed;
    }

    public void setSigned(List<WotLookupSignature> signed) {
        this.signed = signed;
    }
}
