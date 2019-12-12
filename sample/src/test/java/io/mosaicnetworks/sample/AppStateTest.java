package io.mosaicnetworks.sample;

import org.junit.Test;

import java.util.List;

import io.mosaicnetworks.babble.node.BabbleTx;
import io.mosaicnetworks.babble.node.Block;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppStateTest {

    @Test
    public void getMessagesIndexTooBigTest() {

        ChatState chatState = new ChatState();

        List<Message> messageList = chatState.getMessagesFromIndex(0);

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void getMessagesNegativeIndexTest() {

        ChatState appState = new ChatState();

        try {
            List<Message> messageList = appState.getMessagesFromIndex(-5);
        } catch (IllegalArgumentException ex) {
            return;
        }

        throw new AssertionError("AppState should have thrown an IllegalStateException");

    }

    @Test
    public void applyMessageTest() {

        ChatState chatState = new ChatState();

        ChatTx chatTx = new ChatTx("alice", "hello camille, we need to talk!");
        byte[][] txs = new byte[1][];
        txs[0] = chatTx.toBytes();
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        List<Message> messages = chatState.getMessagesFromIndex(0);

        assertEquals(1, messages.size());
        assertEquals("alice", messages.get(0).getUser().getName());
        assertEquals("hello camille, we need to talk!", messages.get(0).getText());
    }

    @Test
    public void applyTwoMessageTest() {

        ChatState chatState = new ChatState();

        ChatTx chatTx = new ChatTx("alice", "hello camille, we need to talk!");
        byte[][] txs = new byte[1][];
        txs[0] = chatTx.toBytes();
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        ChatTx chatTx2 = new ChatTx("camille", "hi alice, sure!");
        byte[][] txs2 = new byte[1][];
        txs2[0] = chatTx2.toBytes();
        Block block2 = new Block();
        block2.body.transactions = txs2;
        chatState.processBlock(block2);

        List<Message> messages = chatState.getMessagesFromIndex(0);

        assertEquals(2, messages.size());
        assertEquals("alice", messages.get(0).getUser().getName());
        assertEquals("hello camille, we need to talk!", messages.get(0).getText());

        assertEquals("camille", messages.get(1).getUser().getName());
        assertEquals("hi alice, sure!", messages.get(1).getText());
    }

    @Test
    public void resetTest() {

        ChatState chatState = new ChatState();

        ChatTx chatTx = new ChatTx("alice", "hello camille, we need to talk!");
        byte[][] txs = new byte[1][];
        txs[0] = chatTx.toBytes();
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        chatState.reset();

        List<Message> messages = chatState.getMessagesFromIndex(0);

        assertEquals(0, messages.size());
    }
}
