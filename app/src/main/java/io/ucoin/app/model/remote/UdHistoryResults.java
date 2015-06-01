package io.ucoin.app.model.remote;

public class UdHistoryResults {

    private String currency;

    private String pubkey;

    private UdHistory history;

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

    public UdHistory getHistory() {
        return history;
    }

    public void setHistory(UdHistory history) {
        this.history = history;
    }
}
