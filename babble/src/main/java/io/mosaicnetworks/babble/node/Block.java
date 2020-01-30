/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

public final class Block {

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
        public byte[][] transactions = new byte[][]{};

        @SerializedName("InternalTransactions")
        public InternalTransaction[] internalTransactions = new InternalTransaction[]{};

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
