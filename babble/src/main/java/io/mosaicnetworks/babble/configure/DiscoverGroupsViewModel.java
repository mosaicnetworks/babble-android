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

package io.mosaicnetworks.babble.configure;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsDiscovery;
import io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCService;

/**
 * This class implements a ViewModel for the {@link DiscoverGroupsFragment}
 */
public class DiscoverGroupsViewModel extends AndroidViewModel {

    private Context mAppContext;

    private List<ResolvedGroup> mServiceInfoList = new ArrayList<>();
    private MutableLiveData<List<ResolvedGroup>> mMutableServiceInfoList;

    private MdnsDiscovery mMdnsDiscovery;
    private WebRTCService mWebRTCService;

    public DiscoverGroupsViewModel(Application application, ConfigManager configManager) {
        super(application);
        mAppContext = application.getApplicationContext();

        initialise();
        initialiseWebRtcDiscovery();
        mMutableServiceInfoList = new MutableLiveData<>();
        mMdnsDiscovery.discoverServices();
        mWebRTCService.discoverService();
    }

    public void refreshDiscovery() {
        mWebRTCService.discoverService();
    }

    private void initialise() {
        mMdnsDiscovery = new MdnsDiscovery(mAppContext, mServiceInfoList, new ServiceDiscoveryListener() {
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
        });
    }

    private void initialiseWebRtcDiscovery() {
        mWebRTCService = WebRTCService.getInstance(mAppContext);

        mWebRTCService.setResolvedGroups(mServiceInfoList);

        mWebRTCService.registerServiceDiscoveryListener(new ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated(boolean groupCountChange) {
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
            public void onStartDiscoveryFailed() {

            }
        });
    }

    public MutableLiveData<List<ResolvedGroup>> getServiceInfoList() {
        return mMutableServiceInfoList;
    }

    public List<ResolvedGroup> getmServiceInfoList() {
        return mServiceInfoList;
    }
}
