package io.ucoin.app.model.http_api;

import android.util.Patterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import io.ucoin.app.enumeration.EndpointProtocol;


public class NetworkPeering implements Serializable {

    public String currency;
    public String pubkey;
    public String signature;
    public String block;
    public Endpoint[] endpoints;


    public static NetworkPeering fromJson(InputStream json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Endpoint.class, new EndpointAdapter())
                .create();

        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, NetworkPeering.class);
    }

    public static NetworkPeering fromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Endpoint.class, new EndpointAdapter())
                .create();

        return gson.fromJson(json, NetworkPeering.class);
    }

    public String toString() {
        String s = "currency=" + currency + "\n" +
                "pubkey=" + pubkey + "\n" +
                "signature=" + signature + "\n" +
                "block=" + block + "\n";
                for(Endpoint endpoint : endpoints) {
                    s += endpoint.toString() + "\n";
                }
        return s;

    }

    public static class Endpoint implements Serializable {
        public EndpointProtocol protocol;
        public String url;
        public String ipv4;
        public String ipv6;
        public Integer port;

        @Override
        public String toString() {
            String s = "protocol=" + protocol.name() + "\n" +
                    "url=" + url + "\n" +
                    "ipv4=" + ipv4 + "\n" +
                    "ipv6=" + ipv6 + "\n" +
                    "port=" + port + "\n";
            return s;
        }
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
                        endpoint.protocol = EndpointProtocol.valueOf(word);
                    } catch (IllegalArgumentException e) {
                        endpoint.protocol = EndpointProtocol.UNDEFINED;
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
            writer.value(endpoint.protocol.name() + " " +
                    endpoint.url + " " +
                    endpoint.ipv4 + " " +
                    endpoint.ipv6 + " " +
                    endpoint.port);
        }
    }
}
