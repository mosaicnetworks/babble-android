package io.mosaicnetworks.sample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TxMessage implements Serializable {

    @SerializedName("from")
    public String from;

    @SerializedName("text")
    public String text;

    public TxMessage(String from, String text) {
        this.from = from;
        this.text = text;
    }

}
