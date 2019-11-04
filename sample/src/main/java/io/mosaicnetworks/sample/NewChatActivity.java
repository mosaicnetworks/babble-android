package io.mosaicnetworks.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Random;

import io.mosaicnetworks.babble.discovery.Peer;

public class NewChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
    }

    // called when the user presses the start chat button
    public void startChat(View view) {
        //get moniker
        EditText editText = findViewById(R.id.editText);
        String moniker = editText.getText().toString();
        if (moniker.isEmpty()) {
            Random random = new Random();
            moniker = "AnonymousUser" + random.nextInt(10000);
        }

        MessagingService messagingService = MessagingService.getInstance();
        messagingService.configure(new ArrayList<Peer>(), moniker, Utils.getIPAddr(this));

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        startActivity(intent);
    }
}
