package io.mosaicnetworks.babble.node;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;
import com.moandjiezana.toml.Toml;



import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import io.mosaicnetworks.babble.discovery.Peer;

public final class ConfigManager {

    public enum ConfigDirectoryBackupPolicy  {DELETE, SINGLE_BACKUP, COMPLETE_BACKUP, ABORT}

    /**
     * The default babbling port. This can be overridden when configuring the service
     */
    public static final int DEFAULT_BABBLING_PORT = 6666;
    public final static String BABBLE_ROOTDIR = "babble";
    public final static String DB_SUBDIR = "badger_db";
    public final static String BABBLE_TOML = "babble.toml";
    public final static String PEERS_JSON = "peers.json";
    public final static String PEERS_GENESIS_JSON = "peers.genesis.json";
    public final static String PRIV_KEY = "priv_key";

    // This constant determines the length of the unique ID
    private final static int sUniqueIdLength = 12;

    private static ConfigManager INSTANCE;
    private String mRootDir;
    private String mTomlDir = "";

    public String getMoniker() {
        return mMoniker;
    }

    private String mMoniker = "";
    private final String mAppId;
    private ConfigDirectoryBackupPolicy mConfigDirectoryBackupPolicy = ConfigDirectoryBackupPolicy.SINGLE_BACKUP; //TODO: requires getter and setter
    private ArrayList<ConfigDirectory> mDirectories = new ArrayList<>();
    private KeyPair mKeyPair;

    public static ConfigManager getInstance(Context context) {
        if (INSTANCE==null) {
            INSTANCE = new ConfigManager(context.getApplicationContext());
        }

        return INSTANCE;
    }

    /**
     * Create an object to manage multiple Babble Configs
     * @param appContext the application context
     */
    private ConfigManager(Context appContext) {
        mRootDir = appContext.getFilesDir().toString();
        mAppId = appContext.getPackageName();
        mKeyPair = new KeyPair(); //TODO:  how should the key be handled??

        File babbleDir = new File(this.mRootDir, BABBLE_ROOTDIR);

        if (babbleDir.exists()) {
            populateDirectories(babbleDir);
        } else { // First run, so we create the root dir - clearly no subdirs yet
            babbleDir.mkdirs();
        }
    }

    //TODO: is this the best way to get the root directory?
    public String getRootDir() {
        return mRootDir;
    }

    //#######################################################
    // NOTE: hacked out of the babble service

