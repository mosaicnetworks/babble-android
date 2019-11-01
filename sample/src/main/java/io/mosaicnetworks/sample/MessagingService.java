package io.mosaicnetworks.sample;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.node.BabbleNode;
import io.mosaicnetworks.babble.node.BabbleNodeListeners;
import io.mosaicnetworks.babble.node.KeyPair;
import io.mosaicnetworks.babble.node.LeaveResponseListener;

public class MessagingService implements BabbleNodeListeners {

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

    private MessagingService() {

    }

    public MessagingService getInstance() {
        if (instance==null) {
            instance = new MessagingService();
        }
        return instance;
    }

    public void configure(List<Peer> peers, String moniker, String inetAddress) {

        if (mState==State.RUNNING) {
            throw new IllegalStateException("Cannot configure when the service is running");
        }

        try {
            mBabbleNode = BabbleNode.create(peers, mKeyPair.privateKey, inetAddress, BABBLING_PORT,
                    moniker, this);
            mState = State.CONFIGURED;
        } catch (IllegalArgumentException ex) {
            //The reassignment of mState has failed, so leave it and the service state as before
            //TODO: need to catch port in use exceptions (IOException) and throw others
        }
    }

    public void start() {
        if (mState==State.UNCONFIGURED || mState==State.RUNNING) {
            throw new IllegalStateException();
        }

        mBabbleNode.run();
        mState=State.RUNNING;
    }

    public void stop() {
        if (mState==State.UNCONFIGURED || mState==State.RUNNING) {
            throw new IllegalStateException();
        }

        mBabbleNode.leave(new LeaveResponseListener() {
            @Override
            public void onSuccess() {
                mBabbleNode=null;
                mState = State.UNCONFIGURED;
            }
        });
    }

    public byte[] onReceiveTransactions(byte[][] transactions) {
        for (byte[] rawTx:transactions) {
            String tx = new String(rawTx, StandardCharsets.UTF_8);
            BabbleTx babbleTx = BabbleTx.fromJson(tx);
            Message message = Message.fromBabbleTx(babbleTx);
            notifyObservers(message);
        }

        return mStateHash;
    }

    public void registerObserver(MessageObserver messageObserver) {
        if (!mObservers.contains(messageObserver)) {
            mObservers.add(messageObserver);
        }
    }

    public void removeObserver(MessageObserver messageObserver) {
        mObservers.remove(messageObserver);
    }

    public void notifyObservers(Message message) {
        for (MessageObserver observer: mObservers) {
            observer.onMessageReceived(message);
        }
    }
}
