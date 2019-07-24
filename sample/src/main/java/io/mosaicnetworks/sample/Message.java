package io.mosaicnetworks.sample;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

public class Message implements IMessage {

    private String text;
    private Author author;
    private Date date;

    public Message(String text, Author author, Date date) {
        this.text = text;
        this.author = author;
        this.date = date;
    }

    @Override
    public String getId() {
        return author.getName();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }
}
