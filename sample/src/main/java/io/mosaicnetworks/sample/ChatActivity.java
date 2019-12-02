package io.mosaicnetworks.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.ServiceObserver;

/**
 * This is the central UI component. It receives messages from the {@link MessagingService} and
 * displays them as a list.
 */
public class ChatActivity extends AppCompatActivity implements ServiceObserver {

    private MessagesListAdapter<Message> mAdapter;
    private String mMoniker;
    private final MessagingService mMessagingService = MessagingService.getInstance();
    private Integer mMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        mMoniker = intent.getStringExtra("MONIKER");

        initialiseAdapter();
        mMessagingService.registerObserver(this);

        if (mMessagingService.getState()!= BabbleService.State.RUNNING_WITH_DISCOVERY) {
            Toast.makeText(this, "Unable to advertise peers", Toast.LENGTH_LONG).show();
        }
    }

    private void initialiseAdapter() {
        MessagesList mMessagesList = findViewById(R.id.messagesList);

        mAdapter = new MessagesListAdapter<>(mMoniker, null);
        mMessagesList.setAdapter(mAdapter);

        MessageInput input = findViewById(R.id.input);

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                mMessagingService.submitTx(new Message(input.toString(), mMoniker).toBabbleTx());
                return true;
            }
        });
    }

    /**
     * Called after the {@link MessagingService} state is updated. This happens after transactions
     * received from the babble node are applied to the state. At this point the
     * {@link ChatActivity} retrieves all messages with index greater than it's current index.
     */
    @Override
    public void stateUpdated() {

        final List<Message> newMessages = mMessagingService.state.getMessagesFromIndex(mMessageIndex);

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
        mMessagingService.leave(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mMessagingService.removeObserver(this);

        super.onDestroy();
    }
}
