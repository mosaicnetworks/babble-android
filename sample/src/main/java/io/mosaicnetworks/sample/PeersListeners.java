package io.mosaicnetworks.sample;

public interface PeersListeners {

    void receivedPeers(String peersJSON);

    void getPeersFail();
}
