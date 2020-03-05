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

package io.mosaicnetworks.babble.discovery;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public final class Disco {


        /**
         * The peer's public key
         */
        @SerializedName("ID")
        public final String ID;

        /**
         * The peer's network address
         */
        @SerializedName("Title")
        public final String Title;

        /**
         * The peer's moniker
         */
        @SerializedName("Peers")
        public final ArrayList<Peer> Peers;


        @SerializedName("InitialPeers")
         public final ArrayList<Peer> InitialPeers;

        /**
         * Constructor
         */
        public Disco(String iD, String title,  ArrayList<Peer> peers,  ArrayList<Peer> initialPeers) {

            if (iD==null || title==null ) {
                throw new NullPointerException("Null arguments are not accepted");
            }

            this.ID = iD;
            this.Title = title;
            this.Peers = peers;
            this.InitialPeers = initialPeers;
        }



}
