package io.mosaicnetworks.babble.discovery;

import com.google.gson.annotations.SerializedName;

public final class Peer {

    @SerializedName("PubKeyHex")
    public final String pubKeyHex;

    @SerializedName("NetAddr")
    public final String netAddr;

    @SerializedName("Moniker")
    public final String moniker;

    public Peer(String pubKeyHex, String netAddr, String moniker) {

        if (pubKeyHex==null || netAddr==null || moniker==null) {
            throw new NullPointerException("Null arguments are not accepted");
        }

        this.pubKeyHex = pubKeyHex;
        this.netAddr = netAddr;
        this.moniker = moniker;
    }
}
