package io.mosaicnetworks.sample;

import android.content.Context;

import io.mosaicnetworks.babble.node.BabbleConfig;

import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser;

/**
 * This is a singleton which provides a Messaging service. It extends the {@link BabbleService}
 * class providing the base class with the {@link ChatState} and implementing a singleton pattern
 */
public final class MessagingService extends BabbleService<ChatState> {

    private static MessagingService INSTANCE;
    private MdnsAdvertiser mMdnsAdvertiser;
    private Context mAppContext;

    /**
     * Factory for the {@link MessagingService}
     * @return a messaging service
     */
    public static MessagingService getInstance(Context context) {
        if (INSTANCE==null) {

            INSTANCE = new MessagingService(context.getApplicationContext());

        }

        return INSTANCE;
    }

    private MessagingService(Context context) {
        super(new ChatState(), new BabbleConfig.Builder().logLevel(BabbleConfig.LogLevel.TRACE).build(), context);

        mAppContext = context;
    }

    @Override
    protected void onStarted() {
        super.onStarted();
        mMdnsAdvertiser = new MdnsAdvertiser("BabbleService", 8988);
        mMdnsAdvertiser.advertise(mAppContext);
    }

    @Override
    protected void onStopped() {
        super.onStopped();
        mMdnsAdvertiser.stopAdvertising();
        mMdnsAdvertiser = null;
    }
}


