package io.ucoin.app.model.remote;

import java.util.List;

public class TxSourceResults {

	private String currency;
	
	private String pubkey;
	    
    private List<TxSource> sources;
    
    private Long credit;

	public Long getCredit() {
		return credit;
	}

	public void setCredit(Long credit) {
		this.credit = credit;
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
