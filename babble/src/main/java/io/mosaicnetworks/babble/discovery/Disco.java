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

/**
 * The Disco class maps the group class in the Disco go repo.
 *
 * The Go structure is:
 *
 * type group struct {
 * 	GroupUID     string        `json:"GroupUID"`
 * 	GroupName    string        `json:"GroupName"`
 * 	AppID        string        `json:"AppID"`
 * 	PubKey       string        `json:"PubKey"`
 * 	LastUpdated  int64         `json:"LastUpdated"`
 * 	Peers        []*peers.Peer `json:"Peers"`
 * 	GenesisPeers []*peers.Peer `json:"InitialPeers"`
 * }
 */
public final class Disco {

        /**
         * Unique Group Identifier
         */
        @SerializedName("GroupUID")
        public final String GroupUID;

        /**
         * The Group Name
         */
        @SerializedName("GroupName")
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
        public final int LastUpdated;



    /**
         * The peers
         */
        @SerializedName("Peers")
        public final ArrayList<Peer> Peers;


        @SerializedName("InitialPeers")
         public final ArrayList<Peer> InitialPeers;

        /**
         * Constructor
         */
        public Disco(String groupUID, String groupName, String appId, String pubKey, int lastUpdated,  ArrayList<Peer> peers,  ArrayList<Peer> initialPeers) {

            if (groupUID==null || groupName==null || appId==null || pubKey==null ) {
                throw new NullPointerException("Null arguments are not accepted");
            }

            this.GroupUID = groupUID;
            this.GroupName = groupName;
            this.AppID = appId;
            this.PubKey = pubKey;
            this.LastUpdated = lastUpdated;
            this.Peers = peers;
            this.InitialPeers = initialPeers;
        }



}
