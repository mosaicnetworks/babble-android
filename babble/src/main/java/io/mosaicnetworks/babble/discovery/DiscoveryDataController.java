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

package io.mosaicnetworks.babble.discovery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.configure.OnBabbleConfigWritten;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.service.BabbleService2;
import io.mosaicnetworks.babble.servicediscovery.JoinGroupConfirmation;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.ServicesListListener;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.RandomString;
import io.mosaicnetworks.babble.utils.Utils;

public class DiscoveryDataController  implements ServicesListListener {

    private final Lock providerLock = new ReentrantLock(true);
    private final Map<String, DiscoveryDataProvider> mDiscoveryDataProviders = new HashMap<>();
    private ResolvedGroupManager mResolvedGroupManager;
    private JoinGroupConfirmation mJoinGroupConfirmation;
    private OnBabbleConfigWritten mOnBabbleConfigWritten;
    private boolean mIsDiscovering = false;
    private Context mContext;

    public void setMoniker(String moniker) {
        this.mMoniker = moniker;
    }

    private String mMoniker;

    public DiscoveryDataController(Context context, ResolvedGroupManager mResolvedGroupManager) {
        this.mResolvedGroupManager = mResolvedGroupManager;
        this.mContext = context;
    }



    /**
     * Registers a Discovery Data Provider.
     * @param discoveryDataProvider
     * @return the unique ID assigned to this Data Provider
     */
    public String registerDiscoveryProvider(DiscoveryDataProvider discoveryDataProvider) {
        String uid = new RandomString().nextString();
        Log.i("DiscoveryDataController", "registerDiscoveryProvider: "  + uid);
        providerLock.lock();
        try {
            discoveryDataProvider.setUid(uid);
            mDiscoveryDataProviders.put(uid, discoveryDataProvider);
        } finally {
            providerLock.unlock();
        }

        // If we are already globally discovering, we need to start this DDP discovering
        if (mIsDiscovering) {
            discoveryDataProvider.startDiscovery(mContext, mResolvedGroupManager);
        }

        return uid;
    }


    /**
     * Registers a {@link JoinGroupConfirmation} instance. This is used to add custom steps to
     * the workflow when joining a group.
     *
     *
     * @param joinGroupConfirmation
     */
    public void registerJoinGroupConfirmation(JoinGroupConfirmation joinGroupConfirmation) {
        this.mJoinGroupConfirmation = joinGroupConfirmation;
    }

    /**
     * Deregisters the {@link JoinGroupConfirmation} instance
     */
    public void deRegisterJoinGroupConfirmation() {
        this.mJoinGroupConfirmation = null;
    }


    /**
     * Registers a {@link OnBabbleConfigWritten} instance. This is used to find a
     * Babble Service and call back when Babble is running.
     *
     *
     * @param OnBabbleConfigWritten
     */
    public void registerOnBabbleConfigWritten(OnBabbleConfigWritten OnBabbleConfigWritten) {
        this.mOnBabbleConfigWritten = OnBabbleConfigWritten;
    }

    /**
     * Deregisters the {@link OnBabbleConfigWritten} instance
     */
    public void deRegisterOnBabbleConfigWritten() {
        this.mOnBabbleConfigWritten = null;
    }





    public void deregisterDiscoveryProvider(String uid) {
        providerLock.lock();
        try {
            //TODO: Make sure DiscoveryDataProvider shuts down cleanly before it is removed.
            mDiscoveryDataProviders.remove(uid);
        } finally {
            providerLock.unlock();
        }
    }

