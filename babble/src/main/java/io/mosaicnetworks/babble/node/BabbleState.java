package io.mosaicnetworks.babble.node;

/**
 * The App state which is passed to the {@link BabbleService} constructor must implement this
 * interface
 */
public interface BabbleState {

    /**
     * Called when a new set of transactions have passed through consensus
     * @param transactions array of raw transactions
     * @return state hash
     */
    byte[] applyTransactions(byte[][] transactions);

    /**
     * Called when the node is reconfigured, at which point the state should be reset to some
     * initial configuration
     */
    void reset();

    //byte[] getStateHash();

}
