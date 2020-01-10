package io.mosaicnetworks.babble.node;

import com.google.gson.Gson;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;

/**
 * This is a wrapper around {@link BabbleNode} to allow the node to run as a service. Beyond what a
 * {@link BabbleNode} provides, this class:
 * 1. Provides a lifecycle around the node configure -> start -> leave -> configure
 * 2. Allows observers to register and unregister
 * 3. Starts a {@link HttpPeerDiscoveryServer} which starts and stops with the node
 * @param <AppState>
 */
public abstract class BabbleService<AppState extends BabbleState> {

    /**
     * The current state of the service
     */
    public enum State {
        UNCONFIGURED,
        CONFIGURED,
        RUNNING,
        RUNNING_WITH_DISCOVERY
    }

    /**
     * The default babbling port. This can be overridden when configuring the service
     */
    public static final int DEFAULT_BABBLING_PORT = 6666;

    /**
     * The default discovery port. This can be overridden when configuring the service
     */
    public static final int DEFAULT_DISCOVERY_PORT = 8988;
    private final static Gson mGson = new Gson();
    private final List<ServiceObserver> mObservers = new ArrayList<>();
    private State mState = State.UNCONFIGURED;
    private KeyPair mKeyPair = new KeyPair();
    private BabbleNode mBabbleNode;
    private BabbleState mBabbleState;
    private final BabbleConfig mNodeConfig;
    private String mConfigDir;
    private String mSubConfigDir = "config";

    /**
     * The underlying app state, to which babble transactions are applied
     */
    public AppState state;

    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;

    /**
     * Constructor
     * @param babbleState the underlying app state, to which babble transactions are applied
     */
    public BabbleService(AppState babbleState, Context context) {
        mBabbleState = babbleState;
        state = babbleState; //TODO: this is just mirroring mBabbleState
        mNodeConfig = new BabbleConfig.Builder().build();
        mConfigDir = context.getApplicationContext().getFilesDir().toString();
        Log.d("BabbleService", "ConfigDir: "+mConfigDir);
    }

    /**
     * Constructor with config parameter
     * @param babbleState the underlying app state, to which babble transactions are applied
     * @param nodeConfig the node configuration
     */
    public BabbleService(AppState babbleState, BabbleConfig nodeConfig, Context context) {
        mBabbleState = babbleState;
        state = babbleState; //TODO: this is just mirroring mBabbleState
        mNodeConfig = nodeConfig;
        mConfigDir = context.getApplicationContext().getFilesDir().toString();
        Log.d("BabbleService", "ConfigDir: "+mConfigDir);
    }

    /**
     * Configure the service to create a new group using the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public void configureNew(String moniker, String inetAddress) {
        configureNew(moniker, inetAddress, DEFAULT_BABBLING_PORT, DEFAULT_DISCOVERY_PORT);
    }

    /**
     * Configure the service to create a new group, overriding the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consesnsus
     * @param discoveryPort the port used by the {@link HttpPeerDiscoveryServer}
     * @throws IllegalStateException if the service is currently running
     */
    public void configureNew(String moniker, String inetAddress, int babblingPort, int discoveryPort) {
        List<Peer> genesisPeers = new ArrayList<>();
        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));

        configure(genesisPeers, currentPeers, moniker, inetAddress, babblingPort, discoveryPort);
    }

    /**
     * Configure the service to join an existing group using the default ports
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public void configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress) {
        configure(genesisPeers, currentPeers, moniker, inetAddress, DEFAULT_BABBLING_PORT, DEFAULT_DISCOVERY_PORT);
    }

    /**
     *
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * @param discoveryPort the port used by the {@link HttpPeerDiscoveryServer}
     * @throws IllegalStateException if the service is currently running
     */
    public void configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress, int babblingPort, int discoveryPort) {
        configure(genesisPeers, currentPeers, moniker, inetAddress, babblingPort, discoveryPort);
    }

    private void configure(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress, int babblingPort, int discoveryPort) {

        if (mState == State.RUNNING || mState == State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        mBabbleNode = BabbleNode.createWithConfig(genesisPeers, currentPeers,
                mKeyPair.privateKey, inetAddress,
                babblingPort, moniker,
                new BlockConsumer() {
                    @Override
                    public Block onReceiveBlock(Block block) {
                        Block processedBlock = mBabbleState.processBlock(block);
                        notifyObservers();
                        return processedBlock;
                    }
                },
                mNodeConfig, mConfigDir, mSubConfigDir);

        mBabbleState.reset();
        mState = State.CONFIGURED;

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(inetAddress, discoveryPort, mBabbleNode);
    }

    /**
     * Start the service
     * @throws IllegalStateException if the service is currently running or is unconfigured
     */
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

        onStarted();
    }

    /**
     * Asynchronous method for leaving a group
     * @param listener called when the leave completes
     * @throws IllegalStateException if the service is not currently running
     */
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

        onStopped();
    }

    /**
     * Submit a transaction
     * @param tx the transaction, which must implement {@link BabbleTx}
     * @throws IllegalStateException if the service is not currently running
     */
    public void submitTx(BabbleTx tx) {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot submit when the service isn't running");
        }
        mBabbleNode.submitTx(tx.toBytes());
    }

    /**
     * Get the genesis peers set
     * @return a list of the genesis peers
     */
    public List<Peer> getGenesisPeers() {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot get peers when the service isn't running");
        }

        Peer[] peers = mGson.fromJson(mBabbleNode.getGenesisPeers(), Peer[].class);

        return new ArrayList<>(Arrays.asList(peers));
    }

    /**
     * Get the current peers set
     * @return a list of the current peers
     */
    public List<Peer> getCurrentPeers() {
        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
            throw new IllegalStateException("Cannot get peers when the service isn't running");
        }

        Peer[] peers = mGson.fromJson(mBabbleNode.getCurrentPeers(), Peer[].class);

        return new ArrayList<>(Arrays.asList(peers));
    }



    /**
     * Get the public key
     * @return public key
     */
    public String getPublicKey() {
        return mKeyPair.publicKey;
    }

    /**
     * Get the current state of the service
     * @return current state if the service
     */
    public State getState() {
        return mState;
    }

    protected void onStarted() {}

    protected void onStopped() {}

    /**
     * Register an observer
     * @param serviceObserver the observer to be registered, the observer must implement the
     * {@link ServiceObserver} interface
     */
    public void registerObserver(ServiceObserver serviceObserver) {
        if (!mObservers.contains(serviceObserver)) {
            mObservers.add(serviceObserver);
        }
    }

    /**
     * Remove an observer
     * @param messageObserver the observer to be removed, the observer must implement the
     *                        {@link ServiceObserver} interface
     */
    public void removeObserver(ServiceObserver messageObserver) {
        mObservers.remove(messageObserver);
    }

    private void notifyObservers() {
        for (ServiceObserver observer: mObservers) {
            observer.stateUpdated();
        }
    }

}