    public void startDiscovery() {
        mIsDiscovering = true;
        Iterator iterator = mDiscoveryDataProviders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            String key = (String) pair.getKey();
            DiscoveryDataProvider ddp = (DiscoveryDataProvider) pair.getValue();
            ddp.startDiscovery(mContext, mResolvedGroupManager);
        }

    }

    public void stopDiscovery() {
        mIsDiscovering = false;
        Iterator iterator = mDiscoveryDataProviders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            String key = (String) pair.getKey();
            DiscoveryDataProvider ddp = (DiscoveryDataProvider) pair.getValue();
            ddp.stopDiscovery();
        }

    }


    @Override
    public void onServiceSelectedSuccess(ResolvedGroup resolvedGroup) {
        String groupName = resolvedGroup.getGroupName();
        Log.i("DiscDataController", "onServiceSelectedSuccess: "+groupName);

        String dataProviderId = resolvedGroup.getDataProviderId();
        Log.i("DiscDataController", "onServiceSelectedSuccess: "+dataProviderId);

        // Sanity check that we actually can do something with this service
        if (mDiscoveryDataProviders.containsKey(dataProviderId)) {


            // If we have a JoinGroupConfirmation object registered, then call it. Otherwise
            // just join directly.
            if (mJoinGroupConfirmation == null) {
                joinGroup(resolvedGroup);
            } else {
                mJoinGroupConfirmation.joinRequested(this, resolvedGroup);
            }



        }  else {
            Log.e("DiscDataController", "onServiceSelectedSuccess: Unknown Discovery Data Provider" );
            //TODO: Error Handling
        }
    }


    /**
     * When the user selects a service, there is potentially some UI interaction. This could be
     * entering a new moniker, confirming that the user wants to actually join that group or
     * any other number of app specific requirements. Upon completion of those additional steps
     * there is a callback to this function to actually effect the join.
     *
     *
     * @param resolvedGroup
     */
    public void joinGroup(ResolvedGroup resolvedGroup) {

        Log.i("DiscDataController", "joinGroup: Joined " + resolvedGroup.getGroupName());

        resolvedGroup.setMoniker(mMoniker);
        String dataProviderId = resolvedGroup.getDataProviderId();
        DiscoveryDataProvider discoveryDataProvider = mDiscoveryDataProviders.get(dataProviderId);
        discoveryDataProvider.selectedDiscoveryResolveGroup(mContext, resolvedGroup);


        // Turn off Discovery for all DiscoveryDataProviders
        stopDiscovery();

        //TODO: This line will go as we use a service
     //   BabbleService<?> babbleService = mOnBabbleConfigWritten.getBabbleService();


        //TODO: Change this to use a Service

        ConfigManager configManager = ConfigManager.getInstance(mContext);
        GroupDescriptor groupDescriptor = new GroupDescriptor(resolvedGroup, mMoniker);
        ResolvedService resolvedService = resolvedGroup.getRandomService();




        try {
            String configDir = configManager.createConfigJoinGroup(resolvedService.getInitialPeers(),
                resolvedService.getCurrentPeers(), groupDescriptor,
                Utils.getIPAddr(mContext), discoveryDataProvider.getNetworkType() );// NB live pull of IP

            mOnBabbleConfigWritten.startBabbleService(configDir, groupDescriptor, false, discoveryDataProvider.getAdvertiser());
            //     public void startBabbleService(String configDir, GroupDescriptor groupDescriptor,
            //                                   boolean isArchive, ServiceAdvertiser serviceAdvertiser) ;

        } catch (IllegalStateException | CannotStartBabbleNodeException | IOException ex ) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(mContext), R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return ;
        }



    }



    @Override
    public void onServiceSelectedFailure() {
        //TODO: Error handling
    }

    @Override
    public void onDiscoveryStartFailure() {
        //TODO: Error handling
    }

    @Override
    public void onListEmptyStatusChange(boolean empty) {
        //TODO: Error handling
    }



    public String getDiscoveryDataProviderByProtocol(int protocol) {

        Iterator iterator = mDiscoveryDataProviders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            String key = (String) pair.getKey();
            DiscoveryDataProvider ddp = (DiscoveryDataProvider) pair.getValue();
            if ( ddp.getNetworkType() == protocol ) {
                return key;
            }
        }
        //TODO: should throw an error here to make the error condition explicit
        return "";
    }



    public void addNewPseudoResolvedGroup(String dataProviderId, ResolvedGroup resolvedGroup) {

        if (mDiscoveryDataProviders.containsKey(dataProviderId)) {
                mDiscoveryDataProviders.get(dataProviderId).addNewPseudoResolvedGroup(resolvedGroup);

        }  else {
            Log.e("DiscDataController", "onServiceSelectedSuccess: Unknown Discovery Data Provider" );
            //TODO: Error Handling
        }
    }


}
