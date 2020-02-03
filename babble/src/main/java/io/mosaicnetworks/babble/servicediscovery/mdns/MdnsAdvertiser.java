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

import java.util.Objects;

public class MdnsAdvertiser {

    public static final String SERVICE_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    public String serviceName;
    private NsdServiceInfo mServiceInfo = new NsdServiceInfo();

    public MdnsAdvertiser(String serviceName, int port) {
        initializeRegistrationListener();

        mServiceInfo.setServiceName(serviceName);
        mServiceInfo.setServiceType(SERVICE_TYPE);
        mServiceInfo.setPort(port);
    }

    public void advertise(Context context) {

        Context appContext = context.getApplicationContext();
        mNsdManager = (NsdManager) appContext.getSystemService(Context.NSD_SERVICE);
        Objects.requireNonNull(mNsdManager).registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void stopAdvertising() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener()  {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {}

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}
        };
    }


}




