package io.mosaicnetworks.sample;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

public final class Message implements IMessage {

    private final String mText;
    private final Author mAuthor;
    private Date mDate;

    public Message(String text, Author author, Date date) {
        mText = text;
        mAuthor = author;
        mDate = date;
    }

    public static Message fromBabbleTx(BabbleTx babbleTx) {
        return new Message(babbleTx.text, new Author(babbleTx.from), new Date());
    }

    public BabbleTx toBabbleTx() {
        return new BabbleTx(mAuthor.getName(), mText);
    }

    @Override
    public String getId() {
        return mAuthor.getName();
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public Author getUser() {
        return mAuthor;
    }

    @Override
    public Date getCreatedAt() {
        return mDate;
    }
}
