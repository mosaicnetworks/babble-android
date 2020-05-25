package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.content.Context;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.service.BabbleService;

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

    @Test
    public void submitTxTest() throws InterruptedException{

        final CountDownLatch lock = new CountDownLatch(1);

        final byte[] sendBytes = new byte[]{(byte) 0x12, (byte) 0xa5, (byte) 0xd1, (byte) 0x00};

        List<Peer> peers = new ArrayList<>();
        peers.add(new Peer("0X042C817E10B79FE2FB4A92BABD74A84E3D3B2E4EAEC03159A99CFC2969B079475A3165C0F473A4FB2602F7948AC2A27BB1945A6F712667E0DF8FE1619FF6D7C3E2",
                "localhost:6666", "camille"));
        String privateKeyHex = "0e63404232a2c096792fcd7e5a8dcf1c5abd2677312aa8492ee86824c02b10d6";
        GroupDescriptor groupDescriptor = new GroupDescriptor("test_group");
        String moniker = "camile";
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        BlockConsumer blockConsumer = new BlockConsumer() {

            @Override
            public Block onReceiveBlock(Block block) {

                rcvBytes = block.body.transactions[0];
                lock.countDown();

                block.body.stateHash = new byte[0];
                return block;
            }
        };

        ConfigManager configManager = ConfigManager.getInstance(context);
        String configDirectory = configManager.createConfigNewGroup(groupDescriptor, moniker, "localhost", "localhost", BabbleService.NETWORK_WIFI);

        BabbleNode babbleNode = BabbleNode.create(blockConsumer, configDirectory, null);

        babbleNode.run();
        babbleNode.submitTx(sendBytes);

        lock.await(3000, TimeUnit.MILLISECONDS);

        babbleNode.shutdown();

        assertArrayEquals(sendBytes, rcvBytes);
    }

}
