package io.mosaicnetworks.sample;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.FailureListener;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.KeyPair;
import io.mosaicnetworks.babble.discovery.ResponseListener;

public class Peers {

    public static void requestPeers(String peerIP, int port, final ChatActivity chatActivity) {

        String endpoint = "http://" + peerIP + ":" + port + "/peers";
        Log.d(MainActivity.TAG, "Connecting to endpoint: " + endpoint);
        HttpDiscoveryRequest httpDiscoveryRequest = new HttpDiscoveryRequest(endpoint, new ResponseListener() {

            @Override
            public void onReceivePeers(List<Peer> peers) {
                //Invoked on the UI thread

                chatActivity.receivedPeers(peers);

            }
        }, new FailureListener() {
            @Override
            public void onFailure(int code) {
                //Invoked on the UI thread

                Log.d(MainActivity.TAG, "Failed to get peers info, failure code: " + code);

                chatActivity.getPeersFail();
            }
        });

        httpDiscoveryRequest.send();

    }

    public static void genPeers(KeyPair keyPair, String mIPAddr, int port, String moniker, ChatActivity chatActivity) {

        List<Peer> peers = new ArrayList();
        peers.add(new Peer(keyPair.publicKey, mIPAddr + ":" + port, moniker));

        chatActivity.receivedPeers(peers);

    }
}
