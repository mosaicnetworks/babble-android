package io.mosaicnetworks.babble.node;

public interface BabbleNodeListeners {

    byte[] onReceiveTransactions(byte[][] transactions);
}
