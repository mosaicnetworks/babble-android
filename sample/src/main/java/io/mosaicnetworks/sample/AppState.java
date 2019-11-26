package io.mosaicnetworks.sample;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.mosaicnetworks.babble.node.BabbleState;

public class AppState implements BabbleState {

    private Message mLatestMessage;
    private static final MessageDigest mSha256Digest;
    private byte[] mStateHash = "genesis-state".getBytes();

    static {
        try {
            mSha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            //  Every implementation of the Java platform is required to support the SHA-256
            //  MessageDigest algorithm, so we shouldn't get here!
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte[] applyTransactions(byte[][] transactions) {
        for (byte[] rawTx:transactions) {
            String tx = new String(rawTx, StandardCharsets.UTF_8);

            BabbleTx babbleTx;
            try {
                babbleTx = BabbleTx.fromJson(tx);
            } catch (JsonSyntaxException ex) {
                //skip any malformed transactions
                continue;
            }

            onMessageReceived(Message.fromBabbleTx(babbleTx));

        }

        return new byte[0];
    }

    @Override
    public void reset() {
        //do nothing
    }

    private void onMessageReceived(Message message) {

        mLatestMessage = message;
    }

    public Message getLatestMessage() {
        //TODO: this can return null if no messages are successfully parsed
        return mLatestMessage;
    }

    //TODO: use state hash
    private void updateStateHash(String tx) {
        mStateHash = hashFromTwoHashes(mStateHash, hash(tx));
    }

    private static byte[] hash(String tx) {
        return mSha256Digest.digest(tx.getBytes(Charset.forName("UTF-8")));
    }

    private static byte[] hashFromTwoHashes(byte[] a, byte[] b) {
        byte[] tempHash = new byte[a.length + b.length];
        System.arraycopy(a, 0, tempHash, 0, a.length);
        System.arraycopy(b, 0, tempHash, 0, b.length);
        return mSha256Digest.digest(tempHash);
    }
}
