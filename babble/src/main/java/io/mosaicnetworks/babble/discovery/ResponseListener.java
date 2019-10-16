package io.mosaicnetworks.babble.discovery;

import java.util.List;

public interface ResponseListener {

    void onReceivePeers(List<Peer> peers);
}
