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


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import io.mosaicnetworks.babble.configure.BaseConfigActivity;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

import io.mosaicnetworks.babble.service.BabbleService2;


public class MainActivity extends BaseConfigActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Control which tabs are shown.
        setShowArchive(true);
        setShowmDNS(true);
        setShowP2P(false);

        // Show all versions of each group in the archive tab
        setShowAllArchiveVersions(true);

        BabbleService2.setAppState(new ChatState());

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
    public void onJoined(String moniker, String group) {
        Intent intent = new Intent(this, ChatActivityAndroidService.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        intent.putExtra("GROUP", group);
        startActivity(intent);
    }

    @Override
    public void onStartedNew(String moniker, String group) {
        Intent intent = new Intent(this, ChatActivityAndroidService.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        intent.putExtra("GROUP", group);
        startActivity(intent);
    }

    public void onArchiveLoaded(String moniker, String group) {
        Intent intent = new Intent(this, ChatActivityAndroidService.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", true);
        intent.putExtra("GROUP", group);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void aboutDialog(MenuItem menuItem) {


        String preBlock = "<table style=\"border-collapse:collapse;border-spacing:0;\">";
        String postBlock = "</table>\n";
        String predata = "<td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#EBF5FF;\">";
        String prelabel = "<tr><td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#D2E4FC;\">";
        String postlabel = "</td>";
        String postdata = "</td></tr>\n";
        String databreak = "<tr><td colspan=\"2\" style=\"border-style:none;\">&nbsp;</td></tr>";

        String aboutText = "<hr>"+preBlock
                +prelabel+"App ID"+postlabel+predata + io.mosaicnetworks.sample.BuildConfig.APPLICATION_ID + postdata
                +prelabel+"Version Code"+postlabel+predata + io.mosaicnetworks.sample.BuildConfig.VERSION_CODE +postdata
                +prelabel+"Version Name"+postlabel+predata + io.mosaicnetworks.sample.BuildConfig.VERSION_NAME + postdata
                +prelabel+"Git Hash"+postlabel+predata + io.mosaicnetworks.sample.BuildConfig.GitHash + postdata
                +prelabel+"Git Branch"+postlabel+predata + io.mosaicnetworks.sample.BuildConfig.GitBranch+postdata
                +databreak
                +prelabel+ "Babble Package"+postlabel+predata + io.mosaicnetworks.babble.BuildConfig.LIBRARY_PACKAGE_NAME + postdata
                +prelabel+ "Version Code"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.VERSION_CODE + postdata
                +prelabel+ "Version Name"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.VERSION_NAME+ postdata
                +prelabel+ "Git Hash"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitHash+ postdata
                +prelabel+ "Git Hash Short"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitHashShort+ postdata
                +prelabel+ "Git Branch"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitBranch +postdata
                +databreak
                +prelabel+ "Babble Version"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleVersion +postdata
                +prelabel+ "Babble Repo"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleRepo +postdata
                +prelabel+ "Babble Method"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleMethod +postdata

                +databreak
                +prelabel+"IP Address"+postlabel+predata+ Utils.getIPAddr(this)+postdata
                +postBlock + "\n<hr>\n";

        DialogUtils.displayOkAlertDialogHTML(this, R.string.about_title, aboutText);
    }
}