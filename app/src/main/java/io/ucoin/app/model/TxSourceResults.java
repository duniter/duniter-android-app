package io.ucoin.app.model;

import java.util.List;

public class TxSourceResults {

	private String currency;
	
	private String pubkey;
	    
    private List<TxSource> sources;
    
    private double balance;

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPubkey() {
		return pubkey;
	}

	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
	}

	public List<TxSource> getSources() {
		return sources;
	}

	public void setSources(List<TxSource> sources) {
		this.sources = sources;
	}
    
    

}
