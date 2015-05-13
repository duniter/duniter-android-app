package io.ucoin.app.model.remote;

import java.util.List;

public class TxHistoryMovement {

    private String version;

    private List<String> issuers;

    private List<String> inputs;

    private List<String> outputs;

    private String comment;

    private List<String> signatures;

    private String hash;

    private long block_number;

    private long time;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getIssuers() {
        return issuers;
    }

    public void setIssuers(List<String> issuers) {
        this.issuers = issuers;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<String> signatures) {
        this.signatures = signatures;
    }

    public String getHash() {
        return hash;
    }

    public String getFingerprint() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @deprecated use getBlockNumber() instead
     * @return
     */
    @Deprecated
    public long getBlock_number() {
        return block_number;
    }

    /**
     * @deprecated use setBlockNumber() instead
     * @return
     */
    @Deprecated
    public void setBlock_number(long block_number) {
        this.block_number = block_number;
    }

    public long getBlockNumber() {
        return block_number;
    }

    public void setNumber(long block_number) {
        this.block_number = block_number;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
