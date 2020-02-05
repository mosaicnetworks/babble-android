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

package io.mosaicnetworks.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import io.mosaicnetworks.babble.configure.BaseConfigActivity;
import io.mosaicnetworks.babble.node.BabbleService;

public class MainActivity extends BaseConfigActivity {

    public MainActivity() {
        super();

        // Control which tabs are shown. This needs to be in the constructor as they are used in the onCreate handler
        try {
            setShowArchive(true);
            setShowmDNS(true);
            setShowP2P(true);
        } catch (IllegalStateException ex) {


            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert Dialog");
            alertDialog.setMessage("Failed to set tab visibility");


            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alertDialog.show();

            ex.printStackTrace();

            throw ex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// If you wish to change the Babble Configuration Backup Policy uncomment the code below:

//        ConfigManager.setConfigDirectoryBackupPolicy(
//                [ConfigManager.ConfigDirectoryBackupPolicy.ABORT|SINGLE_BACKUP|DELETE|COMPLETE_BACKUP]
//        );

// If you want to change the length of the unique ID component of the configuration dir name,
// uncomment the line below

//        ConfigManager.setUniqueIdLength(8);

// If you want to change the storage location used for babble-go configuration and database files
// uncomment the line below.

//        ConfigManager.setRootDir(getApplicationContext().getExternalFilesDir(null).toString());
    }

    @Override
    public BabbleService getBabbleService() {
        return MessagingService.getInstance(this);
    }

    @Override
    public void onJoined(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        startActivity(intent);
    }

    @Override
    public void onStartedNew(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        startActivity(intent);
    }

    @Override
    public void onArchiveLoaded(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", true);
        startActivity(intent);
    }
}