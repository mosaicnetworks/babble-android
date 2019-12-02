package io.mosaicnetworks.babble.discovery;

/**
 * A provider of peer information
 */
public interface PeersProvider {

    /**
     * Provide genesis peers
     * @return a json formatted string of the genesis peers
     */
    String getGenesisPeers();

    /**
     * Provide current peers
     * @return a json formatted string of current peers
     */
    String getCurrentPeers();
}
