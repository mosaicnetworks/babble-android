package io.mosaicnetworks.sample;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;


public class BabbleStateTest {

    @Test
    public void receiveTxEmitMessageTest() {

        BabbleState babbleState = new BabbleState(new StateObserver() {
            @Override
            public void onStateChanged(Message message) {
                assertEquals("alice", message.getUser().getName());
                assertEquals("hello camille, we need to talk!", message.getText());
            }
        });

        BabbleTx babbleTx = new BabbleTx("alice", "hello camille, we need to talk!");
        byte[][] txs = new byte[1][];
        txs[0] = babbleTx.toBytes();
        babbleState.onReceiveTransactions(txs);
    }

    @Test
    public void stateHashTest() {

        BabbleState babbleState = new BabbleState(new StateObserver() {
            @Override
            public void onStateChanged(Message message) {
            }
        });

        BabbleTx babbleTx = new BabbleTx("alice", "hello camille, we need to talk!");

        byte[][] txs = new byte[1][];
        txs[0] = babbleTx.toBytes();
        byte[] rcvdStateHash = babbleState.onReceiveTransactions(txs);
        String rcvdStateHashHex = DatatypeConverter.printHexBinary(rcvdStateHash);

        assertEquals("7BACA63B4D687684899AB730EF4A09DC49DC5CEE44972F5636534C7197F40924", rcvdStateHashHex);
    }
}
