package io.ucoin.app.model.http_api;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

import io.ucoin.app.enumeration.SourceType;


public class TxSources implements Serializable {

    public String currency;
    public String  pubkey;
    public Source[] sources;

    public static TxSources fromJson(InputStream json) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, TxSources.class);
    }

    public static TxSources fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, TxSources.class);
    }

    public String toString() {
        String s = "\ncurency=" + currency;
        s += "\npubkey=" + pubkey;
        s += "\nsources=";
        for (Source source : sources) {
            s += "\nsource=" + source;
        }

        return s;
    }

    public class Source implements Serializable {
        public SourceType type;
        public Integer number;
        public String fingerprint;
        public String amount;

        @Override
        public String toString() {
            String s = "\ntype=" + type.name();
            s += "\nnumber=" + number;
            s += "\nfingerprint=" + fingerprint;
            s += "\namount=" + amount;

            return s;
        }
    }
}
