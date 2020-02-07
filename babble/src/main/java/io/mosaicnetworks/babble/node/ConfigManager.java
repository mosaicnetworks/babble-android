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

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;
import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import io.mosaicnetworks.babble.discovery.Peer;

/**
 * ConfigManager is a singleton class that manages the babble configuration files used by babble-go
 */
public final class ConfigManager {

    /**
     * An enumerated data type that sets the Babble Configuration Backup Policy. When writing
     * the configuration files, if the configuration already exists, the policy defines what
     * actions the ConfigManager takes:
     * DELETE - just deletes any pre-existing configuration for this Group
     * SINGLE_BACKUP - retains just one archive copy at any time
     * COMPLETE_BACKUP - saves all archive copies
     * ABORT - If there is any pre-existing archive for this groups, throw an exception and abort
     */
    public enum ConfigDirectoryBackupPolicy  {DELETE, SINGLE_BACKUP, COMPLETE_BACKUP, ABORT}

    /**
     * The default babbling port. This can be overridden when configuring the service
     */
    public static final int DEFAULT_BABBLING_PORT = 6666;

    /**
     * The subfolder of the file store for the app that contains all of the babble-go configuration
     * files and badger_db databases
     */
    public final static String BABBLE_ROOTDIR = "babble";

    /**
     * The subfolder within a configuration folder that contains the badger_db database
     */
    public final static String DB_SUBDIR = "badger_db";

    /**
     * The name of the configuration file for babble. It will be in the root of the babble
     * configuration folder
     */
    public final static String BABBLE_TOML = "babble.toml";

    /**
     * The name of the peers file for babble. It will be in the root of the babble
     * configuration folder
     */
    public final static String PEERS_JSON = "peers.json";

    /**
     * The name of the initial peers file for babble. It will be in the root of the babble
     * configuration folder
     */
    public final static String PEERS_GENESIS_JSON = "peers.genesis.json";

    /**
     * The name of the file containing the babble private key. It will be in the root of the babble
     * configuration folder
     */
    public final static String PRIV_KEY = "priv_key";

// These variables are static to allow them to be set in the initialisation of any app.
// As ConfigManager is invoked from within BabbleService, it would otherwise not be possible to
// amend these app wide values without passing them into BabbleService.
    private static int sUniqueIdLength = 12;
    private static ConfigManager INSTANCE;
    private static String sRootDir = "";

    private String mTomlDir = "";
    private String mMoniker = "";
    private final String mAppId;
    private static ConfigDirectoryBackupPolicy sConfigDirectoryBackupPolicy = ConfigDirectoryBackupPolicy.SINGLE_BACKUP;
    private ArrayList<ConfigDirectory> mDirectories = new ArrayList<>();
    private KeyPair mKeyPair;


    /**
     * Provides an instance of the static ConfigManager class, reusing one if available, calling the
     * constructor if not.
     * @param context Context. Used to call getFilesDir to find the path to the babble config dirs
     * @return an instance of ConfigManager
     */
    public static ConfigManager getInstance(Context context) throws FileNotFoundException {
        if (INSTANCE==null) {
            Log.v("ConfigManager", "getInstance");
            INSTANCE = new ConfigManager(context.getApplicationContext());
        }

        return INSTANCE;
    }

    /**
     * Create an object to manage multiple Babble Configs
     * @param appContext the application context
     * @throws FileNotFoundException when the babble root dir cannot be created
     */
    private ConfigManager(Context appContext) throws FileNotFoundException{

        Log.v("ConfigManager", "constructor");

        if (sRootDir.equals("")) {
            sRootDir = appContext.getFilesDir().toString();
        }

        Log.v("ConfigManager", "got FilesDir");

        mAppId = appContext.getPackageName();

        Log.v("ConfigManager", "got Package Name");

        mKeyPair = new KeyPair(); //TODO:  how should the key be handled??

        Log.v("ConfigManager", "got Key Pair");

        File babbleDir = new File(sRootDir, BABBLE_ROOTDIR);

        if (babbleDir.exists()) {
            populateDirectories(babbleDir);
        } else { // First run, so we create the root dir - clearly no sub dirs yet
            if ( ( ! babbleDir.mkdirs() ) && (! babbleDir.exists())) {
                throw new FileNotFoundException();
            }
        }
    }


