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
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.node.Peer;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;
import io.mosaicnetworks.babble.utils.HttpsTrustManager;

public class WebRTCAdvertiser implements ServiceAdvertiser {
    private static final String TAG = "WebRTCAdvertiser";

    private static RequestQueue sQueue;

    private static String mGroupID;
    private static String mGroupName;
    private static String mAppID;
    private static DiscoGroup mDisco;

    public WebRTCAdvertiser(GroupDescriptor groupDescriptor, Context context) {
        sQueue = Volley.newRequestQueue(context.getApplicationContext());

        // disable TLS verification if skip-verify is set
        if (Constants.SKIP_VERIFY) {
            HttpsTrustManager.allowAllSSL();
        }

        mGroupID = groupDescriptor.getUid();
        mGroupName = groupDescriptor.getName();
        mAppID = context.getApplicationContext().getPackageName();
    }

    @Override
    public boolean advertise(BabbleNode node) {
        Gson gson = new Gson();

        String currentPeersString = node.getCurrentPeers();
        List<Peer> currentPeers = gson.fromJson(currentPeersString, new TypeToken<List<Peer>>(){}.getType());

        String genesisPeersString = node.getGenesisPeers();
        List<Peer> genesisPeers = gson.fromJson(genesisPeersString, new TypeToken<List<Peer>>(){}.getType());

        mDisco = new DiscoGroup(
                mGroupID,
                mGroupName,
                mAppID,
                node.getPubKey(),
                0,
                -1,
                currentPeers,
                genesisPeers );

        URL url;
        try {
            url = new URL(
                    "https",
                    Constants.DISCOVER_SERVER_HOST,
                    Constants.DISCOVER_SERVER_PORT,
                    Constants.REGISTER_END_POINT);
        } catch (MalformedURLException e) {
            //We should never arrive here!
            throw new RuntimeException("Unexpected Invalid host exception");
        }

        // Build body before launching the request for cleaner tidy up
        final String PostBody;

        PostBody = gson.toJson(mDisco);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url.toString(),
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
                }
        )  {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return PostBody.getBytes();
                }
        } ;

        sQueue.add(request);

        return true;
    }


    @Override
    public void onPeersChange(String newPeers) {
        //TODO: implement this
    }


    @Override
    public void stopAdvertising() {
        // TODO: this is a temporary hack to prevent anyone other than the group creator to delete
        // the group from disco. The functionality should be propertly implemented server side.
        if (!mDisco.PubKey.equals(ConfigManager.getInstance(null).getPublicKey())) {
            Log.d(TAG, "Not group creator => not deleting from disco");
            return;
        }

        URL url;
        try {
            url = new URL(
                    "https",
                    Constants.DISCOVER_SERVER_HOST,
                    Constants.DISCOVER_SERVER_PORT,
                    String.format("groups/%s", mDisco.GroupUID)
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