    /**
     * Configure the service to create a new group using the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String configureNew(String groupName, String moniker, String inetAddress)  throws CannotStartBabbleNodeException, IOException {
        return configureNew(groupName, moniker, inetAddress, DEFAULT_BABBLING_PORT);
    }

    /**
     * Configure the service to create a new group, overriding the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the HttpPeerDiscoveryServer //TODO: how to deal with this
     * @throws IllegalStateException if the service is currently running
     */
    public String configureNew(String groupName, String moniker, String inetAddress, int babblingPort) throws CannotStartBabbleNodeException, IOException{
        List<Peer> genesisPeers = new ArrayList<>();
        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));

        return configure(genesisPeers, currentPeers, groupName, moniker, inetAddress, babblingPort, false);
    }

    /**
     * Configure the service to create an archive group
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String configureArchive(ConfigDirectory configDirectory,  String inetAddress)  throws  IOException {
        return configureArchive(configDirectory, inetAddress, DEFAULT_BABBLING_PORT);
    }

    /**
     *Configure the service to create an archive group, overriding the default ports
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * @throws IllegalStateException if the service is currently running
     */
    public String configureArchive(ConfigDirectory configDirectory, String inetAddress, int babblingPort) throws  IOException{

        Log.i("configureArchive", configDirectory.directoryName);

        setTomlDir(configDirectory.directoryName);

        Log.i("configArchive:tomlDir", mTomlDir);
        Log.i("configArchive:IP", inetAddress);


        Map<String, Object> configChanges = new HashMap<>();
        configChanges.put("maintenance-mode", true);
        configChanges.put("listen", inetAddress + ":" + Integer.toString(babblingPort));
        configChanges.put("advertise", inetAddress + ":" + Integer.toString(babblingPort));

        AmendTomlSettings(configChanges);


        return mTomlDir;

//        List<Peer> genesisPeers = new ArrayList<>();
//        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
//        List<Peer> currentPeers = new ArrayList<>();
//        currentPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));

//        return configure(genesisPeers, currentPeers, "TODO-GROUP-NAME", moniker, inetAddress, babblingPort, true); //TODO: group name
    }

    /**
     * Configure the service to join an existing group using the default ports
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String groupName, String moniker, String inetAddress) throws CannotStartBabbleNodeException, IOException {
        return configure(genesisPeers, currentPeers, groupName, moniker, inetAddress, DEFAULT_BABBLING_PORT, false);
    }

    /**
     *
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the {HttpPeerDiscoveryServer} //TODO: deal with discovery
     * @throws IllegalStateException if the service is currently running
     */
    public String configureJoin(List<Peer> genesisPeers, List<Peer> currentPeers, String groupName, String moniker, String inetAddress, int babblingPort) throws CannotStartBabbleNodeException, IOException{
        return configure(genesisPeers, currentPeers, groupName, moniker, inetAddress, babblingPort, false); //TODO: group name
    }

    private String configure(List<Peer> genesisPeers, List<Peer> currentPeers, String groupName,
                             String moniker, String inetAddress, int babblingPort,
                             boolean isArchive) throws CannotStartBabbleNodeException, IOException {

        NodeConfig nodeConfig = new NodeConfig.Builder().build();
        mMoniker = moniker;
        //TODO: is there a cleaner way of obtaining the path?
        // It is stored in mTomlDir which has getTomlDir and setTomlDir getter and setters
        String fullPath = writeBabbleTomlFiles(nodeConfig, groupName, inetAddress, babblingPort, moniker);
        Log.d("ConfigManager.configure", "Full Path:" + fullPath);




        writePeersJsonFiles(fullPath, genesisPeers, currentPeers);

        // private key -- does not overwrite
        writePrivateKey(fullPath, mKeyPair.privateKey);

        return fullPath;

        /*
        if (mState == BabbleService.State.RUNNING || mState == BabbleService.State.RUNNING_WITH_DISCOVERY) {
            throw new IllegalStateException("Cannot configure while the service is running");
        }
         */

        /*
        mBabbleNode = BabbleNode.createWithConfig(genesisPeers, currentPeers,
                mKeyPair.privateKey, inetAddress,
                babblingPort, moniker,
                new BlockConsumer() {
                    @Override
                    public Block onReceiveBlock(Block block) {
                        Block processedBlock = mBabbleState.processBlock(block);
                        notifyObservers();
                        return processedBlock;
                    }
                },
                mNodeConfig, mConfigDir, mSubConfigDir, mConfigFolderBackupPolicy, mAppId);

        mBabbleState.reset();
        mState = State.CONFIGURED;
         */
    }

    //#######################################################



    /**
     * Write private key
     * @param targetDir is the directory that the private key is to be written to.
     * @param privateKeyHex private key as produced by the {@link KeyPair} class
     */
    public void writePrivateKey(String targetDir, String privateKeyHex) {
        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, PRIV_KEY) );
            fileWriter.write(privateKeyHex);
            fileWriter.close();
        } catch (Exception e) {
            // Log.e("writePrivateKey", e.toString());
        }
    }

    /**
     * Write Both Peers JSON files
     * @param targetDir is the directory that the peers files are to be written to.
     * @param genesisPeers is the initial peer list for this network
     * @param currentPeers is the current peer list for this network
     */
    public void writePeersJsonFiles(String targetDir, List<Peer> genesisPeers, List<Peer> currentPeers) {
        Gson gson = new Gson();
        try {

            Log.i("writePeersJsonFiles", "JSON " + gson.toJson(currentPeers));

            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_JSON));
            gson.toJson(currentPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            Log.e("writePeersJsonFiles", e.toString());
        }

        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, PEERS_GENESIS_JSON));
            gson.toJson(genesisPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            Log.e("writePeersJsonFiles", e.toString());
        }
    }


    /**
     * Getter to get  the tomlDir to the full path to the folder containing the babble toml
     * @return the full file path
     */
    public String getTomlDir() {
        return mTomlDir;
    }

    /**
     * Setter to set the tomlDir to the full path to the folder containing the babble toml
      * @param compositeName the directory name for the config folder. NB this must be the composite version, not the human readable one.
     */
    public void setTomlDir(String compositeName) {
        mTomlDir = this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + compositeName;
        this.mTomlDir = mTomlDir;
    }



    /**
     * Write Babble Config to disk ready for Babble to use
     * @param nodeConfig is the babble configuration object
     * @param subConfigDir is the sub-directory of the babble sub-directory of the local storage as passed to the constructor
     * @return the composite path where the babble.toml file was written
     */
    public String writeBabbleTomlFiles(NodeConfig nodeConfig, String subConfigDir, String inetAddress, int port, String moniker) throws CannotStartBabbleNodeException, IOException {

        //TODO: add inetAddress, port and moniker to nodeConfig??
        Map<String, Object> babble = new HashMap<>();

        String compositeName = getCompositeConfigDir(subConfigDir);
        setTomlDir(compositeName);

        File babbleDir = new File(mTomlDir, DB_SUBDIR);
        if (babbleDir.exists()){
            // We have a clash.
            switch (mConfigDirectoryBackupPolicy) {
                case ABORT:
                    throw new CannotStartBabbleNodeException("Config directory already exists and we have ABORT policy");
                case COMPLETE_BACKUP:
                case SINGLE_BACKUP:
                    // Rename
                    backupOldConfigs(compositeName);
                    babbleDir.mkdirs();
                    break;
                case DELETE:
                    deleteDirectory(subConfigDir);
                    break;
            }
        } else {
            babbleDir.mkdirs();
        }

        babble.put("datadir", mTomlDir) ;
        babble.put("db",  mTomlDir + File.separator + DB_SUBDIR) ;

        babble.put("log", nodeConfig.logLevel);
        babble.put("listen", inetAddress + ":" + port);
        babble.put("advertise", inetAddress + ":" + port);
        babble.put("no-service", nodeConfig.noService);

        if (!nodeConfig.serviceListen.equals("")) {  // Only set if set
            babble.put("service-listen", nodeConfig.serviceListen);
        }
        babble.put("heartbeat", nodeConfig.heartbeat + "ms");
        babble.put("slow-heartbeat", nodeConfig.slowHeartbeat + "ms");
        babble.put("max-pool", nodeConfig.maxPool);
        babble.put("timeout", nodeConfig.tcpTimeout + "ms");
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

        WriteTomlFile(babble);

        Log.i("WriteTomlFile", "Wrote toml file successfully");


        if (!isExistingConfigDirectory(compositeName)) {
            addConfigDirectoryToList(compositeName);
        }
        return mTomlDir;
    }


    /**
     * Loads the Babble Config TOML file. This function relies on mTomlDir being set.
     * @return A HashMap object containing the data from the Toml File.
     */

    protected Map<String, Object> ReadTomlFile(){
        File tomlFile =  new File(mTomlDir, BABBLE_TOML);

        Toml toml = new Toml().read(tomlFile);

        Map<String, Object> configMap = toml.toMap();


        Log.i("ReadTomlFile", "Read toml file successfully");

        return configMap;
    }

    /**
     * Writes the Babble Config TOML file. This function relies on mTomlDir being set.
     * @configHashMap A HashMap object containing the config data to be written the Toml File.
     */
    protected void  WriteTomlFile(Map<String, Object> configHashMap) throws IOException {
        Log.i("WriteTomlFile", mTomlDir);

        try {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(configHashMap, new File(mTomlDir, BABBLE_TOML));

            Log.i("WriteTomlFile", "Wrote toml file");
        } catch (IOException e) {
            // Log and rethrow
            Log.e("WriteTomlFile", e.toString());
            throw e;
        }

    }


    /**
     * Amends the Babble Config TOML file. This function relies on mTomlDir being set.
     * @configHashMapChanges A HashMap object containing the changed config data to be written the Toml File.
     */
    public void AmendTomlSettings(Map<String, Object> configHashMapChanges) throws IOException  {
        boolean hasChanged = false;

        Map<String, Object> configMap = ReadTomlFile();

        for (Map.Entry<String,Object> entry : configHashMapChanges.entrySet()) {

            Log.i("AmendTomlSettings", entry.getKey() + " = " + entry.getValue().toString()) ;

            if (
                ( configMap.containsKey(entry.getKey())) &&
                        (configMap.get(entry.getKey()).equals(entry.getValue()))
            ) { continue ; }    // If key exists and value matches, there is nothing to do
            hasChanged = true;

            Log.i("AmendTomlSettings:SET", entry.getKey() + " = " + entry.getValue().toString());
            configMap.put(entry.getKey(), entry.getValue());
        }

        if (hasChanged) {
            try {
                WriteTomlFile(configMap);
                Log.i("AmendTomlSettings", configMap.toString());

             } catch (IOException e)
            {
                // Rethrow
                throw e;
            }
        } else {
            Log.i("AmendTomlSettings", "No changes, no write");
        }


        if (configMap.containsKey("moniker"))
        {
            mMoniker = configMap.get("moniker").toString();
            Log.i("configArchive:moniker", mMoniker);
        }


    }





    /**
     * Gets a list of the configuration directories available to this app
     * @return An ArrayList<String> of the directory names with no path
     */
    public ArrayList<ConfigDirectory> getDirectories() {
        return mDirectories;
    }

    /**
     * Function to generate a string to render the config name unique.
     * @return a hex string
     */
    public String getUniqueId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
      //   return uuid.toString().replaceAll("[^A]", "A");
    }

    /**
     * Check if this configuration already exists
     * @param subConfigDir is a subdirectory under the babble root
     * @return returns true if it already exists
     */
    private boolean isExistingConfigDirectory(String subConfigDir) {
        for (ConfigDirectory d : mDirectories) {
            if (d.directoryName.equals(subConfigDir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The directory name is composite with the parts separated by underscores.
     * The first part is the appId as set in BabbleService, which may or may not be the FQDN
     * The 2nd part is a unique id as generated in getUniqueId. Currently this is 16 characters
     * The 3rd part is a narrative description field with spaces converted to minus signs and
     * input limit to spaces, letters and numbers only.
     * @param networkDescription contains a human readable description for this network.
     * @return a unique directory name
     */
    private String getCompositeConfigDir(String networkDescription) {

        String unique = getUniqueId();
        String trimmedUnique = unique.length() >= sUniqueIdLength
                ? unique.substring(unique.length() - sUniqueIdLength)
                : unique;
        
        String compositeDir = mAppId + "_" + trimmedUnique + "_" + ConfigDirectory.EncodeDescription(networkDescription) + "_";
        return compositeDir;
    }

    private boolean deleteDirectory(String subConfigDir) {
        Log.d("deleteDirectory", subConfigDir);

        if ( !isExistingConfigDirectory(subConfigDir)) { // Doesn't exist
            Log.e("deleteDirectory !Exists", subConfigDir);
            return false;
        }

        File dir = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir);

        return deleteDir(dir);
    }

    // delete directory and contents
    private boolean deleteDir(File file) {
        try {
            if (file.isDirectory())
                for (String child : file.list())
                    deleteDir(new File(file, child));
            file.delete();  // delete child file or empty directory
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void addConfigDirectoryToList(String directoryName ) {
        try {
            ConfigDirectory configDirectory = new ConfigDirectory(directoryName);
            mDirectories.add(configDirectory);
        } catch (Exception e) {
            // Do nothing, but swallow e
        }
    }
    
    private void renameConfigDirectory(String oldSubConfigDir, int newSuffix) throws CannotStartBabbleNodeException {
        File oldFile = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir);
        File newFile = new File(this.mRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir + Integer.toString(newSuffix));
        
        Log.d("Rename ", oldFile.getAbsolutePath());
        Log.d("Rename ", newFile.getAbsolutePath());
        
        if (!(oldFile.renameTo(newFile))) {
            Log.d("Rename ","Fails");
            throw new CannotStartBabbleNodeException("Cannot backup the old configuration directory");
        }
    }

    private void backupOldConfigs(String compositeName) throws CannotStartBabbleNodeException {
        
        if (mConfigDirectoryBackupPolicy == ConfigDirectoryBackupPolicy.SINGLE_BACKUP) {

            Log.d("backupOldConfigs SINGLE", compositeName);
            for (ConfigDirectory d : mDirectories) {
                if ((d.isBackup) && (d.directoryName.startsWith(compositeName))) {
                    deleteDirectory(d.directoryName);
                }
            }

            renameConfigDirectory(compositeName,1);

        } else { 
            // MULTIPLE_BACKUP
            int newestInt = 0;
            Log.d("backupOldConfigs MULT", compositeName);
            for (ConfigDirectory d : mDirectories) {
                if ((d.isBackup) && (d.directoryName.startsWith(compositeName))) {
                    if (newestInt < d.BackUpVersion) {
                        newestInt = d.BackUpVersion;
                    }
                }
            }

            renameConfigDirectory(compositeName,newestInt + 1);
        }

        Log.d("backupOldConfigs POP", compositeName);
        populateDirectories(new File(this.mRootDir, BABBLE_ROOTDIR));
    }


    /**
     * Function to delete all config folders for a Group. Call the static function
     * ConfigDirectory.rootDirectoryName() on compositeName before passing it to this function to
     * ensure that the name is well-formed.
     * @param compositeName the full underscore separated name of the group
     * @param onlyDeleteBackups if set to true the "live" copy is delete too
     */

    public void DeleteDirectoryAndBackups(String compositeName, boolean onlyDeleteBackups) {

        // Call ConfigDirectory.rootDirectoryName on composite name to debackup the name


        for (ConfigDirectory d : mDirectories) {
            if ((d.isBackup || (!onlyDeleteBackups)) && (d.directoryName.startsWith(compositeName))) {
                deleteDirectory(d.directoryName);
            }
        }

        // Rebuild directory list after pruning backups
        populateDirectories(new File(this.mRootDir, BABBLE_ROOTDIR));
    }




    private void populateDirectories(File babbleDir) {
        ArrayList<String> directories = new ArrayList<>();

        mDirectories = new ArrayList<>();

        Collections.addAll(directories,
                babbleDir.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                }));

        // Popualate mDirectories with the results
        for (String s : directories) {
            addConfigDirectoryToList(s);
        }
    }
}
