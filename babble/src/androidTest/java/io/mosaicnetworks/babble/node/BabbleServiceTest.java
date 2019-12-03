package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BabbleServiceTest {

    private static class TestState implements BabbleState {

        private byte[] mLastTx;

        @Override
        public byte[] applyTransactions(byte[][] transactions) {
            mLastTx = transactions[0];
            return new byte[0];
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
            super(new TestState());
        }
    }

    private byte[] rcvdTx;

    @Test
    public void configureNew() throws InterruptedException {

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
}
