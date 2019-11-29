package io.mosaicnetworks.babble.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import static android.content.Context.WIFI_SERVICE;

public class Utils {

    public static String getIPAddr(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
