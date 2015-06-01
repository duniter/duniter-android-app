package io.ucoin.app.technical.gson;


import com.google.gson.GsonBuilder;

import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.Member;

public class GsonUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    public static GsonBuilder newBuilder() {
        return new GsonBuilder()
                // make sure date will be serialized
                .setDateFormat(DATE_PATTERN)
                // Register identity adapter
                .registerTypeAdapter(Identity.class, new IdentityTypeAdapter())
                .registerTypeAdapter(Member.class, new MemberTypeAdapter())
                ;
    }
}
