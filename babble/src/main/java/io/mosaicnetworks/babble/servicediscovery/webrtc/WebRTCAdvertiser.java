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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;

public class WebRTCAdvertiser implements ServiceAdvertiser {

    private static final String TAG = "WebRTCAdvertiser";
    private RequestQueue mQueue;
    private Disco mDisco;
    private String mServiceName;



    private boolean mIsAdvertising = false;

    public WebRTCAdvertiser(Context context, Disco disco) {
        mQueue = Volley.newRequestQueue(context.getApplicationContext());
        mDisco = disco;
        mServiceName = disco.GroupUID;
    }



    @Override
    public boolean advertise(String genesisPeers, String currentPeers){
        sendGroupToDisco(Request.Method.POST, "", mDisco);
        setIsAdvertising(true);
        return true;
    }

    @Override
    public void stopAdvertising() {
        sendGroupToDisco(Request.Method.DELETE, File.pathSeparator+mDisco.GroupUID, mDisco);
        setIsAdvertising(false);
    }

    @Override
    public void onPeersChange(String newPeers) {
            //TODO
    }


//    @Override
    public void updateAdvertising(List<Peer> peers, int lastBlockIndex) {
        // Only need to update Current Peers as initial peers are unchanging
        mDisco.setPeers(peers);
        mDisco.LastBlockIndex = lastBlockIndex;
        sendGroupToDisco(Request.Method.PATCH, File.pathSeparator+mDisco.GroupUID, mDisco);
    }


    private void sendGroupToDisco(int sendMethod, String urlSuffix, Disco disco) {
        Log.i(TAG,"sendGroupToDisco");

        URL url;
        try {
            url = new URL("http", BabbleConstants.DISCO_DISCOVERY_ADDRESS(),
                    BabbleConstants.DISCO_DISCOVERY_PORT(), BabbleConstants.DISCO_DISCOVERY_ENDPOINT()+urlSuffix);
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
        StringRequest request = new StringRequest(sendMethod, url.toString(),
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

    public boolean isAdvertising() {
        return mIsAdvertising;
    }

    private void setIsAdvertising(boolean isAdvertising) {
        this.mIsAdvertising = isAdvertising;
    }

}
