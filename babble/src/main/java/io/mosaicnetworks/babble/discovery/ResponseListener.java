package io.mosaicnetworks.babble.discovery;

public interface ResponseListener {

    void onReceivePeers(Peer[] peers);
}
