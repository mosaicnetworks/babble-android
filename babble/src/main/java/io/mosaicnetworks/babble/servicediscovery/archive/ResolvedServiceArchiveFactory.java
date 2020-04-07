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
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

public abstract class ResolvedServiceArchiveFactory {


    public static ResolvedService NewNewResolvedService(
            String dataProviderId,
            String inetAddress,
            String inetString,
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

        dnsTxt.put(BabbleConstants.DNS_TXT_HOST_LABEL,inetString );
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
                inetString,
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



        public static ResolvedService NewJoinResolvedService(String dataProviderId, NsdServiceInfo nsdServiceInfo) {
        Map<String, byte[]> serviceAttributes = nsdServiceInfo.getAttributes();

        Map<String, String> map = convertAttributeMap(serviceAttributes);
        //TODO: JK29Mar remove this debugging code
        for (Map.Entry<String,String> entry : map.entrySet()) {
            Log.i("Resolved", entry.getKey()+": "+entry.getValue());
        }

        Log.i("ResolvedServiceMdnsFac", "NewJoinResolvedService: ");

        return new ResolvedService(dataProviderId,
                nsdServiceInfo.getHost().getHostAddress(),
                "",
                BabbleConstants.BABBLE_PORT(),
                nsdServiceInfo.getPort(),
                map,
                extractStringAttribute(serviceAttributes, BabbleConstants.DNS_TXT_APP_LABEL),
                extractStringAttribute(serviceAttributes, BabbleConstants.DNS_TXT_GROUP_LABEL),
                extractStringAttribute(serviceAttributes, BabbleConstants.DNS_TXT_GROUP_ID_LABEL),
                null, null ,  //TODO: expand this,
                false
        );
    }


    private static String extractStringAttribute(Map<String, byte[]> serviceAttributes, String key) {
        if (!serviceAttributes.containsKey(key)) {
            Log.e("ResolvedServMdnsFac", "Map does not contain attribute: " + key );
            throw new IllegalArgumentException("Map does not contain attribute: " + key);
        }
        //TODO: "This method always replaces malformed-input and unmappable-character sequences with
        // this charset's default replacement string" - is this ok?
        // The impact would be a fallback default value for these fields in the event that they could
        // not be retrieved. But in those circumstances another error has probably already been thrown.
        return new String(serviceAttributes.get(key), Charset.forName("UTF-8"));
    }

    /**
     * Converts a Map of byte array to a map of String.
     * @param serviceAttributes
     * @return
     */
    private static Map<String, String> convertAttributeMap(Map<String, byte[]> serviceAttributes) {
        Map<String, String> stringMap = new HashMap<>();

        if (serviceAttributes == null ) {
            Log.e("ResolvedServMdnsFac", "convertAttributeMap: serviceAttribute is null" );
            return stringMap;
        }
        for (Map.Entry<String,byte[]> entry : serviceAttributes.entrySet())
        {

            byte[] bytesValue = entry.getValue();

            //Need to explicitly check for null, as it causes an error in string construction
            if (bytesValue == null) {
                stringMap.put(entry.getKey(), "");

            } else {
                stringMap.put(entry.getKey(), new String(bytesValue, Charset.forName("UTF-8")));
            }
        }

        return stringMap;
    }
}
