package io.mosaicnetworks.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import io.mosaicnetworks.babble.discovery.HttpDiscoveryServer;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.KeyPair;

public class ChatActivity extends AppCompatActivity implements PeersListeners, StateListeners {

    private static final int BABBLE_PORT = 6666;
    private static final int PEER_PORT = 8988;
    private MessagesList messagesList;
    private State state;
    private MessagesListAdapter<Message> adapter;
    private HttpDiscoveryServer httpDiscoveryServer;
    private String moniker;
    private boolean newChat;
    private String peerIP;
    private KeyPair keyPair;
    private String mIPAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get the Intent that started this activity and extract data
        Intent intent = getIntent();
        moniker = intent.getStringExtra("MONIKER");
        newChat = intent.getBooleanExtra("NEW_CHAT", true);
        peerIP = intent.getStringExtra("PEER_IP");

        keyPair = new KeyPair();
        mIPAddr = Utils.getIPAddr(getApplicationContext());

        initialiseAdapter();
        getPeers();
    }

    private void initialiseAdapter() {
        this.messagesList = findViewById(R.id.messagesList);

        adapter = new MessagesListAdapter<>(moniker, null);
        messagesList.setAdapter(adapter);

        MessageInput input = findViewById(R.id.input);

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                TxMessage txMessage = new TxMessage(moniker, input.toString());
                Gson gson = new Gson();
                String txJSON = gson.toJson(txMessage);

                if (state != null) {
                    Log.d(MainActivity.TAG, "Submitting Tx to babble: " + txJSON);
                    state.submitTx(txJSON.getBytes(Charset.forName("UTF-8")));
                }

                return true;
            }
        });
    }

    private void getPeers() {
        if (newChat) {
            Peers.genPeers(keyPair, mIPAddr, BABBLE_PORT, moniker, this);
        } else {
            Peers.requestPeers(peerIP, PEER_PORT, this);
        }
    }

    @Override
    public void getPeersFail() {
        Toast.makeText(ChatActivity.this, "Failed to get a valid peers list",
                Toast.LENGTH_LONG).show();
        // Go back to JoinChatActivity. Cleanup is handled by onDestroy.
        Intent intent = new Intent(ChatActivity.this, JoinChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void receivedPeers(List<Peer> peers) {
        state = new State(keyPair, moniker, this, peers, mIPAddr, BABBLE_PORT);
        advertisePeers();
    }

    @Override
    public void receivedMessage(Message msg) {
        adapter.addToStart(msg, true);
    }

    @Override
    public void babbleError() {
        Toast.makeText(ChatActivity.this, "Oops! Something went wrong.",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void advertisePeers() {

        if (state != null) {
            httpDiscoveryServer = new HttpDiscoveryServer(PEER_PORT, state.getNode());

            try {
                httpDiscoveryServer.start();
            } catch (IOException ex) {
                Log.d(MainActivity.TAG, String.format("Port %d in use, unable to advertise peers",
                        PEER_PORT));
                Toast.makeText(this, String.format("Port %d in use, unable to advertise " +
                        "peers", PEER_PORT), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(MainActivity.TAG, String.format("Babble node is null, unable to advertise peers",
                    PEER_PORT));
            Toast.makeText(this, String.format("Unable to advertise peers", PEER_PORT),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (state != null) {
            state.shutdown();
        }

        if (httpDiscoveryServer != null) {
            httpDiscoveryServer.stop();
        }

        super.onDestroy();
    }
}
