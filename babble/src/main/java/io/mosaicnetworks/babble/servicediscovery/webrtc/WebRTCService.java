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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.PeersProvider;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.utils.HttpsTrustManager;


/**
 * Service class to handle WebRTC
 */
public class WebRTCService implements ServiceAdvertiser {

    private static final String TAG = "WebRTCService";
    // XXX localhost values
    // TODO: these should not be hardcoded
    public static final String DISCOVER_SERVER_HOST = "disco-staging.babble.io";
    public static final int DISCOVER_SERVER_PORT = 1443;
    public static final String RELAY_SEVER_ADDRESS = "disco-staging.babble.io:2443";

    private static final String DISCOVER_END_POINT = "groups";
    private static final String REGISTER_END_POINT = "group";

    private static String mGroupsURL;

    // XXX Unsafe.
    // TODO: this should not be hardcoded
    private static final boolean SKIP_VERIFY = true;

    private static RequestQueue sQueue;
    private ServiceDiscoveryListener mServiceDiscoveryListener;

    private static WebRTCService INSTANCE;

    private List<ResolvedGroup> mResolvedGroups;
    private HashMap<String, Boolean> mResolvedGroupsIndex;
    /**
     * Factory for the {@link WebRTCService}
     *
     * @return a WebRTCService service
     */
    public static WebRTCService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WebRTCService(context);
        }
        return INSTANCE;
    }

    private WebRTCService(Context context) {

        sQueue = Volley.newRequestQueue(context.getApplicationContext());

        // calculate groups URI once and for all
        mGroupsURL = String.format(
                "https://%s:%d/%s?app-id=%s",
                DISCOVER_SERVER_HOST,
                DISCOVER_SERVER_PORT,
                DISCOVER_END_POINT,
                context.getApplicationContext().getPackageName()
        );

        // disable TLS verification if skip-verify is set
        if (SKIP_VERIFY) {
            HttpsTrustManager.allowAllSSL();
        }
    }

    public void registerServiceDiscoveryListener(ServiceDiscoveryListener listener){
        mServiceDiscoveryListener = listener;
    }

    public void setResolvedGroups(List<ResolvedGroup> resolvedGroups) {
        mResolvedGroups = resolvedGroups;
        mResolvedGroupsIndex = new HashMap<String, Boolean>();
    }

    private void addGroup(ResolvedGroup group) {
        mResolvedGroups.add(group);
        mResolvedGroupsIndex.put(group.getGroupUid(), true);
    }

    private void removeGroup(ResolvedGroup group) {
        mResolvedGroups.remove(group);
        mResolvedGroupsIndex.remove(group.getGroupUid());
    }

    public void stopDiscoverService() {
        //do nothing
    }

    public void discoverService() {
        Log.d("WebRTCService", mGroupsURL);

        StringRequest request = new StringRequest(Request.Method.GET, mGroupsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //TODO: move this off the UI thread
                        Gson gson = new Gson();
                        Map<String,Disco> discos = gson.fromJson(response, new TypeToken<Map<String, Disco>>(){}.getType());
                        Iterator<Map.Entry<String, Disco>> itr = discos.entrySet().iterator();

                        while(itr.hasNext()) {

                            Map.Entry<String, Disco> entry = itr.next();
                            Disco disco = entry.getValue();

                            Boolean exists = mResolvedGroupsIndex.get(entry.getKey());
                            if (exists != null) {
                                continue;
                            }

                            ResolvedService webRTCResolvedService =
                                    new ResolvedService(disco.GroupUID,
                                            disco.GroupName,
                                            disco.AppID,
                                            disco.LastUpdated,
                                            disco.InitialPeers,
                                            disco.Peers,
                                            null,
                                            0
                                    );

                            ResolvedGroup webRTCResolvedGroup = new ResolvedGroup(
                                    webRTCResolvedService,
                                    ResolvedGroup.Source.WEBRTC
                            );

                            webRTCResolvedService.setResolvedGroup(webRTCResolvedGroup);

                            addGroup(webRTCResolvedGroup);
                        }

                        // remove deleted groups
                        for (ResolvedGroup g: mResolvedGroups) {
                            if (!discos.containsKey(g.getGroupUid())) {
                                removeGroup(g);
                            }
                        }

                        mServiceDiscoveryListener.onServiceListUpdated(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO: error handling
                        // sQueue.stop();
                        // responseListener.onFailure(ResponseListener.Error.CONNECTION_ERROR);
                        Log.e(TAG, "CONNECTION_ERROR", error);
                    }
        });

        sQueue.add(request);
    }

    @Override
    public boolean advertise(String genesisPeers, String currentPeers, PeersProvider peersProvider) {
        ConfigManager configManager = ConfigManager.getInstance(null);
        Disco disco = configManager.getDisco();
        sendGroupToDisco(REGISTER_END_POINT, disco);
        return true;
    }

    @Override
    public void onPeersChange(String newPeers) {
        //TODO: implement this
    }

    private void sendGroupToDisco(String endPoint, Disco disco) {

        URL url;
        try {
            url = new URL("https", DISCOVER_SERVER_HOST, DISCOVER_SERVER_PORT, endPoint);
        } catch (MalformedURLException e) {
            //We should never arrive here!
            throw new RuntimeException("Unexpected Invalid host exception");
        }

        // Build body before launching the request for cleaner tidy up
        final String PostBody;

        Gson gson = new Gson();
        PostBody = gson.toJson(disco);

        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    //TODO: go.error handling
                }
        })  {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return PostBody.getBytes();
            }
        } ;

        sQueue.add(request);
    }

    @Override
    public void stopAdvertising() {
        Disco group = ConfigManager.getInstance(null).getDisco();

        // TODO: this is a temporary hack to prevent anyone other than the group creator to delete
        // the group from disco. The functionality should be propertly implemented server side.
        if (!group.PubKey.equals(ConfigManager.getInstance(null).getPublicKey())) {
            Log.d(TAG, "Not group creator => not deleting from disco");
            return;
        }

        URL url;
        try {
            url = new URL(
                    "https",
                    DISCOVER_SERVER_HOST,
                    DISCOVER_SERVER_PORT,
                    String.format("groups/%s", group.GroupUID)
            );
        } catch (MalformedURLException e) {
            //We should never arrive here!
            throw new RuntimeException("Unexpected Invalid host exception");
        }

        Log.d(TAG, "URL: " + url.toString());

        StringRequest request = new StringRequest(Request.Method.DELETE, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error removing group from disco", error);
                    }
                }
         )  {} ;

        sQueue.add(request);
    }
}
