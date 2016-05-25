package io.duniter.app.model.EntityWeb;

import android.content.Context;

import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class RequirementWeb extends Web {
    private final Currency currency;
    private final String publicKey;

    public RequirementWeb(Context context, Currency currency, String publicKey) {
        super(context);
        this.currency = currency;
        this.publicKey = publicKey;
    }

    @Override
    public String getUrl() {
        return "http://" + WebService.getServeur(context,currency) + "/wot/requirements/" + publicKey;
    }

    @Override
    public String postUrl() {
        return null;
    }
}
