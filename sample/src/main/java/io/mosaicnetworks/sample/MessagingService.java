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

package io.mosaicnetworks.sample;

import android.content.Context;

import java.io.IOException;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryServer;

import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser;

/**
 * This is a singleton which provides a Messaging service. It extends the {@link BabbleService}
 * class providing the base class with the {@link ChatState} and implementing a singleton pattern
 */
public final class MessagingService extends BabbleService<ChatState> {


    private static int sDiscoveryPort = 8988;

    private static MessagingService INSTANCE;
    private MdnsAdvertiser mMdnsAdvertiser;
    private Context mAppContext;
    private HttpPeerDiscoveryServer mHttpPeerDiscoveryServer;
    private boolean mAdvertising = false;

    /**
     * Factory for the {@link MessagingService}
     * @return a messaging service
     */
    public static MessagingService getInstance(Context context) {
        if (INSTANCE==null) {
            INSTANCE = new MessagingService(context.getApplicationContext());
        }

        return INSTANCE;
    }

    private MessagingService(Context context) {
        super(new ChatState(), context);

        mAppContext = context;
    }

    @Override
    protected void onStarted() {
        //TODO: should this be part of the base service?

        super.onStarted();
        mMdnsAdvertiser = new MdnsAdvertiser(mGroupDescriptor, sDiscoveryPort, mAppContext);

        mHttpPeerDiscoveryServer = new HttpPeerDiscoveryServer(sDiscoveryPort, mBabbleNode); //TODO: use next available port?
        try {
            mHttpPeerDiscoveryServer.start();
            mMdnsAdvertiser.advertise(); // start mDNS advertising if server started
            mAdvertising = true;
        } catch (IOException ex) {
            //Probably the port is in use, we'll continue without the discovery service
        }
    }

    @Override
    protected void onStopped() {
        super.onStopped();
        if (mMdnsAdvertiser != null) {
            mMdnsAdvertiser.stopAdvertising();
            mMdnsAdvertiser = null;
        }

        if (mHttpPeerDiscoveryServer != null) {
            mHttpPeerDiscoveryServer.stop();
            mHttpPeerDiscoveryServer = null;
        }

        mAdvertising = false;
    }

    public boolean isAdvertising() {
        return mAdvertising;

    }


    /**
     * Gets the port for the Discovery Server, defaults to 8988
     * @return the port number
     */
    public static int getDiscoveryPort() {
        return sDiscoveryPort;
    }

    /**
     * Sets the port for the DiscoveryServer. If not called, will default to 8988.
     * @param discoveryPort the port number
     */
    public static void setDiscoveryPort(int discoveryPort) {
        MessagingService.sDiscoveryPort = discoveryPort;
    }




}


