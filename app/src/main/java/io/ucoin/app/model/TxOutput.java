package io.ucoin.app.model;

import java.io.Serializable;

public class TxOutput implements Serializable {

	private static final long serialVersionUID = 8084087351543574142L;

	private String pubKey;	
    
	private long amount;

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}    
}
