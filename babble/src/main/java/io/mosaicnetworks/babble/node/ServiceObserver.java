package io.mosaicnetworks.babble.node;

/**
 * Observers of the {@link BabbleService} are required to implement this interface
 */
public interface ServiceObserver {

    /**
     * Called when transactions are received and the app state is updated
     */
    void stateUpdated();
}
