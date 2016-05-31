package org.duniter.app.model.EntityWeb;

import android.content.Context;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class BlockWeb extends Web{
    private Currency currency;
    private int number;

    public BlockWeb(Context context, Currency currency, int number) {
        super(context);
        this.currency = currency;
        this.number = number;
    }

    @Override
    public String getUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/blockchain/block/"+number;
    }

    @Override
    public String postUrl() {
        return null;
    }
}
