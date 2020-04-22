/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        ARCHIVE,
    }

    private final List<ServiceObserver> mObservers = new ArrayList<>();
    private State mState = State.STOPPED;
    protected BabbleNode mBabbleNode;
    protected GroupDescriptor mGroupDescriptor;

    protected boolean mIsArchive = false;

    public int getNetworkType() {
        return mNetworkType;
    }

    public void setNetworkType(int networkType) {
        this.mNetworkType = mNetworkType;
    }

    public final static int NETWORK_NONE = 0;
    public final static int NETWORK_WIFI = 1;
    public final static int NETWORK_P2P = 2;
    public final static int NETWORK_GLOBAL = 3;

    protected int mNetworkType;

    //TODO: populate this from Gradle
    public final static String BABBLE_VERSION = "0.6.2";



    /**
     * The underlying app state, to which babble transactions are applied
     */
    public AppState state;

    /**
     * Constructor
     * @param babbleState the underlying app state, to which babble transactions are applied
     */
    protected BabbleService(AppState babbleState, Context context) {
        state = babbleState;
    }

    /**
     * Start the service
     * @throws IllegalStateException if the service is currently running
     */
    public void start(String configDirectory, GroupDescriptor groupDescriptor) {
        start(configDirectory, groupDescriptor, NETWORK_WIFI);  // Defaults to the original mDNS configuration
    }


    /**
     * Start the service
     *
     * @param configDirectory The full path to the babble configuration directory
     * @param networkType BabbleService.NETWORK_NONE for archive, NETWORK_WIFI or NETWORK_P2P for mDNS or WiFi Direct
     * @throws IllegalStateException if the service is currently running
     */
    public void start(String configDirectory, int networkType) {
        start(configDirectory, null, networkType);    // Defaults to the original mDNS configuration
    }



    /**
     * Start the service
     *
     * @param configDirectory The full path to the babble configuration directory
     * @param groupDescriptor The group descriptor
     * @param networkType BabbleService.NETWORK_NONE for archive, NETWORK_WIFI or NETWORK_P2P for mDNS or WiFi Direct
     * @throws IllegalStateException if the service is currently running
     */
    public void start(String configDirectory, GroupDescriptor groupDescriptor, int networkType) {
        if (mState!=State.STOPPED) {
            throw new IllegalStateException("Cannot start service which isn't stopped");
        }

        mBabbleNode = BabbleNode.create(new BlockConsumer() {
            @Override
            public Block onReceiveBlock(Block block) {
                Log.i("ProcessBlock", "Process block");
                Block processedBlock = state.processBlock(block);
                notifyObservers();
                return processedBlock;
            }
        }, configDirectory, null);

        this.mNetworkType = networkType;
        this.mIsArchive = (this.mNetworkType == 0) ;

        mBabbleNode.run();

        mState = State.RUNNING;
        mGroupDescriptor = groupDescriptor;

        onStarted();
    }

    public interface StartArchiveListener {

        void onInitialised();

        void onFailed();
    }

    /**
     * This is an asynchronous call to start the service in archive mode
     * @param configDirectory
     * @param groupDescriptor
     */
    public void startArchive(final String configDirectory, GroupDescriptor groupDescriptor,
                             final StartArchiveListener listener) {
        if (mState!=State.STOPPED) {
            throw new IllegalStateException("Cannot start archive service which isn't stopped");
        }

        mState = State.ARCHIVE;
        mIsArchive = true;
        new Thread(new Runnable() {
            public void run() {
                try {
                    mBabbleNode = BabbleNode.create(new BlockConsumer() {
                        @Override
                        public Block onReceiveBlock(Block block) {
                            Log.i("ProcessBlock", "Process block");
                            Block processedBlock = state.processBlock(block);
                            notifyObservers();
                            return processedBlock;
                        }
                    }, configDirectory, null);

                } catch (IllegalArgumentException ex) {
                    //TODO: need more refined Babble exceptions
                    if (listener!=null) {
                        listener.onFailed();
                    }
                    return;
                }

                if (listener!=null) {
                    listener.onInitialised();
                }

            }
        }).start();

        mGroupDescriptor = groupDescriptor;
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

        //TODO: guard against babble node possibly null when in archive mode

        if (mBabbleNode==null) {
            //If an archive fails to load then the babble node can be null
            mState = State.STOPPED;
            mGroupDescriptor = null;
            state.reset();
            onStopped();

            if (listener != null) {
                listener.onComplete();
            }

        } else {

            mBabbleNode.leave(new LeaveResponseListener() {
                @Override
                public void onComplete() {
                    mBabbleNode = null;
                    mState = State.STOPPED;
                    mGroupDescriptor = null;
                    state.reset();

                    if (listener != null) {
                        listener.onComplete();
                    }
                }
            });
        }

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

    public String getMonikerList() {
        return mBabbleNode.getCurrentPeers();
    }


    public String getStats() {
        return mBabbleNode.getStats();
    }

}
