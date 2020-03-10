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

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.ServiceObserver;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This is the central UI component. It receives messages from the {@link MessagingService} and
 * displays them as a list.
 */
public class ChatActivity extends AppCompatActivity implements ServiceObserver, StatsObserver  {

    private MessagesListAdapter<Message> mAdapter;
    private String mMoniker;
    private final MessagingService mMessagingService = MessagingService.getInstance(this);
    private Integer mMessageIndex = 0;
    private boolean mArchiveMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.i("ChatActivity", "onCreate");

        Intent intent = getIntent();
        mMoniker = intent.getStringExtra("MONIKER");
        mArchiveMode = intent.getBooleanExtra("ARCHIVE_MODE", false);
        String group = intent.getStringExtra("GROUP");

        setTitle(mMoniker +" in Group " + group );

        initialiseAdapter();
        mMessagingService.registerObserver(this);

        Log.i("ChatActivity", "registerObserver");


        if (mArchiveMode) {
            stateUpdated();

            if (mMessageIndex==0) {
                findViewById(R.id.relativeLayout_messages).setVisibility(View.GONE);
                findViewById(R.id.linearLayout_empty_archive).setVisibility(View.VISIBLE);
            }

        } else {
            if ((!mMessagingService.isAdvertising()) && (!mArchiveMode )) {
                Toast.makeText(this, "Unable to advertise peers", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initialiseAdapter() {
        MessagesList mMessagesList = findViewById(R.id.messagesList);

        mAdapter = new MessagesListAdapter<>(mMoniker,   new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                // If string URL starts with R. it is a resource.
                if (url.startsWith("R.")) {
                    String[] arrUrl = url.split("\\.", 3);
                    int ResID = getResources().getIdentifier(arrUrl[2] , arrUrl[1], ChatActivity.this.getPackageName());
                    Log.i("ChatActivity", "loadImage: " +ResID + " "+ arrUrl[2] + " "+ arrUrl[1] + " "+ ChatActivity.this.getPackageName());
                    if (ResID == 0) {
                        Picasso.get().load(R.drawable.error).into(imageView);
                    } else {
                        Picasso.get().load(ResID).into(imageView);  //TODO restore this line
                    }
                } else {
                    Picasso.get().load(url).into(imageView);
                }
            }
        });

        mMessagesList.setAdapter(mAdapter);

        MessageInput input = findViewById(R.id.input);
        if (mArchiveMode) {
            input.setVisibility(View.GONE);
        } else {
            input.setInputListener(new MessageInput.InputListener() {
                @Override
                public boolean onSubmit(CharSequence input) {
                    mMessagingService.submitTx(new Message(input.toString(), mMoniker));
                    return true;
                }
            });
        }
    }

    /**
     * Called after the {@link MessagingService} state is updated. This happens after transactions
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

        List<Message> newMessagesTemp = new ArrayList<>();

        for (Message m : mMessagingService.state.getMessagesFromIndex(mMessageIndex)) {

            if (m.author.equals(mMoniker)) {
                newMessagesTemp.add(m);
            } else {
                if (m.author.equals(Message.SYSTEM_MESSAGE_AUTHOR)) {
                    newMessagesTemp.add(m);
                } else {
                    newMessagesTemp.add(new Message(m.author+ ":\n" + m.text, m.author, m.date));
                }
            }

        }

        final List<Message> newMessages = newMessagesTemp;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Message message : newMessages ) {
                    mAdapter.addToStart(message, true);
                }
            }
        });

        mMessageIndex = mMessageIndex + newMessages.size();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }


    public void showChatters(MenuItem menuItem) {
        Gson gson = new Gson();
        Peer[] peers = gson.fromJson(mMessagingService.getMonikerList(), Peer[].class);

        String join = "";
        String peerlist = "Chatters in this Group: ";

        for (int i=0; i< peers.length; i++ ) {
            peerlist = peerlist + join + peers[i].moniker;
            join = ", ";
        }

        showMessage(peerlist, R.string.monikers_title, Toast.LENGTH_LONG);
    }



    public void showIP(MenuItem menuItem) {
        Context context = getApplicationContext();
        String ip = "Your IP is: "+ Utils.getIPAddr(context);

        showMessage(ip, R.string.ip_title, Toast.LENGTH_LONG);
    }


    public void showStats(MenuItem menuItem) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map map = gson.fromJson(mMessagingService.getStats(), Map.class);

        String stats = gson.toJson(map);

        showMessage(stats, R.string.stats_title, Toast.LENGTH_LONG);
    }



    private void showMessage (String message, @StringRes int titleId, int duration) {
        //  showMessageToast( message, duration);
        ShowMessageDialog(message, titleId);

    }

    private void ShowMessageDialog (String message, @StringRes int titleId) {

        DialogUtils.displayOkAlertDialogText(this,titleId,message) ;
    }

    private void showMessageToast(String message, int duration) {
        Context context = getApplicationContext();
        CharSequence text = message;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }



    /**
     * When back is pressed we should leave the group. The {@link #onDestroy()} method will handle
     * unregistering from the service
     */
    @Override
    public void onBackPressed() {
        mMessagingService.leave(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mMessagingService.removeObserver(this);

        super.onDestroy();
    }


    public void pollStats(MenuItem menuItem) {

        LinearLayout linearLayoutStatusLine = findViewById(R.id.statusLine);

        if (mMessagingService.getStatusPolling()) {
            mMessagingService.stopStatsPolling();
            linearLayoutStatusLine.setVisibility(View.GONE);

            Log.i("ChatActivity", "pollStats: stopping");
        } else {
            linearLayoutStatusLine.setVisibility(View.VISIBLE);
            mMessagingService.registerStatsObserver(this);
            mMessagingService.startStatsPolling();
            Log.i("ChatActivity", "pollStats: starting");
        }

    }



    @Override
    public void statsUpdated(Map map) {
        Log.i("ChatActivity", "statsUpdated");
        Log.i("ChatActivity", (String) map.get("time"));
        ((TextView) findViewById(R.id.babbleStatus)).setText((String) map.get("state"));
        ((TextView) findViewById(R.id.babbleEvents)).setText((String) map.get("consensus_events"));
        ((TextView) findViewById(R.id.babbleTransactions)).setText((String) map.get("consensus_transactions"));
        ((TextView) findViewById(R.id.babbleUndetermined)).setText((String) map.get("undetermined_events"));

    }
}