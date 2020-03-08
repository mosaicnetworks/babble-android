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

package io.mosaicnetworks.babble.servicediscovery.webrtc;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.mosaicnetworks.babble.configure.OnNetworkInitialised;
import io.mosaicnetworks.babble.discovery.Disco;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersProvider;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.utils.RandomString;

// import android.net.wifi.WifiManager;

/**
 * Service class to handle WiFi Direct (P2P)
 */
public class WebRTCService implements ServiceAdvertiser{

    public static String mDiscoverServerIP = "34.241.63.109";          //TODO: This is a local dev IP - needs a better default
    public static String mRelayServerAddress = "34.241.63.109:9090";   //TODO: This is a local dev IP - needs a better default
    public static String mRelayServerName = "dev";   //TODO: This is a local dev IP - needs a better default

    public static int mDiscoverServerPort = 8080;

    private final static String mDiscoverEndPoint = "groups";
    private final static String mRegisterEndPoint = "group";

    private PeersProvider peersProvider;
    private static RequestQueue mQueue;


    private ServiceDiscoveryListener mServiceDiscoveryListener;
    private OnNetworkInitialised mNetworkInitListener;
    private WebRTCConnected mP2PConnected;


    // Keys used for the hash map in DNS text file
    public final static String HOST_LABEL = "host";
    public final static String PORT_LABEL = "port";
    public final static String MONIKER_LABEL = "moniker";
    public final static String DNS_VERSION_LABEL = "textvers";
    private final static String DNS_VERSION = "0.0.1";
    public final static String BABBLE_VERSION_LABEL = "babblevers";
    public final static String GROUP_ID_LABEL = "groupid";
    public final static String DEFAULT_IP_ADDRESS = "192.168.49.1";
    public final static String APP_LABEL = "app";
    public final static String GROUP_LABEL = "group";
    final static public int SERVER_PORT = 8988;
    final static private String TAG = "WebRTCService";
    public static final int RETRY_DELAY_MS = 1000;
    public static final int RETRY_LIMIT = 4;

    public static boolean mGroupCreated = false;  //TODO: verify this is set to false on exit.


    private static String mMoniker = "Moniker";

    public static boolean getIsDiscovering() {
        return mIsDiscovering;
    }

    public static void setIsDiscovering(boolean mIsDiscovering) {
        WebRTCService.mIsDiscovering = mIsDiscovering;
        Log.i(TAG, "SetIsDiscovering: " + mIsDiscovering);
    }

    private static boolean mIsDiscovering = false;

    private static WebRTCService INSTANCE;
    private Context mAppContext;

    private final Map<String, WebRTCResolvedService> mResolvedServices = new HashMap<>();
    private List<WebRTCResolvedGroup> mResolvedGroups;  //TODO: Look to make this final
//    final HashMap<String, Map> textRecords = new HashMap<>();

    private String mServiceName ;


