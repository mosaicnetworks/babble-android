package io.mosaicnetworks.babble.node;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.PeersProvider;
import mobile.Mobile;
import mobile.Node;




/**
 * This is the core Babble node. It can be used directly or alternatively the {@link BabbleService}
 * class can be used to offer the same functionality wrapped up as a service. After creating the
 * node, call {@link BabbleNode#run()} to start it.
 */
public final class BabbleNode implements PeersProvider {


    public enum ConfigFolderBackupPolicy  {DELETE, SINGLE_BACKUP, COMPLETE_BACKUP, ABORT}

    private static ConfigFolderBackupPolicy mConfigFolderBackupPolicy = ConfigFolderBackupPolicy.SINGLE_BACKUP;
    private final static Gson mGson = new Gson();
    private final Node mNode;

    /**
     * Create a node with default config
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param privateKeyHex private key as produced by the {@link KeyPair} class
     * @param inetAddress ip address for the node to bind to
     * @param port the port number to bind to
     * @param moniker node moniker
     * @param blockConsumer the object which will receive the transactions
     * @param configDir the root babble configuration folder
     * @param subConfigDir the folder within configDir for this configuration
     * @param configFolderBackupPolicy sets the behaviour for when creating / joining an extant network
     * @param appId a unique identifier for this app, may be FQDN but does not have to be. Used for discovery filtering
     * @return a babble node object
     */
    public static BabbleNode create(List<Peer> genesisPeers, List<Peer> currentPeers,
                                    String privateKeyHex, String inetAddress,
                                    int port, String moniker, BlockConsumer blockConsumer, String configDir,
                                    String subConfigDir, ConfigFolderBackupPolicy configFolderBackupPolicy, String appId) {

        return createWithConfig(genesisPeers, currentPeers, privateKeyHex, inetAddress, port, moniker, blockConsumer,
                new NodeConfig.Builder().build(), configDir, subConfigDir, configFolderBackupPolicy, appId);
    }

    /**
     * Create a node with custom config
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param privateKeyHex private key as produced by the {@link KeyPair} class
     * @param inetAddress ip address for the node to bind to
     * @param port the port number to bind to
     * @param moniker node moniker
     * @param blockConsumer the object which will receive the transactions
     * @param nodeConfig custom configuration
     * @param configDir the root babble configuration folder
     * @param subConfigDir the folder within configDir for this configuration
     * @param configFolderBackupPolicy sets the behaviour for when creating / joining an extant network
     * @param appId a unique identifier for this app, may be FQDN but does not have to be. Used for discovery filtering
     * @return a babble node object
     */
    public static BabbleNode createWithConfig(List<Peer> genesisPeers, List<Peer> currentPeers,
                                              String privateKeyHex,
                                              String inetAddress, int port, String moniker,
                                              final BlockConsumer blockConsumer,
                                              NodeConfig nodeConfig,
                                              String configDir,
                                              String subConfigDir,
                                              ConfigFolderBackupPolicy configFolderBackupPolicy,
                                              String appId) {


        mConfigFolderBackupPolicy = configFolderBackupPolicy ;
        // babble.toml
        ConfigManager configManager = new ConfigManager(configDir, appId, mConfigFolderBackupPolicy);
        String fullPath = configManager.WriteBabbleTomlFiles(nodeConfig, subConfigDir, inetAddress, port, moniker);

        // peers files
        configManager.WritePeersJsonFiles(fullPath,  genesisPeers, currentPeers);

        // private key -- does not overwrite
        configManager.WritePrivateKey(fullPath, privateKeyHex);

        Log.d("fullPath", fullPath);

        Node node = Mobile.new_(
       //         privateKeyHex,
       //         inetAddress + ":" + port,
       //         mGson.toJson(currentPeers),
       //         mGson.toJson(genesisPeers),
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
                }, fullPath);
               // mobileConfig);

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






