package io.ucoin.app.model.remote;

public class TxHistoryResults {

    private String currency;

    private String pubkey;

    private TxHistory history;

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

    public TxHistory getHistory() {
        return history;
    }

    public void setHistory(TxHistory history) {
        this.history = history;
    }
}
