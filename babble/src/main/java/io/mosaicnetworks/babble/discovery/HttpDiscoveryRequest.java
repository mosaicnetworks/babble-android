package io.mosaicnetworks.babble.discovery;

import android.os.AsyncTask;
import android.util.Log;

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

public class HttpDiscoveryRequest {

    private String url;
    private ResponseListener responseListener;
    private FailureListener failureListener;
    private RequestTask requestTask;

    //Define failure codes
    public static final int INVALID_JSON = 0;
    public static final int CONNECTION_ERROR = 1;
    public static final int MALFORMED_URL = 2;

    public HttpDiscoveryRequest(String url, ResponseListener responseListener, FailureListener failureListener) {
        this.url = url;
        this.responseListener = responseListener;
        this.failureListener = failureListener;
        requestTask = new RequestTask();
    }

    public void send() {
        requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class RequestTask extends AsyncTask<Void, Void, Peer[]> {

        HttpURLConnection httpURLConnection = null;
        int errorCode;

        @Override
        protected Peer[] doInBackground(Void... params) {
            try {
                URL urlT = new URL(url);
                httpURLConnection = (HttpURLConnection) urlT.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                String JSON_STRING;
                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                String response = stringBuilder.toString().trim();

                Log.d("Babble", "Received response: " + response);
                Gson gson = new Gson();
                Peer[] peers = gson.fromJson(response, Peer[].class);

                return peers;


            } catch (MalformedURLException e) {
                Log.d("Babble", "HttpDiscoveryRequest error: " + e);
                errorCode = MALFORMED_URL;
            } catch (IOException e) {
                Log.d("Babble", "HttpDiscoveryRequest error: " + e);
                errorCode = CONNECTION_ERROR;
            } catch(JsonSyntaxException e) {
                Log.d("Babble", "HttpDiscoveryRequest error: " + e);
                errorCode = INVALID_JSON;
            } catch(IllegalStateException e) {
                Log.d("Babble", "HttpDiscoveryRequest error: " + e);
                errorCode = INVALID_JSON;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Peer[] result) {
            if (result != null){
                responseListener.onReceivePeers(Arrays.asList(result));
            } else {
                failureListener.onFailure(errorCode);
            }
        }
    }

}



