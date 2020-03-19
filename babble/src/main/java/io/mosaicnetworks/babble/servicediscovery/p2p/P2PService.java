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

package io.mosaicnetworks.babble.servicediscovery.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
// import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import androidx.core.util.Consumer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.configure.OnNetworkInitialised;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.utils.RandomString;

/**
 * Service class to handle WiFi Direct (P2P)
 */
public class P2PService implements ServiceAdvertiser{

    private ServiceDiscoveryListener mServiceDiscoveryListener;
    private OnNetworkInitialised mNetworkInitListener;
    private P2PConnected mP2PConnected;

    public static Consumer<String> mLogFunc;


// If you get errors like this:
// D/WifiP2pManager: Ignored { when=-4ms what=139313 target=android.net.wifi.p2p.WifiP2pManager$Channel$P2pHandler }
// The what is defined relative to a BASE as defined in https://android.googlesource.com/platform/frameworks/base.git/+/android-4.3_r2.1/core/java/com/android/internal/util/Protocol.java
// public static final int BASE_WIFI_P2P_MANAGER = 0x00022000;
// 0x22000 is 139264, 139313 = BASE + 49
// In the WifiP2pManager.Channel source file
// public static final int PING = BASE + 49;



    // Service type should not need to change, but other values are available at:
    // http://www.dns-sd.org/ServiceTypes.html
//    private final static String P2P_SERVICE_TYPE = "_presence._tcp";
//    private final static String P2P_SERVICE_TYPE = "_babble._tcp";


    // Keys used for the hash map in DNS text file

    private final static String TAG = "P2PService";
    private final static String DNS_VERSION = "0.0.1";
    public static boolean mGroupCreated = false;  //TODO: verify this is set to false on exit.

    private static String mMoniker = "Moniker";

    public static boolean getIsDiscovering() {
        return mIsDiscovering;
    }

    public static void setIsDiscovering(boolean mIsDiscovering) {
        P2PService.mIsDiscovering = mIsDiscovering;
        Log.i(TAG, "SetIsDiscovering: " + mIsDiscovering);
    }

    private static boolean mIsDiscovering = false;

    private static P2PService INSTANCE;
    private Context mAppContext;
//    private WifiManager mWifiMgr;
    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mChannel;
    public BroadcastReceiver mReceiver;
    public IntentFilter mIntentFilter;

    private final Map<String, ResolvedService> mResolvedServices = new HashMap<>();
    private List<ResolvedGroup> mResolvedGroups;  //TODO: Look to make this final
//    final HashMap<String, Map> textRecords = new HashMap<>();

    private String mServiceName ;


