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

package io.mosaicnetworks.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;

import io.mosaicnetworks.babble.node.BabbleTx;
import io.mosaicnetworks.sample.chatkit.commons.models.IMessage;
import io.mosaicnetworks.sample.chatkit.commons.models.IUser;

/*
    UnixEraDateTypeAdapter converts dates to and from ints using number of seconds since
    January 1, 1970, 00:00:00 GTM.
 */
class UnixEraDateTypeAdapter extends TypeAdapter<Date> {
    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null)
            out.nullValue();
        else
            out.value(value.getTime() / 1000);
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        if (in != null)
            return new Date(in.nextLong() * 1000);
        else
            return null;
    }
}

/**
 * A chat message class that implements the {@link IMessage} interface, which is required to display
 * messages in the adapter from the same library, and BabbleTx which is required to serialize
 * messages for Babble.
 */
public final class Message implements BabbleTx, IMessage {

    private final static Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,new UnixEraDateTypeAdapter()).create();

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
     * Constructor
     * @param text the message text
     * @param author the message author
     * @param date the message date
     */
    public Message(String text, String author, Date date) {
        this.text = text;
        this.author = author;
        this.date = date;
    }

    /**
     * Factory for constructing a {@link Message} from JSON
     * @param txJson A JSON format transaction string
     * @return a Message object transaction
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
        String json = gson.toJson(this);
        return json.getBytes();
    }
}
