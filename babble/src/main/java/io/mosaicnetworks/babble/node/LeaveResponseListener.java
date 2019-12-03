package io.mosaicnetworks.babble.node;

/**
 * Listener for the {@link BabbleNode} leave request
 */
public interface LeaveResponseListener {

    /**
     * Called when the leave is complete
     */
    void onComplete();
}