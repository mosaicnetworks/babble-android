package io.mosaicnetworks.babble.discovery;

import java.util.List;

/**
 * Listeners to a peer discovery request
 */
public interface ResponseListener {

    /**
     * The error returned when discovery fails
     */
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

        /**
         * Description
         * @return error description
         */
        public String getDescription() {
            return mDescription;
        }

        /**
         * The error code
         * @return error code
         */
        public int getCode() {
            return mCode;
        }

        /**
         * Convert to string
         * @return the string representation of the error
         */
        @Override
        public String toString() {
            return mCode + ": " + mDescription;
        }
    }

    /**
     * Peers received listener
     * @param peers a list of peers
     */
    void onReceivePeers(List<Peer> peers);

    /**
     * Failure listener
     * @param error the cause of the failure
     */
    void onFailure(Error error);
}
