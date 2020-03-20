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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

public class CustomNsdManager {
    private static final String TAG = "CustomNsdManager";
    private NsdManager mNsdManager;
    private boolean AttemptStockResolveFirst = true;
    private static final int RESOLVE_TIMEOUT = 12000;
    // private static final int RESOLVE_TIMEOUT = 0;

    public CustomNsdManager(Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void discoverServices(String serviceType, int protocolType, NsdManager.DiscoveryListener listener) {
        mNsdManager.discoverServices(serviceType, protocolType, listener);
    }

    public void stopServiceDiscovery(NsdManager.DiscoveryListener listener) {
        mNsdManager.stopServiceDiscovery(listener);
    }

    public void resolveService(final NsdServiceInfo serviceInfo, final NsdManager.ResolveListener listener) {


        // If using alternative method by preference
        if (! AttemptStockResolveFirst) {
            resolveServiceAlternative(serviceInfo, listener);
            return;
        }


        // Standard use stock, failover to alternative
        mNsdManager.resolveService(serviceInfo,new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        listener.onResolveFailed(serviceInfo, errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                        ResolvedService resolvedService;
                        try {
                            resolvedService = ResolvedServiceMdnsFactory.NewJoinResolvedService("mdns", nsdServiceInfo);
                            // No error so we make stock return
                            listener.onServiceResolved(nsdServiceInfo);
                        } catch (IllegalArgumentException ex) {
                            //The txt record doesn't even have the attributes we need, so we'll try the alternate method
                            resolveServiceAlternative(serviceInfo, listener);
                        }
                    }
                }  );
    }

    private void resolveServiceAlternative(NsdServiceInfo serviceInfo, NsdManager.ResolveListener listener) {

        Log.i(TAG, "resolveServiceAlternative: Using Alternative Resolution");
        AttemptStockResolveFirst = true;

        try {
            // We have to transform the Result to an NsdServiceInfo instance so that the
            // stock listeners can be used for either resolving method.

            String serviceName = serviceInfo.getServiceName()+"."+ serviceInfo.getServiceType()+"local"; //TODO something better than hardcoding local
            MdnsCustomResolve.Result result = MdnsCustomResolve.resolve(serviceName, RESOLVE_TIMEOUT) ;
            NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
            nsdServiceInfo.setServiceType(serviceInfo.getServiceType());
            nsdServiceInfo.setServiceName(serviceInfo.getServiceName());
            InetAddress inetAddress = InetAddress.getByName(result.a.ipaddr)   ;
            nsdServiceInfo.setHost(inetAddress);
            nsdServiceInfo.setPort(result.srv.port);

            // Iterate through the TXT record to set the NsdServiceInfo attributes
            Iterator<Map.Entry<String, String>> itr = result.txt.dict.entrySet().iterator();
            while(itr.hasNext())
            {
                Map.Entry<String, String> entry = itr.next();
                nsdServiceInfo.setAttribute(entry.getKey(), entry.getValue());
            }

            Log.i(TAG, "resolveServiceAlternative: Used Alternative Resolution");
            listener.onServiceResolved(nsdServiceInfo);
        } catch (IOException ex) {
            Log.i(TAG, "resolveServiceAlternative Exception: " + ex.getMessage());
            ex.printStackTrace();
            listener.onResolveFailed(serviceInfo, 98);
        }
    }



}
