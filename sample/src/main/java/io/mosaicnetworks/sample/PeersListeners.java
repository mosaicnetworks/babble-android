package io.mosaicnetworks.sample;

import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;

public interface PeersListeners {

    void receivedPeers(List<Peer> peersJson);

    void getPeersFail();
}
