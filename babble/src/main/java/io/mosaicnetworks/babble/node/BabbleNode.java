package io.mosaicnetworks.babble.node;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

import io.mosaicnetworks.babble.discovery.PeersGetter;
import mobile.Mobile;
import mobile.Node;
import mobile.MobileConfig;

public class BabbleNode implements PeersGetter {

    private Node node;
    private BabbleNodeListeners listeners;
    private Gson customGson;

    // Constructor which uses the default babble config
    public BabbleNode(String peersJSON, String privateKeyHex, String netAddr, String moniker, BabbleNodeListeners listeners) {
        this(peersJSON, privateKeyHex, netAddr, moniker, listeners, new BabbleConfig());
    }

    public BabbleNode(String peersJSON, String privateKeyHex, String netAddr, String moniker, BabbleNodeListeners listeners, BabbleConfig babbleConfig) {

        this.listeners = listeners;
        initialiseJSONDecoder();

        MobileConfig mobileConfig = new MobileConfig(
                babbleConfig.heartbeat,
                babbleConfig.tcpTimeout,
                babbleConfig.maxPool,
                babbleConfig.cacheSize,
                babbleConfig.syncLimit,
                babbleConfig.enableFastSync,
                babbleConfig.store,
                babbleConfig.loglevel,
                moniker
        );

        node = Mobile.new_(
                privateKeyHex,
                netAddr,
                peersJSON,
                new mobile.CommitHandler() {
                    @Override
                    public byte[] onCommit(final byte[] blockBytes) {
                        String strJson = new String(blockBytes);
                        Log.d("Babble", "Received CommitBlock " + strJson);
                        Block block;
                        try {
                            block = customGson.fromJson(strJson, Block.class);
                        } catch (JsonSyntaxException ex) {
                            Log.e("Babble", "Failed to parse Block", ex);
                            return null;
                        }

                        return BabbleNode.this.listeners.onReceiveTransactions(block.body.transactions);
                    }

                },
                new mobile.ExceptionHandler() {
                    @Override
                    public void onException(final String msg) {
                        BabbleNode.this.listeners.onException(msg);
                    }
                },
                mobileConfig);

    }

    private void initialiseJSONDecoder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(byte[].class, new JsonDeserializer<byte[]>() {
            public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Base64.decode(json.getAsString(), Base64.NO_WRAP);
            }
        });

        customGson = builder.create();
    }

    public void run() {
        //Not sure whether node can be null so we'll check just in case
        if (node != null) {
            node.run(true);
        }
    }

    public void shutdown() {
        if (node != null) {
            node.shutdown();
        }
    }

    public void leave(final LeaveResponseListener listener) {
        if (node != null) {
            // this blocks so we'll run in a separate thread
            new Thread(new Runnable() {
                public void run() {
                    node.leave();
                    listener.onSuccess();
                }
            }).start();
        } else {
            listener.onSuccess();
        }
    }

    public void submitTx(byte[] tx) {
        if (node != null) {
            node.submitTx(tx);
        }
    }

    @Override
    public String getPeers() {
        if (node != null) {
            return node.getPeers();
        }

        return "";
    }

    public String getStats() {
        if (node != null) {
            return node.getStats();
        }

        return "";
    }
}




