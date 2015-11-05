package io.ucoin.app.model.remote;

import java.io.Serializable;

public class WotCertificationWritten implements Serializable{

    private long number = -1;
    
    private String hash = "";

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
