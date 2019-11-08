package io.mosaicnetworks.babble.discovery;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

/**
 * An HttpPeerDiscoveryServer serves a list of peers. The complementary HttpPeersDiscoveryRequest
 * class can be used to request peers from this server. The server obtains the list of peers via
 * a PeersProvider which is passed in the constructor
 */
public final class HttpPeerDiscoveryServer {

    private final NanoWrapper mNanoWrapper;

    /**
     * Constructs the server on given port.
     * @param port the port number on which to serve
     * @param peersProvider provides a method for obtaining the list of peers (the BabbleNode class
     *                      implements this interface so it can be used as a provider)
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

            if (session.getMethod() == Method.GET) {

                if (session.getUri().equals("/peers")) {
                    String peersJson = peersProvider.getPeers();

                    if (peersJson==null) {
                        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                                "Could not get requested resource");
                    }

                    return newFixedLengthResponse(peersJson);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }
}
