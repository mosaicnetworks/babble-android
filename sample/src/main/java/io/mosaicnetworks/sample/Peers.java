package io.mosaicnetworks.sample;

import android.util.Log;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.KeyPair;
import io.mosaicnetworks.babble.discovery.ResponseListener;

public class Peers {

    public static void requestPeers(String peerIP, int port, final ChatActivity chatActivity) {

        String endpoint = "http://" + peerIP + ":" + port + "/peers";
        Log.d(MainActivity.TAG, "Connecting to endpoint: " + endpoint);

        HttpDiscoveryRequest httpDiscoveryRequest;
        try {
            httpDiscoveryRequest = new HttpDiscoveryRequest(endpoint, new ResponseListener() {
                @Override
                public void onReceivePeers(List<Peer> peers) {
                    chatActivity.receivedPeers(peers);
                }

                @Override
                public void onFailure(Error error) {
                    chatActivity.getPeersFail();
                }
            });
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid IP string");
        }

        httpDiscoveryRequest.send();
    }

    public static void genPeers(KeyPair keyPair, String mIPAddr, int port, String moniker, ChatActivity chatActivity) {

        List<Peer> peers = new ArrayList();
        peers.add(new Peer(keyPair.publicKey, mIPAddr + ":" + port, moniker));

        chatActivity.receivedPeers(peers);

    }
}
