package io.mosaicnetworks.babble.discovery;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public final class HttpDiscoveryRequest {

    private final URL mUrl;
    private final ResponseListener mResponseListener;
    private final RequestTask mRequestTask;

    public HttpDiscoveryRequest(String url, ResponseListener responseListener) throws MalformedURLException {
        mUrl = new URL(url);
        mResponseListener = responseListener;
        mRequestTask = new RequestTask();
    }

    public void send() {
        mRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class RequestTask extends AsyncTask<Void, Void, Peer[]> {

        ResponseListener.Error error;

        @Override
        protected Peer[] doInBackground(Void... params) {

            HttpURLConnection httpURLConnection;
            BufferedReader bufferedReader;
            InputStream inputStream;

            try {

                httpURLConnection = (HttpURLConnection) mUrl.openConnection();

                inputStream = httpURLConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();

                String resp;
                while ((resp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(resp);
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                String response = stringBuilder.toString().trim();

                Gson gson = new Gson(); //TODO: static
                Peer[] peers = gson.fromJson(response, Peer[].class);

                return peers;

            } catch (IOException e) {
                error = ResponseListener.Error.CONNECTION_ERROR;
            } catch(JsonSyntaxException | IllegalStateException e) {
                error = ResponseListener.Error.INVALID_JSON;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Peer[] result) {
            if (result != null){
                mResponseListener.onReceivePeers(Arrays.asList(result));
            } else {
                mResponseListener.onFailure(error);
            }
        }
    }

}



