package io.mosaicnetworks.babble.node;

public interface TxConsumer {

    byte[] onReceiveTransactions(byte[][] transactions);
}
