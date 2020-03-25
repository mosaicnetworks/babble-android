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

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.utils.RandomString;

public abstract class ResolvedServiceWebRTCFactory {

    /**
     * Builds a ResolvedService record from WebRTC Discovery record
     * @param dataProviderId
     * @param disco
     * @return
     */
    public static ResolvedService NewJoinResolvedService(String dataProviderId, Disco disco) {
      return new ResolvedService(
                dataProviderId,
                null, // InetAddress not used for WebRTC
                disco.PubKey,
              BabbleConstants.BABBLE_PORT(), // Integer.parseInt((String) map.get(P2PDataProvider.PORT_LABEL)),
              0, // Discovery port is not used in WebRtc
                new HashMap<String, String>(),
                disco.AppID,
                disco.GroupName,
                disco.GroupUID,
                disco.InitialPeers,
                disco.Peers
        );
    }


    /**
     * Returns a New Group ResolvedService record
     * @param context
     * @param dataProviderId
     * @param moniker
     * @param publicKey
     * @param groupName
     * @return
     */
    public static ResolvedService NewNewResolvedService(Context context, String dataProviderId, String moniker,
                                                        String publicKey, String groupName) {
        List<Peer> initialPeers = new ArrayList<>();
        initialPeers.add(new Peer(publicKey, publicKey, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(publicKey, publicKey, moniker));

        String appId = BabbleConstants.APP_ID();
        String groupUid = new RandomString().nextString();

        return new ResolvedService(
                dataProviderId,
                null, // InetAddresses.forString((String) map.get(P2PDataProvider.HOST_LABEL)),
                publicKey,
                BabbleConstants.BABBLE_PORT(), // Integer.parseInt((String) map.get(P2PDataProvider.PORT_LABEL)),
                0,
                new HashMap<String, String>(),
                appId,
                groupName,
                groupUid,
                initialPeers,
                currentPeers);
    }

}
