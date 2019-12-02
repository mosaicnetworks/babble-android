package io.mosaicnetworks.babble.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UtilsTest {

    //@Rule
    //public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
    //        android.Manifest.permission.INTERNET);

    @Test
    public void getIpAddrTest() {

        Context appContext = InstrumentationRegistry.getTargetContext();

        String ipAddr = Utils.getIPAddr(appContext);

        Log.d("asdfgh", ipAddr);

        Log.d("asdfh" , "IP length: " + ipAddr.length());

    }
}
