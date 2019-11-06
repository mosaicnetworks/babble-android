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

import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;

public class JoinChatActivity extends AppCompatActivity implements ResponseListener {

    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpPeerDiscoveryRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_chat);
        initLoadingDialog();
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
            mHttpPeerDiscoveryRequest = new HttpPeerDiscoveryRequest(peerIP, this);
        } catch (IllegalArgumentException ex) {
            displayOkAlertDialog(R.string.invalid_hostname_alert_title, R.string.invalid_hostname_alert_message);
            return;
        }
        mLoadingDialog.show();
        mHttpPeerDiscoveryRequest.send();
    }

    @Override
    public void onReceivePeers(List<Peer> peers) {
        MessagingService messagingService = MessagingService.getInstance();
        messagingService.configure(peers, mMoniker, Utils.getIPAddr(this));

        mLoadingDialog.dismiss();
        Intent intent = new Intent(JoinChatActivity.this, ChatActivity.class);
        intent.putExtra("MONIKER", mMoniker);
        startActivity(intent);
    }

    @Override
    public void onFailure(io.mosaicnetworks.babble.discovery.ResponseListener.Error error) {
        mLoadingDialog.dismiss();
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

    private void initLoadingDialog() {
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setTitle(R.string.loading_title);
        mLoadingDialog.setMessage(getString(R.string.loading_message));
        mLoadingDialog.setIndeterminate(true);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                //TODO: cancel and nullify httpDiscoverRequest - stop memory leak
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
        //TODO: cancel and nullify httpDiscoverRequest - stop memory leak
        super.onDestroy();
    }
}
