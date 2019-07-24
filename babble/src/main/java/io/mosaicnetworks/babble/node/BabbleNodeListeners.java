package io.mosaicnetworks.babble.node;

public interface BabbleNodeListeners {

    void onException(final String msg);

    byte[] onReceiveTransactions(byte[][] transactions);
}
