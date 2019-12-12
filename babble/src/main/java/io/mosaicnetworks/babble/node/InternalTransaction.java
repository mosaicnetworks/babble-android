package io.mosaicnetworks.babble.node;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.net.InterfaceAddress;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;

public final class InternalTransaction {
    private final static Gson mCustomGson;

    static {
        mCustomGson = new GsonBuilder().registerTypeAdapter(byte[].class, new
                JsonDeserializer<byte[]>() {public byte[] deserialize(JsonElement json, Type
                        typeOfT, JsonDeserializationContext context) throws JsonSyntaxException {
                    return Base64.decode(json.getAsString(), Base64.NO_WRAP);
                }
                }).create();
    }

    public static final class InternalTransactionBody {
        @SerializedName("Type")
        public int type = 0;

        @SerializedName("Peer")
        public Peer peer = null;
    }

    @SerializedName("Body")
    public InternalTransactionBody body = new InternalTransactionBody();

    @SerializedName("Signature")
    public String signature = null;

    public final InternalTransactionReceipt AsAccepted() {
        return new InternalTransactionReceipt(this, true);
    }

    public final InternalTransactionReceipt AsRefused() {
        return new InternalTransactionReceipt(this, false);
    }
}
