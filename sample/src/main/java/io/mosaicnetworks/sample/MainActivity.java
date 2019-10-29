package io.mosaicnetworks.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "SAMPLE-CHAT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // called when the user presses the new chat button
    public void newChat(View view) {
        Intent intent = new Intent(this, NewChatActivity.class);
        startActivity(intent);
    }

    // called when the user presses the join chat button
    public void joinChat(View view) {
        Intent intent = new Intent(this, JoinChatActivity.class);
        startActivity(intent);
    }
}