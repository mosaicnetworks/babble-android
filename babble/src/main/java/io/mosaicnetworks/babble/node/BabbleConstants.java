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


    public final int BABBLE_PORT;
    public final String APP_ID;
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

        String appId = context.getResources().getString(R.string.babble_app_id);
        if (appId.equals("")) {
            APP_ID = appContext.getPackageName();
        } else {
            APP_ID = appId;
        }
    }


}
