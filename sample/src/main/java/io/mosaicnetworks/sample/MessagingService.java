package io.mosaicnetworks.sample;

import android.content.Context;

import java.io.IOException;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.node.NodeConfig;

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
    public static final int DEFAULT_DISCOVERY_PORT = 8988;
    private int mDiscoveryPort = DEFAULT_DISCOVERY_PORT;

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
        //super(new ChatState(), new NodeConfig.Builder().logLevel(NodeConfig.LogLevel.TRACE).build(), context);
        super(new ChatState(), context);

        mAppContext = context;
    }

    @Override
    protected void onStarted() {
        super.onStarted();
        mMdnsAdvertiser = new MdnsAdvertiser("BabbleService", 8988);
        mMdnsAdvertiser.advertise(mAppContext);

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(mDiscoveryPort, mBabbleNode);
        try {
            mHttpPeerDiscoveryServer.start();
        } catch (IOException ex) {
            //Probably the port is in use, we'll continue without the discovery service
        }
    }

    @Override
    protected void onStopped() {
        super.onStopped();
        mMdnsAdvertiser.stopAdvertising();
        mMdnsAdvertiser = null;

        mHttpPeerDiscoveryServer.stop();
        mHttpPeerDiscoveryServer = null;
    }

    public int getDiscoveryPort() {
        return mDiscoveryPort;
    }

    public void setDiscoveryPort(int discoveryPort) {
        mDiscoveryPort = discoveryPort;
    }
}


