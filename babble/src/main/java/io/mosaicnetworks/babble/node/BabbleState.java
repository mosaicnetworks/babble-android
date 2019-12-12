package io.mosaicnetworks.babble.node;

/**
 * The App state which is passed to the {@link BabbleService} constructor must implement this
 * interface
 */
public interface BabbleState {

    /**
     * Called when a block is committed by Babble
     * @param block
     * @return processed block (with state-hash and InternalTransactionReceipts)
     */
    Block processBlock(Block block);

    /**
     * Called when the node is reconfigured, at which point the state should be reset to some
     * initial configuration
     */
    void reset();

    //byte[] getStateHash();

}
