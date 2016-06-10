package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.duniter.app.enumeration.TxState;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by naivalf27 on 28/04/16.
 */
public class UdJson implements Serializable {

    public String currency;
    public String pubkey;
    public History history;

    public static List<Tx> fromTx(String json, Wallet wallet){
        Gson gson = new Gson();
        UdJson udJson =  gson.fromJson(json, UdJson.class);

        List<Tx> result = new ArrayList<>();
        for (History.Elem e : udJson.history.history){
            Tx tx = new Tx();
            tx.setPublicKey("");
            tx.setUid("");
            tx.setAmount(new BigInteger(e.amount));
            tx.setDividend(new BigInteger(e.amount));
            tx.setCurrency(wallet.getCurrency());
            tx.setWallet(wallet);
            tx.setState(TxState.VALID.name());
            tx.setComment("");
            tx.setEnc(false);
            tx.setTime(e.time);
            tx.setBlockNumber(e.block_number);
            tx.setUd(true);
            result.add(tx);
        }

        return result;
    }

    public static class History{
        public Elem history[];

        public static class Elem{
            public long block_number;
            public boolean consumed;
            public long time;
            public String amount;
            public int base;
        }
    }
}
