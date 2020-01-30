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

package io.mosaicnetworks.babble.node;

import android.util.Log;

import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;

import io.mosaicnetworks.babble.discovery.PeersProvider;
import mobile.Mobile;
import mobile.Node;

/**
 * This is the core Babble node. It can be used directly or alternatively the {@link BabbleService}
 * class can be used to offer the same functionality wrapped up as a service. After creating the
 * node, call {@link BabbleNode#run()} to start it.
 */
public final class BabbleNode implements PeersProvider {

    private final Node mNode;

    public static BabbleNode create(final BlockConsumer blockConsumer, String configDir) {

        Log.i("BabbleNode.create", configDir);
        Node node = Mobile.new_(
                new mobile.CommitHandler() {
                    @Override
                    public byte[] onCommit(final byte[] blockBytes) {
                        String strJson = new String(blockBytes, Charset.forName("UTF-8"));
                        try {
                            Block incomingBlock = Block.fromJson(strJson);

                            Block processedBlock = blockConsumer.onReceiveBlock(incomingBlock);

                            // Encode and return block
                            String jsonProcessedBlock = processedBlock.toJson();
                            System.out.println("Processed Block " + processedBlock);
                            return jsonProcessedBlock.getBytes();

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

                        //TODO: throw different exceptions based on the received message

                        throw new IllegalArgumentException(msg);
                    }
                }, configDir);

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

    /**
     * Run the node
     */
    //TODO: get rid of null checks
    //TODO: timeout on calls - each call can block indefinitely
    public void run() {
        if (mNode != null) {
            mNode.run(true);
        }
    }

    /**
     * Shutdown the node without exiting the group. The node will not be removed from the validator
     * set when shutdown is called
     */
    public void shutdown() {
        if (mNode != null) {
            mNode.shutdown();
        }
    }

    /**
     * Asynchronous method for leaving a group. The node is removed from the validator set when leave
     * is called
     * @param listener called when leave completes
     */
    public void leave(final LeaveResponseListener listener) {
        if (mNode != null) {
            // this blocks so we'll run in a separate thread
            new Thread(new Runnable() {
                public void run() {
                    mNode.leave();
                    listener.onComplete();
                }
            }).start();
        } else {
            listener.onComplete();
        }
    }

    /**
     * Submit a transaction to the network
     * @param tx the raw transaction
     */
    public void submitTx(byte[] tx) {
        if (mNode != null) {
            mNode.submitTx(tx);
        }
    }

    /**
     * Provide genesis peers
     * @return list of genesis peers
     */
    @Override
    public String getGenesisPeers() {
        if (mNode != null) {
            return mNode.getGenesisPeers();
        }

        return null;
    }

    /**
     * Provide current peers
     * @return list of current peers
     */
    @Override
    public String getCurrentPeers() {
        if (mNode != null) {
            return mNode.getPeers();
        }

        return null;
    }

    /**
     * Provide node statistics
     * @return json formatted string of statistics
     */
    public String getStats() {
        if (mNode != null) {
            return mNode.getStats();
        }

        return null;
    }
}






