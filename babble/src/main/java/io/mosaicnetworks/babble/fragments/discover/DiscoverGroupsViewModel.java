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

package io.mosaicnetworks.babble.fragments.discover;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsDiscovery;
import io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCDiscovery;

/**
 * This class implements a ViewModel for the {@link DiscoverGroupsFragment}
 */
public class DiscoverGroupsViewModel extends AndroidViewModel {

    private Context mAppContext;

    private List<ResolvedGroup> mServiceInfoList = new ArrayList<>();
    private MutableLiveData<List<ResolvedGroup>> mMutableServiceInfoList;

    private MdnsDiscovery mMdnsDiscovery;
    private WebRTCDiscovery mWebRTCDiscovery;

    public DiscoverGroupsViewModel(Application application) {
        super(application);

        mAppContext = application.getApplicationContext();

        mMutableServiceInfoList = new MutableLiveData<>();

        initialise();

        mMdnsDiscovery.discoverServices();
        mWebRTCDiscovery.discoverService();
    }

    public void refreshDiscovery() {
        mWebRTCDiscovery.discoverService();
    }

    private void initialise() {
        ServiceDiscoveryListener listener = new ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated (boolean groupCountChange){
                Handler mainHandler = new Handler(mAppContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mMutableServiceInfoList.setValue(mServiceInfoList);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public void onStartDiscoveryFailed () {
                //TODO: how should mdns discovery start failures be handled?
            }
        };

        mMdnsDiscovery = new MdnsDiscovery(mAppContext, mServiceInfoList, listener);
        mWebRTCDiscovery = new WebRTCDiscovery(mAppContext, mServiceInfoList, listener);
    }

    public MutableLiveData<List<ResolvedGroup>> getServiceInfoList() {
        return mMutableServiceInfoList;
    }

    public List<ResolvedGroup> getmServiceInfoList() {
        return mServiceInfoList;
    }
}
