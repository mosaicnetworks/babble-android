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
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsDiscovery;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsResolvedGroup;

public class DiscoverGroupsViewModel extends AndroidViewModel {
    private MutableLiveData<SelectableData<ConfigDirectory>> mArchivedList;
    private ConfigManager mConfigManager;
    private Context mAppContext;
    private List<MdnsResolvedGroup> mServiceInfoList = new ArrayList<>();
    private MdnsDiscovery mMdnsDiscovery;

    public DiscoverGroupsViewModel(Application application, ConfigManager configManager) {
        super(application);
        mConfigManager = configManager;
        mAppContext = application.getApplicationContext();

        mArchivedList = new MutableLiveData<>();
        loadArchiveList();
        initialise();

        mMdnsDiscovery.discoverServices();
    }

    public void loadArchiveList() {
        SelectableData<ConfigDirectory> data = new SelectableData<>();
        data.addAll(mConfigManager.getDirectories());
        mArchivedList.setValue(data);
    }

    public MutableLiveData<SelectableData<ConfigDirectory>> getArchivedList() {
        return mArchivedList;
    }

    //###################
    private void initialise() {
        mMdnsDiscovery = new MdnsDiscovery(mAppContext, mServiceInfoList, new ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated ( boolean groupCountChange){
                // let the adapter know
                Log.d("MY-TAG", "Service list updated");
            }

            @Override
            public void onStartDiscoveryFailed () {
                Log.d("MY-TAG", "Start discovery failed");
            }
        });
    }
}
