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

package io.mosaicnetworks.babble.servicediscovery;

import android.net.nsd.NsdServiceInfo;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;

import static io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser.APP_IDENTIFIER;
import static io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser.GROUP_NAME;
import static io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser.GROUP_UID;

public class ResolvedService {
    private final InetAddress mInetAddress;
    private final int mPort;
    private final String mAppIdentifier;
    private final String mGroupName;
    private final String mGroupUid;
    private ResolvedGroup mResolvedGroup;
    private final Map<String, byte[]> mServiceAttributes;
    private boolean mAssignedGroup = false;
    private final List<Peer> mInitialPeers;
    private final List<Peer> mCurrentPeers;
    public final int mLastUpdated;

    /**
     * Initialise from components
     */
    public ResolvedService(String groupUid, String groupName, String appIdentifier, int lastUpdated, List<Peer> initialPeers, List<Peer> currentPeers, InetAddress inetAddress, int port) {
        mCurrentPeers = currentPeers;
        mInitialPeers = initialPeers;
        mAppIdentifier = appIdentifier;
        mGroupName = groupName;
        mGroupUid = groupUid;
        mLastUpdated = lastUpdated;
        mInetAddress = inetAddress;
        mPort = port;
        mServiceAttributes = null;

    }

    /**
     * Initialise from service info
     * @param nsdServiceInfo the service info from which service parameters are extracted
     */
    public ResolvedService(NsdServiceInfo nsdServiceInfo) {
        mInetAddress = nsdServiceInfo.getHost();
        mPort = nsdServiceInfo.getPort();
        mServiceAttributes = nsdServiceInfo.getAttributes(); //Min Level API 21
        mAppIdentifier = extractStringAttribute(APP_IDENTIFIER);
        mGroupName = extractStringAttribute(GROUP_NAME);
        mGroupUid = extractStringAttribute(GROUP_UID);

        mInitialPeers = null;
        mCurrentPeers = null;
        mLastUpdated = 0;
    }


    private String extractStringAttribute(String key) {

        if (!mServiceAttributes.containsKey(key)) {
            throw new IllegalArgumentException("Map does not contain attribute: " + key);
        }

        return new String(mServiceAttributes.get(key), Charset.forName("UTF-8"));
    }

    /**
     * Get the group to which this instance has been assigned
     * @return the reoslved group
     */
    public ResolvedGroup getResolvedGroup() {
        return mResolvedGroup;
    }

    /**
     * Assign this service to a group. This can only be done once, attempts to re-assign will result
     * in an {@link IllegalStateException}.
     * @param resolvedGroup the group to which the service should be assigned
     */
    public void setResolvedGroup(ResolvedGroup resolvedGroup) {

        if (mAssignedGroup) {
            throw new IllegalStateException("This service has already been assigned to a group");
        }

        mResolvedGroup = resolvedGroup;
        mAssignedGroup = true;

    }

    /**
     * Get the app identifier
     * @return app identifier
     */
    public String getAppIdentifier() {
        return mAppIdentifier;
    }

    /**
     * Get the group name
     * @return group name
     */
    public String getGroupName() {
        return mGroupName;
    }

    /**
     * Get the group UID
     * @return group UID
     */
    public String getGroupUid() {
        return mGroupUid;
    }

    /**
     * Get the inet address
     * @return
     */
    public InetAddress getInetAddress() {
        return mInetAddress;
    }

    /**
     * Get the port
     * @return the port
     */
    public int getPort() {
        return mPort;
    }

    public List<Peer> getCurrentPeers() {
        return mCurrentPeers;
    }

    public List<Peer> getInitialPeers() {
        return mInitialPeers;
    }



}
