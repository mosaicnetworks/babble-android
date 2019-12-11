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
import java.util.Map;

final class Block {

    private final static Gson mCustomGson;

    static {
        mCustomGson = new GsonBuilder().registerTypeAdapter(byte[].class, new
                JsonDeserializer<byte[]>() {public byte[] deserialize(JsonElement json, Type
                        typeOfT, JsonDeserializationContext context) throws JsonSyntaxException {
                    return Base64.decode(json.getAsString(), Base64.NO_WRAP);
                }
                }).create();
    }

    public static final class BlockBody {
        @SerializedName("Index")
        public final int index = 0;

        @SerializedName("RoundReceived")
        public final int roundReceived = 0;

        @SerializedName("StateHash")
        public byte[] stateHash = new byte[]{};

        @SerializedName("Transactions")
        public final byte[][] transactions = new byte[][]{};

        @SerializedName("InternalTransactions")
        public final InternalTransaction[] internalTransactions = new InternalTransaction[]{};

        @SerializedName("InternalTransactionReceipts")
        public InternalTransactionReceipt[] internalTransactionReceipts = new InternalTransactionReceipt[]{};

    }

    @SerializedName("Body")
    public final BlockBody body = new BlockBody();

    @SerializedName("Signatures")
    public final Map<String, String> signatures = null;

    public static Block fromJson(String blockJson) {
        //TODO: implement checks for correct types and missing and extra JSON attributes using GSON
        return mCustomGson.fromJson(blockJson, Block.class);
    }

    public String toJson() {
        return mCustomGson.toJson(this);
    }
}
