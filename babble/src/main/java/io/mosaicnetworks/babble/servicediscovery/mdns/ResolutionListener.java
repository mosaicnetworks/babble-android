package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.net.nsd.NsdServiceInfo;

public interface ResolutionListener {
    void onResolved(NsdServiceInfo service);
}
