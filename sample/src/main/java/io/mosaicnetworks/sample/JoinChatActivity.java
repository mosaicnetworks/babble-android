package io.mosaicnetworks.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.Random;

public class JoinChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_chat);
    }

    // called when the user presses the join chat button
    public void joinChat(View view) {

        //get moniker
        EditText editText = findViewById(R.id.editText2);
        String moniker = editText.getText().toString();
        if (moniker.isEmpty()) {
            Random random = new Random();
            moniker = "AnonymousUser" + random.nextInt(10000); //TODO: check this is reliable way of getting rand
        }

        //get peer IP address
        EditText editIP = findViewById(R.id.editText);
        String peerIP = editIP.getText().toString();
        if (peerIP.isEmpty()) {
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("PEER_IP", peerIP);
        intent.putExtra("NEW_CHAT", false);
        startActivity(intent);
    }
}
