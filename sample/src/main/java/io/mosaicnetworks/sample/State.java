package io.mosaicnetworks.sample;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleNodeListeners;
import io.mosaicnetworks.babble.node.KeyPair;

public class State implements BabbleNodeListeners {

    private BabbleNode babbleNode;
    private ChatActivity chatActivity;
    private List<Message> pendingMessages = new ArrayList<>();
    private byte[] stateHash = "genesis-hash".getBytes();

    State(KeyPair keyPair, String moniker, ChatActivity chatActivity, List<Peer> peers, String mIPAddr, int babblePort) {

        this.chatActivity = chatActivity;

        babbleNode = new BabbleNode(peers, keyPair.privateKey,
                mIPAddr, babblePort, moniker, this);

        babbleNode.run();
    }

    @Override
    public byte[] onReceiveTransactions(byte[][] transactions) {
        //TODO check which thread this runs on
        for (byte[] rawTx : transactions) {

            String tx = "";
            try {
                tx = new String(rawTx, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d(MainActivity.TAG, "Processing message: " + tx);

            try {
                Gson gson = new Gson();
                TxMessage txMessage = gson.fromJson(tx, TxMessage.class);
                pendingMessages.add(new Message(txMessage.text,new Author(
                        txMessage.from), new Date()));
            } catch (JsonSyntaxException ex){
                Log.d(MainActivity.TAG, "Unable to process transaction" + ex);
            }

            updateStateHash(tx);
        }

        chatActivity.runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {

                                           for (Message msg : pendingMessages) {

                                               chatActivity.receivedMessage(msg);
                                           }

                                           pendingMessages.clear();
                                       }
                                   }
        );

        return stateHash;
    }

    private void updateStateHash(String tx) {
        stateHash = hashFromTwoHashes(stateHash, Hash(tx));
    }

    public byte[] Hash(String tx) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tx.getBytes(Charset.forName("UTF-8")));
            return hash;
        } catch(NoSuchAlgorithmException ex){
            // In a real app we'd probably want to let the user know, then gracefully close the app.
            // We'll throw a runtime exception, which in this app isn't caught, so the app will just
            // crash!
            throw new RuntimeException(ex);
        }
    }

    public byte[] hashFromTwoHashes(byte[] a, byte[] b) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] tempHash = new byte[a.length + b.length];
            System.arraycopy(a, 0, tempHash, 0, a.length);
            System.arraycopy(b, 0, tempHash, 0, b.length);
            return digest.digest(tempHash);
        } catch(NoSuchAlgorithmException ex){
            // In a real app we'd probably want to let the user know, then gracefully close the app.
            // We'll throw a runtime exception, which in this app isn't caught, so the app will just
            // crash!
            throw new RuntimeException(ex);
        }
    }

    public void submitTx(byte[] tx) {
        babbleNode.submitTx(tx);
    }

    public void shutdown() {
        babbleNode.shutdown();
    }

    public BabbleNode getNode() {
        return this.babbleNode;
    }
}
