package io.mosaicnetworks.sample;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import io.mosaicnetworks.babble.discovery.Peer;

public class NewChatActivity extends AppCompatActivity implements StoppedObserver{

    private MessagingService mMessagingService = MessagingService.getInstance();
    private ProgressDialog mServiceStoppingDialog;
    private String mMoniker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        initServiceStoppingDialog();
    }

    // called when the user presses the start chat button
    public void startChat(View view) {
        //get moniker
        EditText editText = findViewById(R.id.editText);
        mMoniker = editText.getText().toString();
        if (mMoniker.isEmpty()) {
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        MessagingService messagingService = MessagingService.getInstance();
        try {
            messagingService.configure(new ArrayList<Peer>(), moniker, Utils.getIPAddr(this));
        } catch (IllegalStateException ex) {
            //we tried to reconfigure before a leave completed
            displayOkAlertDialog(R.string.babble_busy_title, R.string.babble_busy_message);
            return;
        }
        joinChat();
    }

    @Override
    public void onServiceStopped() {
        mMessagingService.removeStoppedObserver(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mServiceStoppingDialog.dismiss();
                joinChat();
            }
        });

    }

        messagingService.start();
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MONIKER", mMoniker);
        startActivity(intent);
    }

    private void initServiceStoppingDialog() {
        mServiceStoppingDialog = new ProgressDialog(this);
        mServiceStoppingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mServiceStoppingDialog.setTitle(R.string.stopping_title);
        mServiceStoppingDialog.setMessage(getString(R.string.stopping_message));
        mServiceStoppingDialog.setIndeterminate(true);
        mServiceStoppingDialog.setCanceledOnTouchOutside(false);
        mServiceStoppingDialog.setCancelable(true);
        mServiceStoppingDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                mMessagingService.removeStoppedObserver(NewChatActivity.this);
            }});
    }

    private void displayOkAlertDialog(@StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        mMessagingService.removeStoppedObserver(this);
        super.onDestroy();
    }
}
