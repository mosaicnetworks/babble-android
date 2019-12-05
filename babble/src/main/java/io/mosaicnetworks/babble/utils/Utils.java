package io.mosaicnetworks.babble.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import static android.content.Context.WIFI_SERVICE;

/**
 * A collection of utilities
 */
public class Utils {

    /**
     * Returns the device's IPv4 address on the wireless network. Returns 0.0.0.0 if the device is
     * not connected.
     * @param context an instance of the applications context
     * @return the ip address in dot-decimal notation
     */
    public static String getIPAddr(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
