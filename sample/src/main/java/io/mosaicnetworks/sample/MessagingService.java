package io.mosaicnetworks.sample;

import android.content.Context;

import java.io.IOException;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;

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
    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;
    private int mDiscoveryPort = 8988;
    private boolean mAdvertising = false;

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
        super(new ChatState(), context);

        mAppContext = context;
    }

    @Override
    protected void onStarted() {
        //TODO: should this be part of the base service?
        //TODO: collisions - add UUID?
        super.onStarted();
        mMdnsAdvertiser = new MdnsAdvertiser(mGroupName, mDiscoveryPort);


        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(mDiscoveryPort, mBabbleNode);
        try {
            mHttpPeerDiscoveryServer.start();
            mMdnsAdvertiser.advertise(mAppContext); // start mDNS advertising if server started
            mAdvertising = true;
        } catch (IOException ex) {
            //Probably the port is in use, we'll continue without the discovery service
        }
    }

    @Override
    protected void onStopped() {
        super.onStopped();
        if (mMdnsAdvertiser != null) {
            mMdnsAdvertiser.stopAdvertising();
            mMdnsAdvertiser = null;
        }

        if (mHttpPeerDiscoveryServer != null) {
            mHttpPeerDiscoveryServer.stop();
            mHttpPeerDiscoveryServer = null;
        }

        mAdvertising = false;
    }

    public boolean isAdvertising() {
        return mAdvertising;

    }
}


