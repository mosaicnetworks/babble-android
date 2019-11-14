package io.mosaicnetworks.sample;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;

public class JoinChatActivity extends AppCompatActivity implements ResponseListener, StoppedObserver {

    private ProgressDialog mPeersLoadingDialog;
    private ProgressDialog mServiceStoppingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpPeerDiscoveryRequest;
    private final MessagingService mMessagingService = MessagingService.getInstance();
    private List<Peer> mDiscoveredPeers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_chat);
        initPeersLoadingDialog();
        initServiceStoppingDialog();
    }

    // called when the user presses the join chat button
    public void joinChat(View view) {
        //get moniker
        EditText editText = findViewById(R.id.editMoniker);
        mMoniker = editText.getText().toString();
        if (mMoniker.isEmpty()) {
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        //get peer IP address
        EditText editIP = findViewById(R.id.editHost);
        final String peerIP = editIP.getText().toString();
        if (peerIP.isEmpty()) {
            displayOkAlertDialog(R.string.no_hostname_alert_title, R.string.no_hostname_alert_message);
            return;
        }

        //get peersJson
        try {
            mHttpPeerDiscoveryRequest = new HttpPeerDiscoveryRequest(peerIP, MessagingService.DISCOVERY_PORT, this);
        } catch (IllegalArgumentException ex) {
            displayOkAlertDialog(R.string.invalid_hostname_alert_title, R.string.invalid_hostname_alert_message);
            return;
        }
        mPeersLoadingDialog.show();
        mHttpPeerDiscoveryRequest.send();
    }

    @Override
    public void onReceivePeers(final List<Peer> peers) {
        mDiscoveredPeers = peers;
        mPeersLoadingDialog.dismiss();

        MessagingService.State serviceState = mMessagingService.getState();
        if (serviceState == MessagingService.State.RUNNING || serviceState == MessagingService.State.RUNNING_WITH_DISCOVERY) {
            mServiceStoppingDialog.show();
            mMessagingService.registerStoppedObserver(this);
            mMessagingService.stop();
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

    private void joinChat() {
        mMessagingService.configure(mDiscoveredPeers, mMoniker, Utils.getIPAddr(this));
        mMessagingService.start();
        Intent intent = new Intent(JoinChatActivity.this, ChatActivity.class);
        intent.putExtra("MONIKER", mMoniker);
        startActivity(intent);
    }

    @Override
    public void onFailure(io.mosaicnetworks.babble.discovery.ResponseListener.Error error) {
        mPeersLoadingDialog.dismiss();
        int messageId;
        switch (error) {
            case INVALID_JSON:
                messageId = R.string.peers_json_error_alert_message;
                break;
            case CONNECTION_ERROR:
                messageId = R.string.peers_connection_error_alert_message;
                break;
            case TIMEOUT:
                messageId = R.string.peers_timeout_error_alert_message;
                break;
            default:
                messageId = R.string.peers_unknown_error_alert_message;
        }
        displayOkAlertDialog(R.string.peers_error_alert_title, messageId);
    }

    private void initPeersLoadingDialog() {
        mPeersLoadingDialog = new ProgressDialog(this);
        mPeersLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mPeersLoadingDialog.setTitle(R.string.loading_title);
        mPeersLoadingDialog.setMessage(getString(R.string.loading_message));
        mPeersLoadingDialog.setIndeterminate(true);
        mPeersLoadingDialog.setCanceledOnTouchOutside(false);
        mPeersLoadingDialog.setCancelable(true);
        mPeersLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                //TODO: cancel httpDiscoverRequest - the callback will still run
            }});
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
                mMessagingService.removeStoppedObserver(JoinChatActivity.this);
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
        //TODO: cancel httpDiscoverRequest - stop memory leak + potential crash when the callback runs
        super.onDestroy();
    }
}
