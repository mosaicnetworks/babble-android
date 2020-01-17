package io.mosaicnetworks.sample;

import android.content.Intent;

import io.mosaicnetworks.babble.configure.BaseConfigActivity;
import io.mosaicnetworks.babble.node.BabbleService;

public class MainActivity extends BaseConfigActivity {

    @Override
    public BabbleService getBabbleService() {
        return MessagingService.getInstance(this);
    }

    @Override
    public void onJoined(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        startActivity(intent);
    }

    @Override
    public void onStartedNew(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", false);
        startActivity(intent);
    }

    @Override
    public void onArchiveLoaded(String moniker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        intent.putExtra("ARCHIVE_MODE", true);
        startActivity(intent);
    }
}