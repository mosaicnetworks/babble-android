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

/**
 * Block is a class to handle the JSON format blocks produced by the Golang Babble Node.
 */
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

    /**
     * A class containing the block of the {@link Block}
     */
    public static final class BlockBody {

        /**
         * The block index
         */
        @SerializedName("Index")
        public final int index = 0;

        /**
         * Round received parameter
         */
        @SerializedName("RoundReceived")
        public final int roundReceived = 0;

        /**
         * The State Hash of the block
         */
        @SerializedName("StateHash")
        public byte[] stateHash = new byte[]{};

        /**
         * An array of transactions in byte format
         */
        @SerializedName("Transactions")
        public byte[][] transactions = new byte[][]{};

        /**
         * An Array of {@link InternalTransaction} instances
         */
        @SerializedName("InternalTransactions")
        public InternalTransaction[] internalTransactions = new InternalTransaction[]{};

        /**
         * An Array of {@link InternalTransactionReceipt} instances
         */
        @SerializedName("InternalTransactionReceipts")
        public InternalTransactionReceipt[] internalTransactionReceipts = new InternalTransactionReceipt[]{};

    }

    /**
     * The Body of the Block in an {@link BlockBody} instance
     */
    @SerializedName("Body")
    public final BlockBody body = new BlockBody();

    /**
     * A map containing the Signatures on the block
     */
    @SerializedName("Signatures")
    public final Map<String, String> signatures = null;

    /**
     * Static method to take a JSON string block and return an instance of {@link Block}
     * @param blockJson a Block in JSON format
     * @return
     */
    public static Block fromJson(String blockJson) {
        //TODO: implement checks for correct types and missing and extra JSON attributes using GSON
        return mCustomGson.fromJson(blockJson, Block.class);
    }

    /**
     * Exports the block to JSON format.
     * @return
     */
    public String toJson() {
        return mCustomGson.toJson(this);
    }
}
