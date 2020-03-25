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
import android.os.Handler;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import io.mosaicnetworks.babble.discovery.DiscoveryDataProvider;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

public class WebRTCDataProvider implements DiscoveryDataProvider {
    private String mUid;
    private ResolvedGroupManager mResolvedGroupManager;
    private List<ResolvedGroup> mResolvedGroups = new ArrayList<>();
    private Map<String, Disco> mDiscos = new HashMap<>();



    private String mDiscoverServerIP;
    private String mDiscoverServerEndpoint;
    private int mDiscoverServerPort;
    private int mPollingInterval;
    private boolean mIsDiscovering = false;

    private static final String TAG = "WebRTCDataProvider";

    private static RequestQueue mQueue;


    public WebRTCDataProvider(Context context, String discoverServerIP, int discoverServerPort, String discoverServerEndpoint, int pollingInterval) {
        this.mDiscoverServerIP = discoverServerIP;
        this.mDiscoverServerPort = discoverServerPort;
        this.mDiscoverServerEndpoint = discoverServerEndpoint;
        this.mPollingInterval = pollingInterval * 1000;  // Convert to milliseconds
        mQueue = Volley.newRequestQueue(context);
    }



    /**
     * Called from DiscoveryDataController to assign a unique id to this data provider.
     * @param uid
     */
    @Override
    public void setUid(String uid){
        this.mUid = uid;
    }

    @Override
    public void startDiscovery(Context context, ResolvedGroupManager resolvedGroupManager) {
        this.mResolvedGroupManager = resolvedGroupManager;

        // Don't start polling again if already polling
        if (mIsDiscovering) {return ;}

        mIsDiscovering = true;

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    if (! mIsDiscovering) { return ; }

                    pollDiscoServer();
                    handler.postDelayed(this, mPollingInterval);
                }
            };
        handler.postDelayed(runnable, 0);
    }

    @Override
    public void stopDiscovery() {
        mIsDiscovering = false;
        mQueue.stop();
    }


    private void pollDiscoServer() {

        URL url;

        try {
            url = new URL("http", mDiscoverServerIP, mDiscoverServerPort, mDiscoverServerEndpoint);
        } catch (MalformedURLException ex) {
            Log.e(TAG, "startDiscovery: Invalid Host ");
            throw new IllegalArgumentException("Invalid Host");
        }

        Log.i(TAG, "startDiscovery: "+url.toString());

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
                        mDiscos.clear();

                        Iterator<Map.Entry<String, Disco>> itr = discos.entrySet().iterator();

                        while(itr.hasNext())  {
                            Map.Entry<String, Disco> entry = itr.next();
                            Disco disco = entry.getValue();

                            ResolvedService resolvedService = ResolvedServiceWebRTCFactory.NewJoinResolvedService(mUid,disco);
                            ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);

                            mResolvedGroups.add(resolvedGroup);
                            mDiscos.put(disco.GroupUID, disco);

                            Log.i(TAG, "onResponse: Added Group");
                        }
                        pushListToResolvedGroupManager();
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
        mIsDiscovering = true;
    }

    @Override
    public void selectedDiscoveryResolveGroup(ResolvedGroup resolvedGroup) {
        Log.i(TAG, "selectedDiscoveryResolveGroup: selected " + resolvedGroup.getGroupName());
    }


    @Override
    public ServiceAdvertiser getAdvertiser() {
        return null;
    };

    private void pushListToResolvedGroupManager() {
        mResolvedGroupManager.setList(mUid, mResolvedGroups);
    }

}
