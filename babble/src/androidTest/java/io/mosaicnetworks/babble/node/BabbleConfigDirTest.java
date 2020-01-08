package io.mosaicnetworks.babble.node;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
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
public class BabbleConfigDirTest {

    @Rule public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET);

    @Test

    public void writeDefaultConfigTest() throws InterruptedException  {
        BabbleConfig babbleConfig = new BabbleConfig.Builder()
                .cacheSize(12)
                .logLevel(BabbleConfig.LogLevel.ERROR)
                .build();




        BabbleConfigDir babbleConfigDir = new BabbleConfigDir(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir().toString());

        babbleConfigDir.WriteBabbleTomlFiles(babbleConfig,"test", "127.0.0.1", 6666,"test");

        assertEquals(12, babbleConfig.cacheSize);
        assertEquals("error", babbleConfig.logLevel);

    }





}
