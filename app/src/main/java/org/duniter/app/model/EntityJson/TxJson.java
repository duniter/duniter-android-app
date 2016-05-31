package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duniter.app.enumeration.TxState;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;


/**
 * Created by naivalf27 on 28/04/16.
 */
public class TxJson implements Serializable {

    public String currency;
    public String pubkey;
    public History history;

    public static TxJson fromJson(String response) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(History.Output.class, new History.OutputAdapter())
                .create();
        return gson.fromJson(response, TxJson.class);
    }

    public static List<String[]> fromTxGetSourcesPending(TxJson sourceJson){
        List<String[]> result = new ArrayList<>();
        for (History.Elem e : sourceJson.history.sending){
            for (String i : e.inputs){
                String type = i.substring(0,i.indexOf(":"));
                String iden = i.substring(i.indexOf(":")+1,i.lastIndexOf(":"));
                String num = i.substring(i.lastIndexOf(":"));
                result.add(new String[]{type,iden,num});
            }
        }
        return result;
    }

    public static List<Tx> fromTx(TxJson sourceJson, Wallet wallet,Map<String,String> mapMember){
        HashMap<String, Tx> txs = new HashMap<>();
        for (History.Elem e : sourceJson.history.sent){
            Tx tx = new Tx();
            BigInteger amount = BigInteger.ZERO;
            for (History.Output o : e.outputs){
                if (o.getPublicKey()!=null && !o.getPublicKey().equals(sourceJson.pubkey)){
                    tx.setPublicKey(o.getPublicKey());
                    amount = amount.subtract(new BigInteger(o.amount));
                }
            }
            if (tx.getPublicKey()==null){
                tx.setPublicKey(sourceJson.pubkey);
            }
            String uid = mapMember.containsKey(tx.getPublicKey()) ? mapMember.get(tx.getPublicKey()) : "";
            tx.setUid(uid);
            tx.setAmount(amount);
            tx.setCurrency(wallet.getCurrency());
            tx.setWallet(wallet);
            tx.setState(TxState.VALID.name());
            tx.setComment(e.comment);
            tx.setEnc(e.comment.length()>3 && e.comment.substring(0,3).equals("ENC"));
            tx.setTime(e.time);
            tx.setHash(e.hash);
            tx.setBlockNumber(e.block_number);
            tx.setLocktime(e.locktime);
            txs.put(e.hash,tx);
        }

        for (History.Elem e : sourceJson.history.received){
            if (!txs.containsKey(e.hash)){
                Tx tx = new Tx();
                BigInteger amount = BigInteger.ZERO;
                for (History.Output o : e.outputs) {
                    if (o.getPublicKey() != null){
                        if (o.getPublicKey().equals(sourceJson.pubkey)){
                            amount = amount.add(new BigInteger(o.amount));
                        }
                    }
                }
                tx.setPublicKey(e.issuers[0]);
                if (tx.getPublicKey()==null){
                    tx.setPublicKey(sourceJson.pubkey);
                }
                String uid = mapMember.containsKey(tx.getPublicKey()) ? mapMember.get(tx.getPublicKey()) : "";
                tx.setUid(uid);
                tx.setAmount(amount);
                tx.setCurrency(wallet.getCurrency());
                tx.setWallet(wallet);
                tx.setState(TxState.VALID.name());
                tx.setComment(e.comment);
                tx.setEnc(e.comment.length()>3 && e.comment.substring(0,3).equals("ENC"));
                tx.setTime(e.time);
                tx.setHash(e.hash);
                tx.setBlockNumber(e.block_number);
                tx.setLocktime(e.locktime);
                txs.put(e.hash,tx);
            }
        }

        for (History.Elem e : sourceJson.history.sending){
            Tx tx = new Tx();
            BigInteger amount = BigInteger.ZERO;
            for (History.Output o : e.outputs){
                if (o.getPublicKey()!=null && !o.getPublicKey().equals(sourceJson.pubkey)){
                    tx.setPublicKey(o.getPublicKey());
                    amount = amount.subtract(new BigInteger(o.amount));
                }
            }
            if (tx.getPublicKey()==null){
                tx.setPublicKey(sourceJson.pubkey);
            }
            String uid = mapMember.containsKey(tx.getPublicKey()) ? mapMember.get(tx.getPublicKey()) : "";
            tx.setUid(uid);
            tx.setAmount(amount);
            tx.setCurrency(wallet.getCurrency());
            tx.setWallet(wallet);
            tx.setState(TxState.PENDING.name());
            tx.setComment(e.comment);
            tx.setEnc(e.comment.length()>3 && e.comment.substring(0,3).equals("ENC"));
            tx.setTime(Long.valueOf("999999999999"));
            tx.setHash(e.hash);
            tx.setBlockNumber(0);
            tx.setLocktime(e.locktime);
            txs.put(e.hash,tx);
        }

        for (History.Elem e : sourceJson.history.pending){
            if (!txs.containsKey(e.hash)){
                Tx tx = new Tx();
                BigInteger amount = BigInteger.ZERO;
                for (History.Output o : e.outputs) {
                    if (o.getPublicKey() != null){
                        if (o.getPublicKey().equals(sourceJson.pubkey)){
                            amount = amount.add(new BigInteger(o.amount));
                        }
                    }
                }
                tx.setPublicKey(e.issuers[0]);
                if (tx.getPublicKey()==null){
                    tx.setPublicKey(sourceJson.pubkey);
                }
                String uid = mapMember.containsKey(tx.getPublicKey()) ? mapMember.get(tx.getPublicKey()) : "";
                tx.setUid(uid);
                tx.setAmount(amount);
                tx.setCurrency(wallet.getCurrency());
                tx.setWallet(wallet);
                tx.setState(TxState.PENDING.name());
                tx.setComment(e.comment);
                tx.setEnc(e.comment.length()>3 && e.comment.substring(0,3).equals("ENC"));
                tx.setTime(Long.valueOf("999999999999"));
                tx.setHash(e.hash);
                tx.setBlockNumber(0);
                tx.setLocktime(e.locktime);
                txs.put(e.hash,tx);
            }
        }

        return new ArrayList<Tx>(txs.values());
    }

    public static class History{
        public Elem sent[];
        public Elem received[];
        public Elem sending[];
        public Elem receiving[];
        public Elem pending[];

        public static class Elem{
            public int version;
            public String[] issuers;
            public String[] inputs;
            public Output[] outputs;
            public String comment;
            public Long locktime;
            public String[] signature;
            public String hash;
            public Long block_number;
            public Long time;
            public Long received;
        }

        public static class Output{
            public String amount;
            public Long base;
            public String conditions;

            public String getPublicKey(){
                String def = conditions.split(" ")[0].substring(0,3);
                String publicKey = null;
                switch (def){
                    case "SIG":
                        publicKey = conditions.split(" ")[0].substring(4,conditions.indexOf(")"));
                        break;
                    case "XHX":
                        break;
                }
                return publicKey;
            }
        }

        public static class OutputAdapter extends TypeAdapter<Output> {

            @Override
            public Output read(JsonReader reader) throws IOException {
                if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }

                String opt = reader.nextString();

                ArrayList<String> parts = new ArrayList<>(Arrays.asList(opt.split(":")));
                Output output = new Output();
                output.amount = parts.get(0);
                output.base = Long.getLong(parts.get(1),0);
                output.conditions = parts.get(2);
                return output;
            }

            @Override
            public void write(JsonWriter writer, Output output) throws IOException {
                if (output == null) {
                    writer.nullValue();
                    return;
                }
                writer.value(output.amount + ":" +
                        output.base + ":" +
                        output.conditions);
            }
        }
    }
}
