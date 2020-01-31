package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.discovery.Peer;
// import io.mosaicnetworks.babble.test.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BabbleServiceTest {

    /*
    private static class TestState implements BabbleState {

        private byte[] mLastTx;

        @Override
        public Block processBlock(Block block) {
            mLastTx = block.body.transactions[0];
            block.body.stateHash = new byte[0];
            return block;
        }

        @Override
        public void reset() {

        }

        public byte[] getLastTx() {
            return mLastTx;
        }
    }

    private static class TestService extends BabbleService<TestState> {

        TestService() {
            super(new TestState(), InstrumentationRegistry.getInstrumentation().getTargetContext());
        }
    }

    private static class TestServiceWithConfig extends BabbleService<TestState> {

        TestServiceWithConfig() {
            super(new TestState(), new NodeConfig.Builder().logLevel(NodeConfig.LogLevel.DEBUG).build(), InstrumentationRegistry.getInstrumentation().getTargetContext());
        }
    }


    private byte[] rcvdTx;

    @Test
    public void configureNewTest() throws InterruptedException {

        final CountDownLatch lock = new CountDownLatch(1);
        final byte[] sentBytes = new byte[0];

        final TestService testService = new TestService();
        testService.registerObserver(new ServiceObserver() {
            @Override
            public void stateUpdated() {
                rcvdTx = testService.state.getLastTx();
                lock.countDown();
            }
        });

        testService.configureNew("alice", "localhost");
        testService.start();
        testService.submitTx(new BabbleTx() {
            @Override
            public byte[] toBytes() {
                return sentBytes;
            }
        });
        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(rcvdTx);
        assertTrue(Arrays.equals(sentBytes, rcvdTx));
    }

    @Test
    public void configureNewWithConfigTest() throws InterruptedException {

        final CountDownLatch lock = new CountDownLatch(1);
        final byte[] sentBytes = new byte[0];

        final TestServiceWithConfig testService = new TestServiceWithConfig();
        testService.registerObserver(new ServiceObserver() {
            @Override
            public void stateUpdated() {
                rcvdTx = testService.state.getLastTx();
                lock.countDown();
            }
        });

        testService.configureNew("alice", "localhost");
        testService.start();
        testService.submitTx(new BabbleTx() {
            @Override
            public byte[] toBytes() {
                return sentBytes;
            }
        });
        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(rcvdTx);
        assertTrue(Arrays.equals(sentBytes, rcvdTx));
    }

    @Test
    public void getPeersTest() {

        final TestService testService = new TestService();

        testService.configureNew("alice", "localhost");
        testService.start();

        List<Peer> currentPeers = testService.getCurrentPeers();
        assertEquals(1, currentPeers.size());
        assertEquals("alice", currentPeers.get(0).moniker);

        List<Peer> genesisPeers = testService.getGenesisPeers();
        assertEquals(1, genesisPeers.size());
        assertEquals("alice", currentPeers.get(0).moniker);

    }

     */
}
