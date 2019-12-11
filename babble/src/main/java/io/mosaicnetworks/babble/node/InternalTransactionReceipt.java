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

import io.mosaicnetworks.babble.discovery.Peer;

final class InternalTransactionReceipt {
    private final static Gson mCustomGson;

    static {
        mCustomGson = new GsonBuilder().registerTypeAdapter(byte[].class, new
                JsonDeserializer<byte[]>() {public byte[] deserialize(JsonElement json, Type
                        typeOfT, JsonDeserializationContext context) throws JsonSyntaxException {
                    return Base64.decode(json.getAsString(), Base64.NO_WRAP);
                }
                }).create();
    }

    @SerializedName("InternalTransaction")
    public InternalTransaction mInternalTransaction;

    @SerializedName("Accepted")
    public Boolean mAccepted;

    public InternalTransactionReceipt(InternalTransaction it, Boolean accepted) {
        mInternalTransaction = it;
        mAccepted = accepted;
    }
}
