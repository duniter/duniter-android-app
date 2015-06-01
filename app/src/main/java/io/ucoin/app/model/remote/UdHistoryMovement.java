package io.ucoin.app.model.remote;

public class UdHistoryMovement {

    private int block_number;

    private boolean consumed;

    private long time;

    private long amount;

    /**
     * @deprecated use getBlockNumber() instead
     * @return
     */
    @Deprecated
    public int getBlock_number() {
        return block_number;
    }

    /**
     * @deprecated use setBlockNumber() instead
     * @return
     */
    @Deprecated
    public void setBlock_number(int block_number) {
        this.block_number = block_number;
    }

    public int getBlockNumber() {
        return block_number;
    }

    public void setBlockNumber(int block_number) {
        this.block_number = block_number;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
