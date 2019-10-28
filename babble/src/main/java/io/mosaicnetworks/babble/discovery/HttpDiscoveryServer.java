package io.mosaicnetworks.babble.discovery;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public final class HttpDiscoveryServer {

    private final NanoWrapper mNanoWrapper;

    public HttpDiscoveryServer(int port, PeersProvider peersProvider) {
        mNanoWrapper = new NanoWrapper(port, peersProvider);
    }

    public HttpDiscoveryServer(String hostname, int port, PeersProvider peersProvider) {
        mNanoWrapper = new NanoWrapper(hostname, port, peersProvider);
    }

    /***
     *
     * @throws IOException if the socket is in use.
     */
    public void start() throws IOException {
        mNanoWrapper.start();
    }

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

        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() == Method.GET) {

                if (session.getUri().equals("/peers")) {
                    if (peersProvider != null) {
                        return newFixedLengthResponse(peersProvider.getPeers());
                    }
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                            "Could not get requested resource");
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }
}
