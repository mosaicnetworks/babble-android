package io.mosaicnetworks.babble;

import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.discovery.HttpDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.HttpDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersGetter;
import io.mosaicnetworks.babble.discovery.ResponseListener;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HttpDiscoveryTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    private List<Peer> mRcvPeers;

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

        HttpDiscoveryServer httpDiscoveryServer = new HttpDiscoveryServer("localhost", 8988, new PeersGet());
        httpDiscoveryServer.start();

        String url = "http://localhost:8988/peers";

        HttpDiscoveryRequest httpDiscoveryRequest = new HttpDiscoveryRequest(url, new ResponseListener() {
            @Override
            public void onReceivePeers(List<Peer> peers) {
                mRcvPeers = peers;
                lock.countDown();
            }

            @Override
            public void onFailure(Error error) {
                lock.countDown();
            }
        });

        httpDiscoveryRequest.send();

        lock.await(3000, TimeUnit.MILLISECONDS);

        httpDiscoveryServer.stop();

        assertNotNull(mRcvPeers);
        assertEquals("mosaic", mRcvPeers.get(0).moniker);
        assertEquals("localhost:6666", mRcvPeers.get(0).netAddr);
        assertEquals("0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B",
                mRcvPeers.get(0).pubKeyHex);

    }

}
