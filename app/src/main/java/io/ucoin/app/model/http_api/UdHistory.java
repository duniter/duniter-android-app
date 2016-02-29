package io.ucoin.app.model.http_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

public class UdHistory implements Serializable {
    public String currency;
    public String pubkey;
    public History history;

    public static UdHistory fromJson(InputStream json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TxHistory.Tx.Input.class, new TxHistory.Tx.InputAdapter())
                .registerTypeAdapter(TxHistory.Tx.Output.class, new TxHistory.Tx.OutputAdapter())
                .create();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, UdHistory.class);
    }


    public static UdHistory fromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TxHistory.Tx.Input.class, new TxHistory.Tx.InputAdapter())
                .registerTypeAdapter(TxHistory.Tx.Output.class, new TxHistory.Tx.OutputAdapter())
                .create();
        return gson.fromJson(json, UdHistory.class);
    }

    public class History implements Serializable {
        public Ud[] history;
    }

    public class Ud implements Serializable {
        public Long block_number;
        public Boolean consumed;
        public Long time;
        public String amount;
    }
}
