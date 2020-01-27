package io.mosaicnetworks.babble.node;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;

/**
 * This is a wrapper around {@link BabbleNode} to allow the node to run as a service. Beyond what a
 * {@link BabbleNode} provides, this class:
 * 1. Provides a lifecycle around the node configure -> start -> leave -> configure
 * 2. Allows observers to register and unregister
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



    //private final static Gson mGson = new Gson();
    private final List<ServiceObserver> mObservers = new ArrayList<>();
    private State mState = State.UNCONFIGURED;
    private KeyPair mKeyPair = new KeyPair();
    protected BabbleNode mBabbleNode;
    private BabbleState mBabbleState;
    private String mConfigDir;
    private String mSubConfigDir = "config";

    private String mAppId; // Set by default in the construct to the App Package Name

    /**
     * The underlying app state, to which babble transactions are applied
     */
    public AppState state;

    /**
     * Constructor
     * @param babbleState the underlying app state, to which babble transactions are applied
     */
    public BabbleService(AppState babbleState, Context context) {
        mBabbleState = babbleState;
        state = babbleState; //TODO: this is just mirroring mBabbleState
        mConfigDir = context.getApplicationContext().getFilesDir().toString();
        mAppId = context.getApplicationContext().getPackageName();
    }



    private void configure(List<Peer> genesisPeers, List<Peer> currentPeers, String moniker, String inetAddress, int babblingPort, int discoveryPort, boolean isArchive) throws CannotStartBabbleNodeException {

        if (mState == State.RUNNING || mState == State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        /*
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
                mNodeConfig, mConfigDir, mSubConfigDir, mConfigFolderBackupPolicy, mAppId);

        mBabbleState.reset();
        mState = State.CONFIGURED;
         */
    }

    /**
     * Start the service
     * @throws IllegalStateException if the service is currently running or is unconfigured
     */
    public void start(String configDirectory) {
        /*
        TODO: sort out this logic
        if (mState==State.UNCONFIGURED || mState==State.RUNNING ||
                mState==State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot start an unconfigured or running service");
        }

         */


        mBabbleNode = BabbleNode.create(new BlockConsumer() {
                                            @Override
                                            public Block onReceiveBlock(Block block) {
                                                Log.d("MY-TAG", "Process block");
                                                Block processedBlock = mBabbleState.processBlock(block);
                                                notifyObservers();
                                                return processedBlock;
                                            }
                                        }, configDirectory);
        mBabbleNode.run();
        mState=State.RUNNING;

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

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onComplete() {
                mBabbleNode=null;
                mState = State.UNCONFIGURED;
                mBabbleState.reset();

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


//    /**
//     * Get the genesis peers set
//     * @return a list of the genesis peers
//     */
//    public List<Peer> getGenesisPeers() {
//        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
//            throw new IllegalStateException("Cannot get peers when the service isn't running");
//        }
//
//        Peer[] peers = mGson.fromJson(mBabbleNode.getGenesisPeers(), Peer[].class);
//
//        return new ArrayList<>(Arrays.asList(peers));
//    }
//
//    /**
//     * Get the current peers set
//     * @return a list of the current peers
//     */
//    public List<Peer> getCurrentPeers() {
//        if (!(mState==State.RUNNING || mState==State.RUNNING_WITH_DISCOVERY)) {
//            throw new IllegalStateException("Cannot get peers when the service isn't running");
//        }
//
//        Peer[] peers = mGson.fromJson(mBabbleNode.getCurrentPeers(), Peer[].class);
//
//        return new ArrayList<>(Arrays.asList(peers));
//    }


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
