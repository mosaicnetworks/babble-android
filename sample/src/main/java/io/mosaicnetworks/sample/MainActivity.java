package io.mosaicnetworks.sample;

import android.content.Intent;

import io.mosaicnetworks.babble.configure.BaseConfigActivity;
import io.mosaicnetworks.babble.node.BabbleService;

public class MainActivity extends BaseConfigActivity {

    @Override
    public BabbleService getBabbleService() {
        return MessagingService.getInstance();
    }

    @Override
    public void onJoined(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        startActivity(intent);
    }

    @Override
    public void onStartedNew(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        startActivity(intent);
    }
}