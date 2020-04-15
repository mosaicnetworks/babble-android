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
import com.google.gson.reflect.TypeToken;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.configure.OnNetworkInitialised;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersProvider;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;

// import android.net.wifi.WifiManager;

/**
 * Service class to handle WebRTC
 */
public class WebRTCService implements ServiceAdvertiser{

    public static String mDiscoverServerIP = "disco.babble.io";
    public static String mRelayServerAddress = "disco.babble.io:2443";
    public static String mRelayServerName = "dev";

    public static int mDiscoverServerPort = 1443;
    public static int mRelayServerPort = 9090;

    private final static String mDiscoverEndPoint = "groups";
    private final static String mRegisterEndPoint = "group";
    private final static String mUpdateEndPoint = "update";

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

    private static String mMoniker = "Moniker";

    public static boolean getIsDiscovering() {
        return mIsDiscovering;
    }

    public static void setIsDiscovering(boolean mIsDiscovering) {
        WebRTCService.mIsDiscovering = mIsDiscovering;
        Log.i(TAG, "SetIsDiscovering: " + mIsDiscovering);
    }
    private static boolean mIsDiscovering = false;

    public static void setIsAdvertising(boolean mAdvertising) {
        WebRTCService.mIsAdvertising = mIsAdvertising;
        Log.i(TAG, "SetIsAdvertising: " + mIsAdvertising);
    }
    private static boolean mIsAdvertising = false;

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




    public void stopRegistration(){

    }


    public void startRegistration(String moniker, String groupName, String babbleVersion, boolean startDiscoverService) {
        //  Create a string map containing information about your service.

        Log.i(TAG, "startRegistration()");

        if (startDiscoverService) {
            discoverService();
        }
    }



    public void stopDiscoverService() {
        setIsDiscovering(false);
    }

    public void discoverService() {
        Log.i(TAG, "discoverService()");
        URL url;

        try {
            url = new URL("https", mDiscoverServerIP, mDiscoverServerPort, mDiscoverEndPoint);
            //url = new URL("https", "disco.babble.io", mDiscoverServerPort, mDiscoverEndPoint);
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
                        Map<String,Disco> discos = gson.fromJson(response, new TypeToken<Map<String, Disco>>(){}.getType());

                        Log.i(TAG, "onResponse: Disco unpacked" );

                        mResolvedGroups.clear();

                        Iterator<Map.Entry<String, Disco>> itr = discos.entrySet().iterator();

                        while(itr.hasNext())  {
                            try {
                                Map.Entry<String, Disco> entry = itr.next();
                                Disco disco = entry.getValue();

                                Log.i(TAG, "onResponse: disco: " + disco);
                                WebRTCResolvedService webRTCResolvedService =
                                        new WebRTCResolvedService(disco.GroupUID,
                                                disco.GroupName,
                                                disco.AppID,
                                                disco.PubKey,
                                                disco.LastUpdated,
                                                disco.InitialPeers,
                                                disco.Peers,
                                                null,
                                                0
                                        );

                                WebRTCResolvedGroup webRTCResolvedGroup = new WebRTCResolvedGroup(webRTCResolvedService);
                                mResolvedGroups.add(webRTCResolvedGroup);
                                webRTCResolvedService.setResolvedGroup(webRTCResolvedGroup);
                                mResolvedServices.put(disco.GroupUID, webRTCResolvedService);

                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                        mServiceDiscoveryListener.onServiceListUpdated(true);

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

    @Override
    public void advertise() {

    }

    /*
    @Override
    public void advertise() {
        Log.i(TAG,"WebRTC advertise");

        ConfigManager configManager = ConfigManager.getInstance(null);
        Disco disco = configManager.getDisco();

        Log.d(TAG, "Advertising disco params:" + disco);

        sendGroupToDisco(mRegisterEndPoint, disco);
        setIsAdvertising(true);
    }


    public void updateAdvertise(List<Peer> peers) {
        Log.i(TAG,"WebRTC advertise");

        ConfigManager configManager = ConfigManager.getInstance(null);
        Disco disco = configManager.getDisco();

        disco.setPeers(peers);
        sendGroupToDisco(mUpdateEndPoint, disco);
    }

     */



    private void sendGroupToDisco(String endPoint, Disco disco) {
        Log.i(TAG,"sendGroupToDisco");

        URL url;
        try {
            url = new URL("https", mDiscoverServerIP, mDiscoverServerPort, endPoint);
        } catch (MalformedURLException e) {
            Log.e(TAG, "sendGroupToDisco: Invalid Host" );
            throw new IllegalArgumentException("Invalid host");
        }

        // Build body before launching the request for cleaner tidy up
        final String PostBody;

        Gson gson = new Gson();
        PostBody = gson.toJson(disco);
        Log.i(TAG, "sendGroupToDisco: " + PostBody);


        Log.i(TAG, "sendGroupToDisco: "+ url.toString());
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
        } ;

        mQueue.add(request);
    }




    @Override
    public void stopAdvertising() {
        setIsAdvertising(false);
        Log.i(TAG,"WebRTC stop advertise, does nothing");
    }

    @Override
    public String getServiceName() {
        return mServiceName;
    }



}