    /**
     * ConfigDirectoryBackupPolicy getter takes an enumerated type ConfigDirectoryBackupPolicy
     * which can take the following types:
     * DELETE - just deletes any pre-existing configuration for this Group
     * SINGLE_BACKUP - retains just one archive copy at any time
     * COMPLETE_BACKUP - saves all archive copies
     * ABORT - If there is any pre-existing archive for this groups, throw an exception and abort
     * @return ConfigDirectoryBackupPolicy
     */
    public static ConfigDirectoryBackupPolicy getConfigDirectoryBackupPolicy() {
        return sConfigDirectoryBackupPolicy;
    }

    /**
     * ConfigDirectoryBackupPolicy setter takes an enumerated type ConfigDirectoryBackupPolicy
     * which can take the following types:
     * DELETE - just deletes any pre-existing configuration for this Group
     * SINGLE_BACKUP - retains just one archive copy at any time
     * COMPLETE_BACKUP - saves all archive copies
     * ABORT - If there is any pre-existing archive for this groups, throw an exception and abort
     * @param mConfigDirectoryBackupPolicy the policy to use
     */
    public static void setConfigDirectoryBackupPolicy(ConfigDirectoryBackupPolicy mConfigDirectoryBackupPolicy) {
        ConfigManager.sConfigDirectoryBackupPolicy = mConfigDirectoryBackupPolicy;
    }


    /**
     * Getter method for Unique ID Length
     *
     * Each configuration has a unique identifier that is formed by concatenating an appId with
     * a unique ID and the group description using underscores. The Unique ID length controls how
     * long the unique hex string is. The default length is 12.
     * @return the unique ID length
     */
    public static int getUniqueIdLength() {
        return sUniqueIdLength;
    }


    /**
     * Setter method for Unique ID Length
     *
     * Each configuration has a unique identifier that is formed by concatenating an appId with
     * a unique ID and the group description using underscores. The Unique ID length controls how
     * long the unique hex string is. The default length is 12.
     * @param sUniqueIdLength the unique ID length
     */
    public static void setUniqueIdLength(int sUniqueIdLength) {
        ConfigManager.sUniqueIdLength = sUniqueIdLength;
    }

    /**
     * The moniker is a human readable name for a babble node. The moniker is not guaranteed to
     * be unique and can be trivially spoofed
     * @return the Moniker
     */
    public String getMoniker() {
        return mMoniker;
    }


    //TODO: is this the best way to get the root directory?

    /**
     * Returns the root directory of the App-specific storage. It defaults to
     * appContext.getFilesDir().
     * @return the Root Dir
     */
    public static String getRootDir() {
        return sRootDir;
    }

    /**
     * Setter for root directory of the App-specific storage. It defaults to
     * appContext.getFilesDir(). This needs to be set before the first call to ConfigManager.getInstance()
     * @param sRootDir the root directory to create the babble config directories hierarchy
     */
    public static void setRootDir(String sRootDir) {
        ConfigManager.sRootDir = sRootDir;
    }




