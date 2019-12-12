package io.mosaicnetworks.sample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * An immutable babble transaction for this chat app
 */
public class ChatTx implements io.mosaicnetworks.babble.node.BabbleTx {

    private final static Gson gson = new Gson();

    /**
     * Who the message is from
     */
    @SerializedName("from")
    public final String from;

    /**
     * The text component of the message
     */
    @SerializedName("text")
    public final String text;

    /**
     * Constructor
     * @param from who the message is from
     * @param text the text component of the message
     */
    public ChatTx(String from, String text) {
        this.from = from;
        this.text = text;
    }

    /**
     * Factory for constructing a {@link ChatTx} from JSON
     * @param txJson
     * @return
     */
    public static ChatTx fromJson(String txJson) {
        return gson.fromJson(txJson, ChatTx.class);
    }

    /**
     * Export to bytes. Converts to JSON {@link String}, then converts to a {@link byte} array
     * @return byte array
     */
    @Override
    public byte[] toBytes() {
        return gson.toJson(this).getBytes();
    }
}
