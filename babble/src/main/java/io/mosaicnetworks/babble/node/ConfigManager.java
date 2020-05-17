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
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.service.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.webrtc.Disco;
import io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCService;

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
    public enum ConfigDirectoryBackupPolicy  {
        DELETE,
        SINGLE_BACKUP,
        COMPLETE_BACKUP,
        ABORT
    }

    private static ConfigManager INSTANCE;
    private int mUniqueIdLength = 12;
    private String mRootDir = "";
    private String mTomlDir = "";
    private String mMoniker = "";
    private final String mAppId;
    private static ConfigDirectoryBackupPolicy sConfigDirectoryBackupPolicy = ConfigDirectoryBackupPolicy.SINGLE_BACKUP;
    private ArrayList<ConfigDirectory> mDirectories = new ArrayList<>();
    private KeyPair mKeyPair;
    private Disco mDisco;
    private String mBabbleRootDir = "babble";
    private int mDefaultBabblePort = 6666;
    private String mDbSubDir = "badger_db";
    private String mPrivKeyFile = "priv_key";
    private String mPeersJsonFile = "peers.json";
    private String mPeersGenesisJsonFile = "peers.genesis.json";
    private String mBabbleTomlFile = "babble.toml";
    private boolean mSkipVerify = true; // XXX Unsafe This should be read from context or config file

    /**
     * Provides an instance of the static ConfigManager class, reusing one if available, calling the
     * constructor if not.
     * @param context Context. Used to call getFilesDir to find the path to the babble config dirs
     * @return an instance of ConfigManager
     */
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
        if (mRootDir.equals("")) {
            mRootDir = appContext.getFilesDir().toString();
        }

        mAppId = appContext.getPackageName();
        mKeyPair = new KeyPair(); //TODO:  how should the key be handled??
        File babbleDir = new File(mRootDir, mBabbleRootDir);

        if (babbleDir.exists()) {
            populateDirectories(babbleDir);
        } else { // First run, so we create the root dir - clearly no sub dirs yet
            if ( ( ! babbleDir.mkdirs() ) && (! babbleDir.exists())) {
                throw new RuntimeException("Could not make babble config directory");
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
    public ConfigDirectoryBackupPolicy getConfigDirectoryBackupPolicy() {
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
    public void setConfigDirectoryBackupPolicy(ConfigDirectoryBackupPolicy mConfigDirectoryBackupPolicy) {
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
    public int getUniqueIdLength() {
        return mUniqueIdLength;
    }

    /**
     * Setter method for Unique ID Length
     *
     * Each configuration has a unique identifier that is formed by concatenating an appId with
     * a unique ID and the group description using underscores. The Unique ID length controls how
     * long the unique hex string is. The default length is 12.
     * @param uniqueIdLength the unique ID length
     */
    public void setUniqueIdLength(int uniqueIdLength) {
        mUniqueIdLength = uniqueIdLength;
    }

    /**
     * The moniker is a human readable name for a babble node. The moniker is not guaranteed to
     * be unique and can be trivially spoofed
     * @return the Moniker
     */
    public String getMoniker() {
        return mMoniker;
    }

    /**
     * Returns the root directory of the App-specific storage. It defaults to
     * appContext.getFilesDir().
     * @return the Root Dir
     */
    public String getRootDir() {
        return mRootDir;
    }

    /**
     * Returns the ID of the package that uses this class (ex: io.mosaicnetworks.sample)
     * @return the AppID
     */
    public String getAppID() { return mAppId; }

    /**
     * Returns the value of mSkipVerify which decides whether to check TLS certificates in https
     * requests.
     * @return value of skip-verify option
     */
    public boolean getSkipVerify() { return mSkipVerify; }

    /**
     * Configure the service to create a new group using the default ports
     * @param moniker node moniker
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor, String moniker,  String peersInetAddress, String babbleInetAddress, int networkType) {
        return createConfigNewGroup(groupDescriptor, moniker, peersInetAddress, babbleInetAddress, mDefaultBabblePort, networkType);
    }

    /**
     * Configure the service to create a new group, overriding the default ports
     * @param moniker node moniker
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the HttpPeerDiscoveryServer //TODO: how to deal with this
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor, String moniker,  String peersInetAddress, String babbleInetAddress, int babblingPort, int networkType) {

        List<Peer> genesisPeers = new ArrayList<>();

        // Only append port if no @ in the NetAddr - i.e. not WebRTC
        String suffix = peersInetAddress.startsWith("0X") ? "" : ":" + babblingPort;


        genesisPeers.add(new Peer(mKeyPair.publicKey, peersInetAddress +suffix, moniker));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(mKeyPair.publicKey, peersInetAddress + suffix, moniker));

        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, babbleInetAddress, babblingPort, networkType);
    }

    /**
     * Configure the service to create an archive group
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress) throws IOException {
        return setGroupToArchive(configDirectory, inetAddress, mDefaultBabblePort);
    }

    /**
     *Configure the service to create an archive group, overriding the default ports
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress, int babblingPort) {
        setTomlDir(configDirectory.directoryName);
        Map<String, Object> configChanges = new HashMap<>();
        configChanges.put("maintenance-mode", true);
        configChanges.put("listen", inetAddress + ":" + babblingPort);
        configChanges.put("advertise", inetAddress + ":" + babblingPort);

        //TODO: possibly move these amendments into the backup config processing to avoid having to
        //      set them here
        configChanges.put("datadir", mTomlDir);
        configChanges.put("db",  mTomlDir + File.separator+ mDbSubDir);
        amendTomlSettings(configChanges);

        return mTomlDir;
    }

    /**
     * Configure the service to join an existing group using the default ports
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param moniker node moniker
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor, String moniker, String inetAddress, int networkType) {
        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, inetAddress, mDefaultBabblePort, networkType);
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
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor, String moniker, String inetAddress, int babblingPort, int networkType) throws CannotStartBabbleNodeException, IOException{
        return createConfig(genesisPeers, currentPeers, groupDescriptor, moniker, inetAddress, babblingPort, networkType); //TODO: group name
    }

    private String createConfig(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor,
                                String moniker, String inetAddress, int babblingPort, int networkType) {

        String compositeGroupName = getCompositeConfigDir(groupDescriptor);

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .webrtc(networkType == BabbleService.NETWORK_GLOBAL)
                .signalAddress(networkType == BabbleService.NETWORK_GLOBAL ? WebRTCService.RELAY_SEVER_ADDRESS : "")
                .skipVerify(mSkipVerify)
                .build();
        mMoniker = moniker;

        String fullPath = writeBabbleTomlFiles(nodeConfig, compositeGroupName, inetAddress, babblingPort, moniker);
        writePeersJsonFiles(fullPath, genesisPeers, currentPeers);

        // private key -- does not overwrite
        writePrivateKey(fullPath, mKeyPair.privateKey);

        // If we are a WebRTC/Global type, build the disco object for use later.
        if (networkType == BabbleService.NETWORK_GLOBAL) {
            mDisco = new Disco( groupDescriptor.getUid(),
                    groupDescriptor.getName(),
                    mAppId,
                    mKeyPair.publicKey,
                    0,
                    -1,
                    currentPeers,
                    genesisPeers  );
        } else {
            mDisco = null;
        }

        return fullPath;
    }

    /**
     * Write private key
     * @param targetDir is the directory that the private key is to be written to.
     * @param privateKeyHex private key as produced by the {@link KeyPair} class
     */
    public void writePrivateKey(String targetDir, String privateKeyHex) {
        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, mPrivKeyFile) );
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
            FileWriter fileWriter = new FileWriter(new File(targetDir, mPeersJsonFile));
            gson.toJson(currentPeers, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, mPeersGenesisJsonFile));
            gson.toJson(genesisPeers, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
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
        mTomlDir = mRootDir + File.separator + mBabbleRootDir + File.separator + compositeName;
        this.mTomlDir = mTomlDir;
    }

    /**
     * Write Babble Config to disk ready for Babble to use
     * @param nodeConfig is the babble configuration object
     * @param compositeGroupName is the sub-directory of the babble sub-directory of the local storage as passed to the constructor
     * @return the composite path where the babble.toml file was written
     */
    public String writeBabbleTomlFiles(NodeConfig nodeConfig, String compositeGroupName, String inetAddress, int port, String moniker) {

        //TODO: add inetAddress, port and moniker to nodeConfig??
        Map<String, Object> babble = new HashMap<>();

        setTomlDir(compositeGroupName);

        File babbleDir = new File(mTomlDir, mDbSubDir);
        if (babbleDir.exists()){
            // We have a clash.
            switch (sConfigDirectoryBackupPolicy) {
                case ABORT:
                    throw new IllegalArgumentException("Config directory already exists and we have ABORT policy");
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
            throw new IllegalArgumentException("Cannot create new Config directory (no previous backup)");
        }

        babble.put("datadir", mTomlDir) ;
        babble.put("db",  mTomlDir + File.separator + mDbSubDir) ;

        babble.put("log", nodeConfig.logLevel);
        babble.put("listen", inetAddress + ":" + port);
        babble.put("advertise", inetAddress + ":" + port);
        babble.put("no-service", nodeConfig.noService);

        if (!nodeConfig.signalAddr.equals("")) {  // Only set if set
            babble.put("signal-addr", nodeConfig.signalAddr);
        }
        babble.put("webrtc", nodeConfig.webrtc);

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
        babble.put("signal-skip-verify", nodeConfig.skipVerify);

        writeTomlFile(babble);

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
        File tomlFile =  new File(mTomlDir, mBabbleTomlFile);
        Toml toml = new Toml().read(tomlFile);

        return toml.toMap();
    }

    /**
     * Writes the Babble Config TOML file. This function relies on mTomlDir being set.
     * @param configHashMap A HashMap object containing the config data to be written the Toml File.
     */
    protected void writeTomlFile(Map<String, Object> configHashMap) {

        try {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(configHashMap, new File(mTomlDir, mBabbleTomlFile));
        } catch (IOException e) {
            // Log and rethrow
            Log.e("writeTomlFile", e.toString());
            throw new RuntimeException(e.toString());
        }

    }

    /**
     * Amends the Babble Config TOML file. This function relies on mTomlDir being set.
     * @param configHashMapChanges A HashMap object containing the changed config data to be written the Toml File.
     */
    public void amendTomlSettings(Map<String, Object> configHashMapChanges) {
        boolean hasChanged = false;

        Map<String, Object> configMap = readTomlFile();

        for (Map.Entry<String,Object> entry : configHashMapChanges.entrySet()) {
            if (
                ( configMap.containsKey(entry.getKey())) &&
                        (Objects.requireNonNull(configMap.get(entry.getKey())).equals(entry.getValue()))
            ) { continue ; }    // If key exists and value matches, there is nothing to do
            hasChanged = true;
            configMap.put(entry.getKey(), entry.getValue());
        }

        if (hasChanged) {
                writeTomlFile(configMap);
        }


        if (configMap.containsKey("moniker"))
        {
            mMoniker = Objects.requireNonNull(configMap.get("moniker")).toString();
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
        String trimmedUnique = unique.length() >= mUniqueIdLength
                ? unique.substring(unique.length() - mUniqueIdLength)
                : unique;

        //TODO: the encodeDescription method is lossy e.g. "My-Group" and "MyGroup" will be mapped to "MyGroup"
        //      We should consider the limitations imposed by MDNS (this should all be done in the
        //      GroupDescriptor)
        return mAppId + "_" + trimmedUnique + "_" + ConfigDirectory.encodeDescription(groupDescriptor.getName()) + "_";
    }

    private boolean deleteDirectory(String subConfigDir) {

        if (isNotExistingConfigDirectory(subConfigDir)) { // Doesn't exist
            Log.w("deleteDirectory !Exists", subConfigDir);
            return false;
        }

        File dir = new File(mRootDir + File.separator + mBabbleRootDir +
                File.separator + subConfigDir);

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
    
    private void renameConfigDirectory(String oldSubConfigDir, int newSuffix) {
        File oldFile = new File(mRootDir + File.separator + mBabbleRootDir +
                File.separator + oldSubConfigDir);
        File newFile = new File(mRootDir + File.separator + mBabbleRootDir +
                File.separator + oldSubConfigDir + newSuffix);
        
        if (!(oldFile.renameTo(newFile))) {
            Log.e("Rename ","Fails");
            throw new RuntimeException("Cannot backup the old configuration directory");
        }
    }

    private void backupOldConfigs(String compositeName) {
        
        if (sConfigDirectoryBackupPolicy == ConfigDirectoryBackupPolicy.SINGLE_BACKUP) {

            for (ConfigDirectory d : mDirectories) {
                if ((d.isBackup) && (d.directoryName.startsWith(compositeName))) {
                    deleteDirectory(d.directoryName);
                }
            }

            renameConfigDirectory(compositeName,1);

        } else { 
            // MULTIPLE_BACKUP
            int newestInt = 0;
            for (ConfigDirectory d : mDirectories) {
                if ((d.isBackup) && (d.directoryName.startsWith(compositeName))) {
                    if (newestInt < d.BackUpVersion) {
                        newestInt = d.BackUpVersion;
                    }
                }
            }

            renameConfigDirectory(compositeName,newestInt + 1);
        }

        populateDirectories(new File(mRootDir, mBabbleRootDir));
    }


    /**
     * Function to delete all config folders for a Group. Call the static function
     * ConfigDirectory.rootDirectoryName() on compositeName before passing it to this function to
     * ensure that the name is well-formed.
     * @param compositeName the full underscore separated name of the group
     * @param onlyDeleteBackups if set to true the "live" copy is delete too
     */

    public void deleteDirectoryAndBackups(String compositeName, boolean onlyDeleteBackups) {
        for (ConfigDirectory d : mDirectories) {
            if ((d.isBackup || (!onlyDeleteBackups)) && (d.directoryName.startsWith(compositeName))) {
                deleteDirectory(d.directoryName);
            }
        }

        // Rebuild directory list after pruning backups
        populateDirectories(new File(mRootDir, mBabbleRootDir));
    }

    public String getPublicKey() throws IllegalAccessError {
        if (mKeyPair != null) {
            return mKeyPair.publicKey;
        } else {
            throw  new IllegalAccessError();
        }
    }

    public Disco getDisco() {
        return mDisco;
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
