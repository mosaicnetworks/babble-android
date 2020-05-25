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

package io.mosaicnetworks.babble.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleState;
import io.mosaicnetworks.babble.node.BabbleTx;
import io.mosaicnetworks.babble.node.Block;
import io.mosaicnetworks.babble.node.BlockConsumer;
import io.mosaicnetworks.babble.node.LeaveResponseListener;
import io.mosaicnetworks.babble.node.NodeStateChangeHandler;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

/**
 * The Android Service that manages the Babble Node
 */
public class BabbleService extends Service {
    /**
     * The state of the Babble Service
     */
    public enum State {
        /**
         * Babble has stopped
         */
        STOPPED,
        /**
         * Babble is running
         */
        RUNNING,
        /**
         * Babble is in non-gossipping archive mode
         */
        ARCHIVE,
    }

    /**
     * Network type of WiFi i.e. mDNS
     */
    public final static int NETWORK_WIFI = 1;
    /**
     * Network type of Global i.e. WebRTC
     */
    public final static int NETWORK_GLOBAL = 2;

    // mState corresponds to the state of the BabbleService
    private State mState = State.STOPPED;

    // mBabbleNode is a reference to the underlying Babble node
    private BabbleNode mBabbleNode;

    // mNodeState corresponds to the state of the Babble node
    private BabbleNode.State mNodeState;

    // mAppState corresponds to the state of the application that uses this BabbleService
    private static BabbleState mAppState;

    // mServiceAdvertiser is used to expose the presence of the group to which this node belongs.
    // By advertising a group, other users can discover and join it.
    private ServiceAdvertiser mServiceAdvertiser;


    /**
     * Start the service and advertise the group if serviceAdvertiser is not null. The service
     * connects the AppState to a Babble node. The Babble node commits blocks to the AppState via
     * its processBlock handler, and reads its configuration from the configDirectory.
     *
     * @param configDirectory The full path to the babble configuration directory
     * @param serviceAdvertiser The ServiceAdvertiser
     * @throws IllegalStateException if the service isn't currently STOPPED
     */
    public void start(String configDirectory, ServiceAdvertiser serviceAdvertiser) {

        if (mState!= State.STOPPED) {
            throw new IllegalStateException("Cannot start service which isn't stopped");
        }

        mServiceAdvertiser = serviceAdvertiser;

        mBabbleNode = BabbleNode.create(
                new BlockConsumer() {
                    @Override
                    public Block onReceiveBlock(Block block) {
                        Log.i("ProcessBlock", "Process block");
                        Block processedBlock = mAppState.processBlock(block);
                        notifyObservers();
                        return processedBlock;
                    }
                },
                configDirectory,
                new NodeStateChangeHandler() {
                    @Override
                    public void onStateChanged(BabbleNode.State state) {
                        Log.i("OnStateChanged", state.name());
                        mNodeState = state;
                    }
                }
        );

        mBabbleNode.run();

        if (mServiceAdvertiser != null) {
            mServiceAdvertiser.advertise(mBabbleNode);
        }

        mState = State.RUNNING;
    }


    /**
     * This is an asynchronous call to start the service in archive mode
     * @param configDirectory The full path to the Babble configuration directory
     * @throws IllegalStateException if the service isn't currently STOPPED
     */
    public void startArchive(final String configDirectory) {

        if (mState!=State.STOPPED) {
            throw new IllegalStateException("Cannot start archive service which isn't stopped");
        }

        mState = State.ARCHIVE;

        new Thread(new Runnable() {
            public void run() {
                mBabbleNode = BabbleNode.create(
                    new BlockConsumer() {
                        @Override
                        public Block onReceiveBlock(Block block) {
                            Log.i("ProcessBlock", "Process block");
                            Block processedBlock = mAppState.processBlock(block);
                            notifyObservers();
                            return processedBlock;
                        }
                    },
                    configDirectory,
                    new NodeStateChangeHandler() {
                        @Override
                        public void onStateChanged(BabbleNode.State state) {
                            mNodeState = state;
                        }
                    }
                );
            }
        }).start();
    }

    /**
     * Set the {@link BabbleState} for the service
     * @param appState
     */
    public static void setAppState(BabbleState appState) {
        mAppState = appState;
    }

    /**
     * Get the {@link BabbleState} for the service
     * @return
     */
    public BabbleState getAppState() {
        return mAppState;
    }

    /**
     * Asynchronous method for leaving a group. Stop advertising and call Babble leave method to
     * exit the group politely.
     * @param listener called when the leave completes
     * @throws IllegalStateException if the service is not currently running
     */
    public void leave(final LeaveResponseListener listener) {
        if (mState== State.STOPPED) {
            throw new IllegalStateException("Service is not running");
        }

        if (mServiceAdvertiser!=null) {
            mServiceAdvertiser.stopAdvertising();
        }

        if (mBabbleNode==null) {
            //If an archive fails to load then the babble node can be null
            mState = State.STOPPED;
            mAppState.reset();
            stopSelf();

            if (listener != null) {
                listener.onComplete();
            }

        } else {

            mBabbleNode.leave(new LeaveResponseListener() {
                @Override
                public void onComplete() {
                    mBabbleNode = null;
                    mState = State.STOPPED;
                    mAppState.reset();
                    stopSelf();

                    if (listener != null) {
                        listener.onComplete();
                    }
                }
            });
        }
    }

    /**
     * Submit a transaction
     * @param tx the transaction, which must implement {@link BabbleTx}
     * @throws IllegalStateException if the service is not currently running
     */
    public void submitTx(BabbleTx tx) {
        if (!(mState== State.RUNNING)) {
            throw new IllegalStateException("Cannot submit when the service isn't running");
        }
        mBabbleNode.submitTx(tx.toBytes());
    }

    /**
     * Gets the current peers set from the Babble Node
     * @return
     */
    public String getMonikerList() {
        return mBabbleNode.getCurrentPeers();
    }

    /**
     * Retrieves a JSON formatted list of stats from the Babble Node
     * @return
     */
    public String getStats() {
        return mBabbleNode.getStats();
    }


    //##############################################################################################
    // Service specific section

    private static final int NOTIF_ID = 1;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        BabbleService getService() {
            return BabbleService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        //configure();
        //start();

        startForeground(NOTIF_ID, getNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceAdvertiser!=null) {
            mServiceAdvertiser.stopAdvertising();
        }
    }

    //##############################################################################################
    // Observer components

    private List<ServiceObserver> mObservers = new ArrayList<>();

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

    //##############################################################################################
    // Creating notifications

    public Notification getNotification() {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle(getResources().getString(R.string.babble_service_running));

        return mBuilder
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "babble service running";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("babble service channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }

        return "babble service channel";

    }

}
