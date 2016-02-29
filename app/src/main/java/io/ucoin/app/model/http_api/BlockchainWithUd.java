package io.ucoin.app.model.http_api;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

public class BlockchainWithUd implements Serializable {

    public Result result = new Result();

    public static BlockchainWithUd fromJson(InputStream json) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, BlockchainWithUd.class);
    }

    public static BlockchainWithUd fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, BlockchainWithUd.class);
    }

    public String toString() {
        String s = "";
        for (Long block : result.blocks) {
            s += "block=" + block + "\n";
        }
        return s;
    }


    public class Result implements Serializable {
        public Long[] blocks = new Long[]{};
    }
}
