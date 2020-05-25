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

package io.mosaicnetworks.babble.servicediscovery.webrtc;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.mosaicnetworks.babble.node.Peer;

/**
 * The Disco class maps the group class in the Disco go repo.
 *
 * The Go structure is:
 *
 * type Group struct {
 * 	ID           string
 * 	Name         string
 * 	AppID        string
 * 	PubKey       string
 * 	LastUpdated  int64
 * 	Peers        []*peers.Peer
 * 	GenesisPeers []*peers.Peer
 * }
 */
public final class Disco {

        /**
         * Unique Group Identifier
         */
        @SerializedName("ID")
        public final String GroupUID;

        /**
         * The Group Name
         */
        @SerializedName("Name")
        public final String GroupName;

        /**
         * App ID
         */
        @SerializedName("AppID")
        public final String AppID;

        /**
         * The Public Key of the Group Creator
         */
        @SerializedName("PubKey")
        public final String PubKey;

        /**
         * The Last Update for this Group in Unix Time in seconds
         */
        @SerializedName("LastUpdated")
        public int LastUpdated;

        /**
         * The Last Block Index
         */
        @SerializedName("LastBlockIndex")
        public int LastBlockIndex;


    /**
         * The peers
         */
        @SerializedName("Peers")
        public final List<Peer> Peers;


        @SerializedName("GenesisPeers")
         public final List<Peer> InitialPeers;

        /**
         * Constructor
         */
        public Disco(String groupUID, String groupName, String appId, String pubKey, int lastUpdated, int lastBlockIndex, List<Peer> peers, List<Peer> initialPeers) {

            if (groupUID==null || groupName==null || appId==null || pubKey==null ) {
                throw new NullPointerException("Null arguments are not accepted");
            }

            this.GroupUID = groupUID;
            this.GroupName = groupName;
            this.AppID = appId;
            this.PubKey = pubKey;
            this.LastUpdated = lastUpdated;
            this.LastBlockIndex = lastBlockIndex;
            this.Peers = peers;
            this.InitialPeers = initialPeers;
        }

        public void setPeers(List<Peer> peers) {
            // Clear down and delete as Peers is instantiated as a final object.
            this.Peers.clear();
            this.Peers.addAll(peers);
        }

    @Override
    public String toString() {
        return "Disco{" +
                "GroupUID='" + GroupUID + '\'' +
                ", GroupName='" + GroupName + '\'' +
                ", AppID='" + AppID + '\'' +
                ", PubKey='" + PubKey + '\'' +
                ", LastUpdated=" + LastUpdated +
                ", LastBlockIndex=" + LastBlockIndex +
                ", Peers=" + Peers +
                ", InitialPeers=" + InitialPeers +
                '}';
    }
}
