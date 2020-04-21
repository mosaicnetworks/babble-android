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

package io.mosaicnetworks.babble.servicediscovery.archive;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.DiscoveryDataProvider;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.mdns.CustomNsdManager;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser2;
import io.mosaicnetworks.babble.servicediscovery.mdns.ResolvedServiceMdnsFactory;

/**
 * This class spoofs discovery for archive to allow the same service code to be reused.
 * N.B. because it is assumed the actual live discovery will be displayed, the archive items
 * are never exported to the main view. It would be possible to properly generate lists and
 * produce a single combined list - but it is a worse UX so we have not implemented it.
 */
public class ArchiveDataProvider implements DiscoveryDataProvider {
    private String mUid;
    private final String TAG = "ArchiveDataProvider";
    private ResolvedGroupManager mResolvedGroupManager;
    private String mPackageName;
    private boolean mIsDiscovering = false;
    private ResolvedGroup mResolvedGroup;


    public ArchiveDataProvider(Context context) {
        Context appContext = context.getApplicationContext();
        mPackageName =  BabbleConstants.APP_ID() ;

        Log.i(TAG, "ArchiveDataProvider: Created");
    }


    /**
     * Called from DiscoveryDataController to assign a unique id to this data provider.
     * @param uid
     */
    @Override
    public void setUid(String uid){
        this.mUid = uid;
    }

    @Override
    public void startDiscovery(Context context,  ResolvedGroupManager resolvedGroupManager) {
        Log.i(TAG, "startDiscovery: ");

        // Not needed, but left wired up as it could conceivably be of use.
        this.mResolvedGroupManager = resolvedGroupManager;

        // Whilst this flag is not really needed, it seemed neater to leave it in situ
        mIsDiscovering = true;
    }

    @Override
    public void stopDiscovery() {
        mIsDiscovering = false;
    }

    @Override
    public void selectedDiscoveryResolveGroup(Context context, ResolvedGroup resolvedGroup) {
        Log.i(TAG, "selectedDiscoveryResolveGroup: selected " + resolvedGroup.getGroupName());
    }

    @Override
    public ServiceAdvertiser getAdvertiser() {
        return null;
    };


    private void pushListToResolvedGroupManager() {
            // DO NOTHING
    }


    @Override
    public int getNetworkType() {
        return BabbleConstants.NETWORK_NONE;
    }

    @Override
    public void addNewPseudoResolvedGroup(ResolvedGroup resolvedGroup) {
        // We stop discovery to prevent overwriting our manually inserted group
        stopDiscovery();

        mResolvedGroup = resolvedGroup;
    }
}
