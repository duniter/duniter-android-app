package io.ucoin.app.model;

import java.io.Serializable;

/**
 * Blockwhain parameters.
 * 
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class BlockchainParameter implements Serializable{

    private String currency;
    
    /**
     * The %growth of the UD every [dt] period
     */
    private Double c;
    
    /**
     * Time period between two UD
     */
    private Integer dt;
    
    /**
     * UD(0), i.e. initial Universal Dividend
     */
    private Long ud0;
    
    /**
     * Minimum delay between 2 identical certifications (same pubkeys)
     */
    private Integer sigDelay;
    
    
    /**
     * Maximum age of a valid signature (in seconds) (e.g. 2629800)
     */
    private Integer sigValidity;
 
    /**
     * Minimum quantity of signatures to be part of the WoT (e.g. 3)
     */
    private Integer sigQty;

    /**
     * Minimum quantity of valid made certifications to be part of the WoT for distance rule
     */
    private Integer sigWoT;
    
    /**
     * Maximum age of a valid membership (in seconds)
     */
    private Integer msValidity;
    
    /**
     * Maximum distance between each WoT member and a newcomer
     */
    private Integer stepMax;
    

    /**
     * Number of blocks used for calculating median time.
     */
    private Integer medianTimeBlocks;

    /**
     * The average time for writing 1 block (wished time)
     */
    private Integer avgGenTime;

    /**
     * The number of blocks required to evaluate again PoWMin value
     */
    private Integer dtDiffEval;

    /**
     * The number of previous blocks to check for personalized difficulty
     */
    private Integer blocksRot;

    /**
     * The percent of previous issuers to reach for personalized difficulty
     */
    private Double percentRot;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("currency: ").append(currency).append("\n")
        .append("c: ").append(c).append("\n")
        .append("dt: ").append(dt).append("\n")
        .append("ud0: ").append(ud0).append("\n")
        .append("sigDelay: ").append(sigDelay);
        // TODO : display missing fields
        return sb.toString();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getC() {
        return c;
    }

    public void setC(Double c) {
        this.c = c;
    }

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Long getUd0() {
        return ud0;
    }

    public void setUd0(Long ud0) {
        this.ud0 = ud0;
    }

    public Integer getSigDelay() {
        return sigDelay;
    }

    public void setSigDelay(Integer sigDelay) {
        this.sigDelay = sigDelay;
    }

    public Integer getSigValidity() {
        return sigValidity;
    }

    public void setSigValidity(Integer sigValidity) {
        this.sigValidity = sigValidity;
    }

    public Integer getSigQty() {
        return sigQty;
    }

    public void setSigQty(Integer sigQty) {
        this.sigQty = sigQty;
    }

    public Integer getSigWoT() {
        return sigWoT;
    }

    public void setSigWoT(Integer sigWoT) {
        this.sigWoT = sigWoT;
    }

    public Integer getMsValidity() {
        return msValidity;
    }

    public void setMsValidity(Integer msValidity) {
        this.msValidity = msValidity;
    }

    public Integer getStepMax() {
        return stepMax;
    }

    public void setStepMax(Integer stepMax) {
        this.stepMax = stepMax;
    }

    public Integer getMedianTimeBlocks() {
        return medianTimeBlocks;
    }

    public void setMedianTimeBlocks(Integer medianTimeBlocks) {
        this.medianTimeBlocks = medianTimeBlocks;
    }

    public Integer getAvgGenTime() {
        return avgGenTime;
    }

    public void setAvgGenTime(Integer avgGenTime) {
        this.avgGenTime = avgGenTime;
    }

    public Integer getDtDiffEval() {
        return dtDiffEval;
    }

    public void setDtDiffEval(Integer dtDiffEval) {
        this.dtDiffEval = dtDiffEval;
    }

    public Integer getBlocksRot() {
        return blocksRot;
    }

    public void setBlocksRot(Integer blocksRot) {
        this.blocksRot = blocksRot;
    }

    public Double getPercentRot() {
        return percentRot;
    }

    public void setPercentRot(Double percentRot) {
        this.percentRot = percentRot;
    }
}
