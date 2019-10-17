package io.mosaicnetworks.babble.discovery;

import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class HttpDiscoveryServer {

    private NanoWrapper nanoWrapper;

    public HttpDiscoveryServer(int port, PeersGetter peersGetter) {
        nanoWrapper = new NanoWrapper(port, peersGetter);
    }

    public HttpDiscoveryServer(String hostname, int port, PeersGetter peersGetter) {
        nanoWrapper = new NanoWrapper(hostname, port, peersGetter);
    }

    /***
     *
     * @throws IOException if the socket is in use.
     */
    public void start() throws IOException {
        nanoWrapper.start();
    }

    public void stop() {
        nanoWrapper.stop();
    }

    private class NanoWrapper extends NanoHTTPD {

        private PeersGetter peersGetter;

        public NanoWrapper(int port, PeersGetter peersGetter) {
            super(port);
            this.peersGetter = peersGetter;
        }

        public NanoWrapper(String hostname, int port, PeersGetter peersGetter) {
            super(hostname, port);
            this.peersGetter = peersGetter;
        }

        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() == Method.GET) {

                if (session.getUri().equals("/peers")) {
                    if (peersGetter != null) {
                        return newFixedLengthResponse(peersGetter.getPeers());
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
