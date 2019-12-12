package io.mosaicnetworks.sample;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.mosaicnetworks.babble.node.BabbleTx;

/**
 * A message class for the UI side defined as a message author, the text and the time of the
 * message. This class implements the {@link IMessage} interface, which is required to display
 * messages in the adapter from the same library.
 */
public final class Message implements IMessage {

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

    private final String mText;
    private final String mAuthor;
    private final Date mDate;

    /**
     * Constructor
     * @param text the message text
     * @param author the message author
     */
    public Message(String text, String author) {
        mText = text;
        mAuthor = author;
        mDate = new Date();
    }

    /**
     * Construct a {@link Message} from a {@link ChatTx}. A {@link Message} instance is created from the
     * {@link ChatTx#from} and {@link ChatTx#text} attributes
     * @param chatTx the chat transaction
     * @return the message
     */
    public static Message fromChatTx(ChatTx chatTx) {
        return new Message(chatTx.text, chatTx.from);
    }

    /**
     * Construct a {@link ChatTx} from a {@link Message} using the message text and author
     * attributes
     * @return the chat transaction
     */
    public ChatTx toChatTx() {
        return new ChatTx(mAuthor, mText);
    }

    @Override
    public String getId() {
        return mAuthor;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public Author getUser() {
        return new Author(mAuthor);
    }

    @Override
    public Date getCreatedAt() {
        return mDate;
    }

}
