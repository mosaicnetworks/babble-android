package io.mosaicnetworks.babble.discovery;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import io.mosaicnetworks.babble.node.Peer;
import io.mosaicnetworks.babble.servicediscovery.mdns.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.node.PeersProvider;
import io.mosaicnetworks.babble.servicediscovery.mdns.ResponseListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpPeerDiscoveryRequestTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    private ResponseListener.Error mError;
    private List<Peer> mRcvPeers;

    @Test
    public void requestCurrentPeersTest() throws IOException, InterruptedException {

        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final CountDownLatch lock = new CountDownLatch(1);
        final String peersJSON = "[{\"NetAddr\":\"localhost:6666\",\"PubKeyHex\":\"0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B\",\"Moniker\":\"mosaic\"}]\n";

        class PeersGet implements PeersProvider {
            @Override
            public String getGenesisPeers() {
                return peersJSON;
            }

            @Override
            public String getCurrentPeers() {
                return peersJSON;
            }
        }

        MockHttpPeerDiscoveryServer mockHttpPeerDiscoveryServer = new MockHttpPeerDiscoveryServer(
                "localhost", 8988, new PeersGet(), 0);
        mockHttpPeerDiscoveryServer.start();

        String host = "localhost";

        HttpPeerDiscoveryRequest httpPeerDiscoveryRequest =
                HttpPeerDiscoveryRequest.createCurrentPeersRequest(host, 8988, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
                mRcvPeers = peers;
                lock.countDown();
            }
            @Override
            public void onFailure(Error error) {
            }
        }, appContext);

        httpPeerDiscoveryRequest.send();

        lock.await(3000, TimeUnit.MILLISECONDS);

        mockHttpPeerDiscoveryServer.stop();

        assertNotNull(mRcvPeers);
        assertEquals("mosaic", mRcvPeers.get(0).moniker);
        assertEquals("localhost:6666", mRcvPeers.get(0).netAddr);
        assertEquals("0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B",
                mRcvPeers.get(0).pubKeyHex);

    }

    @Test
    public void requestTimeoutTest() throws InterruptedException {

        final int requestTimeout = 100;
        final int testTimeout = 3000;
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        final CountDownLatch lock = new CountDownLatch(1);

        String host = "198.51.100.255"; // should be an unreachable ip address

        HttpPeerDiscoveryRequest httpPeerDiscoveryRequest =
                HttpPeerDiscoveryRequest.createCurrentPeersRequest(host, 8988, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
            }

            @Override
            public void onFailure(Error error) {
                mError = error;
                lock.countDown();
            }
        }, appContext);

        httpPeerDiscoveryRequest.setRetryPolicy(requestTimeout, 0, 0);
        httpPeerDiscoveryRequest.send();

        lock.await(testTimeout, TimeUnit.MILLISECONDS);

        assertNotNull(mError);
        assertEquals(ResponseListener.Error.CONNECTION_ERROR, mError);

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

                if (session.getUri().equals("/current-peers")) {
                    if (peersProvider != null) {
                        try {
                            Thread.sleep(mResponseDelayMilliSec);
                        } catch (InterruptedException e) {
                            //Cannot change the method signature, so throw an unchecked exception
                            throw new RuntimeException();
                        }
                        return newFixedLengthResponse(peersProvider.getCurrentPeers());
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

