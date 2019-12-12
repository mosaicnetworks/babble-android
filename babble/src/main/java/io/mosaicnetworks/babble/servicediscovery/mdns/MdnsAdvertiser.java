package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

public class MdnsAdvertiser {

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    public String serviceName;
    private NsdServiceInfo mServiceInfo = new NsdServiceInfo();

    public MdnsAdvertiser(String serviceName, int port) {
        initializeRegistrationListener();

        mServiceInfo.setServiceName(serviceName);
        mServiceInfo.setServiceType("_http._tcp.");
        mServiceInfo.setPort(port);
    }

    public void advertise(Context context) {

        Context appContext = context.getApplicationContext();
        mNsdManager = (NsdManager) appContext.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void stopAdvertising() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener()  {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {}

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}
        };
    }


}




