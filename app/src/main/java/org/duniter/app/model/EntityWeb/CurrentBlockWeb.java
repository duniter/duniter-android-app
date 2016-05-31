package org.duniter.app.model.EntityWeb;

import android.content.Context;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class CurrentBlockWeb extends Web{
    private Currency currency;

    public CurrentBlockWeb(Context context, Currency currency) {
        super(context);
        this.currency = currency;
    }

    @Override
    public String getUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/blockchain/current/";
    }

    @Override
    public String postUrl() {
        return null;
    }
}
