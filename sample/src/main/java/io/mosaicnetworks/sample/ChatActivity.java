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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.service.BabbleServiceBinderActivity;
import io.mosaicnetworks.babble.service.ServiceObserver2;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;
import io.mosaicnetworks.sample.chatkit.commons.models.IMessage;
import io.mosaicnetworks.sample.chatkit.messages.MessageHolders;
import io.mosaicnetworks.sample.chatkit.messages.MessageInput;
import io.mosaicnetworks.sample.chatkit.messages.MessagesList;
import io.mosaicnetworks.sample.chatkit.messages.MessagesListAdapter;
import io.mosaicnetworks.sample.notification.Checker;
import io.mosaicnetworks.sample.notification.NotificationHolder;

/**
 * This is the central UI component. It receives messages from the BabbleService} and
 * displays them as a list.
 */
public class ChatActivity extends BabbleServiceBinderActivity implements ServiceObserver2 {

    private MessagesListAdapter mAdapter;
    private String mMoniker;
    private Integer mMessageIndex = 0;
    private boolean mArchiveMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        mMoniker = intent.getStringExtra("MONIKER");
        mArchiveMode = intent.getBooleanExtra("ARCHIVE_MODE", false);
        String group = intent.getStringExtra("GROUP");

        setTitle(group);
        initialiseAdapter();
        doBindService();
    }

    @Override
    protected void onServiceConnected() {
        mBoundService.registerObserver(this);

        //we need to call stateUpdate() to ensure messages are pulled on configuration changes
        stateUpdated();

        if (mArchiveMode) {
            stateUpdated();
            if (mMessageIndex==0) {
                findViewById(R.id.relativeLayout_messages).setVisibility(View.GONE);
                findViewById(R.id.linearLayout_empty_archive).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onServiceDisconnected() {
        //do nothing
    }

    private void initialiseAdapter() {
        MessagesList mMessagesList = findViewById(R.id.messagesList);

        MessageHolders messageHolders = new MessageHolders();
        messageHolders.registerContentType(new Integer(12).byteValue(), NotificationHolder.class,
                R.layout.item_notification_message, R.layout.item_notification_message, new Checker());

        mAdapter = new MessagesListAdapter<>(mMoniker, messageHolders, null);

        mMessagesList.setAdapter(mAdapter);

        MessageInput input = findViewById(R.id.input);
        if (mArchiveMode) {
            input.setVisibility(View.GONE);
        } else {
            input.setInputListener(new MessageInput.InputListener() {
                @Override
                public boolean onSubmit(CharSequence input) {
                    mBoundService.submitTx(new Message(input.toString(), mMoniker));
                    return true;
                }
            });
        }
    }

    /**
     * Called after the BabbleService state is updated. This happens after transactions
     * received from the babble node are applied to the state. At this point the
     * {@link ChatActivity} retrieves all messages with index greater than it's current index.
     */
    @Override
    public void stateUpdated() {

        if (mMessageIndex==0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.relativeLayout_messages).setVisibility(View.VISIBLE);
                    findViewById(R.id.linearLayout_empty_archive).setVisibility(View.GONE);
                }
            });
        }

        final List<IMessage> newMessages = ((ChatState) mBoundService.getAppState()).getMessagesFromIndex(mMessageIndex);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (IMessage message : newMessages ) {
                    mAdapter.addToStart(message, true);
                }
            }
        });

        mMessageIndex = mMessageIndex + newMessages.size();
    }

    @Override
    public void onNodeStateChanged(BabbleNode.State state) {

        if (state== BabbleNode.State.Suspended) {
            DialogUtils.displayOkAlertDialog(this, R.string.node_suspended_title, R.string.node_suspended_message);
            MessageInput input = findViewById(R.id.input);
            input.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat2, menu);
        return true;
    }

    public void showChatters(MenuItem menuItem) {
        Gson gson = new Gson();
        Peer[] peers = gson.fromJson(mBoundService.getMonikerList(), Peer[].class);

        String preBlock = "<table style=\"border-collapse:collapse;border-spacing:0;\">";
        String postBlock = "</table>\n";
        String predata = "<td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#EBF5FF;font-size: 50%;\">";
        String prelabel = "<tr><td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#D2E4FC;\">";
        String postlabel = "</td>";
        String postdata = "</td></tr>\n";
        String databreak = "<tr><td colspan=\"2\" style=\"border-style:none;\">&nbsp;</td></tr>";

        // String html = "<h4>"+this.getResources().getString(R.string.monikers_preamble)+"</h4>\n"+preBlock;
        String html = preBlock;

        for (int i=0; i< peers.length; i++ ) {
            html = html + prelabel + peers[i].moniker + postlabel+predata + peers[i].pubKeyHex + postdata;
        }

        html = html + postBlock;
        DialogUtils.displayOkAlertDialogHTML(this,R.string.monikers_title,html) ;
    }

    public void showIP(MenuItem menuItem) {
        Context context = getApplicationContext();
        String ip = Utils.getIPAddr(context);
        DialogUtils.displayOkAlertDialogText(this,R.string.ip_title,ip) ;
    }

    public void showStats(MenuItem menuItem) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map map = gson.fromJson(mBoundService.getStats(), Map.class);

        if (map.containsKey("time"))  // Convert Unix nano seconds to a real date time
        {
            String timeStr = (String) map.get("time");
            Date currentTime = new Date(Long.parseLong(timeStr)  / 1000000L);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateString = formatter.format(currentTime);
            map.put("time", dateString);
        }

        String preBlock = "<table style=\"border-collapse:collapse;border-spacing:0;\">";
        String postBlock = "</table>\n";
        String predata = "<td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#EBF5FF;\">";
        String prelabel = "<tr><td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#D2E4FC;\">";
        String postlabel = "</td>";
        String postdata = "</td></tr>\n";
        String databreak = "<tr><td colspan=\"2\" style=\"border-style:none;\">&nbsp;</td></tr>";




        Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
        String html = preBlock;

        while(itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            html = html + prelabel + entry.getKey() + postlabel+predata + entry.getValue() + postdata;
        }

        html = html + postBlock;

        DialogUtils.displayOkAlertDialogHTML(this, R.string.stats_title, html);

    }

    /**
     * When back is pressed we should leave the group. The {@link #onDestroy()} method will handle
     * unregistering from the service
     */
    @Override
    public void onBackPressed() {
        mBoundService.leave(null);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        mBoundService.removeObserver(this);
        super.onDestroy();
    }
}