    /**b
     * Configure the service to create a new group using the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor, String moniker, String inetAddress)  throws CannotStartBabbleNodeException, IOException {
        return createConfigNewGroup(groupDescriptor, moniker, inetAddress, DEFAULT_BABBLING_PORT);
    }

    /**
     * Configure the service to create a new group, overriding the default ports
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the HttpPeerDiscoveryServer //TODO: how to deal with this
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor, String moniker, String inetAddress, int babblingPort) throws CannotStartBabbleNodeException, IOException{
        List<Peer> genesisPeers = new ArrayList<>();
        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));

        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, inetAddress, babblingPort);
    }

    /**
     * Configure the service to create an archive group
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress)  throws  IOException {
        return setGroupToArchive(configDirectory, inetAddress, DEFAULT_BABBLING_PORT);
    }

    /**
     *Configure the service to create an archive group, overriding the default ports
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * @throws IllegalStateException if the service is currently running
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress, int babblingPort) throws  IOException{

        Log.i("setGroupToArchive", configDirectory.directoryName);

        setTomlDir(configDirectory.directoryName);

        Log.i("configArchive:tomlDir", mTomlDir);
        Log.i("configArchive:IP", inetAddress);


        Map<String, Object> configChanges = new HashMap<>();
        configChanges.put("maintenance-mode", true);
        configChanges.put("listen", inetAddress + ":" + babblingPort);
        configChanges.put("advertise", inetAddress + ":" + babblingPort);


        //TODO: possibly move these amendments into the backup config processing to avoid having to
        //      set them here
        configChanges.put("datadir", mTomlDir);
        configChanges.put("db",  mTomlDir + File.separator+ DB_SUBDIR);


        amendTomlSettings(configChanges);

        return mTomlDir;

//        List<Peer> genesisPeers = new ArrayList<>();
//        genesisPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));
//        List<Peer> currentPeers = new ArrayList<>();
//        currentPeers.add(new Peer(mKeyPair.publicKey, inetAddress + ":" + babblingPort, moniker));

//        return createConfig(genesisPeers, currentPeers, "TODO-GROUP-NAME", moniker, inetAddress, babblingPort, true); //TODO: group name
    }

    /**
     * Configure the service to join an existing group using the default ports
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor, String moniker, String inetAddress) throws CannotStartBabbleNodeException, IOException {
        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, inetAddress, DEFAULT_BABBLING_PORT);
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
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor, String moniker, String inetAddress, int babblingPort) throws CannotStartBabbleNodeException, IOException{
        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, inetAddress, babblingPort); //TODO: group name
    }

    private String createConfig(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor,
                                String moniker, String inetAddress, int babblingPort) throws CannotStartBabbleNodeException, IOException {

        String compositeGroupName = getCompositeConfigDir(groupDescriptor);

        NodeConfig nodeConfig = new NodeConfig.Builder().build();
        mMoniker = moniker;
        //TODO: is there a cleaner way of obtaining the path?
        // It is stored in mTomlDir which has getTomlDir and setTomlDir getter and setters
        String fullPath = writeBabbleTomlFiles(nodeConfig, compositeGroupName, inetAddress, babblingPort, moniker);
        Log.v("Config.createConfig", "Full Path:" + fullPath);

        writePeersJsonFiles(fullPath, genesisPeers, currentPeers);

        // private key -- does not overwrite
        writePrivateKey(fullPath, mKeyPair.privateKey);

        return fullPath;
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
        mTomlDir = sRootDir + File.separator + BABBLE_ROOTDIR + File.separator + compositeName;
        this.mTomlDir = mTomlDir;
    }



    /**
     * Write Babble Config to disk ready for Babble to use
     * @param nodeConfig is the babble configuration object
     * @param compositeGroupName is the sub-directory of the babble sub-directory of the local storage as passed to the constructor
     * @return the composite path where the babble.toml file was written
     */
    public String writeBabbleTomlFiles(NodeConfig nodeConfig, String compositeGroupName, String inetAddress, int port, String moniker) throws CannotStartBabbleNodeException, IOException {

        //TODO: add inetAddress, port and moniker to nodeConfig??
        Map<String, Object> babble = new HashMap<>();


        setTomlDir(compositeGroupName);

        File babbleDir = new File(mTomlDir, DB_SUBDIR);
        if (babbleDir.exists()){
            // We have a clash.
            switch (sConfigDirectoryBackupPolicy) {
                case ABORT:
                    throw new CannotStartBabbleNodeException("Config directory already exists and we have ABORT policy");
                case COMPLETE_BACKUP:
                case SINGLE_BACKUP:
                    // Rename
                    backupOldConfigs(compositeGroupName);
                    break;
                case DELETE:
                    deleteDirectory(compositeGroupName);
                    break;
            }
        }

        if   ( ( ! babbleDir.mkdirs() ) && (! babbleDir.exists())) {
            throw new CannotStartBabbleNodeException("Cannot create new Config directory (no previous backup)");
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

        writeTomlFile(babble);

        Log.i("writeTomlFile", "Wrote toml file successfully");

        if (isNotExistingConfigDirectory(compositeGroupName)) {
            addConfigDirectoryToList(compositeGroupName);
        }

        return mTomlDir;
    }


    /**
     * Loads the Babble Config TOML file. This function relies on mTomlDir being set.
     * @return A HashMap object containing the data from the Toml File.
     */

    protected Map<String, Object> readTomlFile(){
        File tomlFile =  new File(mTomlDir, BABBLE_TOML);

        Toml toml = new Toml().read(tomlFile);

        Map<String, Object> configMap = toml.toMap();


        Log.i("readTomlFile", "Read toml file successfully");

        return configMap;
    }

    /**
     * Writes the Babble Config TOML file. This function relies on mTomlDir being set.
     * @param configHashMap A HashMap object containing the config data to be written the Toml File.
     */
    protected void writeTomlFile(Map<String, Object> configHashMap) throws IOException {
        Log.i("writeTomlFile", mTomlDir);

        try {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(configHashMap, new File(mTomlDir, BABBLE_TOML));

            Log.i("writeTomlFile", "Wrote toml file");
        } catch (IOException e) {
            // Log and rethrow
            Log.e("writeTomlFile", e.toString());
            throw e;
        }

    }


    /**
     * Amends the Babble Config TOML file. This function relies on mTomlDir being set.
     * @param configHashMapChanges A HashMap object containing the changed config data to be written the Toml File.
     */
    public void amendTomlSettings(Map<String, Object> configHashMapChanges) throws IOException  {
        boolean hasChanged = false;

        Map<String, Object> configMap = readTomlFile();

        for (Map.Entry<String,Object> entry : configHashMapChanges.entrySet()) {

            Log.i("amendTomlSettings", entry.getKey() + " = " + entry.getValue().toString()) ;

            if (
                ( configMap.containsKey(entry.getKey())) &&
                        (Objects.requireNonNull(configMap.get(entry.getKey())).equals(entry.getValue()))
            ) { continue ; }    // If key exists and value matches, there is nothing to do
            hasChanged = true;

            Log.i("amendTomlSettings:SET", entry.getKey() + " = " + entry.getValue().toString());
            configMap.put(entry.getKey(), entry.getValue());
        }

        if (hasChanged) {
            try {
                writeTomlFile(configMap);
                Log.i("amendTomlSettings", configMap.toString());

             } catch (IOException e)
            {
                // Rethrow
                throw e;
            }
        } else {
            Log.i("amendTomlSettings", "No changes, no write");
        }


        if (configMap.containsKey("moniker"))
        {
            mMoniker = Objects.requireNonNull(configMap.get("moniker")).toString();
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
        // return uuid.toString().replaceAll("[^A]", "A");
    }

    /**
     * Check if this configuration already exists
     * @param subConfigDir is a subdirectory under the babble root
     * @return returns true if it already exists
     */
    private boolean isNotExistingConfigDirectory(String subConfigDir) {
        for (ConfigDirectory d : mDirectories) {
            if (d.directoryName.equals(subConfigDir)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The directory name is composite with the parts separated by underscores.
     * The first part is the appId as set in BabbleService, which may or may not be the FQDN
     * The 2nd part is a unique id as generated in getUniqueId. Currently this is 16 characters
     * The 3rd part is a narrative description field with spaces converted to minus signs and
     * input limit to spaces, letters and numbers only.
     * @param groupDescriptor is the group information
     * @return a unique directory name
     */
    private String getCompositeConfigDir(GroupDescriptor groupDescriptor) {

        String unique = groupDescriptor.getUid();

        //TODO: should this length be controlled in this class or in the group descriptor?
        String trimmedUnique = unique.length() >= sUniqueIdLength
                ? unique.substring(unique.length() - sUniqueIdLength)
                : unique;

        //TODO: the encodeDescription method is lossy e.g. "My-Group" and "MyGroup" will be mapped to "MyGroup"
        //      We should consider the limitations imposed by MDNS (this should all be done in the
        //      GroupDescriptor)
        return mAppId + "_" + trimmedUnique + "_" + ConfigDirectory.encodeDescription(groupDescriptor.getName()) + "_";
    }

    private boolean deleteDirectory(String subConfigDir) {
        Log.d("deleteDirectory", subConfigDir);

        if (isNotExistingConfigDirectory(subConfigDir)) { // Doesn't exist
            Log.e("deleteDirectory !Exists", subConfigDir);
            return false;
        }

        File dir = new File(sRootDir + File.separator + BABBLE_ROOTDIR + File.separator + subConfigDir);

        return deleteDir(dir);
    }

    // delete directory and contents
    private boolean deleteDir(File file) {
        try {
            if (file.isDirectory())
                for (String child : Objects.requireNonNull(file.list()))
                    deleteDir(new File(file, child));
            if (! file.delete() ) {return false;}  // delete child file or empty directory
        } catch (Exception e) {  //TODO: narrow the scope of thus catch
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
        File oldFile = new File(sRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir);
        File newFile = new File(sRootDir + File.separator + BABBLE_ROOTDIR + File.separator + oldSubConfigDir + newSuffix);
        
        Log.d("Rename ", oldFile.getAbsolutePath());
        Log.d("Rename ", newFile.getAbsolutePath());
        
        if (!(oldFile.renameTo(newFile))) {
            Log.d("Rename ","Fails");
            throw new CannotStartBabbleNodeException("Cannot backup the old configuration directory");
        }
    }

    private void backupOldConfigs(String compositeName) throws CannotStartBabbleNodeException {
        
        if (sConfigDirectoryBackupPolicy == ConfigDirectoryBackupPolicy.SINGLE_BACKUP) {

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
        populateDirectories(new File(sRootDir, BABBLE_ROOTDIR));
    }


    /**
     * Function to delete all config folders for a Group. Call the static function
     * ConfigDirectory.rootDirectoryName() on compositeName before passing it to this function to
     * ensure that the name is well-formed.
     * @param compositeName the full underscore separated name of the group
     * @param onlyDeleteBackups if set to true the "live" copy is delete too
     */

    public void deleteDirectoryAndBackups(String compositeName, boolean onlyDeleteBackups) {

        // Call ConfigDirectory.rootDirectoryName on composite name to de-backup the name


        for (ConfigDirectory d : mDirectories) {
            if ((d.isBackup || (!onlyDeleteBackups)) && (d.directoryName.startsWith(compositeName))) {
                deleteDirectory(d.directoryName);
            }
        }

        // Rebuild directory list after pruning backups
        populateDirectories(new File(sRootDir, BABBLE_ROOTDIR));
    }




    private void populateDirectories(File babbleDir) {
        ArrayList<String> directories = new ArrayList<>();

        mDirectories = new ArrayList<>();

        Collections.addAll(directories,
                Objects.requireNonNull(babbleDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                })));

        // Populate mDirectories with the results
        for (String s : directories) {
            addConfigDirectoryToList(s);
        }
    }
}
