package io.ucoin.app.model.ext;

import io.ucoin.app.model.Peer;
import io.ucoin.app.technical.StringUtils;

/**
 * Created by eis on 21/05/15.
 */
public class PeerWithCurrency extends Peer {

    private String currencyName;

    public PeerWithCurrency(String currencyName, String host, int port) {
        super(host, port);
        this.currencyName = currencyName;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public String toString() {

        return isEmpty()
                ? ""
                : String.format("%s (%s:%s)", this.currencyName, getHost(), getPort());
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(currencyName) && StringUtils.isBlank(getHost());
    }
}
