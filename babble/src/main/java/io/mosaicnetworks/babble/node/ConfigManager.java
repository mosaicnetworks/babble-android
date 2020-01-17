package io.mosaicnetworks.babble.node;


 import android.util.Log;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    // This constant determines how long the unique ID as part of a
    private final static int UniqueIdLength = 16;


    private String mRootDir;
    private String mTomlDir = "";
    private final String mAppId;
    private final BabbleNode.ConfigFolderBackupPolicy mConfigFolderBackupPolicy;


    private ArrayList<ConfigFolder> mDirectories;

    /**
     * Create an object to manage multiple Babble Configs
     * @param storageDir the root of the babble storage. Likely to be context.getFilesDir() or context.getExternalFilesDir().
     */
    public ConfigManager(String storageDir, String appID, BabbleNode.ConfigFolderBackupPolicy configFolderBackupPolicy) {
        mRootDir = storageDir;
        mAppId = appID;
        mConfigFolderBackupPolicy = configFolderBackupPolicy;

//        Log.i("ConfigManager", "ConfigDir: "+storageDir);

        File babbleDir = new File(this.mRootDir, BABBLE_ROOTDIR);
        mDirectories = new ArrayList<ConfigFolder>();

        if(babbleDir.exists()) {
            PopulateDirectories(babbleDir);
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
        for (ConfigFolder f : mDirectories) {
            if (f.FolderName.equals(subConfigDir)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets a list of the configuration folders available to this app
     * @return An ArrayList<String> of the folder names with no path
     */
    public ArrayList<ConfigFolder> getDirectories() {
        return mDirectories;
    }


    /**
     * Function to generate a string to render the config name unique.
     * @return a hex string
     */
    public String GetUniqueId() {
        UUID uuid = UUID.randomUUID();
      //   return uuid.toString().replaceAll("-", "");
        return uuid.toString().replaceAll("[^A]", "A");
    }


    /**
     * The folder name is composite with the parts separated by underscores.
     * The first part is the appId as set in BabbleService, which may or may not be the FQDN
     * The 2nd part is a unique id as generated in GetUniqueId. Currently this is 16 characters
     * The 3rd part is a narrative description field with spaces converted to minus signs and
     * input limit to spaces, letters and numbers only.
     * @param networkDescription contains a human readable description for this network.
     * @return a unique folder name
     */
    public String GetCompositeConfigDir(String networkDescription) {

        String unique = GetUniqueId();
        String trimmedUnique = unique.length() >= UniqueIdLength
                ? unique.substring(unique.length() - UniqueIdLength)
                : unique ;



        String compositeDir = mAppId + "_" + trimmedUnique + "_"+ConfigFolder.EncodeDescription(networkDescription)+"_";
        return compositeDir;
    }



    boolean DeleteDirectory(String subConfigDir) {
        Log.d("DeleteDirectory", subConfigDir);

        if ( ! this.CheckDirectory(subConfigDir)) { // Doesn't exist
            Log.e("DeleteDirectory !Exists", subConfigDir);
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

            Log.i("WritePeersJsonFiles", "JSON "+gson.toJson(currentPeers));

            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_JSON));
            gson.toJson(currentPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            Log.e("WritePeersJsonFiles", e.toString());
        }

        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_GENESIS_JSON));
            gson.toJson(genesisPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            Log.e("WritePeersJsonFiles", e.toString());
        }
    }

        /**
         * Write Babble Config to disk ready for Babble to use
         * @param nodeConfig is the babble configuration object
         * @param subConfigDir is the subfolder of the babble Subfolder of the local storage as passed to the constructor
         * @return the composite path where the babble.toml file was written
         */
    String WriteBabbleTomlFiles(NodeConfig nodeConfig, String subConfigDir, String inetAddress, int port, String moniker) throws CannotStartBabbleNodeException{

        TomlWriter tomlWriter = new TomlWriter();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> babble = new HashMap<>();


        String compositeName = GetCompositeConfigDir(subConfigDir);

        mTomlDir = this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + compositeName;
        File babbleDir = new File(mTomlDir, DB_SUBDIR);
        if (babbleDir.exists()){
            // We have a clash.
            switch (mConfigFolderBackupPolicy) {
                case ABORT:
                    throw new CannotStartBabbleNodeException("Config Folder already exists and we have ABORT policy");

                case COMPLETE_BACKUP:
                case SINGLE_BACKUP:
                    // Rename
                    BackupOldConfigs(compositeName);
                    babbleDir.mkdirs();
                    break;

                case DELETE:
                    DeleteDirectory(subConfigDir);
                    break;
            }

        } else{
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
            AddConfigFolderToList(subConfigDir);
        }
        return mTomlDir;

    }



    private void AddConfigFolderToList(String folderName ) {
        try
        {
            ConfigFolder configFolder = new ConfigFolder(folderName);
            mDirectories.add(configFolder);
        }
                        catch (Exception e)
        {
            // Do nothing, but swallow e
        }
    }


    private void RenameConfigFolder(String oldSubConfigDir, int newSuffix) throws CannotStartBabbleNodeException{
        File oldFile = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir);
        File newFile = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir+Integer.toString(newSuffix));


        Log.d("Rename ", oldFile.getAbsolutePath());
        Log.d("Rename ", newFile.getAbsolutePath());


        if (!(oldFile.renameTo(newFile))) {
            Log.d("Rename ","Fails");
            throw new CannotStartBabbleNodeException("Cannot backup the old configuration folder");
        }




    }


    private void BackupOldConfigs(String compositeName) throws CannotStartBabbleNodeException
    {
        ArrayList<String> suffix = new ArrayList<String>();

        String newest = "";
        int newestInt = 0;


        if (mConfigFolderBackupPolicy == BabbleNode.ConfigFolderBackupPolicy.SINGLE_BACKUP) {

            Log.d("BackupOldConfigs SINGLE", compositeName);
            for (ConfigFolder f : mDirectories) {
                if ((f.IsBackup) && (f.FolderName.startsWith(compositeName))) {
                    DeleteDirectory(f.FolderName);
                }
            }

            RenameConfigFolder(compositeName,1);

        } else  // MULTIPLE_BACKUP
        {
            Log.d("BackupOldConfigs MULT", compositeName);
            for (ConfigFolder f : mDirectories) {
                if ((f.IsBackup) && (f.FolderName.startsWith(compositeName))) {
                    if (newestInt < f.BackUpVersion) {
                        newestInt = f.BackUpVersion;
                        newest = f.FolderName;
                    }
                }
            }

            RenameConfigFolder(compositeName,newestInt + 1);

        }

        Log.d("BackupOldConfigs POP", compositeName);
        PopulateDirectories(new File(this.mRootDir, BABBLE_ROOTDIR));

    }



    void PopulateDirectories(File babbleDir)
    {
        ArrayList<String> directories = new ArrayList<String>();
        mDirectories = new ArrayList<ConfigFolder>();

        Collections.addAll(directories,
                babbleDir.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                }));

        // Popualate mDirectories with the results
        for (String s : directories) {
            AddConfigFolderToList(s);
        }
    }



}
