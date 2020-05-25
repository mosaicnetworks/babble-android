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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.fragments.discover.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.utils.HttpsTrustManager;

public class WebRTCDiscovery {
    private static final String TAG = "WebRTCDiscovery";
    private static RequestQueue sQueue;
    private static String mGroupsURL;
    private ServiceDiscoveryListener mServiceDiscoveryListener;
    private List<ResolvedGroup> mResolvedGroups;
    private HashMap<String, Boolean> mResolvedGroupsIndex;

    public WebRTCDiscovery(Context context,
                           List<ResolvedGroup> resolvedGroups,
                           ServiceDiscoveryListener serviceDiscoveryListener) {

        sQueue = Volley.newRequestQueue(context.getApplicationContext());

        // calculate groups URI once and for all
        mGroupsURL = String.format(
                "https://%s:%d/%s?app-id=%s",
                Constants.DISCOVER_SERVER_HOST,
                Constants.DISCOVER_SERVER_PORT,
                Constants.DISCOVER_END_POINT,
                context.getApplicationContext().getPackageName()
        );

        // disable TLS verification if skip-verify is set
        if (Constants.SKIP_VERIFY) {
            HttpsTrustManager.allowAllSSL();
        }

        mServiceDiscoveryListener = serviceDiscoveryListener;

        mResolvedGroups = resolvedGroups;
        mResolvedGroupsIndex = new HashMap<>();
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
                        Map<String, DiscoGroup> discos = gson.fromJson(response, new TypeToken<Map<String, DiscoGroup>>(){}.getType());
                        Iterator<Map.Entry<String, DiscoGroup>> itr = discos.entrySet().iterator();

                        while(itr.hasNext()) {

                            Map.Entry<String, DiscoGroup> entry = itr.next();
                            DiscoGroup disco = entry.getValue();

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
}
