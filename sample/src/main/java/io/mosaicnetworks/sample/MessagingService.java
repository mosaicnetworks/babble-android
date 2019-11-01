package io.mosaicnetworks.sample;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleNodeListeners;
import io.mosaicnetworks.babble.node.KeyPair;
import io.mosaicnetworks.babble.node.LeaveResponseListener;

public class MessagingService {

    private enum State {
        UNCONFIGURED,
        CONFIGURED,
        RUNNING
    }

    private static MessagingService instance;
    private List<MessageObserver> mObservers = new ArrayList<>();
    private BabbleNode mBabbleNode;
    private KeyPair mKeyPair = new KeyPair();
    private static final int BABBLING_PORT = 6666;
    private byte[] mStateHash;
    private State mState = State.UNCONFIGURED;

    public static MessagingService getInstance() {
        if (instance==null) {
            instance = new MessagingService();
        }
        return instance;
    }

    public void configure(List<Peer> peers, String moniker, String inetAddress) {

        if (mState==State.RUNNING) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }

        //add ourselves to the peers list
        peers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + BABBLING_PORT, moniker));

        try {
            mBabbleNode = BabbleNode.create(peers, mKeyPair.privateKey, inetAddress, BABBLING_PORT,
                    moniker, new BabbleNodeListeners() {
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
                                notifyObservers(message);
                            }

                            return mStateHash;
                        }
                    });
            mState = State.CONFIGURED;
        } catch (IllegalArgumentException ex) {
            //The reassignment of mState has failed, so leave it and the service state as before
            //TODO: need to catch port in use exception (IOException) and throw others
        }
    }

    public void start() {
        if (mState==State.UNCONFIGURED || mState==State.RUNNING) {
            throw new IllegalStateException("Cannot start an unconfigured or running service");
        }

        mBabbleNode.run();
        mState=State.RUNNING;
    }

    public void stop() {
        if (mState!=State.RUNNING) {
            throw new IllegalStateException("Cannot stop a service which isn't running");
        }

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onSuccess() {
                mBabbleNode=null;
                mState = State.UNCONFIGURED;
            }
        });
    }

    public void submitMessage(Message message) {
        mBabbleNode.submitTx(message.toBabbleTx().toBytes());
    }

    public void registerObserver(MessageObserver messageObserver) {
        if (!mObservers.contains(messageObserver)) {
            mObservers.add(messageObserver);
        }
    }

    public void removeObserver(MessageObserver messageObserver) {
        mObservers.remove(messageObserver);
    }

    private void notifyObservers(Message message) {
        for (MessageObserver observer: mObservers) {
            observer.onMessageReceived(message);
        }
    }
}
