package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.math.BigInteger;

import org.duniter.app.model.Entity.BlockUd;

/**
 * Created by naivalf27 on 04/04/16.
 */
public final class BlockJson {
    public long version;
    public long nonce;
    public long number;
    public long powMin;
    public long time;
    public long medianTime;
    public long membersCount;
    public long monetaryMass;
    public long unitbase;
    public String currency;
    public String issuer;
    public String signature;
    public String hash;
    public String parameters;
    public String previousHash;
    public String previousIssuer;
    public String inner_hash;
    public long dividend;
//    public String[] identities;
//    public String[] joiners;
//    public String[] actives;
//    public String[] leavers;
//    public String[] revoked;
//    public String[] excluded;
//    public String[] certifications;
//    public String[] transactions;
//    public String raw;

    public static BlockJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, BlockJson.class);
    }

    public static BlockUd fromBlock(BlockJson block){
        BlockUd res = new BlockUd();

        res.setNumber(block.number);
        res.setMedianTime(block.medianTime);
        res.setMembersCount(block.membersCount);
        res.setMonetaryMass(new BigInteger(String.valueOf(block.monetaryMass)));
        res.setHash(block.hash);
        res.setPowMin(block.powMin);
        res.setDividend(new BigInteger(String.valueOf(block.dividend)));

        return res;
    }

}
