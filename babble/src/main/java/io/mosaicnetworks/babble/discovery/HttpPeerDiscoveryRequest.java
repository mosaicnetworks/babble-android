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

public final class HttpPeerDiscoveryRequest {

    private final URL mUrl;
    private final ResponseListener mResponseListener;
    private final RequestTask mRequestTask;
    private int mConnectTimeout = 0;
    private int mReadTimeout = 0;

    public HttpPeerDiscoveryRequest(String host, int port, ResponseListener responseListener) {
        try {
            mUrl = new URL("http", host, port, "/peers");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid host");
        }

        mResponseListener = responseListener;
        mRequestTask = new RequestTask();
    }

    public void setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
    }

    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

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



