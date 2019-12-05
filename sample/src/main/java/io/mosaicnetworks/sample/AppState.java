package io.mosaicnetworks.sample;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.node.BabbleState;

/**
 * The core state of the App. The state is passed to the service during service construction. Public
 * methods in this class will be accessible to observers of the service.
 */
public class AppState implements BabbleState {

    private byte[] mStateHash = new byte[0];
    private final Map<Integer, BabbleTx> mState = new HashMap<>();
    private Integer mNextIndex = 0;

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

            mState.put(mNextIndex, babbleTx);
            mNextIndex++;
        }

        updateStateHash();
        return mStateHash;
    }

    @Override
    public void reset() {
        mState.clear();
        mNextIndex = 0;
    }

    /**
     * Observers of the service can query the state to get all messages from a given index
     * @param index the index from which all messages with a higher index should be returned
     * @return a list of messages
     */
    public List<Message> getMessagesFromIndex(Integer index) {

        if (index<0) {
            throw new IllegalArgumentException("Index cannot be less than 0");
        }

        if (index >= mNextIndex) {
            return new ArrayList<>();
        }

        Integer numMessages = mNextIndex - index;

        List<Message> messages = new ArrayList<>(numMessages);

        for (int i = 0; i < numMessages; i++) {
            messages.add(Message.fromBabbleTx(mState.get(index + i)));
        }

        return messages;
    }

    private void updateStateHash() {
        //TODO: implement this
    }
}