    /**
     * Factory for the {@link WebRTCService}
     *
     * @return a messaging service
     */
    public static WebRTCService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WebRTCService(context.getApplicationContext());
             mQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return INSTANCE;
    }


    private WebRTCService(Context context) {
        mAppContext = context;
        Log.i(TAG,"Constructed");
        initWebRTC();
    }


    public void registerOnNetworkInitialised(OnNetworkInitialised listener) {
        this.mNetworkInitListener = listener;
    }


    public void registerP2PConnected(WebRTCConnected listener) {
        this.mP2PConnected = listener;
    }



    public void registerServiceDiscoveryListener(ServiceDiscoveryListener listener){
        this.mServiceDiscoveryListener  =listener;
    }


    public void setResolvedGroups(List<WebRTCResolvedGroup> resolvedGroups) {
        this.mResolvedGroups = resolvedGroups;
    }

    protected void initWebRTC(){
           //Init WebRTC
        Log.i(TAG, "initWebRTC: ");
    }

    private void getDiscovery(){

        Log.i(TAG, "getDiscovery: ");
        URL url;

        try {
            url = new URL("http", mDiscoverServerIP, mDiscoverServerPort, mDiscoverEndPoint);
        } catch (MalformedURLException e) {
            Log.e(TAG, "getDiscovery: Invalid Host" );
            throw new IllegalArgumentException("Invalid host");
        }


        Log.i(TAG, "getDiscovery: "+ url.toString());
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: "+ response);
                        //TODO: move this off the UI thread
                        Gson gson = new Gson();
                        Disco[] discos = gson.fromJson(response, Disco[].class);

                        Log.i(TAG, "onResponse: Disco unpacked" );

                        int i;
                        for (i = 0; i < discos.length; i++) {

                            try {
                                WebRTCResolvedService webRTCResolvedService =
                                        new WebRTCResolvedService(discos[i].GroupUID,
                                                discos[i].GroupName,
                                                discos[i].AppID,
                                                discos[i].PubKey,
                                                discos[i].LastUpdated,
                                                discos[i].InitialPeers,
                                                discos[i].Peers,
                                                null,
                                                0
                                                );

                                WebRTCResolvedGroup webRTCResolvedGroup = new WebRTCResolvedGroup(webRTCResolvedService);
                                mResolvedGroups.add(webRTCResolvedGroup);
                                webRTCResolvedService.setResolvedGroup(webRTCResolvedGroup);
                                mResolvedServices.put(discos[i].GroupUID, webRTCResolvedService);
                                mServiceDiscoveryListener.onServiceListUpdated(true);

                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
//   public WebRTCResolvedService(String groupUid, String groupName, String appIdentifier, String pubKey, int lastUpdated, Peer[] initialPeers, Peer[] currentPeers, InetAddress inetAddress, int port) throws UnknownHostException {
//
                        }
                            //                       mQueue.stop();
 //                       responseListener.onReceivePeers(new ArrayList<>(Arrays.asList(peers)));
                    }



                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: "+ error.toString());
                //TODO: error handling
   //             mQueue.stop();
    //            responseListener.onFailure(ResponseListener.Error.CONNECTION_ERROR);
            }
        })      ;

        mQueue.add(request);

    }



    public void stopRegistration(){

    }


    public void startRegistration(String moniker, String groupName, String babbleVersion, boolean startDiscoverService) {
        //  Create a string map containing information about your service.

        Log.i(TAG, "startRegistration()");

        if (startDiscoverService) {
            discoverService();
        }
    }







    public void createGroup(final int count) {

        Log.i(TAG,"createGroup");
        if (mGroupCreated) {
            return ; // onInitialiseNetworkCallback();
        }

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

        getDiscovery();


    }





    @Override
    public void advertise() {
        Log.i(TAG,"WebRTC advertise");

        URL url;

        try {
            url = new URL("http", mDiscoverServerIP, mDiscoverServerPort, mRegisterEndPoint);
        } catch (MalformedURLException e) {
            Log.e(TAG, "getDiscovery: Invalid Host" );
            throw new IllegalArgumentException("Invalid host");
        }

        // Build body before launching the request for cleaner tidy up

        //        public Disco(String iD, String title,  ArrayList<Peer> peers,  ArrayList<Peer> initialPeers) {
//        Disco groupObject = new Disco();

        final String PostBody;
  /*
        try {
  */
            ConfigManager configManager = ConfigManager.getInstance(null);
            Disco disco = configManager.getDisco();
            Gson gson = new Gson();
            PostBody = gson.toJson(disco);

            Log.i(TAG, "advertise: " + PostBody);

/*
        } catch (FileNotFoundException ex) {
            Log.e(TAG, "advertise: " + ex.getMessage() );
            throw new IllegalArgumentException("Config Manager not initialised");
        }
*/

        Log.i(TAG, "getDiscovery: "+ url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: "+ response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: "+ error.toString());
                //TODO: error handling
                //             mQueue.stop();
                //            responseListener.onFailure(ResponseListener.Error.CONNECTION_ERROR);
            }
        })  {
                @Override
                public byte[] getBody() throws AuthFailureError {
                     return PostBody.getBytes();
            }
        }

                ;

        mQueue.add(request);


    }

    @Override
    public void stopAdvertising() {
        // Do nothing. It is implicit in the starting a Wifi Direct node
        mGroupCreated = false;
        Log.i(TAG,"WebRTC stop advertise, does nothing");
    }

    @Override
    public String getServiceName() {
        return mServiceName;
    }



}
