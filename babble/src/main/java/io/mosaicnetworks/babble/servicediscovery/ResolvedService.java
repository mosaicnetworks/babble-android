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
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;

/**
 * A class representing service information for network service discovery. Unlike the
 * {@link NsdServiceInfo} class this class unpacks the data in the TXT record into class properties.
 * In this sense it holds NSD info specific to this app's service.
 *
 * Several instances of this class may refer to the same group that are advertised by a different
 * hosts. Instances of the {@link ResolvedGroup} class can be used to hold a collection of
 * {@link ResolvedService} that represent the same group. In this sense a {@link ResolvedService} is
 * assigned to a group. This assignment can be set by calling the
 * {@link #setResolvedGroup(ResolvedGroup)} method.
 */
public final class ResolvedService {


    public static final String SERVICE_TYPE = "_babble._tcp.";
    public static final String APP_IDENTIFIER = "appIdentifier";
    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_UID = "groupUid";


    private final String mDataProviderId;
    private final String mInetAddress;
    private final String mInetString;

    private final int mDiscoveryPort;
    private final int mBabblePort;

    private final String mAppIdentifier;
    private final String mGroupName;
    private final String mGroupUid;
    private ResolvedGroup mResolvedGroup;
    private final Map<String, String> mServiceAttributes;
    private final List<Peer> mInitialPeers;
    private final List<Peer> mCurrentPeers;



    private boolean mAssignedGroup = false;

    public ResolvedService(String dataProviderId, String inetAddress,
                           String inetString,
                           int babblePort, int discoveryPort, Map<String,
                           String> serviceAttributes, String appIdentifier, String groupName,
                           String groupUID, List<Peer> initialPeers,
                           List<Peer> currentPeers ) {
        this.mInetAddress = inetAddress;
        this.mBabblePort = babblePort;
        this.mDiscoveryPort = discoveryPort;
        this.mServiceAttributes = serviceAttributes;
        this.mAppIdentifier = appIdentifier;
        this.mGroupName = groupName;
        this.mGroupUid = groupUID;
        this.mDataProviderId = dataProviderId;
        this.mInitialPeers = initialPeers;
        this.mCurrentPeers = currentPeers;

        if (inetString.equals("")) {
            this.mInetString = mInetAddress;
        } else {
            this.mInetString = inetString;
        }

    }


    /**
     * Returns the given attribute. If the attribute is not set, it returns an empty string
     *
     * @param key
     * @return
     */
    public String getAttribute(String key) {
        if (mServiceAttributes.containsKey(key)) {
            return mServiceAttributes.get(key);
        }

        return "";
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

        mResolvedGroup =  resolvedGroup;
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
     * Get the Data Provider ID
     * @return  Data Provider ID
     */
    public String getDataProviderId() {
        return mDataProviderId;
    }

    /**
     * Get the inet address
     * @return
     */
    public String getInetAddress() {
        return mInetAddress;
    }

    /**
     * Get the discovery port
     * @return the discovery port
     */
    public int getDiscoveryPort() {
        return mDiscoveryPort;
    }

    /**
     * Get the babble port
     * @return the babble port
     */
    public int getBabblePort() {
        return mBabblePort;
    }


    /**
     * Gets the string representation of the InetAddress. For some protocols
     * such as WebRTC, this is used for the NetAddr field as the underlying
     * field will not be an IP address (it uses a public key instead).
     *
     * @return
     */
    public String getInetString() {
        return mInetString;
    }

    //TODO: Javadocs

    public List<Peer> getInitialPeers() {
        return mInitialPeers;
    }

    public List<Peer> getCurrentPeers() {
        return mCurrentPeers;
    }
}