    /**
     * Factory for the {@link P2PService}
     *
     * @return a messaging service
     */
    public static P2PService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new P2PService(context.getApplicationContext());
        }
        return INSTANCE;
    }


    private P2PService(Context context) {
        mAppContext = context;
        Log.i(TAG,"Constructed");
        initP2P();
    }


    public void registerOnNetworkInitialised(OnNetworkInitialised listener) {
        this.mNetworkInitListener = listener;
    }


    public void registerP2PConnected(P2PConnected listener) {
        this.mP2PConnected = listener;
    }



    public void registerServiceDiscoveryListener(ServiceDiscoveryListener listener){
        this.mServiceDiscoveryListener  =listener;
    }


    public void setResolvedGroups(List<ResolvedGroup> resolvedGroups) {
        this.mResolvedGroups = resolvedGroups;
    }

    protected void initP2P(){
        // mWifiMgr = (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);
        //       mWifiMgr.setWifiEnabled(false);
        // This is deprecated in API 29 and on. 3rd party apps may no longer turn WiFi on or off.
        //TODO: Remove all the mWifiMgr code which has been commented out for now.

        mWifiP2pMgr = (WifiP2pManager) mAppContext.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        Log.i(TAG, "Got manager" );

        mChannel = mWifiP2pMgr.initialize(mAppContext, mAppContext.getMainLooper(), null);
        Log.i(TAG, "Initialised manager" );

        mReceiver = new WifiDirectBroadcastReceiver(mWifiP2pMgr, mChannel, mAppContext);
        Log.i(TAG, "Created Receiver" );

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        Log.i(TAG, "Added intent actions" );



    }


    public void stopRegistration(){

    }


    public void startRegistration(String moniker, String groupName, String babbleVersion, boolean startDiscoverService) {
        //  Create a string map containing information about your service.

        Log.i(TAG, "startRegistration()");
        HashMap<String,String> record = new HashMap<String,String>();

        mServiceName = new RandomString(32).nextString();
        record.put(BabbleConstants.DNS_TXT_GROUP_LABEL, groupName);
        record.put(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, mServiceName);
        // This is a bit chicken and egg.
        // But we know what the address of the leader will be, so it is safe to do this.
        record.put(BabbleConstants.DNS_TXT_HOST_LABEL, BabbleConstants.DEFAULT_P2P_IP_ADDRESS);
        record.put(BabbleConstants.DNS_TXT_PORT_LABEL, String.valueOf(BabbleConstants.DISCOVERY_PORT()));
        record.put(BabbleConstants.DNS_TXT_APP_LABEL, BabbleConstants.APP_ID());
        record.put(BabbleConstants.DNS_TXT_MONIKER_LABEL, moniker); // "John Doe" + (int) (Math.random() * 1000));
        record.put(BabbleConstants.DNS_TXT_DNS_VERSION_LABEL, DNS_VERSION);
        record.put(BabbleConstants.DNS_TXT_BABBLE_VERSION_LABEL, babbleVersion);

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
//        WifiP2pDnsSdServiceInfo serviceInfo =
  //              WifiP2pDnsSdServiceInfo.newInstance(mServiceName, P2P_SERVICE_TYPE, record);

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_Babble", BabbleConstants.P2P_SERVICE_TYPE(), record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mWifiP2pMgr.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                Log.i(TAG,"Added local service");
                mGroupCreated = false;
                createGroup(0);
         //       onInitialiseNetworkCallback();   //TODO: JK - Review and remove
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.i(TAG,"Failed to add local service");
            }
        });

        if (startDiscoverService) {
            discoverService();
        }
    }

    public void onInitialiseNetworkCallback() {
        if (this.mNetworkInitListener != null) {
            String ip = getLocalIPAddress();
            Log.i(TAG,"IP" + ip);

            // This is a strange one. Some devices do initialise their group quite happily.
            // And some don't. This checks to see if the IP is set, and calls the fallback
            // createGroup code if not.
            //TODO: JK prevent infinite loop here. CreateGroup will call this function. If it succeeds
            //      whilst failing to set the IP we have an infinite loop.
            if ( (ip == null) ) { // || (!mGroupCreated )) {
                Log.i(TAG,"Null IP - Pausing");




                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.i(TAG,"Retry onInitialiseNetworkCallback");
                                onInitialiseNetworkCallback();
                            }
                        },
                        BabbleConstants.P2P_RETRY_DELAY_MS);

                return;
            }

            mNetworkInitListener.onNetworkInitialised(ip);
        }

    }


    public void createGroup(final int count) {

        Log.i(TAG,"createGroup");
        if (mGroupCreated) {
            return ; // onInitialiseNetworkCallback();
        }

        mWifiP2pMgr.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "P2PAdvertise: Successfully created group");
                mGroupCreated = true;
                onInitialiseNetworkCallback();
            }

            @Override
            public void onFailure(int arg0) {

                String errType = "Unknown error";

                switch (arg0) {
                    case   WifiP2pManager.P2P_UNSUPPORTED:
                        errType = "Unsupported";
                        break;

                    case WifiP2pManager.ERROR:
                        errType = "Error";
                        break;

                    case WifiP2pManager.BUSY:
                        errType = "Busy";
                        break;
                }

                Log.e(TAG, "P2PAdvertise: Error creating group: " + errType + "("+arg0+")");


                if ( ( arg0 == WifiP2pManager.BUSY )  && (! mGroupCreated ) ){

                    if (count > BabbleConstants.P2P_RETRY_LIMIT) {
                        //TODO: error handle
                        onInitialiseNetworkCallback();
                        return;
                    }

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.i(TAG,"Retry createGroup");
                                    createGroup(count+1);
                                }
                            },
                            BabbleConstants.P2P_RETRY_DELAY_MS);
                }


            }
        });

    }




    public void stopDiscoverService() {

        //TODO: Actually stop discovery. Not a serious issue, it times out anyway
        /*
            Service discovery will only last for 120 seconds from the time the discoverServices
            method of WifiP2pManager is called. If application developers require service discovery
            for a longer period, they will need to re-call the WifiP2pManager.discoverServices method.
         */
        setIsDiscovering(false);
        mGroupCreated = false;

    }

    public void discoverService() {
        Log.i(TAG, "discoverService()");
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.i(TAG, "DnsSdTxtRecord available -" + record.toString());

                String groupId = device.deviceAddress;
                record.put(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, groupId);  //TODO: JK20Mar review this line

                //TODO: Restore this deduplicating code. Or at least stop it suppressing all entries.
           /*
                if (mResolvedServices.containsKey(groupId)) {
                    //we already have this service
                    Log.i(TAG, "We already have this service " + groupId);
                    //TODO: remove this hacky way to force screen update
                    mServiceDiscoveryListener.onServiceListUpdated(true);
                    return;
                };
*/
       //         try {
                    ResolvedService resolvedService = ResolvedServiceP2PFactory.NewJoinResolvedService("p2p", record);


                    if (!resolvedService.getAppIdentifier().equals(BabbleConstants.APP_ID())) {
                        //The service is not for this app, we'll skip it
                        return;  //TODO: this may need to be modified if multiple apps share babble
                    }
                    ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);
                    mResolvedGroups.add(resolvedGroup);
                    resolvedService.setResolvedGroup(resolvedGroup);
                    mResolvedServices.put(groupId, resolvedService);
                    mServiceDiscoveryListener.onServiceListUpdated(true);

           /*     } catch (UnknownHostException ex) {

                    Log.i(TAG,ex.getMessage());  //TODO: Implement proper error handling

                    return;
                } */

            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // We always set a TXT file, so in a correctly functioning environment,
                // the TXT file should always be available.
                Log.i(TAG,"Found " + instanceName + " " + registrationType);

            }
        };

        mWifiP2pMgr.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pMgr.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.i(TAG,"mWifiP2pMgr.addServiceRequest success");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY

                        Log.i(TAG,"mWifiP2pMgr.addServiceRequest fail ("+Integer.toString(code));
                        if (mServiceDiscoveryListener != null) {
                            mServiceDiscoveryListener.onStartDiscoveryFailed();
                        }
                    }
                });


        mWifiP2pMgr.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                Log.i(TAG,"mWifiP2pMgr.discoverServices success");
                setIsDiscovering(true);
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.i(TAG, "P2P isn't supported on this device.");
                } else {
                    Log.i(TAG, "P2P error. " + Integer.toString(code));
                }

                if (mServiceDiscoveryListener != null) {
                    mServiceDiscoveryListener.onStartDiscoveryFailed();
                }

            }
        });

    }


    public void connectToPeer(String deviceAddress, final String peerIP, final int peerPort) {

        Log.i(TAG,"P2PService.connectToPeer: "+ deviceAddress);

        if (mResolvedServices.containsKey(deviceAddress)) {
            ResolvedService prs = mResolvedServices.get(deviceAddress);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            config.groupOwnerIntent = 0; //TODO: WifiP2pConfig.GROUP_OWNER_INTENT_MIN for API >= 30
            mWifiP2pMgr.connect(mChannel, config,new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                    Log.i(TAG,"mWifiP2pMgr.connect succeeded.");


                    getClientIP(peerIP, peerPort, 0);

                }

                @Override
                public void onFailure(int reason) {
                    Log.i(TAG,"mWifiP2pMgr.connect failed. Retry.");
                }
            });

        } else {
            throw new Resources.NotFoundException("Selected Peer not discovered");
        }

    }


    private void getClientIP(final String peerIP, final int peerPort, final int count) {

        Log.i(TAG,"getClientIP");

        String ip = getLocalIPAddress();
        Log.i(TAG,"IP: " + ip);

        if (  ( (ip == null) || (ip.equals(BabbleConstants.DEFAULT_P2P_IP_ADDRESS)) ) && ( count < BabbleConstants.P2P_RETRY_LIMIT)) {

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i(TAG,"Retry getClientIP");
                            getClientIP(peerIP, peerPort, count+1);
                        }
                    },
                    BabbleConstants.P2P_RETRY_DELAY_MS);
            return;
        }


        //Callback
        if ( mP2PConnected != null) {
            mP2PConnected.onConnected(peerIP, peerPort);
        }

    }



    private String getLocalIPAddress() {
        try {


            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String interfaceName = intf.getDisplayName();
                Log.i(TAG,"Interface: " + interfaceName);
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    String ipStr = getDottedDecimalIP(inetAddress.getAddress());
                                     Log.i(TAG,"IP: "+ipStr);
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            if (ipStr.startsWith(BabbleConstants.DEFAULT_P2P_IP_PREFIX)) {
                                Log.i(TAG,"Using interface: " + interfaceName);
                                Log.i(TAG,"Using address: " + ipStr);
                                return ipStr;
                            }
                        }

                    }
                }
            }

        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    @Override
    public void advertise() {
        // Do nothing. It is implicit in the starting a Wifi Direct node
        Log.i(TAG,"P2P advertise, does nothing");
    }

    @Override
    public void stopAdvertising() {
        // Do nothing. It is implicit in the starting a Wifi Direct node
        mGroupCreated = false;
        Log.i(TAG,"P2P stop advertise, does nothing");
    }

    @Override
    public String getServiceName() {
        return mServiceName;
    }



}
