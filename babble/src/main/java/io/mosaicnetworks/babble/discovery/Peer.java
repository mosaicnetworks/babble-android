package io.mosaicnetworks.babble.discovery;

import com.google.gson.annotations.SerializedName;

public class Peer {

    @SerializedName("PubKeyHex")
    public String pubKeyHex;

    @SerializedName("NetAddr")
    public String netAddr;

    @SerializedName("Moniker")
    public String moniker;

    public Peer(String pubKeyHex, String netAddr, String moniker) {
        this.pubKeyHex = pubKeyHex;
        this.netAddr = netAddr;
        this.moniker = moniker;
    }
}
