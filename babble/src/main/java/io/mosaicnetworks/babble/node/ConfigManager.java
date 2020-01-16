package io.mosaicnetworks.babble.node;


// import android.util.Log;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;


import io.mosaicnetworks.babble.discovery.Peer;

public class ConfigManager {



    public final static String BABBLE_ROOTDIR = "babble";
    public final static  String DB_SUBDIR = "badger_db";
    public final static  String BABBLE_TOML = "babble.toml";
    public final static  String PEERS_JSON = "peers.json";
    public final static  String PEERS_GENESIS_JSON = "peers.genesis.json";
    public final static  String PRIV_KEY = "priv_key";

    private String mRootDir;
    private String mTomlDir = "";
    private final String mAppId;


    private ArrayList<String> mDirectories;

    /**
     * Create an object to manage multiple Babble Configs
     * @param storageDir the root of the babble storage. Likely to be context.getFilesDir() or context.getExternalFilesDir().
     */
    public ConfigManager(String storageDir, String appID) {
        mRootDir = storageDir;
        mAppId = appID;

//        Log.i("ConfigManager", "ConfigDir: "+storageDir);

        File babbleDir = new File(this.mRootDir, BABBLE_ROOTDIR);
        this.mDirectories = new ArrayList<String>();
        if(babbleDir.exists()) {
            // Popualate mDirectories with the subfolders that are configured
            Collections.addAll(this.mDirectories,
                babbleDir.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                }));
        } else { // First run, so we create the root dir - clearly no subdirs yet
            babbleDir.mkdirs();
        }
    }

    /**
     * Check if this configuration already exists
     * @param subConfigDir is a subdirectory under the babble root
     * @return returns true if it already exists
     */
    boolean CheckDirectory(String subConfigDir) {
        return this.mDirectories.contains(subConfigDir);
    }

    /**
     * Gets a list of the configuration folders available to this app
     * @return An ArrayList<String> of the folder names with no path
     */
    public ArrayList<String> getmDirectories() {
        return mDirectories;
    }



    public String GetRandomSubConfigDir() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }



    boolean DeleteDirectory(String subConfigDir) {
        if ( ! this.CheckDirectory(subConfigDir)) { // Doesn't exist
            return false;
        }

        File dir = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir);

        return FileUtils.deleteQuietly(dir);
    }



    /**
     * Write Both Peers JSON files to the
     * @param targetDir is the directory that the peers files are to be written to.
     * @param privateKeyHex private key as produced by the {@link KeyPair} class
     */
    void WritePrivateKey(String targetDir, String privateKeyHex) {
        try {

        FileWriter fileWriter = new FileWriter(new File(targetDir, PRIV_KEY) );
        fileWriter.write(privateKeyHex);
        fileWriter.close();
        } catch (Exception e) {
            // Log.e("WritePrivateKey", e.toString());
        }
    }


    /**
     * Write Both Peers JSON files to the
     * @param targetDir is the directory that the peers files are to be written to.
     * @param genesisPeers is the initial peer list for this network
     * @param currentPeers is the current peer list for this network
     */
    void WritePeersJsonFiles(String targetDir, List<Peer> genesisPeers, List<Peer> currentPeers) {
        Gson gson = new Gson();
        try {

         //   Log.i("WritePeersJsonFiles", "JSON "+gson.toJson(currentPeers));

            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_JSON));
            gson.toJson(currentPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
         //   Log.e("WritePeersJsonFiles", e.toString());
        }

        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_GENESIS_JSON));
            gson.toJson(genesisPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
         //   Log.e("WritePeersJsonFiles", e.toString());
        }
    }

        /**
         * Write Babble Config to disk ready for Babble to use
         * @param nodeConfig is the babble configuration object
         * @param subConfigDir is the subfolder of the babble Subfolder of the local storage as passed to the constructor
         * @return the composite path where the babble.toml file was written
         */
    String WriteBabbleTomlFiles(NodeConfig nodeConfig, String subConfigDir, String inetAddress, int port, String moniker) {

        TomlWriter tomlWriter = new TomlWriter();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> babble = new HashMap<>();

        mTomlDir = this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir;
        File babbleDir = new File(mTomlDir, DB_SUBDIR);
        if(! babbleDir.exists()) {
            // Log.i("ConfigManager", "Creating "+DB_SUBDIR);
            babbleDir.mkdirs();
        }

        babble.put("datadir", mTomlDir) ;
        babble.put("db",  mTomlDir +File.separator+DB_SUBDIR) ;

        babble.put("log", nodeConfig.logLevel);
        babble.put("listen", inetAddress+":"+port);
        babble.put("advertise", inetAddress+":"+port);
        babble.put("no-service", nodeConfig.noService);

        if (! nodeConfig.serviceListen.equals("")) {  // Only set if set
            babble.put("service-listen", nodeConfig.serviceListen);
        }
        babble.put("heartbeat", nodeConfig.heartbeat+"ms");
        babble.put("slow-heartbeat", nodeConfig.slowHeartbeat+"ms");
        babble.put("max-pool", nodeConfig.maxPool);
        babble.put("timeout", nodeConfig.tcpTimeout+"ms");
        babble.put("join_timeout", nodeConfig.joinTimeout);
        babble.put("sync-limit", nodeConfig.syncLimit);
        babble.put("fast-sync", nodeConfig.enableFastSync);
        babble.put("store", nodeConfig.store);
        babble.put("cache-size", nodeConfig.cacheSize);
        babble.put("bootstrap", nodeConfig.bootstrap);
        babble.put("maintenance-mode", nodeConfig.maintenanceMode);
        babble.put("suspend-limit", nodeConfig.suspendLimit);
        babble.put("moniker", moniker);
        babble.put("loadpeers", nodeConfig.loadPeers);

        map.put("Babble", babble);

        try {
            tomlWriter.write(babble, new File(mTomlDir, BABBLE_TOML));
        } catch (Exception e) {
            //TODO catch this
            // Log.e(" WriteBabbleTomlFiles", e.toString());
        }


        if (! this.CheckDirectory(subConfigDir)) {
            this.mDirectories.add(subConfigDir);
        }
        return mTomlDir;

    }







}
