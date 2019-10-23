package io.mosaicnetworks.babble;

import android.support.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import io.mosaicnetworks.babble.discovery.HttpDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersGetter;
import io.mosaicnetworks.babble.discovery.ResponseListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpDiscoveryRequestTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    private ResponseListener.Error mError;

    @Test
    public void requestReadTimeoutTest() throws IOException, InterruptedException {

        final int serverResponseDelay = 2000;
        final int requestReadTimeout = 100;
        final int testTimeout = 3000;

        final CountDownLatch lock = new CountDownLatch(1);
        final String peersJSON = "[{\"NetAddr\":\"localhost:6666\",\"PubKeyHex\":\"0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B\",\"Moniker\":\"mosaic\"}]\n";

        class PeersGet implements PeersGetter {
            @Override
            public String getPeers() {
                return peersJSON;
            }
        }

        MockHttpDiscoveryServer mockHttpDiscoveryServer = new MockHttpDiscoveryServer("localhost", 8988, new PeersGet(), serverResponseDelay);
        mockHttpDiscoveryServer.start();

        String host = "localhost";

        HttpDiscoveryRequest httpDiscoveryRequest = new HttpDiscoveryRequest(host, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
            }

            @Override
            public void onFailure(Error error) {
                mError = error;
                lock.countDown();
            }
        });

        httpDiscoveryRequest.setReadTimeout(requestReadTimeout);
        httpDiscoveryRequest.send();

        lock.await(testTimeout, TimeUnit.MILLISECONDS);

        mockHttpDiscoveryServer.stop();

        assertNotNull(mError);
        assertEquals(ResponseListener.Error.TIMEOUT, mError);

    }

    @Test
    public void requestConnectTimeoutTest() throws IOException, InterruptedException {

        final int requestConnectTimeout = 100;
        final int testTimeout = 3000;

        final CountDownLatch lock = new CountDownLatch(1);
        final String peersJSON = "[{\"NetAddr\":\"localhost:6666\",\"PubKeyHex\":\"0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B\",\"Moniker\":\"mosaic\"}]\n";


        String host = "10.255.255.1"; // should be an unreachable ip address

        HttpDiscoveryRequest httpDiscoveryRequest = new HttpDiscoveryRequest(host, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
            }

            @Override
            public void onFailure(Error error) {
                mError = error;
                lock.countDown();
            }
        });

        httpDiscoveryRequest.setConnectTimeout(requestConnectTimeout);
        httpDiscoveryRequest.send();

        lock.await(testTimeout, TimeUnit.MILLISECONDS);

        assertNotNull(mError);
        assertEquals(ResponseListener.Error.TIMEOUT, mError);

    }
}

class MockHttpDiscoveryServer {

    private NanoWrapper nanoWrapper;
    private int mResponseDelayMilliSec;

    public MockHttpDiscoveryServer(String hostname, int port, PeersGetter peersGetter, int responseDelayMilliSec) {
        nanoWrapper = new NanoWrapper(hostname, port, peersGetter);
        mResponseDelayMilliSec = responseDelayMilliSec;
    }

    public void start() throws IOException {
        nanoWrapper.start();
    }

    public void stop() {
        nanoWrapper.stop();
    }

    private class NanoWrapper extends NanoHTTPD {

        private PeersGetter peersGetter;

        public NanoWrapper(int port, PeersGetter peersGetter) {
            super(port);
            this.peersGetter = peersGetter;
        }

        public NanoWrapper(String hostname, int port, PeersGetter peersGetter) {
            super(hostname, port);
            this.peersGetter = peersGetter;
        }

        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() == Method.GET) {

                if (session.getUri().equals("/peers")) {
                    if (peersGetter != null) {
                        try {
                            Thread.sleep(mResponseDelayMilliSec);
                        } catch (InterruptedException e) {
                            //Cannot change the method signature, so throw an unchecked exception
                            throw new RuntimeException();
                        }
                        return newFixedLengthResponse(peersGetter.getPeers());
                    }
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                            "Could not get requested resource");
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }
}

