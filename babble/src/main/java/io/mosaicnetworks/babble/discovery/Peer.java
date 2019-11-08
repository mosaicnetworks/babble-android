package io.mosaicnetworks.babble.discovery;

import com.google.gson.annotations.SerializedName;

/**
 * An immutable class representing a Babble peer.
 */
public final class Peer {

    /**
     * The peer's public key
     */
    @SerializedName("PubKeyHex")
    public final String pubKeyHex;

    /**
     * The peer's network address
     */
    @SerializedName("NetAddr")
    public final String netAddr;

    /**
     * The peer's moniker
     */
    @SerializedName("Moniker")
    public final String moniker;

    /**
     * Constructor
     * @param pubKeyHex public key as created by the KeyPair class
     * @param netAddr the network address on which the peer can be contacted
     * @param moniker a moniker, this does not need to be unique across all peer's in a network
     */
    public Peer(String pubKeyHex, String netAddr, String moniker) {

        if (pubKeyHex==null || netAddr==null || moniker==null) {
            throw new NullPointerException("Null arguments are not accepted");
        }

        this.pubKeyHex = pubKeyHex;
        this.netAddr = netAddr;
        this.moniker = moniker;
    }
}
