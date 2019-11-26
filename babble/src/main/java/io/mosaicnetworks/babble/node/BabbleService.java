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

    public static final int DEFAULT_BABBLING_PORT = 6666;
    public static final int DEFAULT_DISCOVERY_PORT = 8988;
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
        configureNew(moniker, inetAddress, DEFAULT_BABBLING_PORT, DEFAULT_DISCOVERY_PORT);
    }

    public void configureNew(String moniker, String inetAddress, int babblingPort, int discoveryPort) {
        List<Peer> genesisPeers = new ArrayList<>();
        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
        List<Peer> currentPeers = genesisPeers;

        configure(genesisPeers, currentPeers, moniker, inetAddress, babblingPort, discoveryPort);
    }

    public void configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress) {
        configure(genesisPeers, currentPeers, moniker, inetAddress, DEFAULT_BABBLING_PORT, DEFAULT_DISCOVERY_PORT);
    }

    public void configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress, int babblingPort, int discoveryPort) {
        configure(genesisPeers, currentPeers, moniker, inetAddress, babblingPort, discoveryPort);
    }

    private void configure(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress, int babblingPort, int discoveryPort) {

        if (mState == State.RUNNING || mState == State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        mBabbleNode = BabbleNode.createWithConfig(genesisPeers, currentPeers,
                mKeyPair.privateKey, inetAddress,
                babblingPort, moniker, new TxConsumer() {
                    @Override
                    public byte[] onReceiveTransactions(byte[][] transactions) {

                        byte[] stateHash = mBabbleState.applyTransactions(transactions);

                        notifyObservers();

                        return stateHash;
                    }
                },
                new BabbleConfig.Builder().logLevel(BabbleConfig.LogLevel.DEBUG).build());

        mBabbleState.reset();
        mState = State.CONFIGURED;

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(inetAddress, discoveryPort, mBabbleNode);
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
        mHttpPeerDiscoveryServer = null;

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
