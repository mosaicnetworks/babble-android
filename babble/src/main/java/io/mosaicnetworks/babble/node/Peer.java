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

package io.mosaicnetworks.babble.node;

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
