package io.ucoin.app.model.remote;

import java.io.Serializable;

public class TxSource implements Serializable, Cloneable {

    public static final String SOURCE_TYPE_UD = "D";
    public static final String SOURCE_TYPE_TRASNACTION = "T";

	private static final long serialVersionUID = 8084087351543574142L;

	private String type;	
	private long number;
	private String fingerprint;	
    private long amount;

    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	
    	TxSource clone = (TxSource)super.clone();
    	clone.type = type;
    	clone.number = number;
    	clone.fingerprint = fingerprint;
    	clone.amount = amount;
    	return clone;
    }
    
    /**
	 * Source type : <ul>
	 * <li><code>D</code> : Universal Dividend</li>
	 * <li><code>T</code> : Transaction</li>
	 * </ul>
	 * @return
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * The block number where the source has been written
	 * @return
	 */
	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

    public String toString() {
        return new StringBuilder()
                .append("type=").append(type)
                .append(",number=").append(number)
                .append(",fingerprint=").append(fingerprint)
                .append(",amount=").append(amount)
                .toString();
    }
}
