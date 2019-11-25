package io.mosaicnetworks.babble.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;

public abstract class BabbleService<AppState extends BabbleState> {

    public enum State {
        UNCONFIGURED,
        CONFIGURED,
        RUNNING,
        RUNNING_WITH_DISCOVERY
    }

    private static final int BABBLING_PORT = 6666; //TODO: this cannot be hard coded in the library
    public static final int DISCOVERY_PORT = 8988; //TODO: this cannot be hard coded in the library
    private final List<ServiceObserver> mObservers = new ArrayList<>();
    private State mState = State.UNCONFIGURED;
    private KeyPair mKeyPair = new KeyPair();
    private BabbleNode mBabbleNode;
    private BabbleState mBabbleState;
    public AppState state;

    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;

    public BabbleService(AppState babbleState) {
        mBabbleState = babbleState;
        state = babbleState; //TODO: this is just mirroring mBabbleState
    }

    public void configureNew(String moniker, String inetAddress) {
        List<Peer> genesisPeers = new ArrayList<>();
        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + BABBLING_PORT, moniker));
        List<Peer> currentPeers = genesisPeers;

        configure(genesisPeers, currentPeers, moniker, inetAddress);
    }

    public void configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress) {
        configure(genesisPeers, currentPeers, moniker, inetAddress);
    }

    private void configure(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress) {

        if (mState == State.RUNNING || mState == State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        try {
            mBabbleNode = BabbleNode.createWithConfig(genesisPeers, currentPeers,
                    mKeyPair.privateKey, inetAddress,
                    BABBLING_PORT, moniker, new TxConsumer() {
                        @Override
                        public byte[] onReceiveTransactions(byte[][] transactions) {

                            byte[] stateHash = mBabbleState.applyTransactions(transactions);

                            notifyObservers();

                            return stateHash;
                        }
                    },
                    new BabbleConfig.Builder().logLevel(BabbleConfig.LogLevel.DEBUG).build());
            mState = State.CONFIGURED;
        } catch (IllegalArgumentException ex) {
            //The reassignment of mState and mBabbleNode has failed, so leave them as before
            //TODO: need to catch port in use exception (IOException) and throw others
            throw new RuntimeException(ex);
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

    public void leave(final LeaveResponseListener listener) {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot stop a service which isn't running");
        }

        mHttpPeerDiscoveryServer.stop();
        mHttpPeerDiscoveryServer=null;

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onComplete() {
                mBabbleNode=null;
                mState = State.UNCONFIGURED;

                if (listener!=null) {
                    listener.onComplete();
                }
            }
        });
    }

    public void submitTx(BabbleTx tx) {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot submit when the service isn't running");
        }
        mBabbleNode.submitTx(tx.toBytes());
    }

    public State getState() {
        return mState;
    }

    public void registerObserver(ServiceObserver serviceObserver) {
        if (!mObservers.contains(serviceObserver)) {
            mObservers.add(serviceObserver);
        }
    }

    public void removeObserver(ServiceObserver messageObserver) {
        mObservers.remove(messageObserver);
    }

    private void notifyObservers() {
        for (ServiceObserver observer: mObservers) {
            observer.stateUpdated();
        }
    }

}
