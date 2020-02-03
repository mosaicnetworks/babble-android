/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
