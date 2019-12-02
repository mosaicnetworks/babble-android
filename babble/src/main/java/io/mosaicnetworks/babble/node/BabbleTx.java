package io.mosaicnetworks.babble.node;

/**
 * A transaction to be submitted to the {@link BabbleService} must implement this interface
 */
public interface BabbleTx {

    /**
     * Convert transaction to bytes
     * @return byte representation of the transaction
     */
    byte[] toBytes();
}
