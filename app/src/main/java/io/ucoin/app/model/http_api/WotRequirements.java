package io.ucoin.app.model.http_api;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

public class WotRequirements implements Serializable {
    public Identitie[] identities;

    public static WotRequirements fromJson(InputStream json) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF_8"));
        return gson.fromJson(reader, WotRequirements.class);
    }

    public static WotRequirements fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, WotRequirements.class);
    }

    public String toString() {
        String s = "pubkey=" + identities[0].pubkey;
        s += "\nuid=" + identities[0].uid;
        for(Certification certification : identities[0].certifications) {
            s += "\n\tpubkey=" + certification.from;
            s += "\n\tto=" + certification.to;
            s += "\n\texpiresIn=" + certification.expiresIn;
        }
        return s;
    }

    public static class Certification implements Serializable {
        public String from;
        public String to;
        public long expiresIn;
    }

    public static class Identitie implements Serializable {
        public String pubkey;
        public String uid;
        public Meta meta;
        public boolean outdistanced;
        public Certification[] certifications;
        public long membershipPendingExpiresIn;
        public long membershipExpiresIn;
    }

    public class Meta implements Serializable {
        public Long timestamp;
    }
}