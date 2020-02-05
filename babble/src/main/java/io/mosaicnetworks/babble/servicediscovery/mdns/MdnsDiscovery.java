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
    private final List<NsdResolvedService> mResolvedServices;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private boolean mDiscoveryActive = false;
    private ServiceDiscoveryListener mServiceDiscoveryListener;

    public interface ServiceDiscoveryListener {
        void onServiceListUpdated();
        void onStartDiscoveryFailed();
    }

    public MdnsDiscovery(Context context, List<NsdResolvedService> resolvedServices,
                         ServiceDiscoveryListener serviceDiscoveryListener) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mResolvedServices = resolvedServices;
        mServiceDiscoveryListener = serviceDiscoveryListener;
        initializeDiscoveryListener(serviceDiscoveryListener);
    }

    private void initializeDiscoveryListener(final ServiceDiscoveryListener serviceDiscoveryListener) {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                mDiscoveryActive = true;
            }
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {

                if (serviceInfo.getServiceType().equals(MdnsAdvertiser.SERVICE_TYPE)) {

                    //TODO: confusing naming
                    NsdResolvedService discoveredService = new NsdResolvedService(serviceInfo);

                    if (!mResolvedServices.contains(discoveredService)) {
                        resolveService(serviceInfo);
                    }
                }
            }
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "service lost" + serviceInfo);
                mResolvedServices.remove(new NsdResolvedService(serviceInfo));
                serviceDiscoveryListener.onServiceListUpdated();
            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                mDiscoveryActive = false;
                mResolvedServices.clear();
                serviceDiscoveryListener.onServiceListUpdated();
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

    public void discoverServices() {

        mNsdManager.discoverServices(
                MdnsAdvertiser.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    private void resolveService(NsdServiceInfo serviceInfo) {

        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                //do nothing
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mResolvedServices.add(new NsdResolvedService(serviceInfo));
                mServiceDiscoveryListener.onServiceListUpdated();
            }
        });
    }

    public void stopDiscovery() {
        if (mDiscoveryActive) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }
}
