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

import java.util.List;

public class MdnsDiscovery {

    private static final String TAG = "MdnsDiscovery";
    private final List<NsdDiscoveredService> mDiscoveredServices;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private boolean mDiscoveryActive = false;

    public interface ServiceDiscoveryListener {
        void onServiceListUpdated();
        void onStartDiscoveryFailed();
    }

    public interface ResolutionListener {
        void onServiceResolved(NsdServiceInfo service);
        void onResolveFailed();
    }

    public MdnsDiscovery(Context context, List<NsdDiscoveredService> discoveredServices,
                         ServiceDiscoveryListener serviceDiscoveryListener) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mDiscoveredServices = discoveredServices;
        initializeDiscoveryListener(serviceDiscoveryListener);
    }

    private void initializeDiscoveryListener(final ServiceDiscoveryListener serviceDiscoveryListener) {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                mDiscoveryActive = true;
            }
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {

                if (serviceInfo.getServiceType().equals(MdnsAdvertiser.SERVICE_TYPE)) {
                    Log.d(TAG, "Service discovery success" + serviceInfo);

                    NsdDiscoveredService discoveredService = new NsdDiscoveredService(serviceInfo);

                    if (!mDiscoveredServices.contains(discoveredService)) {
                        mDiscoveredServices.add(discoveredService);
                    }

                    serviceDiscoveryListener.onServiceListUpdated();
                }
            }
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "service lost" + serviceInfo);
                mDiscoveredServices.remove(new NsdDiscoveredService(serviceInfo));
                serviceDiscoveryListener.onServiceListUpdated();
            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                mDiscoveryActive = false;
                mDiscoveredServices.clear();
                serviceDiscoveryListener.onServiceListUpdated();
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                serviceDiscoveryListener.onStartDiscoveryFailed();
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    private void initializeResolveListener(final ResolutionListener resolutionListener) {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
                resolutionListener.onResolveFailed();
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                resolutionListener.onServiceResolved(serviceInfo);
            }
        };
    }

    public void discoverServices() {

        mNsdManager.discoverServices(
                MdnsAdvertiser.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    public void resolveService(NsdDiscoveredService serviceInfo, ResolutionListener resolutionListener) {
        initializeResolveListener(resolutionListener);

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceType(serviceInfo.getServiceType());
        nsdServiceInfo.setServiceName(serviceInfo.getServiceName());
        mNsdManager.resolveService(nsdServiceInfo, mResolveListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryActive) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }
}
