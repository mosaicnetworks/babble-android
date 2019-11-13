package io.mosaicnetworks.babble.discovery;

import android.support.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpPeerDiscoveryRequestTest {

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

        class PeersGet implements PeersProvider {
            @Override
            public String getPeers() {
                return peersJSON;
            }
        }

        MockHttpPeerDiscoveryServer mockHttpPeerDiscoveryServer = new MockHttpPeerDiscoveryServer("localhost", 8988, new PeersGet(), serverResponseDelay);
        mockHttpPeerDiscoveryServer.start();

        String host = "localhost";

        HttpPeerDiscoveryRequest httpPeerDiscoveryRequest = new HttpPeerDiscoveryRequest(host, 8988, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
            }

            @Override
            public void onFailure(Error error) {
                mError = error;
                lock.countDown();
            }
        });

        httpPeerDiscoveryRequest.setReadTimeout(requestReadTimeout);
        httpPeerDiscoveryRequest.send();

        lock.await(testTimeout, TimeUnit.MILLISECONDS);

        mockHttpPeerDiscoveryServer.stop();

        assertNotNull(mError);
        assertEquals(ResponseListener.Error.TIMEOUT, mError);

    }

    @Test
    public void requestConnectTimeoutTest() throws InterruptedException {

        final int requestConnectTimeout = 100;
        final int testTimeout = 3000;

        final CountDownLatch lock = new CountDownLatch(1);

        String host = "10.255.255.1"; // should be an unreachable ip address

        HttpPeerDiscoveryRequest httpPeerDiscoveryRequest = new HttpPeerDiscoveryRequest(host, 8988, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
            }

            @Override
            public void onFailure(Error error) {
                mError = error;
                lock.countDown();
            }
        });

        httpPeerDiscoveryRequest.setConnectTimeout(requestConnectTimeout);
        httpPeerDiscoveryRequest.send();

        lock.await(testTimeout, TimeUnit.MILLISECONDS);

        assertNotNull(mError);
        assertEquals(ResponseListener.Error.TIMEOUT, mError);

    }
}

class MockHttpPeerDiscoveryServer {

    private NanoWrapper nanoWrapper;
    private int mResponseDelayMilliSec;

    public MockHttpPeerDiscoveryServer(String hostname, int port, PeersProvider peersProvider, int responseDelayMilliSec) {
        nanoWrapper = new NanoWrapper(hostname, port, peersProvider);
        mResponseDelayMilliSec = responseDelayMilliSec;
    }

    public void start() throws IOException {
        nanoWrapper.start();
    }

    public void stop() {
        nanoWrapper.stop();
    }

    private class NanoWrapper extends NanoHTTPD {

        private PeersProvider peersProvider;

        public NanoWrapper(String hostname, int port, PeersProvider peersProvider) {
            super(hostname, port);
            this.peersProvider = peersProvider;
        }

        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() == Method.GET) {

                if (session.getUri().equals("/peers")) {
                    if (peersProvider != null) {
                        try {
                            Thread.sleep(mResponseDelayMilliSec);
                        } catch (InterruptedException e) {
                            //Cannot change the method signature, so throw an unchecked exception
                            throw new RuntimeException();
                        }
                        return newFixedLengthResponse(peersProvider.getPeers());
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

