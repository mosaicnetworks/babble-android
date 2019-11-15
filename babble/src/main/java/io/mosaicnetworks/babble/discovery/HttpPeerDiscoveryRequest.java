package io.mosaicnetworks.babble.discovery;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public final class HttpPeerDiscoveryRequest {

    public static HttpPeerDiscoveryRequest createGenesisPeersRequest(String host, int port,
                                                                final ResponseListener responseListener,
                                                                Context context) {

        return createPeersRequest("/genesis-peers", host, port, responseListener, context);
    }

    public static HttpPeerDiscoveryRequest createCurrentPeersRequest(String host, int port,
                                                                     final ResponseListener responseListener,
                                                                     Context context) {

        return createPeersRequest("/current-peers", host, port, responseListener, context);
    }

    private static HttpPeerDiscoveryRequest createPeersRequest(String file, String host, int port,
                                                          final ResponseListener responseListener,
                                                          Context context) {

        URL url;

        try {
            url = new URL("http", host, port, file);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid host");
        }

        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //TODO: move this off the UI thread
                        Gson gson = new Gson();
                        Peer[] peers = gson.fromJson(response, Peer[].class);

                        queue.stop();
                        responseListener.onReceivePeers(new ArrayList<>(Arrays.asList(peers)));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: error handling
                queue.stop();
                responseListener.onFailure(ResponseListener.Error.CONNECTION_ERROR);
            }
        });

        return new HttpPeerDiscoveryRequest(queue, request);
    }

    private final RequestQueue mQueue;
    private final StringRequest mRequest;

    private HttpPeerDiscoveryRequest(RequestQueue queue, StringRequest request) {
        mQueue = queue;
        mRequest = request;
    }

    public void setRetryPolicy(int timeoutMs, int maxNumRetries, float backoffMultiplier) {
        mRequest.setRetryPolicy(new DefaultRetryPolicy(timeoutMs, maxNumRetries, backoffMultiplier));
    }

    public void send() {
        mQueue.add(mRequest);
    }

    public void cancel() {
        mRequest.cancel();
        mQueue.stop();
    }

}



