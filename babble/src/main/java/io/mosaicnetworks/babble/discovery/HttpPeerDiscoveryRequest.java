package io.mosaicnetworks.babble.discovery;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Requests a list of peers from a remote device. The peers list can be passed to the BabbleNode
 * constructor. The complement class, HttpPeerDiscoveryServer, can be used on the remote device
 * to service the request.
 */
public final class HttpPeerDiscoveryRequest {

    private final URL mUrl;
    private final ResponseListener mResponseListener;
    private final RequestTask mRequestTask;
    private int mConnectTimeout = 0;
    private int mReadTimeout = 0;

    /**
     * Creates a request but does not send it.
     * @param host the hostname of the device to connect to
     * @param port the port number on the host
     * @param responseListener the response listener is either notified of any errors or is passed a
     *                         peers list
     * @throws IllegalArgumentException if the host is not valid as determined by java.net.URL
     */
    public HttpPeerDiscoveryRequest(String host, int port, ResponseListener responseListener) {
        try {
            mUrl = new URL("http", host, port, "/peers");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid host");
        }

        mResponseListener = responseListener;
        mRequestTask = new RequestTask();
    }

    /**
     * Set the connect timeout
     * @param timeout connect timeout in milliseconds
     */
    public void setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
    }

    /**
     * Set the read timeout
     * @param timeout read timeout in milliseconds
     */
    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

    //TODO: don't use asynctask
    /**
     * Send the request. This is an asynchronous call, the response listener passed via the
     * constructor will be notified of any failures or will receive a peers list. The response
     * listener is called on the main (UI) thread.
     */
    public void send() {
        mRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RequestTask extends AsyncTask<Void, Void, Peer[]> {

        ResponseListener.Error error;

        @Override
        protected Peer[] doInBackground(Void... params) {

            HttpURLConnection httpURLConnection;

            try {

                httpURLConnection = (HttpURLConnection) mUrl.openConnection();
                httpURLConnection.setConnectTimeout(mConnectTimeout);
                httpURLConnection.setReadTimeout(mReadTimeout);

                try (BufferedReader bufRd = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getInputStream()))) {

                    StringBuilder stringBuilder = new StringBuilder();

                    String resp;
                    while ((resp = bufRd.readLine()) != null) {
                        stringBuilder.append(resp);
                    }

                    String response = stringBuilder.toString().trim();

                    Gson gson = new Gson();
                    Peer[] peers = gson.fromJson(response, Peer[].class);

                    return peers;

                } catch (SocketTimeoutException e) {
                    error = ResponseListener.Error.TIMEOUT;
                } catch (IOException e) {
                    error = ResponseListener.Error.CONNECTION_ERROR;
                } catch (JsonSyntaxException | IllegalStateException e) {
                    error = ResponseListener.Error.INVALID_JSON;
                }

            } catch (IOException e) {
                error = ResponseListener.Error.CONNECTION_ERROR;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Peer[] result) {
            if (result != null){
                mResponseListener.onReceivePeers(new ArrayList<Peer>(Arrays.asList(result)));
            } else {
                mResponseListener.onFailure(error);
            }
        }
    }

}



