package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.List;

public class MdnsDiscovery {

    private static final String TAG = "MdnsDiscovery";
    private final List<NsdServiceInfo> mDiscoveredServices;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    public interface ServiceDiscoveryListener {
        void onServiceListUpdated();
        void onStartDiscoveryFailed();
    }

    public interface ResolutionListener {
        void onServiceResolved(NsdServiceInfo service);
        void onResolveFailed();
    }

    public MdnsDiscovery(Context context, List<NsdServiceInfo> discoveredServices,
                         ServiceDiscoveryListener serviceDiscoveryListener) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mDiscoveredServices = discoveredServices;
        initializeDiscoveryListener(serviceDiscoveryListener);
    }

    private void initializeDiscoveryListener(final ServiceDiscoveryListener serviceDiscoveryListener) {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {

                if (serviceInfo.getServiceType().equals(MdnsAdvertiser.SERVICE_TYPE)) {
                    Log.d(TAG, "Service discovery success" + serviceInfo);
                    mDiscoveredServices.add(serviceInfo);
                    serviceDiscoveryListener.onServiceListUpdated();
                }
            }
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "service lost" + serviceInfo);
                mDiscoveredServices.remove(serviceInfo);
                serviceDiscoveryListener.onServiceListUpdated();
            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                serviceDiscoveryListener.onStartDiscoveryFailed();
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    private void initializeResolveListener(final ResolutionListener resolutionListener) {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
                resolutionListener.onResolveFailed();
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                resolutionListener.onServiceResolved(serviceInfo);
            }
        };
    }

    public void discoverServices() {
        //#############
        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceName("bob");
        nsdServiceInfo.setServiceType(MdnsAdvertiser.SERVICE_TYPE);
        mDiscoveryListener.onServiceFound(nsdServiceInfo);
        //#############

        mNsdManager.discoverServices(
                MdnsAdvertiser.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void resolveService(NsdServiceInfo serviceInfo, ResolutionListener resolutionListener) {
        initializeResolveListener(resolutionListener);
        mNsdManager.resolveService(serviceInfo, mResolveListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
}
