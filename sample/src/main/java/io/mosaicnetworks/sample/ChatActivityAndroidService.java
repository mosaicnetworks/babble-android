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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.node.ServiceObserver;
import io.mosaicnetworks.babble.service.BabbleServiceBinderActivity;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This is the central UI component. It receives messages from the {@link MessagingService} and
 * displays them as a list.
 */
public class ChatActivityAndroidService extends BabbleServiceBinderActivity implements ServiceObserver {

    private MessagesListAdapter<Message> mAdapter;
    private String mMoniker;
    private Integer mMessageIndex = 0;
    private boolean mArchiveMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        BabbleConstants.getInstance(this);

        Log.i("ChatActivity", "onCreate");

        Intent intent = getIntent();
        mMoniker = intent.getStringExtra("MONIKER");
        mArchiveMode = intent.getBooleanExtra("ARCHIVE_MODE", false);

        initialiseAdapter();
        doBindService();


    }

    @Override
    protected void onServiceConnected() {

        mBoundService.registerObserver(this);

        Log.i("ChatActivity", "registerObserver");


        if (mArchiveMode) {
            stateUpdated();

            if (mMessageIndex==0) {
                findViewById(R.id.relativeLayout_messages).setVisibility(View.GONE);
                findViewById(R.id.linearLayout_empty_archive).setVisibility(View.VISIBLE);
            }

        } else {
            /*
            if ((!mBoundService.isAdvertising()) && (!mArchiveMode )) {
                Toast.makeText(this, "Unable to advertise peers", Toast.LENGTH_LONG).show();
            }

             */
        }


    }

    @Override
    protected void onServiceDisconnected() {

    }

    private void initialiseAdapter() {
        MessagesList mMessagesList = findViewById(R.id.messagesList);

        mAdapter = new MessagesListAdapter<>(mMoniker,   new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                // If string URL starts with R. it is a resource.
                if (url.startsWith("R.")) {
                    String[] arrUrl = url.split("\\.", 3);
                    int ResID = getResources().getIdentifier(arrUrl[2] , arrUrl[1], ChatActivityAndroidService.this.getPackageName());
                    Log.i("ChatActivity", "loadImage: " +ResID + " "+ arrUrl[2] + " "+ arrUrl[1] + " "+ ChatActivityAndroidService.this.getPackageName());
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
                    mBoundService.submitTx(new Message(input.toString(), mMoniker));
                    return true;
                }
            });
        }
    }

    /**
     * Called after the {@link MessagingService} state is updated. This happens after transactions
     * received from the babble node are applied to the state. At this point the
     * {@link ChatActivityAndroidService} retrieves all messages with index greater than it's current index.
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

        for (Message m : ((ChatState) mBoundService.getAppState()).getMessagesFromIndex(mMessageIndex)) {

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
        Peer[] peers = gson.fromJson(mBoundService.getMonikerList(), Peer[].class);

        String join = "";
        String peerList = this.getResources().getString(R.string.monikers_preamble);

        for (int i=0; i< peers.length; i++ ) {
            peerList = peerList + join + peers[i].moniker;
            join = ",\n";
        }

        DialogUtils.displayOkAlertDialogText(this,R.string.monikers_title,peerList) ;

    }



    public void showIP(MenuItem menuItem) {
        Context context = getApplicationContext();
        String ip = "Your IP is: "+ Utils.getIPAddr(context);

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



        boolean plainText = false;
        if (plainText) {
            String rawStats = gson.toJson(map);

            String stats = rawStats.replace('{', '\0')
                    .replace('}', '\0')
                    .replace('"', '\0')
                    .replace(',', '\0');

            DialogUtils.displayOkAlertDialogText(this, R.string.stats_title, stats);
        } else {
            Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
            String html = "<table>\n";

            while(itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                html = html + "<tr><td><font color=\"#0000ff\">" + entry.getKey() + "</font></td><td><font color=\"#ff0000\"> " + entry.getValue() + "</font></td></tr>\n";
            }

            html = html + "</table>\n";

            DialogUtils.displayOkAlertDialogHTML(this, R.string.stats_title, html);
        }
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
