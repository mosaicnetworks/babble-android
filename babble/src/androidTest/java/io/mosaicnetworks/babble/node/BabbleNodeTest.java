package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.discovery.Peer;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BabbleNodeTest {

    @Rule public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    private byte[] rcvBytes;
    private byte[] nodeOneRcvBytes;
    private byte[] nodeTwoRcvBytes;
    private byte[] nodeThreeRcvBytes;

    @Test
    public void submitTxTest() throws InterruptedException{

        final CountDownLatch lock = new CountDownLatch(1);

        final byte[] sendBytes = new byte[]{(byte) 0x12, (byte) 0xa5, (byte) 0xd1, (byte) 0x00};

        List<Peer> peers = new ArrayList<>();
        peers.add(new Peer("0X042C817E10B79FE2FB4A92BABD74A84E3D3B2E4EAEC03159A99CFC2969B079475A3165C0F473A4FB2602F7948AC2A27BB1945A6F712667E0DF8FE1619FF6D7C3E2",
                "localhost:6666", "camille"));
        String privateKeyHex = "0e63404232a2c096792fcd7e5a8dcf1c5abd2677312aa8492ee86824c02b10d6";

        BlockConsumer blockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNode = BabbleNode.create(peers, peers, privateKeyHex, "localhost",
                6666, "camille", blockConsumer);

        babbleNode.run();
        babbleNode.submitTx(sendBytes);

        lock.await(3000, TimeUnit.MILLISECONDS);

        babbleNode.shutdown();

        assertArrayEquals(sendBytes, rcvBytes);
    }

    @Test
    public void submitTxTwoNodesTest() throws InterruptedException {

        final CountDownLatch lock = new CountDownLatch(2);
        final byte[] sendBytes = new byte[]{(byte) 0x12, (byte) 0xa5, (byte) 0xd1, (byte) 0x00};

        //initial peers list containing Node 1
        List<Peer> peers = new ArrayList<>();
        peers.add(new Peer("0X042C817E10B79FE2FB4A92BABD74A84E3D3B2E4EAEC03159A99CFC2969B079475A3165C0F473A4FB2602F7948AC2A27BB1945A6F712667E0DF8FE1619FF6D7C3E2",
                "localhost:6666", "camille"));

        //region Node one setup
        //==========================================================================================

        String nodeOnePrivateKeyHex = "0e63404232a2c096792fcd7e5a8dcf1c5abd2677312aa8492ee86824c02b10d6";

        BlockConsumer nodeOneBlockConsumer = new BlockConsumer() {
            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNodeOne = BabbleNode.create(peers, peers, nodeOnePrivateKeyHex, "localhost",
                6666, "camille", nodeOneBlockConsumer);

        babbleNodeOne.run();

        //==========================================================================================
        //endregion

        //region Node two setup
        //==========================================================================================

        String nodeTwoPrivateKeyHex = "11784fe73705dc6f337f954f4695376e6ceae1f8045734074aeb13b3708a9bc5";

        BlockConsumer nodeTwoBlockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNodeTwo = BabbleNode.create(peers, peers, nodeTwoPrivateKeyHex, "localhost",
                6667, "alice", nodeTwoBlockConsumer);

        babbleNodeTwo.run();

        KeyPair keyPair = new KeyPair();
        Log.d("TAG-test", keyPair.privateKey);
        //==========================================================================================
        //endregion

        babbleNodeOne.submitTx(sendBytes);

        lock.await(3000, TimeUnit.MILLISECONDS);

        babbleNodeOne.shutdown();
        babbleNodeTwo.shutdown();

        assertArrayEquals(sendBytes, nodeOneRcvBytes);
        assertArrayEquals(sendBytes, nodeTwoRcvBytes);
    }

    @Test
    public void submitTxThreeNodesTest() throws InterruptedException {

        final CountDownLatch lock = new CountDownLatch(3);
        final byte[] sendBytes = new byte[]{(byte) 0x12, (byte) 0xa5, (byte) 0xd1, (byte) 0x00};

        //initial peers list containing Node 1
        List<Peer> peers = new ArrayList<>();
        peers.add(new Peer("0X042C817E10B79FE2FB4A92BABD74A84E3D3B2E4EAEC03159A99CFC2969B079475A3165C0F473A4FB2602F7948AC2A27BB1945A6F712667E0DF8FE1619FF6D7C3E2",
                "localhost:6666", "camille"));

        //region Node one setup
        //==========================================================================================

        String nodeOnePrivateKeyHex = "0e63404232a2c096792fcd7e5a8dcf1c5abd2677312aa8492ee86824c02b10d6";

        BlockConsumer nodeOneBlockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNodeOne = BabbleNode.create(peers, peers, nodeOnePrivateKeyHex, "localhost",
                6666, "camille", nodeOneBlockConsumer);

        babbleNodeOne.run();

        //==========================================================================================
        //endregion

        //region Node two setup
        //==========================================================================================

        String nodeTwoPrivateKeyHex = "11784fe73705dc6f337f954f4695376e6ceae1f8045734074aeb13b3708a9bc5";

        BlockConsumer nodeTwoBlockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNodeTwo = BabbleNode.create(peers, peers, nodeTwoPrivateKeyHex, "localhost",
                6667, "alice", nodeTwoBlockConsumer);

        babbleNodeTwo.run();

        //==========================================================================================
        //endregion

        //region Node three setup
        //==========================================================================================

        String nodeThreePrivateKeyHex = "1cf7c949b5ffe3591f48d5bb2bcb41ba72e6600fe9ff5d302d45409a6c4a5733";

        BlockConsumer nodeThreeBlockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        BabbleNode babbleNodeThree = BabbleNode.create(peers, peers, nodeThreePrivateKeyHex, "localhost",
                6668, "monet", nodeThreeBlockConsumer);

        babbleNodeThree.run();

        //==========================================================================================
        //endregion

        babbleNodeOne.submitTx(sendBytes);

        lock.await(3000, TimeUnit.MILLISECONDS);

        babbleNodeOne.shutdown();
        babbleNodeTwo.shutdown();
        babbleNodeThree.shutdown();

        assertArrayEquals(sendBytes, nodeOneRcvBytes);
        assertArrayEquals(sendBytes, nodeTwoRcvBytes);
        assertArrayEquals(sendBytes, nodeThreeRcvBytes);
    }
}
