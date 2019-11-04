package io.mosaicnetworks.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;

public class ChatActivity extends AppCompatActivity implements MessageObserver {

    private MessagesList mMessagesList;
    private MessagesListAdapter<Message> mAdapter;
    private String mMoniker;
    private final MessagingService mMessagingService = MessagingService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        mMoniker = intent.getStringExtra("MONIKER");

        initialiseAdapter();
        mMessagingService.registerObserver(this);
        mMessagingService.start();

        if (mMessagingService.getState()!=MessagingService.State.RUNNING_WITH_DISCOVERY) {
            Toast.makeText(this, "Unable to advertise peers", Toast.LENGTH_LONG).show();
        }
    }

    private void initialiseAdapter() {
        mMessagesList = findViewById(R.id.messagesList);

        mAdapter = new MessagesListAdapter<>(mMoniker, null);
        mMessagesList.setAdapter(mAdapter);

        MessageInput input = findViewById(R.id.input);

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                mMessagingService.submitMessage(new Message(input.toString(), new Author(mMoniker), new Date()));
                return true;
            }
        });
    }

    @Override
    public void onMessageReceived(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addToStart(message, true);
            }
        });
    };

    @Override
    protected void onDestroy() {
        mMessagingService.stop();
        mMessagingService.removeObserver(this);

        super.onDestroy();
    }
}
