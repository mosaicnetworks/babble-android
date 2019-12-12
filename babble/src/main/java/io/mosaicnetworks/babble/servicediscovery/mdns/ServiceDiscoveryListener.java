package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.net.nsd.NsdServiceInfo;

public interface ServiceDiscoveryListener {

    void onDiscoverService(NsdServiceInfo nsdServiceInfo);
}
