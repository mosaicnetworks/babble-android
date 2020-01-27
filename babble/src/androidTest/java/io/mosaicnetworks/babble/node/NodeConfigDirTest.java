package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class NodeConfigDirTest {

    /*
    @Rule public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    @Test

    public void writeDefaultConfigTest() throws InterruptedException  {
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .cacheSize(12)
                .logLevel(NodeConfig.LogLevel.ERROR)
                .build();




        ConfigManager configManager = new ConfigManager(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir().toString());

        configManager.writeBabbleTomlFiles(nodeConfig,"test", "127.0.0.1", 6666,"test");

        assertEquals(12, nodeConfig.cacheSize);
        assertEquals("error", nodeConfig.logLevel);

    }


     */




}
