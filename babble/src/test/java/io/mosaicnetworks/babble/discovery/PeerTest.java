package io.mosaicnetworks.babble.discovery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PeerTest {



    @Test
    public void peersCreateTest() {

        Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", "127.0.0.1:6666", "testpeer");

        assertEquals(peer.moniker, "testpeer");
        assertEquals(peer.netAddr, "127.0.0.1:6666");
        assertEquals(peer.pubKeyHex, "54dbb737eac5007103e729e9ab7ce64a6850a310");

    }


    @Test(expected = NullPointerException.class)
    public void peersNull1Test() {
        Peer peer = new Peer(null, "127.0.0.1:6666", "testpeer");
    }


    @Test(expected = NullPointerException.class)
    public void peersNull2Test() {
        Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", null, "testpeer");
    }


    @Test(expected = NullPointerException.class)
    public void peersNull3Test() {
        Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", "127.0.0.1:6666", null);
    }


    @Test(expected = NullPointerException.class)
    public void peersNull4Test() {
        Peer peer = new Peer(null, "127.0.0.1:6666", null);
    }


    @Test(expected = NullPointerException.class)
    public void peersNull5Test() {
        Peer peer = new Peer(null, null, "testpeer");
    }


    @Test(expected = NullPointerException.class)
    public void peersNull6Test() {
        Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", null, null);
    }

    @Test(expected = NullPointerException.class)
    public void peersNull7Test() {
        Peer peer = new Peer(null, null, null);
    }

}
