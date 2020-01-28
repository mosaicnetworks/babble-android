package io.mosaicnetworks.babble.node;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a wrapper around {@link BabbleNode} to allow the node to run as a pseudo service. Beyond
 * what a {@link BabbleNode} provides, this class:
 * 1. Allows the Node to be stopped and started
 * 2. Incorporates the State to which Babble transactions are applied
 * @param <AppState>
 */
public abstract class BabbleService<AppState extends BabbleState> {

    /**
     * The current state of the service
     */
    public enum State {
        STOPPED,
        RUNNING,
    }

    private final List<ServiceObserver> mObservers = new ArrayList<>();
    private State mState = State.STOPPED;
    protected BabbleNode mBabbleNode;
    protected String mGroupName;

    /**
     * The underlying app state, to which babble transactions are applied
     */
    public AppState state;

    /**
     * Constructor
     * @param babbleState the underlying app state, to which babble transactions are applied
     */
    public BabbleService(AppState babbleState, Context context) {
        state = babbleState;
    }

    /**
     * Start the service
     * @throws IllegalStateException if the service is currently running
     */
    public void start(String configDirectory, String groupName) {
        if (mState==State.RUNNING) {
            Log.e("BabbleService.start", "Service is already running");
            throw new IllegalStateException("Service is already running");
        }

        mBabbleNode = BabbleNode.create(new BlockConsumer() {
                                            @Override
                                            public Block onReceiveBlock(Block block) {
                                                Log.i("ProcessBlock", "Process block");
                                                Block processedBlock = state.processBlock(block);
                                                notifyObservers();
                                                return processedBlock;
                                            }
                                        }, configDirectory);
        mBabbleNode.run();
        mState = State.RUNNING;
        mGroupName = groupName;

        onStarted();
    }

    /**
     * Asynchronous method for leaving a group
     * @param listener called when the leave completes
     * @throws IllegalStateException if the service is not currently running
     */
    public void leave(final LeaveResponseListener listener) {
        if (mState==State.STOPPED) {
            throw new IllegalStateException("Service is not running");
        }

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onComplete() {
                mBabbleNode=null;
                mState = State.STOPPED;
                mGroupName = null;
                state.reset();

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
        if (!(mState==State.RUNNING)) {
            throw new IllegalStateException("Cannot submit when the service isn't running");
        }
        mBabbleNode.submitTx(tx.toBytes());
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
