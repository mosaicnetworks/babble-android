package io.mosaicnetworks.babble.node;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;


class Block implements Serializable {

    public class BlockBody implements Serializable {
        @SerializedName("Index")
        public int index;

        @SerializedName("RoundReceived")
        public int roundReceived;

        @SerializedName("StateHash")
        public byte[] stateHash;

        @SerializedName("Transactions")
        public byte[][] transactions;
    }

    @SerializedName("Body")
    public BlockBody body;

    @SerializedName("Signatures")
    public Map<String, String> signatures;
}