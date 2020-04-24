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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;

/**
 * This class encapsulates the MDNS discover process.
 */
public class MdnsDiscovery {
    private static final String TAG = "MdnsDiscovery";

    private final Map<String, MdnsResolvedService> mResolvedServices = new HashMap<>();
    private final List<MdnsResolvedGroup> mResolvedGroups;
    private CustomNsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private boolean mDiscoveryActive = false;
    private ServiceDiscoveryListener mServiceDiscoveryListener;

    private final String mPackageName;




    public MdnsDiscovery(Context context, List<MdnsResolvedGroup> resolvedGroups,
                         ServiceDiscoveryListener serviceDiscoveryListener) {
        Context appContext = context.getApplicationContext();
        mNsdManager = new CustomNsdManager(context);

        mResolvedGroups = resolvedGroups;
        mServiceDiscoveryListener = serviceDiscoveryListener;
        mPackageName =  appContext.getPackageName() ;

        initializeDiscoveryListener(serviceDiscoveryListener);

    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                MdnsAdvertiser.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    public void stopDiscovery() {
        if (mDiscoveryActive) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    private void initializeDiscoveryListener(final ServiceDiscoveryListener serviceDiscoveryListener) {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                mDiscoveryActive = true;
            }
            @Override
            public void onServiceFound(NsdServiceInfo discoveredServiceInfo) {
                if (discoveredServiceInfo.getServiceType().equals(MdnsAdvertiser.SERVICE_TYPE)) {

                    if (mResolvedServices.containsKey(discoveredServiceInfo.getServiceName())) {
                        //we already have this service
                        return;
                    };

                    resolveService(discoveredServiceInfo);
                }
            }
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {

                if (mResolvedServices.containsKey(serviceInfo.getServiceName())) {
                    MdnsResolvedService lostService = mResolvedServices.get(serviceInfo.getServiceName());
                    boolean empty = lostService.getResolvedGroup().removeService(lostService);
                    mResolvedServices.remove(serviceInfo.getServiceName());

                    if (empty) {
                        mResolvedGroups.remove(lostService.getResolvedGroup());
                    }

                    serviceDiscoveryListener.onServiceListUpdated(empty);
                }

            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                mDiscoveryActive = false;
                mResolvedServices.clear();
                mResolvedGroups.clear();
                serviceDiscoveryListener.onServiceListUpdated(true);
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                serviceDiscoveryListener.onStartDiscoveryFailed();
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                //do nothing
            }
        };
    }


    private void resolveService(final NsdServiceInfo serviceInfo) {

        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                //do nothing
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {

                //TODO: is this check needed?
                if (mResolvedServices.containsKey(nsdServiceInfo.getServiceName())) {
                    //we already have this service
                    return;
                };

                MdnsResolvedService resolvedService;
                try {
                    resolvedService = new MdnsResolvedService(nsdServiceInfo);
                } catch (IllegalArgumentException ex) {
                    //The txt record doesn't even have the attributes we need, so we'll try the alternate method
                    onResolveFailed(serviceInfo, 97);
                    return;
                }

                if (!resolvedService.getAppIdentifier().equals(mPackageName)) {
                    //The service is not for this app, we'll skip it
                    return;
                }

                mResolvedServices.put(nsdServiceInfo.getServiceName(), resolvedService);

                for (MdnsResolvedGroup group:mResolvedGroups) {
                    if (group.getGroupUid().equals(resolvedService.getGroupUid())) {
                        resolvedService.setResolvedGroup(group);
                        group.addService(resolvedService);

                        mServiceDiscoveryListener.onServiceListUpdated(true);
                        return;
                    }
                }

                //no matching group - create a new group
                MdnsResolvedGroup resolvedGroup = new MdnsResolvedGroup(resolvedService);
                mResolvedGroups.add(resolvedGroup);
                resolvedService.setResolvedGroup(resolvedGroup);
                mServiceDiscoveryListener.onServiceListUpdated(false);

            }
        });
    }

}
