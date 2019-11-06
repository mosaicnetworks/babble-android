package io.mosaicnetworks.sample;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleConfig;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleNodeListeners;
import io.mosaicnetworks.babble.node.KeyPair;
import io.mosaicnetworks.babble.node.LeaveResponseListener;

public class MessagingService {

    public enum State {
        UNCONFIGURED,
        CONFIGURED,
        RUNNING,
        RUNNING_WITH_DISCOVERY
    }

    private static MessagingService instance;
    private List<MessageObserver> mObservers = new ArrayList<>();
    private BabbleNode mBabbleNode;
    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;
    private KeyPair mKeyPair = new KeyPair();
    private static final int BABBLING_PORT = 6666;
    private static final int DISCOVERY_PORT = 8988;
    private byte[] mStateHash;
    private State mState = State.UNCONFIGURED;

    public static MessagingService getInstance() {
        if (instance==null) {
            instance = new MessagingService();
        }
        return instance;
    }

    public void configure(List<Peer> peers, String moniker, String inetAddress) {

        if (mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        // If peers list is empty we need to setup a new babble group, this requires a peers list
        // which contains this node
        if (peers.isEmpty()) {
            peers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + BABBLING_PORT, moniker));
        }

        try {
            mBabbleNode = BabbleNode.createWithConfig(peers, mKeyPair.privateKey, inetAddress, BABBLING_PORT,
                    moniker, new BabbleNodeListeners() {
                        @Override
                        public byte[] onReceiveTransactions(byte[][] transactions) {
                            for (byte[] rawTx:transactions) {
                                String tx = new String(rawTx, StandardCharsets.UTF_8);

                                BabbleTx babbleTx;
                                try {
                                    babbleTx = BabbleTx.fromJson(tx);
                                } catch (JsonSyntaxException ex) {
                                    //skip any malformed transactions
                                    continue;
                                }

                                Message message = Message.fromBabbleTx(babbleTx);
                                notifyObservers(message);
                            }

                            //TODO: update state hash
                            return mStateHash;
                        }
                    }, new BabbleConfig.Builder().logLevel(BabbleConfig.LogLevel.DEBUG).build());
            mState = State.CONFIGURED;
        } catch (IllegalArgumentException ex) {
            //The reassignment of mState and MBabbleNode has failed, so leave them as before
            //TODO: need to catch port in use exception (IOException) and throw others
        }

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(inetAddress, DISCOVERY_PORT, mBabbleNode);
    }

    public void start() {
        if (mState==State.UNCONFIGURED || mState==State.RUNNING ||
                mState==State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot start an unconfigured or running service");
        }

        mBabbleNode.run();
        mState=State.RUNNING;

        try {
            mHttpPeerDiscoveryServer.start();
            mState=State.RUNNING_WITH_DISCOVERY;
        } catch (IOException ex) {
            //Probably the port is in use, we'll continue without the discovery service
        }
    }

    public void stop() {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot stop a service which isn't running");
        }

        mHttpPeerDiscoveryServer.stop();
        mHttpPeerDiscoveryServer=null;

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onSuccess() {
                mBabbleNode=null;
                mState = State.UNCONFIGURED;
            }
        });
    }

    public void submitMessage(Message message) {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot submit when the service isn't running");
        }
        mBabbleNode.submitTx(message.toBabbleTx().toBytes());
    }

    public State getState() {
        return mState;
    }

    public void registerObserver(MessageObserver messageObserver) {
        if (!mObservers.contains(messageObserver)) {
            mObservers.add(messageObserver);
        }
    }

    public void removeObserver(MessageObserver messageObserver) {
        mObservers.remove(messageObserver);
    }

    private void notifyObservers(Message message) {
        for (MessageObserver observer: mObservers) {
            observer.onMessageReceived(message);
        }
    }
}
