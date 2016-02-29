package io.ucoin.app.model.http_api;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

import io.ucoin.app.enumeration.MembershipType;

public class BlockchainMemberships implements Serializable {

    public String pubkey;
    public String uid;
    public Long sigDate;
    public Membership[] memberships;

    public static BlockchainMemberships fromJson(InputStream json) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, BlockchainMemberships.class);
    }
    public static BlockchainMemberships fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, BlockchainMemberships.class);
    }

    public String toString() {
        String s = "pubkey=" + pubkey;
        s += "\nuid=" + uid;
        s += "\nsigDate=" + sigDate;

        for (Membership membership : memberships) {
            s += "\n\tversion=" + membership.version;
            s += "\n\tcurrency=" + membership.currency;
            s += "\n\ttype=" + membership.membership;
            s += "\n\tblockNumber=" + membership.blockNumber;
            s += "\n\tblockHash=" + membership.blockHash;
        }

        return s;
    }

    public static class Membership {
        public Integer version;
        public String currency;
        public MembershipType membership;
        public Long blockNumber;
        public String blockHash;
    }
}

