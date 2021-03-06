package io.mosaicnetworks.sample;

import org.junit.Test;

import java.util.List;

import io.mosaicnetworks.babble.node.Block;
import io.mosaicnetworks.sample.chatkit.commons.models.IMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatStateTest {

    @Test
    public void getMessagesIndexTooBigTest() {

        ChatState chatState = new ChatState();

        List<IMessage> messageList = chatState.getMessagesFromIndex(0);

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void getMessagesNegativeIndexTest() {

        ChatState appState = new ChatState();

        try {
            List<IMessage> messageList = appState.getMessagesFromIndex(-5);
        } catch (IllegalArgumentException ex) {
            return;
        }

        throw new AssertionError("AppState should have thrown an IllegalStateException");

    }

    @Test
    public void applyMessageTest() {

        ChatState chatState = new ChatState();

        byte[][] txs = {
                new Message("hello camille, we need to talk!","alice" ).toBytes()
        };
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        List<IMessage> messages = chatState.getMessagesFromIndex(0);

        assertEquals(1, messages.size());
        assertEquals("alice", messages.get(0).getUser().getName());
        assertEquals("hello camille, we need to talk!", messages.get(0).getText());
    }

    @Test
    public void applyTwoMessageTest() {

        ChatState chatState = new ChatState();

        byte[][] txs = {
                new Message("hello camille, we need to talk!", "alice").toBytes()
        };
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        byte[][] txs2 = {
                new Message( "hi alice, sure!", "camille").toBytes()
        };
        Block block2 = new Block();
        block2.body.transactions = txs2;
        chatState.processBlock(block2);

        List<IMessage> messages = chatState.getMessagesFromIndex(0);

        assertEquals(2, messages.size());
        assertEquals("alice", messages.get(0).getUser().getName());
        assertEquals("hello camille, we need to talk!", messages.get(0).getText());

        assertEquals("camille", messages.get(1).getUser().getName());
        assertEquals("hi alice, sure!", messages.get(1).getText());
    }

    @Test
    public void resetTest() {

        ChatState chatState = new ChatState();

        byte[][] txs = {
                new Message("hello camille, we need to talk!", "alice" ).toBytes()
        };
        Block block = new Block();
        block.body.transactions = txs;
        chatState.processBlock(block);

        chatState.reset();

        List<IMessage> messages = chatState.getMessagesFromIndex(0);

        assertEquals(0, messages.size());
    }
}
