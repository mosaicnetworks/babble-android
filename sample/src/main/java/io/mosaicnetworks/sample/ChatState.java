/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.sample;

import android.annotation.SuppressLint;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosaicnetworks.babble.node.BabbleState;
import io.mosaicnetworks.babble.node.Block;
import io.mosaicnetworks.babble.node.InternalTransactionReceipt;
import io.mosaicnetworks.sample.chatkit.commons.models.IMessage;
import io.mosaicnetworks.sample.notification.NotificationMessage;

/**
 * The core state of the App. The state is passed to the service during service construction. Public
 * methods in this class will be accessible to observers of the service.
 */
public class ChatState implements BabbleState {

    private byte[] mStateHash = new byte[0];
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, IMessage> mState = new HashMap<>();
    private Integer mNextIndex = 0;

    @Override
    public Block processBlock(Block block) {
        // Process regular transactions
        for (byte[] rawTx:block.body.transactions) {
            String tx = new String(rawTx, StandardCharsets.UTF_8);
            Message msg;
            try {
                msg = Message.fromJson(tx);
            } catch (JsonSyntaxException ex) {
                //skip any malformed transactions
                continue;
            }

            mState.put(mNextIndex, msg);
            mNextIndex++;
        }

        // Accept all internal transactions, and populate receipts.
        InternalTransactionReceipt[] itr = new InternalTransactionReceipt[block.body.internalTransactions.length];
        for(int i=0; i< block.body.internalTransactions.length; i++){
            itr[i] = block.body.internalTransactions[i].asAccepted();
            IMessage msg;

            if (block.body.internalTransactions[i].body.type == 0 ) {
                msg = new NotificationMessage(block.body.internalTransactions[i].body.peer.moniker + " joined the group");
            } else {
                msg = new NotificationMessage(block.body.internalTransactions[i].body.peer.moniker + " left the group");
            }
            mState.put(mNextIndex, msg);
            mNextIndex++;

        }

        // Set block stateHash and receipts
        block.body.stateHash = mStateHash;
        block.body.internalTransactionReceipts = itr;

        return block;
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
    public List<IMessage> getMessagesFromIndex(Integer index) {

        if (index<0) {
            throw new IllegalArgumentException("Index cannot be less than 0");
        }

        if (index >= mNextIndex) {
            return new ArrayList<>();
        }

        Integer numMessages = mNextIndex - index;

        List<IMessage> messages = new ArrayList<>(numMessages);

        for (int i = 0; i < numMessages; i++) {
            messages.add(mState.get(index + i));
        }

        return messages;
    }

    private void updateStateHash() {
        //TODO: implement the state hash
    }
}
