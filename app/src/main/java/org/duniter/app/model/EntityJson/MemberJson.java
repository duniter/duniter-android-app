package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naivalf27 on 04/04/16.
 */
public final class MemberJson {
    public Result[] results;

    public static Map<String,String> fromJson(String response){
        Gson gson = new Gson();
        MemberJson memberJson = gson.fromJson(response, MemberJson.class);

        Map<String,String> map = new HashMap<>();
        for (Result result:memberJson.results){
            map.put(result.pubkey,result.uid);
        }
        return map;
    }

    public class Result{
        public String pubkey;
        public String uid;
    }

}
