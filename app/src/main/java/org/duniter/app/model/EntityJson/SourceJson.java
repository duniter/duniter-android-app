package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.enumeration.SourceState;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Wallet;


/**
 * Created by naivalf27 on 28/04/16.
 */
public class SourceJson implements Serializable {

    public String currency;
    public String pubkey;
    public Sources[] sources;

    public static SourceJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, SourceJson.class);
    }

    public static List<Source> fromSource(SourceJson sourceJson, Currency currency, Wallet wallet){
        List<Source> res = new ArrayList<>();
        for(Sources s : sourceJson.sources){
            Source source = new Source();
            source.setCurrency(currency);
            source.setWallet(wallet);
            source.setState(SourceState.VALID.name());
            source.setType(s.type);
            source.setNoffset(s.noffset);
            source.setIdentifier(s.identifier);
            source.setAmount(new BigInteger(String.valueOf(s.amount)));
            res.add(source);
        }
        return res;
    }

    public class Sources implements Serializable {
        public String type;
        public int noffset;
        public String identifier;
        public long amount;
    }
}
