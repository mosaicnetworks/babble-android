package io.mosaicnetworks.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;

public class JoinChatActivity extends AppCompatActivity implements ResponseListener {

    private ProgressDialog mLoadingDialog;
    private static final Gson gson = new Gson();
    private String moniker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_chat);
        initLoadingDialog();
    }

    // called when the user presses the join chat button
    public void joinChat(View view) {
        mLoadingDialog.show();

        //get peer IP address
        EditText editIP = findViewById(R.id.editText);
        final String peerIP = editIP.getText().toString();
        if (peerIP.isEmpty()) {
            return; //TODO: display alert dialog?
        }

        //get moniker
        EditText editText = findViewById(R.id.editText2);
        moniker = editText.getText().toString();
        if (moniker.isEmpty()) {
            Random random = new Random();
            moniker = "AnonymousUser" + random.nextInt(10000); //TODO: check this is a reliable way of getting rand
        }

        //get peersJson
        HttpPeerDiscoveryRequest httpPeerDiscoveryRequest;
        try {
            httpPeerDiscoveryRequest = new HttpPeerDiscoveryRequest(peerIP, this);

        } catch (IllegalArgumentException ex) {
            mLoadingDialog.dismiss();
            return; //TODO: display alert dialog?
        }
        httpPeerDiscoveryRequest.send();
    }

    public void onReceivePeers(List<Peer> peers) {
        MessagingService messagingService = MessagingService.getInstance();
        messagingService.configure(peers, moniker, Utils.getIPAddr(this));

        mLoadingDialog.dismiss();
        Intent intent = new Intent(JoinChatActivity.this, ChatActivity.class);
        intent.putExtra("MONIKER", moniker);
        startActivity(intent);
    }

    public void onFailure(io.mosaicnetworks.babble.discovery.ResponseListener.Error error) {
        //TODO: display alert dialog
        Log.d("TAG", error.toString());
        mLoadingDialog.dismiss();
    }


    private void initLoadingDialog() {
        //TODO: replace ProgressDialog with ProgressBar
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setTitle("Please wait...");
        mLoadingDialog.setMessage("Fetching peers list from host");
        mLoadingDialog.setIndeterminate(true);
        mLoadingDialog.setCanceledOnTouchOutside(false);
    }
}
