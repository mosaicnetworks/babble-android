/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.babble.node;


import android.content.Context;
import android.util.Log;

import io.mosaicnetworks.babble.R;

/**
 * The babble library contains a resource file babble.xml which contains constant parameters
 * used by the babble library. Any of these constants can be overwritten by including
 * a resource file in your app that contains the same field names.
 */
public class BabbleConstants {

    /**
     * The port used by babble for gossiping.
     *
     * Set in {@link io.mosaicnetworks.babble.R.integer#babble_port}
     *
     * @return the Babble port
     */
    public static int BABBLE_PORT() {return INSTANCE.BABBLE_PORT;}

    /**
     * The app id used by Babble identify itself (and ignore other app Ids).
     * If this field is set to an empty string, this is set to the app package
     * name.
     *
     * Set in {@link io.mosaicnetworks.babble.R.string#babble_app_id}
     *
     * @return the Babble port
     */
    public static String APP_ID() {return INSTANCE.APP_ID;}

    /**
     * The subfolder of the file store for the app that contains all of the babble-go configuration
     * files and badger_db databases
     */
    public static String BABBLE_ROOTDIR() {return INSTANCE.BABBLE_ROOTDIR;}

    /**
     * The subfolder within a configuration folder that contains the badger_db database
     */
    public static String DB_SUBDIR() {return INSTANCE.DB_SUBDIR;}


    /**
     * The name of the configuration file for babble. It will be in the root of the babble
     * configuration folder
     */
    public static String BABBLE_TOML() {return INSTANCE.BABBLE_TOML;}

    /**
     * The name of the peers file for babble. It will be in the root of the babble
     * configuration folder
     */
    public static String PEERS_JSON() {return INSTANCE.PEERS_JSON;}

    /**
     * The name of the initial peers file for babble. It will be in the root of the babble
     * configuration folder
     */
    public static String PEERS_GENESIS_JSON() {return INSTANCE.PEERS_GENESIS_JSON;}

    /**
     * The name of the file containing the babble private key. It will be in the root of the babble
     * configuration folder
     */
    public static String PRIV_KEY() {return INSTANCE.PRIV_KEY;}



    public final int BABBLE_PORT;
    public final String APP_ID;
    public final String BABBLE_ROOTDIR;
    public final String DB_SUBDIR;

    public final String BABBLE_TOML;
    public final String PEERS_JSON;
    public final String PEERS_GENESIS_JSON;
    public final String PRIV_KEY;


    private static BabbleConstants INSTANCE;



    public static void initialise(Context context) {
        if (INSTANCE==null) {
            Log.v("ConfigManager", "getInstance");
            INSTANCE = new BabbleConstants(context.getApplicationContext());
            return ;
        }

        throw new RuntimeException("BabbleConstants already initialised");
    }

    public static BabbleConstants getInstance(Context context) {
        if (INSTANCE==null) {
            Log.v("ConfigManager", "getInstance");
            INSTANCE = new BabbleConstants(context.getApplicationContext());
        }

        return INSTANCE;
    }


    public BabbleConstants(Context context) {

        Context appContext = context.getApplicationContext();

        BABBLE_PORT = context.getResources().getInteger(R.integer.babble_port);
        BABBLE_ROOTDIR = context.getResources().getString(R.string.babble_root_dir);
        DB_SUBDIR = context.getResources().getString(R.string.babble_db_subdir);

        BABBLE_TOML = context.getResources().getString(R.string.babble_toml);
        PEERS_JSON = context.getResources().getString(R.string.babble_peers_json);
        PEERS_GENESIS_JSON = context.getResources().getString(R.string.babble_peers_genesis_json);
        PRIV_KEY = context.getResources().getString(R.string.babble_priv_key);


        String appId = context.getResources().getString(R.string.babble_app_id);
        if (appId.equals("")) {
            APP_ID = appContext.getPackageName();
        } else {
            APP_ID = appId;
        }
    }


}
