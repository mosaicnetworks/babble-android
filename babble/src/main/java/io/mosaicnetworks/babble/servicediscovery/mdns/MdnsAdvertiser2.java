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
import java.util.Objects;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;
import io.mosaicnetworks.babble.discovery.PeersProvider;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.utils.RandomString;

public class MdnsAdvertiser2 implements ServiceAdvertiser {

    public static final String SERVICE_TYPE = "_babble._tcp.";
    public static final String APP_IDENTIFIER = "appIdentifier";
    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_UID = "groupUid";
    private static int sDiscoveryPort = 8988;
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private String mServiceName;
    private NsdServiceInfo mServiceInfo = new NsdServiceInfo();
    private Context mAppContext;
    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;
    private String mCurrentPeers;
    private boolean mAdvertising = false;

    public MdnsAdvertiser2(GroupDescriptor groupDescriptor, Context context) {
        initializeRegistrationListener();

        mAppContext = context.getApplicationContext();
        mServiceInfo.setServiceType(SERVICE_TYPE);
        mServiceName = new RandomString(32).nextString();
        mServiceInfo.setServiceName(mServiceName);
        mServiceInfo.setAttribute(APP_IDENTIFIER, mAppContext.getPackageName());
        // https://developer.android.com/studio/build/application-id note: The application ID
        // used to be directly tied to your code's package name; so some Android APIs use the term
        // "package name" in their method names and parameter names, but this is actually your
        // application ID. For example, the Context.getPackageName() method
        // returns your application ID
        mServiceInfo.setAttribute(GROUP_NAME, groupDescriptor.getName());
        mServiceInfo.setAttribute(GROUP_UID, groupDescriptor.getUid());
        mServiceInfo.setPort(sDiscoveryPort);
    }

    @Override
    public boolean advertise(final String genesisPeers, final String currentPeers, PeersProvider peersProvider) {

        mCurrentPeers = currentPeers;

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(sDiscoveryPort, peersProvider);

        try {
            mHttpPeerDiscoveryServer.start();
        } catch (IOException ex) {
            //Probably the port is in use, we'll continue without the discovery service
            return false;
        }

        mNsdManager = (NsdManager) mAppContext.getSystemService(Context.NSD_SERVICE);
        Objects.requireNonNull(mNsdManager).registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

        mAdvertising = true;

        //TODO: is it worth this using the success/fail callback of the NSD manager service registration?
        return true;
    }

    @Override
    public void onPeersChange(String newPeers) {
        mCurrentPeers = newPeers;
    }

    @Override
    public void stopAdvertising() {

        if (mAdvertising) {
            mNsdManager.unregisterService(mRegistrationListener);

            mHttpPeerDiscoveryServer.stop();
            mHttpPeerDiscoveryServer = null;
            mAdvertising = false;
        }
    }

    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener()  {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name we initially requested
                // with the name Android actually used - We chose a random string
                // anyway, so:
                // 1) It's almost certain to be unique
                // 2) We don't care if Android changes it
                mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {}

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}
        };
    }

    public String getServiceName() {
        return mServiceName;
    }
}




