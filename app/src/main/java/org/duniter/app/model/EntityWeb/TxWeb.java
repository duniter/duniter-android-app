package org.duniter.app.model.EntityWeb;

import android.content.Context;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class TxWeb extends Web{
    private Currency currency;
    private String publicKey;

    public TxWeb(Context context, Currency currency, String publicKey) {
        super(context);
        this.currency = currency;
        this.publicKey = publicKey;
    }

    public TxWeb(Context context, Currency currency) {
        super(context);
        this.currency = currency;
    }


    @Override
    public String getUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/tx/history/" + publicKey;
    }

    @Override
    public String postUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/tx/process";
    }
}
