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

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

/**
 * An HttpPeerDiscoveryServer serves a list of peers. There are two endpoints, genesis-peers and
 * current-peers. The complementary {@link HttpPeerDiscoveryRequest} class can be used to request
 * peers from this server. The server obtains the list of peers via a PeersProvider which is passed
 * in the constructor
 */
public final class HttpPeerDiscoveryServer {

    private final NanoWrapper mNanoWrapper;

    /**
     * Constructs the server on given port.
     * @param port the port number on which to serve
     * @param peersProvider provides a method for obtaining the list of peers (the
     *                      {@link io.mosaicnetworks.babble.node.BabbleNode} class implements this
     *                      interface so it can be used as a provider)
     */
    public HttpPeerDiscoveryServer(int port, PeersProvider peersProvider) {
        mNanoWrapper = new NanoWrapper(port, peersProvider);
    }

    /**
     * Constructs the server on given hostname and port
     * @param hostname the hostname of the interface to bind to
     * @param port the port number on which to serve
     * @param peersProvider provides a method for obtaining the list of peers (the BabbleNode class
     *                      implements this interface so it can be used as a provider)
     */
    public HttpPeerDiscoveryServer(String hostname, int port, PeersProvider peersProvider) {
        mNanoWrapper = new NanoWrapper(hostname, port, peersProvider);
    }

    /**
     * Start serving
     * @throws IOException if the socket is in use or it cannot bind to the interface.
     */
    public void start() throws IOException {
        mNanoWrapper.start();
    }

    /**
     * Stop serving
     */
    public void stop() {
        mNanoWrapper.stop();
    }

    private class NanoWrapper extends NanoHTTPD {

        private final PeersProvider peersProvider;

        public NanoWrapper(int port, PeersProvider peersProvider) {
            super(port);
            this.peersProvider = peersProvider;
        }

        public NanoWrapper(String hostname, int port, PeersProvider peersProvider) {
            super(hostname, port);
            this.peersProvider = peersProvider;
        }

        //TODO: return correct http status codes
        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() != Method.GET) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                        "The requested resource does not exist");
            }

            String uri = session.getUri();
            String peersJson;

            switch (uri) {
                case "/genesis-peers":
                    peersJson = peersProvider.getGenesisPeers();
                    break;
                case "/current-peers":
                    peersJson = peersProvider.getCurrentPeers();
                    break;
                default :
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                            "The requested resource does not exist");
            }

            if (peersJson==null) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                        "Could not get requested resource");
            }

            return newFixedLengthResponse(peersJson);

        }
    }
}
