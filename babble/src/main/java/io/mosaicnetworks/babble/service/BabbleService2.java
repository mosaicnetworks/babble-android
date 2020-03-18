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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.BabbleState;
import io.mosaicnetworks.babble.node.BabbleTx;
import io.mosaicnetworks.babble.node.Block;
import io.mosaicnetworks.babble.node.BlockConsumer;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.node.LeaveResponseListener;
import io.mosaicnetworks.babble.node.ServiceObserver;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

public class BabbleService2 extends Service {

    public enum State {
        STOPPED,
        RUNNING,
        ARCHIVE,
    }

    private BabbleNode mBabbleNode;
    private State mState = State.STOPPED;
    private GroupDescriptor mGroupDescriptor;
    private static BabbleState mAppState;
    private ServiceAdvertiser mServiceAdvertiser;
    private boolean mIsArchive = false;

    /**
     * Start the service
     *
     * @param configDirectory The full path to the babble configuration directory
     * @param groupDescriptor The group descriptor
     * @throws IllegalStateException if the service is currently running
     */
    public void start(String configDirectory, GroupDescriptor groupDescriptor,
                      ServiceAdvertiser serviceAdvertiser) {
        if (mState!= State.STOPPED) {
            throw new IllegalStateException("Cannot start service which isn't stopped");
        }

        mServiceAdvertiser = serviceAdvertiser;
        mGroupDescriptor = groupDescriptor;

        mBabbleNode = BabbleNode.create(new BlockConsumer() {
            @Override
            public Block onReceiveBlock(Block block) {
                Log.i("ProcessBlock", "Process block");
                Block processedBlock = mAppState.processBlock(block);
                notifyObservers();
                return processedBlock;
            }
        }, configDirectory);

        mBabbleNode.run();
        mServiceAdvertiser.advertise(mBabbleNode.getGenesisPeers(), mBabbleNode.getCurrentPeers());
        mState = State.RUNNING;
    }

    /**
     * This is an asynchronous call to start the service in archive mode
     * @param configDirectory
     * @param groupDescriptor
     */
    public void startArchive(final String configDirectory, GroupDescriptor groupDescriptor,
                             final BabbleService.StartArchiveListener listener) {
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
                            Block processedBlock = mAppState.processBlock(block);
                            notifyObservers();
                            return processedBlock;
                        }
                    }, configDirectory);

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

    public static void setAppState(BabbleState appState) {
        mAppState = appState;
    }

    public BabbleState getAppState() {
        return mAppState;
    }

    /**
     * Asynchronous method for leaving a group
     * @param listener called when the leave completes
     * @throws IllegalStateException if the service is not currently running
     */
    public void leave(final LeaveResponseListener listener) {
        if (mState== State.STOPPED) {
            throw new IllegalStateException("Service is not running");
        }

        mServiceAdvertiser.stopAdvertising();

        if (mBabbleNode==null) {
            //If an archive fails to load then the babble node can be null
            mState = State.STOPPED;
            mGroupDescriptor = null;
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
                    mGroupDescriptor = null;
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


    //###########
    // service specific stuff

    private static final int NOTIF_ID = 1;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        BabbleService2 getService() {
            return BabbleService2.this;
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

    //#############
    // observer stuff

    private List<ServiceObserver> mObservers = new ArrayList<>();;

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
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("Babble service is running");
        Notification notification = mBuilder
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();


        return notification;
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "snap map fake location ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }

        return "snap map channel";

    }

    //##############################################################################################

}
