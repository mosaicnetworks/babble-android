package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.net.nsd.NsdServiceInfo;

public class NsdDiscoveredService {

    private String mServiceName;
    private String mServiceType;

    NsdDiscoveredService(NsdServiceInfo nsdServiceInfo) {
        mServiceName = nsdServiceInfo.getServiceName();
        mServiceType = nsdServiceInfo.getServiceType();
    }

    public String getServiceName() {
        return mServiceName;
    }

    public void setServiceName(String serviceName) {
        mServiceName = serviceName;
    }

    public String getServiceType() {
        return mServiceType;
    }

    public void setServiceType(String serviceType) {
        mServiceType = serviceType;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }


        if (!(o instanceof NsdDiscoveredService)) {
            return false;
        }

        NsdDiscoveredService service = (NsdDiscoveredService) o;

        return service.getServiceName().equals(mServiceName);
    }
}
