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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.node.ServiceObserver;
import io.mosaicnetworks.babble.service.BabbleServiceBinderActivity;

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
