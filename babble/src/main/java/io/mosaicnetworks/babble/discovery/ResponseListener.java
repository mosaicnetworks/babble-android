package io.mosaicnetworks.babble.discovery;

import java.util.List;

public interface ResponseListener {

    enum Error {

        INVALID_JSON(0, "Invalid JSON"),
        CONNECTION_ERROR(1, "Connection error"),
        TIMEOUT(2,"Timed out");

        private final int mCode;
        private final String mDescription;

        Error(int code, String description) {
            mCode = code;
            mDescription = description;
        }

        public String getDescription() {
            return mDescription;
        }

        public int getCode() {
            return mCode;
        }

        @Override
        public String toString() {
            return mCode + ": " + mDescription;
        }
    }

    void onReceivePeers(List<Peer> peers);

    void onFailure(Error error);
}
