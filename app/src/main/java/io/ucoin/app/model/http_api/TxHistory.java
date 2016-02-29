package io.ucoin.app.model.http_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

import io.ucoin.app.enumeration.SourceType;

public class TxHistory implements Serializable {
    public String currency;
    public String pubkey;
    public History history;

    public static TxHistory fromJson(InputStream json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Tx.Input.class, new Tx.InputAdapter())
                .registerTypeAdapter(Tx.Output.class, new Tx.OutputAdapter())
                .create();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, TxHistory.class);
    }


    public static TxHistory fromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Tx.Input.class, new Tx.InputAdapter())
                .registerTypeAdapter(Tx.Output.class, new Tx.OutputAdapter())
                .create();
        return gson.fromJson(json, TxHistory.class);
    }

    public class History implements Serializable {
        public ConfirmedTx[] sent;
        public ConfirmedTx[] received;
        public PendingTx[] pending;
    }

    public abstract static class Tx implements Serializable {
        public Integer version;
        public String[] issuers;
        public Input[] inputs;
        public Output[] outputs;
        public String comment;
        public String[] signatures;
        public String hash;


        public static Tx fromJson(InputStream json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
            return gson.fromJson(reader, Tx.class);
        }


        public static Tx fromJson(String json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            return gson.fromJson(json, Tx.class);
        }


        public static class Input implements Serializable {
            public Integer index;
            public SourceType type;
            public Long number;
            public String fingerprint;
            public String amount;
        }

        public static class Output implements Serializable {
            public String publicKey;
            public String amount;
        }

        public static class InputAdapter extends TypeAdapter<Input> {

            @Override
            public Input read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                String ipt = reader.nextString();
                String[] parts = ipt.split(":");
                Input input = new Input();
                input.index = Integer.parseInt(parts[0]);
                input.type = SourceType.valueOf(parts[1]);
                input.number = Long.parseLong(parts[2]);
                input.fingerprint = parts[3];
                input.amount = parts[4];

                return input;
            }

            @Override
            public void write(JsonWriter writer, Input input) throws IOException {
                if (input == null) {
                    writer.nullValue();
                    return;
                }
                writer.value(input.index + ":" +
                        input.type.name() + ":" +
                        input.number + ":" +
                        input.fingerprint + ":" +
                        input.amount);
            }
        }

        public static class OutputAdapter extends TypeAdapter<Output> {

            @Override
            public Output read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                String upt = reader.nextString();
                String[] parts = upt.split(":");
                Output output = new Output();
                output.publicKey = parts[0];
                output.amount = parts[1];

                return output;
            }

            @Override
            public void write(JsonWriter writer, Output output) throws IOException {
                if (output == null) {
                    writer.nullValue();
                    return;
                }
                writer.value(output.publicKey + ":" +
                        output.amount);
            }
        }

    }

    public static class PendingTx extends Tx {

        public static PendingTx fromJson(InputStream json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
            return gson.fromJson(reader, PendingTx.class);
        }


        public static PendingTx fromJson(String json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            return gson.fromJson(json, PendingTx.class);
        }
    }


    public static class ConfirmedTx extends Tx {
        public Long block_number;
        public Long time;

        public static ConfirmedTx fromJson(InputStream json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
            return gson.fromJson(reader, ConfirmedTx.class);
        }

        public static ConfirmedTx fromJson(String json) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Input.class, new InputAdapter())
                    .registerTypeAdapter(Output.class, new OutputAdapter())
                    .create();
            return gson.fromJson(json, ConfirmedTx.class);
        }
    }


}
      /*
         public static class TxSerializer implements JsonSerializer<Tx> {

            @Override
            public JsonElement serialize(final Tx src, final Type typeOfSrc, final JsonSerializationContext context) {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("version", src.version);
                jsonObject.addProperty("type", src.type.name());
                jsonObject.addProperty("currency", src.currency);


                final JsonArray jsonIssuersArray = new JsonArray();
                for (final String issuer : src.issuers) {
                    jsonIssuersArray.add(new JsonPrimitive(issuer));
                }
                jsonObject.add("issuers", jsonIssuersArray);

                final JsonArray jsonInputsArray = new JsonArray();
                for (final Input input : src.inputs) {
                    String inputStr = input.index + ":" +
                            input.type.name() + ":" +
                            input.number + ":" +
                            input.fingerprint + ":" +
                            input.amount;

                    jsonInputsArray.add(new JsonPrimitive(inputStr));
                }
                jsonObject.add("inputs", jsonInputsArray);

                final JsonArray jsonOutputsArray = new JsonArray();
                for (final Output output : src.outputs) {
                    String outputStr = output.publicKey + ":" +
                            output.amount;

                    jsonOutputsArray.add(new JsonPrimitive(outputStr));
                }
                jsonObject.add("outputs", jsonOutputsArray);

                jsonObject.addProperty("comment", src.comment);

                if(src.signatures != null) {
                    final JsonArray jsonSignaturesArray = new JsonArray();
                    for (final String signature : src.signatures) {
                        jsonSignaturesArray.add(new JsonPrimitive(signature));
                    }
                    jsonObject.add("signatures", jsonSignaturesArray);
                }

                return jsonObject;
            }
        }
    */

