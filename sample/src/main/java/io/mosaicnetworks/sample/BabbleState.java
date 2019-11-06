package io.mosaicnetworks.sample;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;

import io.mosaicnetworks.babble.node.BabbleNodeListeners;

public class BabbleState implements BabbleNodeListeners {

    private StateObserver mObserver;
    private byte[] mStateHash;

    public BabbleState(StateObserver observer) {
        mObserver = observer;
    }

    @Override
    public byte[] onReceiveTransactions(byte[][] transactions) {
        for (byte[] rawTx:transactions) {
            String tx = new String(rawTx, StandardCharsets.UTF_8);

            BabbleTx babbleTx;
            try {
                babbleTx = BabbleTx.fromJson(tx);
            } catch (JsonSyntaxException ex) {
                //skip any malformed transactions
                continue;
            }

            Message message = Message.fromBabbleTx(babbleTx);

            mObserver.onStateChanged(message);
        }

        //TODO: update state hash
        return mStateHash;
    }
}
