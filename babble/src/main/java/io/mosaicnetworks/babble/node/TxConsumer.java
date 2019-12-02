package io.mosaicnetworks.babble.node;

/**
 * A consumer of Babble transactions
 */
public interface TxConsumer {

    /**
     * Called when a new set of transactions have passed through consensus
     * @param transactions array of raw transactions
     * @return state hash
     */
    byte[] onReceiveTransactions(byte[][] transactions);
}
