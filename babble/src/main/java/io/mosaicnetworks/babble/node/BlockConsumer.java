package io.mosaicnetworks.babble.node;

/**
 * A consumer of Babble blocks
 */
public interface BlockConsumer {

    /**
     * Called when a new block is received
     * @param block incoming block
     * @return processed block (with state-hash, and InternalTransactionReceipts)
     */
    Block onReceiveBlock(Block block);
}


