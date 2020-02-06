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

import android.net.nsd.NsdServiceInfo;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Map;

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

    public ResolvedService(NsdServiceInfo nsdServiceInfo) {
        mInetAddress = nsdServiceInfo.getHost();
        mPort = nsdServiceInfo.getPort();
        mServiceAttributes = nsdServiceInfo.getAttributes(); //TODO: can this be null?
        mAppIdentifier = extractStringAttribute(APP_IDENTIFIER);
        mGroupName = extractStringAttribute(GROUP_NAME);
        mGroupUid = extractStringAttribute(GROUP_UID);
    }

    private String extractStringAttribute(String key) {
        //TODO: check what happens with empty attribute names ""
        //TODO: check what happens when bytes are not valid UTF-8

        if (!mServiceAttributes.containsKey(key)) {
            throw new IllegalArgumentException("Map does not contain attribute: " + key);
        }

        return new String(mServiceAttributes.get(key), Charset.forName("UTF-8"));
    }

    public ResolvedGroup getResolvedGroup() {
        return mResolvedGroup;
    }

    public void setResolvedGroup(ResolvedGroup resolvedGroup) {
        //TODO: limit to one call
        mResolvedGroup = resolvedGroup;
    }

    public String getAppIdentifier() {
        return mAppIdentifier;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getGroupUid() {
        return mGroupUid;
    }

    public InetAddress getInetAddress() {
        return mInetAddress;
    }

    public int getPort() {
        return mPort;
    }
}
