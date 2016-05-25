package org.duniter.app.model.Entity;

import java.io.Serializable;


/**
 * Created by naivalf27 on 04/04/16.
 */
public class Currency implements Serializable{

    private BlockUd currentBlock;
    private BlockUd lastUdBlock;
    private String name;
    private Long id;
    private Float c;
    private Float xpercent;
    private Float percentRot;
    private Long dt;
    private Long ud0;
    private Long sigPeriod;
    private Long sigStock;
    private Long sigWindow;
    private Long sigValidity;
    private Long sigQty;
    private Long idtyWindow;
    private Long msWindow;
    private Long msValidity;
    private Long stepMax;
    private Long medianTimeBlocks;
    private Long avgGenTime;
    private Long dtDiffEval;
    private Long blocksRot;

    public Currency(long id){
        this.id= id;
    }

    public Currency(){}

    public BlockUd getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(BlockUd currentBlock) {
        this.currentBlock = currentBlock;
    }

    public BlockUd getLastUdBlock() {
        return lastUdBlock;
    }

    public void setLastUdBlock(BlockUd lastUdBlock) {
        this.lastUdBlock = lastUdBlock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getC() {
        return c;
    }

    public void setC(Float c) {
        this.c = c;
    }

    public Float getXpercent() {
        return xpercent;
    }

    public void setXpercent(Float xpercent) {
        this.xpercent = xpercent;
    }

    public Float getPercentRot() {
        return percentRot;
    }

    public void setPercentRot(Float percentRot) {
        this.percentRot = percentRot;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    public Long getUd0() {
        return ud0;
    }

    public void setUd0(Long ud0) {
        this.ud0 = ud0;
    }

    public Long getSigPeriod() {
        return sigPeriod;
    }

    public void setSigPeriod(Long sigPeriod) {
        this.sigPeriod = sigPeriod;
    }

    public Long getSigStock() {
        return sigStock;
    }

    public void setSigStock(Long sigStock) {
        this.sigStock = sigStock;
    }

    public Long getSigWindow() {
        return sigWindow;
    }

    public void setSigWindow(Long sigWindow) {
        this.sigWindow = sigWindow;
    }

    public Long getSigValidity() {
        return sigValidity;
    }

    public void setSigValidity(Long sigValidity) {
        this.sigValidity = sigValidity;
    }

    public Long getSigQty() {
        return sigQty;
    }

    public void setSigQty(Long sigQty) {
        this.sigQty = sigQty;
    }

    public Long getIdtyWindow() {
        return idtyWindow;
    }

    public void setIdtyWindow(Long idtyWindow) {
        this.idtyWindow = idtyWindow;
    }

    public Long getMsWindow() {
        return msWindow;
    }

    public void setMsWindow(Long msWindow) {
        this.msWindow = msWindow;
    }

    public Long getMsValidity() {
        return msValidity;
    }

    public void setMsValidity(Long msValidity) {
        this.msValidity = msValidity;
    }

    public Long getStepMax() {
        return stepMax;
    }

    public void setStepMax(Long stepMax) {
        this.stepMax = stepMax;
    }

    public Long getMedianTimeBlocks() {
        return medianTimeBlocks;
    }

    public void setMedianTimeBlocks(Long medianTimeBlocks) {
        this.medianTimeBlocks = medianTimeBlocks;
    }

    public Long getAvgGenTime() {
        return avgGenTime;
    }

    public void setAvgGenTime(Long avgGenTime) {
        this.avgGenTime = avgGenTime;
    }

    public Long getDtDiffEval() {
        return dtDiffEval;
    }

    public void setDtDiffEval(Long dtDiffEval) {
        this.dtDiffEval = dtDiffEval;
    }

    public Long getBlocksRot() {
        return blocksRot;
    }

    public void setBlocksRot(Long blocksRot) {
        this.blocksRot = blocksRot;
    }
}
