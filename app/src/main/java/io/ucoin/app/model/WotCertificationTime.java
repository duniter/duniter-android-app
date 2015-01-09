package io.ucoin.app.model;

import java.io.Serializable;

public class WotCertificationTime implements Serializable{

    private static final long serialVersionUID = -358639516878884523L;

    private int block = -1;
    
    private int medianTime = -1;

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getMedianTime() {
        return medianTime;
    }

    public void setMedianTime(int medianTime) {
        this.medianTime = medianTime;
    }
    
}
