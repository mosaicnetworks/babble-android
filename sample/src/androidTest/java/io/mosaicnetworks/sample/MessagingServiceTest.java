package io.mosaicnetworks.sample;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.mosaicnetworks.babble.discovery.Peer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceTest {

    private Message rcvdMessage;
    @Test
    public void submitTxTest() throws InterruptedException{

        final CountDownLatch lock = new CountDownLatch(1);
        Message sentMessage = new Message("hello", "alice");

        MessagingService messagingService = MessagingService.getInstance();
        messagingService.registerObserver(new MessageObserver() {
            @Override
            public void onMessageReceived(Message message) {
                rcvdMessage = message;
                lock.countDown();
            }
        });

        List<Peer> peers = new ArrayList<>();
        messagingService.configure(peers, "alice", "localhost");
        messagingService.start();
        messagingService.submitMessage(sentMessage);
        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(rcvdMessage);
        assertEquals(sentMessage.getText(), rcvdMessage.getText());
        assertEquals(sentMessage.getUser().getName(), rcvdMessage.getUser().getName());
    }
}
