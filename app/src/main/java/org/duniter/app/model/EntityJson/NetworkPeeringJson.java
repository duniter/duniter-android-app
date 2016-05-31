package org.duniter.app.model.EntityJson;

import android.util.Patterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkPeeringJson implements Serializable {
    public int version;
    public String currency;
    public String pubkey;
    public String signature;
    public String block;
    public Endpoint[] endpoints;

    public static NetworkPeeringJson fromJson(String response) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Endpoint.class, new EndpointAdapter())
                .create();

        return gson.fromJson(response, NetworkPeeringJson.class);
    }

    public static class Endpoint implements Serializable {
        public String protocol;
        public String url;
        public String ipv4;
        public String ipv6;
        public Integer port;
    }

    public static class EndpointAdapter extends TypeAdapter<Endpoint> {

        @Override
        public Endpoint read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            String ept = reader.nextString();
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(ept.split(" ")));
            Endpoint endpoint = new Endpoint();
            endpoint.port = Integer.parseInt(parts.remove(parts.size() - 1));
            for (String word : parts) {
                if (InetAddressUtils.isIPv4Address(word)) {
                    endpoint.ipv4 = word;
                } else if (InetAddressUtils.isIPv6Address(word)) {
                    endpoint.ipv6 = word;
                } else if (Patterns.WEB_URL.matcher(word).matches()) {
                    endpoint.url = word;
                } else {
                    try {
                        endpoint.protocol = "BASIC_MERKLED_API";
                    } catch (IllegalArgumentException e) {
                        endpoint.protocol = "UNDEFINED";
                    }
                }
            }

            return endpoint;
        }

        public void write(JsonWriter writer, Endpoint endpoint) throws IOException {
            if (endpoint == null) {
                writer.nullValue();
                return;
            }
            writer.value(endpoint.protocol + " " +
                    endpoint.url + " " +
                    endpoint.ipv4 + " " +
                    endpoint.ipv6 + " " +
                    endpoint.port);
        }
    }
}
