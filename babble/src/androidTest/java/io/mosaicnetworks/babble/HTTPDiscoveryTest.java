package io.mosaicnetworks.babble;

import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.discovery.FailureListener;
import io.mosaicnetworks.babble.discovery.HTTPDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.HTTPDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersGetter;
import io.mosaicnetworks.babble.discovery.ResponseListener;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HTTPDiscoveryTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    private Peer[] rcvPeers;

    @Test
    public void integrationTest() throws IOException, InterruptedException {

        final CountDownLatch lock = new CountDownLatch(1);
        final String peersJSON = "[{\"NetAddr\":\"localhost:6666\",\"PubKeyHex\":\"0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B\",\"Moniker\":\"mosaic\"}]\n";

        class PeersGet implements PeersGetter {
            @Override
            public String getPeers() {
                return peersJSON;
            }
        }

        HTTPDiscoveryServer httpDiscoveryServer = new HTTPDiscoveryServer("localhost", 8988, new PeersGet());
        httpDiscoveryServer.start();

        String url = "http://localhost:8988/peers";
        HTTPDiscoveryRequest httpDiscoveryRequest = new HTTPDiscoveryRequest(url, new ResponseListener() {
            @Override
            public void onReceivePeers(Peer[] peers) {
                rcvPeers = peers;
                lock.countDown();

            }
        }, new FailureListener() {
            @Override
            public void onFailure(int code) {
                lock.countDown();
            }
        });

        httpDiscoveryRequest.send();

        lock.await(3000, TimeUnit.MILLISECONDS);

        httpDiscoveryServer.stop();

        assertNotNull(rcvPeers);

        assertEquals(rcvPeers[0].moniker, "mosaic");
        assertEquals(rcvPeers[0].netAddr, "localhost:6666");
        assertEquals(rcvPeers[0].pubKeyHex, "0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B");

    }
}
