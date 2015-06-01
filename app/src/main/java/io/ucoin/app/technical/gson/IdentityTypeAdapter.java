package io.ucoin.app.technical.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.technical.StringUtils;

public class IdentityTypeAdapter implements JsonDeserializer<Identity>{

    @Override
    public Identity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String identityStr = json.getAsString();
        if (StringUtils.isBlank(identityStr)) {
            return null;
        }
        
        String[] identityParts = identityStr.split(":");
        
        Identity result = new Identity();
        int i = 0;
        
        result.setPubkey(identityParts[i++]);
        result.setSignature(identityParts[i++]);
        result.setTimestamp(Integer.parseInt(identityParts[i++]));
        result.setUid(identityParts[i++]);

        return result;
    }
}
