package io.duniter.app.model.EntityWeb;

import android.content.Context;

import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class UdWeb extends Web{
    private Currency currency;

    public UdWeb(Context context, Currency currency) {
        super(context);
        this.currency = currency;
    }

    @Override
    public String getUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/blockchain/with/ud";
    }

    @Override
    public String postUrl() {
        return null;
    }
}
