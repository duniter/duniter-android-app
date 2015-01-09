package io.ucoin.app.model;

public class Identity extends BasicIdentity {

    private static final long serialVersionUID = -7451079677730158794L;

    private int timestamp = -1;

    /**
     * The timestamp value of the signature date
     * @return
     */
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

}
