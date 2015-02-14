package io.ucoin.app.model;

import java.io.Serializable;

public class WotCertificationTime implements Serializable{

    private static final long serialVersionUID = -358639516878884523L;

    private int block = -1;
    
    private long medianTime = -1;

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public long getMedianTime() {
        return medianTime;
    }

    public void setMedianTime(long medianTime) {
        this.medianTime = medianTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof WotCertificationTime) {
            WotCertificationTime wt = (WotCertificationTime)o;
            return
                this.medianTime == wt.medianTime
                && this.block == wt.block ;
        }
        return false;
    }
}
