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

package io.mosaicnetworks.babble.servicediscovery.p2p;

import android.net.nsd.NsdServiceInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.InterfaceResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.InterfaceResolvedService;
import com.google.common.net.InetAddresses;

/**
 * A class representing service information for network service discovery. Unlike the
 * {@link NsdServiceInfo} class this class unpacks the data in the TXT record into class properties.
 * In this sense it holds NSD info specific to this app's service.
 *
 * Several instances of this class may refer to the same group that are advertised by a different
 * hosts. Instances of the {@link P2PResolvedGroup} class can be used to hold a collection of
 * {@link P2PResolvedService} that represent the same group. In this sense a {@link P2PResolvedService} is
 * assigned to a group. This assignment can be set by calling the
 */
public final class P2PResolvedService implements InterfaceResolvedService {

    private final InetAddress mInetAddress ;
    private final int mPort;
    private final String mAppIdentifier;
    private final String mGroupName;
    private final String mGroupUid;
    private P2PResolvedGroup mResolvedGroup;
    private final Map<String, byte[]> mServiceAttributes;
    private boolean mAssignedGroup = false;

    /**
     * Initialise from service info
     */
    public P2PResolvedService(String uuID, Map map) throws UnknownHostException {

        /*

    public final static String DNS_TXT_PORT_LABEL = "port";
    public final static String DNS_TXT_MONIKER_LABEL = "moniker";
    public final static String DNS_TXT_DNS_VERSION_LABEL = "textvers";
    private final static String DNS_VERSION = "0.0.1";
    public final static String DNS_TXT_BABBLE_VERSION_LABEL = "babblevers";

    public final static String DNS_TXT_GROUP_LABEL = "group";

         */

   //     mInetAddress =  InetAddresses.forString((String) map.get(P2PService.DNS_TXT_HOST_LABEL));
        mInetAddress =  InetAddresses.forString((String) map.get(BabbleConstants.DNS_TXT_HOST_LABEL));
        mPort = Integer.parseInt((String) map.get(BabbleConstants.DNS_TXT_PORT_LABEL));

        mServiceAttributes = new HashMap<>(); //nsdServiceInfo.getAttributes();

        mAppIdentifier = (String) map.get(BabbleConstants.DNS_TXT_APP_LABEL);
        mGroupName = (String) map.get(BabbleConstants.DNS_TXT_GROUP_LABEL);
        mGroupUid = uuID;
    }

    /**
     * Get the group to which this instance has been assigned
     * @return the reoslved group
     */
    public InterfaceResolvedGroup getResolvedGroup() {
        return mResolvedGroup;
    }

    /**
     * Assign this service to a group. This can only be done once, attempts to re-assign will result
     * in an {@link IllegalStateException}.
     * @param resolvedGroup the group to which the service should be assigned
     */
    public void setResolvedGroup(InterfaceResolvedGroup resolvedGroup) {

        if (mAssignedGroup) {
            throw new IllegalStateException("This service has already been assigned to a group");
        }

        mResolvedGroup = (P2PResolvedGroup) resolvedGroup;
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
}
