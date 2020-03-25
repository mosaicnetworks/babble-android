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

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ResolvedGroupManager {

    // The combined List that is wired into the Recycler View
    private final List<ResolvedGroup> mCombinedResolvedGroups;
    private final Lock groupsLock = new ReentrantLock(true);

    // A Map of Lists. THre is one per Discovery Data Provider
    private final Map<String, List<ResolvedGroup>>  mIndividualResolvedGroups= new HashMap<>();

    private ServicesListUpdater mServicesListUpdater;
    private Context mContext; //TODO: Try to remove this

    public ResolvedGroupManager(Context context, List<ResolvedGroup> resolvedGroups) {
        this.mCombinedResolvedGroups = resolvedGroups;
        this.mContext = context;
    }


    public void setList(String uid, List<ResolvedGroup> resolvedGroups){
        Log.i("ResolvedGroupManager", "setList: ");
        groupsLock.lock();
        try {
            mIndividualResolvedGroups.put(uid, resolvedGroups);
            rebuildCombinedList();
        } finally {
            groupsLock.unlock();
        }
    }

    public void registerServicesListUpdater(ServicesListUpdater servicesListUpdater){
        this.mServicesListUpdater = servicesListUpdater;
    }

    public void removeList(String uid){
        groupsLock.lock();
        try {
            mIndividualResolvedGroups.remove(uid);
            rebuildCombinedList();
        } finally {
            groupsLock.unlock();
        }
    }


    private void rebuildCombinedList(){
        // This function should be called from within the locked sections of other functions
        mCombinedResolvedGroups.clear();
        for (Map.Entry<String, List<ResolvedGroup>> entry : mIndividualResolvedGroups.entrySet()) {
            mCombinedResolvedGroups.addAll(entry.getValue());
        }

        Log.i("ResolvedGroupManager", "rebuildCombinedList: "+mIndividualResolvedGroups.size());

        if (mServicesListUpdater != null) {
            mServicesListUpdater.onServiceListUpdated(mContext, true);
        } else {
            Log.e("ResolvedGroupManager", "rebuildCombinedList: mServicesListUpdater is null" );
        }

    }


}
