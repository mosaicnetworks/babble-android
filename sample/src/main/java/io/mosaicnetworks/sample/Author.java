package io.mosaicnetworks.sample;


import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    private String name;

    public Author(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }
}