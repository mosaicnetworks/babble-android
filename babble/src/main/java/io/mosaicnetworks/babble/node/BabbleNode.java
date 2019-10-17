package io.mosaicnetworks.babble.node;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersGetter;
import mobile.Mobile;
import mobile.Node;
import mobile.MobileConfig;

public final class BabbleNode implements PeersGetter {

    private final static Gson mGson = new Gson();
    private final Node mNode;

    public static BabbleNode create(List<Peer> peers, String privateKeyHex, String inetAddress,
                                    int port, String moniker, BabbleNodeListeners listeners) {

        return createWithConfig(peers, privateKeyHex, inetAddress, port, moniker, listeners,
                new BabbleConfig.Builder().build());
    }

    public static BabbleNode createWithConfig(List<Peer> peers, String privateKeyHex,
                                              String inetAddress, int port, String moniker,
                                              final BabbleNodeListeners listeners,
                                              BabbleConfig babbleConfig) {

        MobileConfig mobileConfig = new MobileConfig(
                babbleConfig.heartbeat,
                babbleConfig.tcpTimeout,
                babbleConfig.maxPool,
                babbleConfig.cacheSize,
                babbleConfig.syncLimit,
                babbleConfig.enableFastSync,
                babbleConfig.store,
                babbleConfig.logLevel,
                moniker
        );

        Node node = Mobile.new_(
                privateKeyHex,
                inetAddress + ":" + port,
                mGson.toJson(peers),
                new mobile.CommitHandler() {
                    @Override
                    public byte[] onCommit(final byte[] blockBytes) {
                        String strJson = new String(blockBytes, Charset.forName("UTF-8"));
                        try {
                            Block block = Block.fromJson(strJson);
                            return listeners.onReceiveTransactions(block.body.transactions);
                        } catch (JsonSyntaxException ex) {
                            return null;
                        }
                    }
                },
                new mobile.ExceptionHandler() {
                    @Override
                    public void onException(final String msg) {
                        // Since golang does not support throwing exceptions, if an
                        // initialisation error occurs, this callback is called synchronously
                        // (before Mobile.new_ returns).

                        throw new IllegalArgumentException(msg);
                    }
                },
                mobileConfig);

        // If mobile ExceptionHandler isn't called then mNode should not be null, however
        // just in case...
        if (node==null) {
            throw new IllegalArgumentException("Failed to initialise node");
        }

        return new BabbleNode(node);
    }

    private BabbleNode(Node node) {
        mNode = node;
    }

    //TODO: get rid of null checks
    //TODO: timeout on calls - each call can block indefinitely
    public void run() {
        if (mNode != null) {
            mNode.run(true);
        }
    }

    public void shutdown() {
        if (mNode != null) {
            mNode.shutdown();
        }
    }

    public void leave(final LeaveResponseListener listener) {
        if (mNode != null) {
            // this blocks so we'll run in a separate thread
            new Thread(new Runnable() {
                public void run() {
                    mNode.leave();
                    listener.onSuccess();
                }
            }).start();
        } else {
            listener.onSuccess();
        }
    }

    public void submitTx(byte[] tx) {
        if (mNode != null) {
            mNode.submitTx(tx);
        }
    }

    @Override
    public String getPeers() {
        if (mNode != null) {
            return mNode.getPeers();
        }

        return "";
    }

    public String getStats() {
        if (mNode != null) {
            return mNode.getStats();
        }

        return "";
    }
}






