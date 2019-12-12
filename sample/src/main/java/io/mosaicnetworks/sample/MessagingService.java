package io.mosaicnetworks.sample;

import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser;

/**
 * This is a singleton which provides a Messaging service. It extends the {@link BabbleService}
 * class providing the base class with the {@link AppState} and implementing a singleton pattern
 */
public final class MessagingService extends BabbleService<AppState> {

    private static MessagingService INSTANCE;
    private MdnsAdvertiser mMdnsAdvertiser;

    /**
     * Factory for the {@link MessagingService}
     * @return a messaging service
     */
    public static MessagingService getInstance() {
        if (INSTANCE==null) {
            INSTANCE = new MessagingService();
        }

        return INSTANCE;
    }

    private MessagingService() {
        super(new AppState());
    }

    @Override
    protected void onStarted() {
        super.onStarted();

    }

    @Override
    protected void onStopped() {
        super.onStopped();
        mMdnsAdvertiser.stopAdvertising();
    }
}


