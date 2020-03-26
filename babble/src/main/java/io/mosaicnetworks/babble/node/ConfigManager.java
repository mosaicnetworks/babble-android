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
import io.mosaicnetworks.babble.servicediscovery.webrtc.Disco;

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

// These variables are static to allow them to be set in the initialisation of any app.
// As ConfigManager is invoked from within BabbleService, it would otherwise not be possible to
// amend these app wide values without passing them into BabbleService.
    private static int sUniqueIdLength = 12;
    private static ConfigManager INSTANCE;
    private static String sRootDir = "";

    private String mTomlDir = "";
    private String mMoniker = "";
    private static ConfigDirectoryBackupPolicy sConfigDirectoryBackupPolicy = ConfigDirectoryBackupPolicy.SINGLE_BACKUP;
    private ArrayList<ConfigDirectory> mDirectories = new ArrayList<>();
    private KeyPair mKeyPair;
    private int mNetworkType = BabbleConstants.NETWORK_NONE;

    private Disco mDisco;  //TODO: Review if we need this specific protocol variable

    /**
     * Provides an instance of the static ConfigManager class, reusing one if available, calling the
     * constructor if not.
     * @param context Context. Used to call getFilesDir to find the path to the babble config dirs
     * @return an instance of ConfigManager
     */
    public static ConfigManager getInstance(Context context, KeyPair keyPair) {
        if (INSTANCE==null) {
            Log.v("ConfigManager", "getInstance");
            INSTANCE = new ConfigManager(context.getApplicationContext(), keyPair);
        }

        return INSTANCE;
    }


    /**
     * Provides an instance of the static ConfigManager class, reusing one if available, calling the
     * constructor if not.
     * @param context Context. Used to call getFilesDir to find the path to the babble config dirs
     * @return an instance of ConfigManager
     */
    public static ConfigManager getInstance(Context context) {
        if (INSTANCE==null) {
            Log.v("ConfigManager", "getInstance");
            INSTANCE = new ConfigManager(context.getApplicationContext(), null);
        }

        return INSTANCE;
    }


    /**
     * Create an object to manage multiple Babble Configs
     * @param appContext the application context
     * @throws FileNotFoundException when the babble root dir cannot be created
     */
    private ConfigManager(Context appContext, KeyPair keyPair) {

        Log.v("ConfigManager", "constructor");

        if (sRootDir.equals("")) {
            sRootDir = appContext.getFilesDir().toString();
        }

        Log.v("ConfigManager", "got FilesDir");


        Log.v("ConfigManager", "got Package Name");

        if ( keyPair == null) {
            mKeyPair = new KeyPair(); //TODO:  how should the key be handled??
        } else {
            mKeyPair = keyPair;
        }

        Log.v("ConfigManager", "got Key Pair");

        File babbleDir = new File(sRootDir, BabbleConstants.BABBLE_ROOTDIR());

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




    /**
     * Configure the service to create a new group using the default ports
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor,  String peersInetAddress,
                                       String babbleInetAddress, int networkType)  throws CannotStartBabbleNodeException, IOException {
        return createConfigNewGroup(groupDescriptor,  peersInetAddress, babbleInetAddress, BabbleConstants.BABBLE_PORT(), networkType);
    }

    /**
     * Configure the service to create a new group, overriding the default ports
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the HttpPeerDiscoveryServer //TODO: how to deal with this
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigNewGroup(GroupDescriptor groupDescriptor,  String peersInetAddress, String babbleInetAddress, int babblingPort, int networkType) throws CannotStartBabbleNodeException, IOException{

        List<Peer> genesisPeers = new ArrayList<>();

        // Only append port if no @ in the NetAddr - i.e. not WebRTC
        String suffix = peersInetAddress.startsWith("0X") ? "" : ":" + babblingPort;


        genesisPeers.add(new Peer(mKeyPair.publicKey, peersInetAddress +suffix, groupDescriptor.getMoniker()));
        List<Peer> currentPeers = new ArrayList<>();
        currentPeers.add(new Peer(mKeyPair.publicKey, peersInetAddress + suffix, groupDescriptor.getMoniker()));

        return createConfig(genesisPeers, currentPeers, groupDescriptor,  babbleInetAddress, babblingPort, networkType);
    }

    /**
     * Configure the service to create an archive group
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress)  throws  IOException {
        return setGroupToArchive(configDirectory, inetAddress, BabbleConstants.BABBLE_PORT());
    }

    /**
     *Configure the service to create an archive group, overriding the default ports
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     */
    public String setGroupToArchive(ConfigDirectory configDirectory, String inetAddress, int babblingPort) {

        Log.i("setGroupToArchive", configDirectory.directoryName);
        mNetworkType = BabbleConstants.NETWORK_NONE;
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
        configChanges.put("db",  mTomlDir + File.separator+ BabbleConstants.DB_SUBDIR());


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
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor,  String inetAddress, int networkType) throws CannotStartBabbleNodeException, IOException {
        return createConfig(genesisPeers, currentPeers, groupDescriptor,  inetAddress, BabbleConstants.BABBLE_PORT(), networkType);
    }

    /**
     *
     * @param genesisPeers list of genesis peers
     * @param currentPeers list of current peers
     * @param inetAddress the IPv4 address of the interface to which the Babble node will bind
     * @param babblingPort the port used for Babble consensus
     * //@param discoveryPort the port used by the {HttpPeerDiscoveryServer} //TODO: deal with discovery
     * @throws IllegalStateException if the service is currently running
     */
    public String createConfigJoinGroup(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor,  String inetAddress, int babblingPort, int networkType) throws CannotStartBabbleNodeException, IOException{
        return createConfig(genesisPeers, currentPeers, groupDescriptor,  inetAddress, babblingPort, networkType); //TODO: group name
    }

    private String createConfig(List<Peer> genesisPeers, List<Peer> currentPeers, GroupDescriptor groupDescriptor,
                                String inetAddress, int babblingPort, int networkType) throws CannotStartBabbleNodeException, IOException {

        mNetworkType = networkType;

        String compositeGroupName = getCompositeConfigDir(groupDescriptor);

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .webrtc(networkType == BabbleConstants.NETWORK_GLOBAL)
                .signalAddress(networkType == BabbleConstants.NETWORK_GLOBAL ? BabbleConstants.DISCO_RELAY_ADDRESS() : "")
                .build();

        mMoniker = groupDescriptor.getMoniker();
        //TODO: is there a cleaner way of obtaining the path?
        // It is stored in mTomlDir which has getTomlDir and setTomlDir getter and setters
        String fullPath = writeBabbleTomlFiles(nodeConfig, compositeGroupName, inetAddress, babblingPort, mMoniker);
        Log.v("Config.createConfig", "Full Path:" + fullPath);

        writePeersJsonFiles(fullPath, genesisPeers, currentPeers);

        // private key -- does not overwrite
        writePrivateKey(fullPath, mKeyPair.privateKey);
        // If we are a WebRTC/Global type, build the disco object for use later.
        if (networkType == BabbleConstants.NETWORK_GLOBAL) {
            Log.i("ConfigManager", "createConfig: Network type global" );
            mDisco = new Disco( groupDescriptor.getUid(), groupDescriptor.getName(),
                    BabbleConstants.APP_ID(), mKeyPair.publicKey, 0, -1,
                    currentPeers, genesisPeers  );
        } else {
            Log.i("ConfigManager", "createConfig: Network type not global" );
            mDisco = null;
        }

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
            FileWriter fileWriter = new FileWriter(new File(targetDir, BabbleConstants.PRIV_KEY()) );
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

            FileWriter fileWriter = new FileWriter(new File(targetDir, BabbleConstants.PEERS_JSON()));
            gson.toJson(currentPeers, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            Log.e("writePeersJsonFiles", e.toString());
        }

        try {
            FileWriter fileWriter = new FileWriter(new File(targetDir, BabbleConstants.PEERS_GENESIS_JSON()));
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
        mTomlDir = sRootDir + File.separator + BabbleConstants.BABBLE_ROOTDIR() + File.separator + compositeName;
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



        setTomlDir(compositeGroupName);

        File babbleDir = new File(mTomlDir, BabbleConstants.DB_SUBDIR());
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

        Map<String, Object> babble = nodeConfig.getTomlMap(mTomlDir,inetAddress + ":" + port, moniker );
        // Logic to build the map was relocated from here to NodeConfig where it more naturally sits
        //TODO: remove the comment above once everyone has seen it

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
        File tomlFile =  new File(mTomlDir, BabbleConstants.BABBLE_TOML());

        Toml toml = new Toml().read(tomlFile);

        Map<String, Object> configMap = toml.toMap();


        Log.i("readTomlFile", "Read toml file successfully");

        return configMap;
    }

    /**
     * Writes the Babble Config TOML file. This function relies on mTomlDir being set.
     * @param configHashMap A HashMap object containing the config data to be written the Toml File.
     */
    protected void writeTomlFile(Map<String, Object> configHashMap) {
        Log.i("writeTomlFile", mTomlDir);

        try {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(configHashMap, new File(mTomlDir, BabbleConstants.BABBLE_TOML()));

            Log.i("writeTomlFile", "Wrote toml file");
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
                writeTomlFile(configMap);
                Log.i("amendTomlSettings", configMap.toString());

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
        return BabbleConstants.APP_ID() + "_" + trimmedUnique + "_" +
                ConfigDirectory.encodeDescription(groupDescriptor.getName()) + "_";
    }

    private boolean deleteDirectory(String subConfigDir) {
        Log.d("deleteDirectory", subConfigDir);

        if (isNotExistingConfigDirectory(subConfigDir)) { // Doesn't exist
            Log.e("deleteDirectory !Exists", subConfigDir);
            return false;
        }

        File dir = new File(sRootDir + File.separator + BabbleConstants.BABBLE_ROOTDIR() +
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
        File oldFile = new File(sRootDir + File.separator + BabbleConstants.BABBLE_ROOTDIR() +
                File.separator + oldSubConfigDir);
        File newFile = new File(sRootDir + File.separator + BabbleConstants.BABBLE_ROOTDIR() +
                File.separator + oldSubConfigDir + newSuffix);

        if (!(oldFile.renameTo(newFile))) {
            Log.i("Rename ", oldFile.getAbsolutePath());
            Log.i("Rename ", newFile.getAbsolutePath());

            Log.e("Rename ","Fails");
            throw new RuntimeException("Cannot backup the old configuration directory");
        }
    }

    private void backupOldConfigs(String compositeName) {
        
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
        populateDirectories(new File(sRootDir, BabbleConstants.BABBLE_ROOTDIR()));
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
        populateDirectories(new File(sRootDir, BabbleConstants.BABBLE_ROOTDIR()));
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
