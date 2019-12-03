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

/**
 * A wrapper around volley's {@link StringRequest} to request peers over http. There are two request
 * types that can be constructed, a genesis-peers request and a current-peers request. The
 * complementary {@link HttpPeerDiscoveryServer} can be used a serve the peers list
 */
public final class HttpPeerDiscoveryRequest {

    /**
     * Construct a genesis-peers request
     * @param host the host address of the server
     * @param port the port number on the server
     * @param responseListener a listener to return the result or error
     * @param context the application context
     * @return the request
     */
    public static HttpPeerDiscoveryRequest createGenesisPeersRequest(String host, int port,
                                                                final ResponseListener responseListener,
                                                                Context context) {

        return createPeersRequest("/genesis-peers", host, port, responseListener, context);
    }

    /**
     * Construct a current-peers request
     * @param host the host address of the server
     * @param port the port number on the server
     * @param responseListener a listener to return the result or error
     * @param context the application context
     * @return the request
     */
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

    /**
     * Set the retry policy
     * @param timeoutMs request timeout in milliseconds
     * @param maxNumRetries num of retries before throwing an error
     * @param backoffMultiplier back off multiplier value
     */
    public void setRetryPolicy(int timeoutMs, int maxNumRetries, float backoffMultiplier) {
        mRequest.setRetryPolicy(new DefaultRetryPolicy(timeoutMs, maxNumRetries, backoffMultiplier));
    }

    /**
     * Send the request
     */
    public void send() {
        mQueue.add(mRequest);
    }

    /**
     * Cancel the request
     */
    public void cancel() {
        mRequest.cancel();
        mQueue.stop();
    }

}



