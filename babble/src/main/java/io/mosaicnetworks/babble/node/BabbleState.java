package io.mosaicnetworks.babble.node;

public interface BabbleState {

    byte[] applyTransactions(byte[][] transactions);

    //byte[] getStateHash();

}
