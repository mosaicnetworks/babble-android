package io.mosaicnetworks.babble.discovery;

import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class HTTPDiscoveryServer {

    private NanoWrapper nanoWrapper;

    public HTTPDiscoveryServer(int port, PeersGetter peersGetter) {
        nanoWrapper = new NanoWrapper(port, peersGetter);
    }

    public HTTPDiscoveryServer(String hostname, int port, PeersGetter peersGetter) {
        nanoWrapper = new NanoWrapper(hostname, port, peersGetter);
    }

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

                Log.d("Babble", session.getUri());

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
