package io.mosaicnetworks.babble.node;


// import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;




import io.mosaicnetworks.babble.discovery.Peer;

public class BabbleConfigDir {

    public final static String BABBLE_ROOTDIR = "babble";
    public final static  String DB_SUBDIR = "badger_db";
    public final static  String BABBLE_TOML = "babble.toml";
    public final static  String PEERS_JSON = "peers.json";
    public final static  String PEERS_GENESIS_JSON = "peers.genesis.json";
    public final static  String PRIV_KEY = "priv_key";

    private String rootDir;
    private String tomlDir = "";
    private ArrayList<String> directories;

    /**
     * Create an object to manage multiple Babble Configs
     * @param storageDir the root of the babble storage. Likely to be context.getFilesDir() or context.getExternalFilesDir().
     */
    public BabbleConfigDir(String storageDir) {
        this.rootDir = storageDir;


//        Log.i("BabbleConfigDir", "ConfigDir: "+storageDir);

        File babbleDir = new File(this.rootDir, BABBLE_ROOTDIR);
        this.directories = new ArrayList<String>();
        if(babbleDir.exists()) {
            // Popualate directories with the subfolders that are configured
            Collections.addAll(this.directories,
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
        return this.directories.contains(subConfigDir);
    }


    boolean DeleteDirectory(String subConfigDir) {
        if ( ! this.CheckDirectory(subConfigDir)) { // Doesn't exist
            return false;
        }

        File dir = new File(this.rootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir);

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
         * @param babbleConfig is the babble configuration object
         * @param subConfigDir is the subfolder of the babble Subfolder of the local storage as passed to the constructor
         * @return the composite path where the babble.toml file was written
         */
    String WriteBabbleTomlFiles(BabbleConfig babbleConfig, String subConfigDir, String inetAddress, int port, String moniker) {



        TomlWriter tomlWriter = new TomlWriter();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> babble = new HashMap<>();

        tomlDir = this.rootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir;
        File babbleDir = new File(tomlDir, DB_SUBDIR);
        if(! babbleDir.exists()) {
            // Log.i("BabbleConfigDir", "Creating "+DB_SUBDIR);
            babbleDir.mkdirs();
        }

        babble.put("datadir",tomlDir) ;
        babble.put("db",  tomlDir+File.separator+DB_SUBDIR) ;

        babble.put("log", babbleConfig.logLevel);
        babble.put("listen", inetAddress+":"+port);
        babble.put("advertise", inetAddress+":"+port);
        babble.put("no-service", babbleConfig.noService);

        if (! babbleConfig.serviceListen.equals("")) {  // Only set if set
            babble.put("service-listen", babbleConfig.serviceListen);
        }
        babble.put("heartbeat", babbleConfig.heartbeat+"ms");
        babble.put("slow-heartbeat", babbleConfig.slowHeartbeat+"ms");
        babble.put("max-pool", babbleConfig.maxPool);
        babble.put("timeout", babbleConfig.tcpTimeout);
        babble.put("join_timeout", babbleConfig.joinTimeout);
        babble.put("sync-limit", babbleConfig.syncLimit);
        babble.put("fast-sync", babbleConfig.enableFastSync);
        babble.put("store", babbleConfig.store);
        babble.put("cache-size", babbleConfig.cacheSize);
        babble.put("bootstrap", babbleConfig.bootstrap);
        babble.put("maintenance-mode", babbleConfig.maintenanceMode);
        babble.put("suspend-limit", babbleConfig.suspendLimit);
        babble.put("moniker", moniker);
        babble.put("loadpeers", babbleConfig.loadPeers);

        map.put("Babble", babble);

        try {
            tomlWriter.write(babble, new File(tomlDir, BABBLE_TOML));
        } catch (Exception e) {
            //TODO catch this
            // Log.e(" WriteBabbleTomlFiles", e.toString());
        }


        if (! this.CheckDirectory(subConfigDir)) {
            this.directories.add(subConfigDir);
        }
        return tomlDir;

    }







}
