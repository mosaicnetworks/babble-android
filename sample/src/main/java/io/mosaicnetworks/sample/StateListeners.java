package io.mosaicnetworks.sample;

public interface StateListeners {

    void receivedMessage(Message msg);

    void babbleError();
}
