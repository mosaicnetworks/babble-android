package io.mosaicnetworks.babble.node;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.mosaicnetworks.babble.discovery.Peer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NodeConfigDirTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void simpleCreateTest() {

        String dir = temporaryFolder.toString();

        ConfigManager configManager = new ConfigManager(dir, "io.mosaicnetworks.tests", BabbleNode.ConfigFolderBackupPolicy.DELETE);


        File file = new File(dir, ConfigManager.BABBLE_ROOTDIR);
    // Check Babble configuration Root folder exists
        assertTrue(file.exists());
    // Verify a folder we did not create does not exist
        assertFalse(configManager.CheckDirectory("doesnotexist"));
    }


    @Test
    public void writeConfigTest() throws IOException,CannotStartBabbleNodeException
    {
        try
        {
            String dir = temporaryFolder.newFolder().getAbsolutePath();
//            System.out.println(dir);

            ConfigManager configManager = new ConfigManager(dir, "io.mosaicnetworks.tests", BabbleNode.ConfigFolderBackupPolicy.DELETE);

            File file = new File(dir, ConfigManager.BABBLE_ROOTDIR);
            // Check Babble configuration Root folder exists
            assertTrue(file.exists());

            // Create a config and write the toml file.

            String subConfigDir = "unittestconfig";
            assertFalse(configManager.CheckDirectory(subConfigDir)); // Should not exist yet


            NodeConfig nodeConfig = new NodeConfig.Builder()
                    .cacheSize(12)
                    .logLevel(NodeConfig.LogLevel.ERROR)
                    .build();


            String targetDir = configManager.WriteBabbleTomlFiles(nodeConfig, subConfigDir, "127.0.0.1",
                    6666, "unittest");

            System.out.println(targetDir);

// BEGIN List Files
            String[] pathnames;
            pathnames = file.list();
            for (String pathname : pathnames) {
                System.out.println(pathname);
            }
// END List File
//            System.out.println(configManager.directories.get(0));

            assertTrue(configManager.CheckDirectory(subConfigDir));

            File configFile = new File(targetDir, ConfigManager.BABBLE_TOML);
            assertTrue(configFile.exists());

            configManager.WritePrivateKey(targetDir, "DummyPrivateKey");
            configFile = new File(targetDir, ConfigManager.PRIV_KEY);
            assertTrue(configFile.exists());

    //        void WritePrivateKey(String targetDir, String privateKeyHex)
    //        void WritePeersJsonFiles(String targetDir, List<Peer> genesisPeers, List<Peer> currentPeers)



            Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", "127.0.0.1:6666", "testpeer");
            ArrayList<Peer> peers = new ArrayList<Peer>();
            peers.add(peer);


            configManager.WritePeersJsonFiles(targetDir,peers, peers);
            configFile = new File(targetDir, ConfigManager.PEERS_GENESIS_JSON);
            assertTrue(configFile.exists());
            configFile = new File(targetDir, ConfigManager.PEERS_JSON);
            assertTrue(configFile.exists());

        }
        finally
        {
       //     temporaryFolder.delete();
        }
    }


    @Test
    public void getRandomSubConfigDir() {

     try {
         String dir = temporaryFolder.newFolder().getAbsolutePath();
//            System.out.println(dir);

         ConfigManager configManager = new ConfigManager(dir, "io.mosaicnetworks.tests", BabbleNode.ConfigFolderBackupPolicy.DELETE);
         String uuid = configManager.GetUniqueId();
         assertEquals(uuid.length(), 36);
     }
     catch (Exception e)
        {

        }
    }
}
