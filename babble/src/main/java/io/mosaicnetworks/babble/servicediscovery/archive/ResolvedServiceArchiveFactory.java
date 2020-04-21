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

package io.mosaicnetworks.babble.servicediscovery.archive;

import android.graphics.Bitmap;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.BuildConfig;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersFactory;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

public abstract class ResolvedServiceArchiveFactory {


    public static ResolvedService NewNewResolvedService(
            String dataProviderId,
            String inetAddress,
            int babblePort,
            int discoveryPort,
            String groupName,
            String groupUID,
            String publicKey,
            String moniker
    ) {


        List<Peer> initialPeers = new ArrayList<>();
        initialPeers.add(new Peer(publicKey, inetAddress + ":" + babblePort, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(publicKey, inetAddress + ":" + babblePort, moniker));



        Map<String,String> dnsTxt =  new HashMap<>();

        dnsTxt.put(BabbleConstants.DNS_TXT_HOST_LABEL,inetAddress );
        dnsTxt.put(BabbleConstants.DNS_TXT_PORT_LABEL, Integer.toString(discoveryPort));
        dnsTxt.put(BabbleConstants.DNS_TXT_MONIKER_LABEL, moniker);
        dnsTxt.put(BabbleConstants.DNS_TXT_DNS_VERSION_LABEL, BabbleConstants.DNS_VERSION);
        dnsTxt.put(BabbleConstants.DNS_TXT_BABBLE_VERSION_LABEL, BuildConfig.BabbleVersion);
        dnsTxt.put(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, groupUID);
        dnsTxt.put(BabbleConstants.DNS_TXT_APP_LABEL, BabbleConstants.APP_ID());
        dnsTxt.put(BabbleConstants.DNS_TXT_GROUP_LABEL, groupName);
        dnsTxt.put(BabbleConstants.DNS_TXT_CURRENT_PEERS_LABEL, PeersFactory.toJson(currentPeers));
        dnsTxt.put(BabbleConstants.DNS_TXT_INITIAL_PEERS_LABEL,  PeersFactory.toJson(initialPeers));


        ResolvedService resolvedService = new ResolvedService(
                dataProviderId,
                inetAddress,
                inetAddress,
                babblePort,
                discoveryPort,
                dnsTxt,
                BabbleConstants.APP_ID(),
                groupName,
                groupUID,
                initialPeers,
                currentPeers,
                true
        );


        return resolvedService;
    }




}
