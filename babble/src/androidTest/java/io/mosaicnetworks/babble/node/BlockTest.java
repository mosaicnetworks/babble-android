package io.mosaicnetworks.babble.node;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

// import static org.junit.Assert.assertArrayEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BlockTest {

    @Test
    public void BlocksTest() {

        String blockJson = "{\"Body\":{\"Index\":1,\"RoundReceived\":6,\"StateHash\":\"\",\"FrameHash\":\"mVKthxLv6woSfAZjZRp9nXsxIBunM7Poszm4q26EzqQ=\",\"PeersHash\":\"rbeyzzZrJI3VQFyS2f9gteGLPWZ1HM24VA5ET/fKbAc=\",\"Transactions\":[\"eyJmcm9tIjoiZmZmYyIsImltYWdlcyI6W10sInRleHQiOiJnaHZjIiwidHlwZSI6InRleHQifQ==\"],\"InternalTransactions\":[],\"InternalTransactionReceipts\":null},\"Signatures\":{}}";

        Block block = Block.fromJson(blockJson);

        assertNotNull(block.body);
    }
}
