package io.mosaicnetworks.sample;

import org.junit.Test;

import java.util.List;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppStateTest {

    @Test
    public void getMessagesIndexTooBigTest() {

        AppState appState = new AppState();

        List<Message> messageList = appState.getMessagesFromIndex(0);

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void getMessagesNegativeIndexTest() {

        AppState appState = new AppState();

        try {
            List<Message> messageList = appState.getMessagesFromIndex(-5);
        } catch (IllegalArgumentException ex) {
            return;
        }

        throw new AssertionError("AppState should have thrown an IllegalStateException");

    }

    @Test
    public void applyMessageTest() {

        AppState appState = new AppState();

        BabbleTx babbleTx = new BabbleTx("alice", "hello camille, we need to talk!");
        byte[][] txs = new byte[1][];
        txs[0] = babbleTx.toBytes();
        appState.applyTransactions(txs);

        List<Message> messages = appState.getMessagesFromIndex(0);

        assertEquals(1, messages.size());
        assertEquals("alice", messages.get(0).getUser().getName());
        assertEquals("hello camille, we need to talk!", messages.get(0).getText());
    }
}
