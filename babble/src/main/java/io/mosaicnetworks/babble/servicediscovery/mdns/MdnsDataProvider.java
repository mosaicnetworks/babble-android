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

package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import io.mosaicnetworks.babble.discovery.DiscoveryDataProvider;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;

public class MdnsDataProvider implements DiscoveryDataProvider {
    private String mUid;
    private final String TAG = "MdsnDataProvider";
    private ResolvedGroupManager mResolvedGroupManager;
    private CustomNsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    private String mPackageName;
    private final Map<String, ResolvedService> mResolvedServices = new HashMap<>();
    private final List<ResolvedGroup> mResolvedGroupList = new ArrayList<>();

    private MdnsAdvertiser2 mMdnsAdvertiser;


    private boolean mIsDiscovering = false;


    public MdnsDataProvider(Context context) {
        Context appContext = context.getApplicationContext();
        mPackageName =  BabbleConstants.APP_ID() ;


        Log.i(TAG, "MdnsDataProvider: Created");
    }


    /**
     * Called from DiscoveryDataController to assign a unique id to this data provider.
     * @param uid
     */
    @Override
    public void setUid(String uid){
        this.mUid = uid;
    }

    @Override
    public void startDiscovery(Context context,  ResolvedGroupManager resolvedGroupManager) {
        Log.i(TAG, "startDiscovery: ");


        this.mResolvedGroupManager = resolvedGroupManager;
        mNsdManager = new CustomNsdManager(context, mUid);

//        mResolvedGroups = resolvedGroups;
//        mServiceDiscoveryListener = serviceDiscoveryListener;

        initializeDiscoveryListener();

        mNsdManager.discoverServices(
                ResolvedService.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


        mIsDiscovering = true;
    }

    @Override
    public void stopDiscovery() {
        mIsDiscovering = false;
    }

    @Override
    public void selectedDiscoveryResolveGroup(Context context, ResolvedGroup resolvedGroup) {
        mMdnsAdvertiser = new MdnsAdvertiser2(context, resolvedGroup);
        Log.i(TAG, "selectedDiscoveryResolveGroup: selected " + resolvedGroup.getGroupName());
    }

    @Override
    public ServiceAdvertiser getAdvertiser() {
        return mMdnsAdvertiser;
    };


    private void pushListToResolvedGroupManager() {
        mResolvedGroupManager.setList(mUid, mResolvedGroupList);
    }


    private void initializeDiscoveryListener() {

        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                mIsDiscovering = true;
            }
            @Override
            public void onServiceFound(NsdServiceInfo discoveredServiceInfo) {
                String serviceName = discoveredServiceInfo.getServiceName();
                Log.i(TAG, "onServiceFound: "+serviceName);
                //TODO: if statement commented out to help testing
//                if (discoveredServiceInfo.getServiceType().equals(ServiceAdvertiser.SERVICE_TYPE)) {

                    if (mResolvedServices.containsKey(serviceName)) {
                        //we already have this service
                        Log.i(TAG, "onServiceFound: Already found "+serviceName);  //TODO: remove this debug line
                        return;
                    };

                    resolveService(discoveredServiceInfo);
  //              }
            }
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {

                if (mResolvedServices.containsKey(serviceInfo.getServiceName())) {
                    ResolvedService lostService = mResolvedServices.get(serviceInfo.getServiceName());
                    boolean empty = lostService.getResolvedGroup().removeService(lostService);
                    mResolvedServices.remove(serviceInfo.getServiceName());

                    if (empty) {
                        mResolvedGroupList.remove(lostService.getResolvedGroup());
                        pushListToResolvedGroupManager();
                    }
                }

            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                mIsDiscovering = false;
                mResolvedServices.clear();
                mResolvedGroupList.clear();
                pushListToResolvedGroupManager();
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                pushListToResolvedGroupManager();
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                //do nothing
            }
        };
    }



    private void resolveService(final NsdServiceInfo serviceInfo) {


        String serviceName = serviceInfo.getServiceName();

        Log.i(TAG, "resolveService: " + serviceName);

        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                //do nothing
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {

                //TODO: is this check needed?
                String serviceName = serviceInfo.getServiceName();
                Log.i(TAG, "onServiceResolved: "+ serviceName);
                if (mResolvedServices.containsKey(serviceName)) {
                    //we already have this service
                    Log.i(TAG, "onServiceResolved: Already got "+ serviceName);
                    return;
                };

                ResolvedService resolvedService;
                try {
                    resolvedService = ResolvedServiceMdnsFactory.NewJoinResolvedService(mUid, nsdServiceInfo);
                } catch (IllegalArgumentException ex) {
                    //The txt record doesn't even have the attributes we need, so we'll try the alternate method
                    onResolveFailed(serviceInfo, 97);
                    Log.e(TAG, "onServiceResolved: failed to resolve" );
                    return;
                }


                if (!resolvedService.getAppIdentifier().equals(mPackageName)) {
                    //The service is not for this app, we'll skip it
                    Log.e(TAG, "onServiceResolved: package mismatch "+ mPackageName + " to " + resolvedService.getAppIdentifier() );
            //TODO: Restore this line to restore app check.
          //          return;
                }

                mResolvedServices.put(nsdServiceInfo.getServiceName(), resolvedService);

                for (ResolvedGroup group:mResolvedGroupList) {
                    if (group.getGroupUid().equals(resolvedService.getGroupUid())) {
                        resolvedService.setResolvedGroup(group);
                        group.addService(resolvedService);

                        pushListToResolvedGroupManager();
                        return;
                    }
                }

                //no matching group - create a new group
                ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);
                mResolvedGroupList.add(resolvedGroup);
                resolvedService.setResolvedGroup(resolvedGroup);
                pushListToResolvedGroupManager();

            }
        });
    }

    @Override
    public int getNetworkType() {
        return BabbleConstants.NETWORK_WIFI;
    }

    @Override
    public void addNewPseudoResolvedGroup(ResolvedGroup resolvedGroup) {
        // We stop discovery to prevent overwriting our manually inserted group
        stopDiscovery();

        mResolvedServices.put(resolvedGroup.getGroupUid(), resolvedGroup.getRandomService());
        mResolvedGroupList.add(resolvedGroup);
    }
}
