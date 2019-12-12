package io.mosaicnetworks.sample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.mosaicnetworks.babble.node.BabbleTx;

/**
 * A chat message class that implements the {@link IMessage} interface, which is required to display
 * messages in the adapter from the same library, and BabbleTx which is required to serialize
 * messages for Babble.
 */
public final class Message implements BabbleTx, IMessage {

    private final static Gson gson = new Gson();

    /**
     * This class implements the {@link IUser} interface which is required by the message adapter
     * library
     */
    public final static class Author implements IUser {

        private final String mName;

        public Author(String name) {
            mName = name;
        }

        @Override
        public String getId() {
            return mName;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getAvatar() {
            return null;
        }
    }

    @SerializedName("text")
    public final String text;

    @SerializedName("author")
    public final String author;

    @SerializedName("date")
    public final Date date;

    /**
     * Constructor
     * @param text the message text
     * @param author the message author
     */
    public Message(String text, String author) {
        this.text = text;
        this.author = author;
        this.date = new Date();
    }

    /**
     * Factory for constructing a {@link Message} from JSON
     * @param txJson
     * @return
     */
    public static Message fromJson(String txJson) {
        return gson.fromJson(txJson, Message.class);
    }

    // Implement IMessage
    @Override
    public String getId() {
        return this.author;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public Author getUser() {
        return new Author(this.author);
    }

    @Override
    public Date getCreatedAt() {
        return this.date;
    }

    // Implement BabbleTx
    /**
     * Export to bytes. Converts to JSON {@link String}, then converts to a {@link byte} array
     * @return byte array
     */
    @Override
    public byte[] toBytes() {
        return gson.toJson(this).getBytes();
    }

}
