package io.mosaicnetworks.babble.node;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.discovery.Peer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BabbleConfigDirTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void simpleCreateTest() {

        String dir = temporaryFolder.toString();

        BabbleConfigDir babbleConfigDir = new BabbleConfigDir(dir);


        File file = new File(dir, BabbleConfigDir.BABBLE_ROOTDIR);
    // Check Babble configuration Root folder exists
        assertTrue(file.exists());
    // Verify a folder we did not create does not exist
        assertFalse(babbleConfigDir.CheckDirectory("doesnotexist"));
    }


    @Test
    public void writeConfigTest() throws IOException
    {
        try
        {
            String dir = temporaryFolder.newFolder().getAbsolutePath();
//            System.out.println(dir);

            BabbleConfigDir babbleConfigDir = new BabbleConfigDir(dir);

            File file = new File(dir, BabbleConfigDir.BABBLE_ROOTDIR);
            // Check Babble configuration Root folder exists
            assertTrue(file.exists());

            // Create a config and write the toml file.

            String subConfigDir = "unittestconfig";
            assertFalse(babbleConfigDir.CheckDirectory(subConfigDir)); // Should not exist yet


            BabbleConfig babbleConfig = new BabbleConfig.Builder()
                    .cacheSize(12)
                    .logLevel(BabbleConfig.LogLevel.ERROR)
                    .build();

            String targetDir = babbleConfigDir.WriteBabbleTomlFiles(babbleConfig, subConfigDir, "127.0.0.1",
                    6666, "unittest");

            System.out.println(targetDir);

// BEGIN List Files
            String[] pathnames;
            pathnames = file.list();
            for (String pathname : pathnames) {
                System.out.println(pathname);
            }
// END List File
//            System.out.println(babbleConfigDir.directories.get(0));

            assertTrue(babbleConfigDir.CheckDirectory(subConfigDir));

            File configFile = new File(targetDir, BabbleConfigDir.BABBLE_TOML);
            assertTrue(configFile.exists());

            babbleConfigDir.WritePrivateKey(targetDir, "DummyPrivateKey");
            configFile = new File(targetDir, BabbleConfigDir.PRIV_KEY);
            assertTrue(configFile.exists());

    //        void WritePrivateKey(String targetDir, String privateKeyHex)
    //        void WritePeersJsonFiles(String targetDir, List<Peer> genesisPeers, List<Peer> currentPeers)



            Peer peer = new Peer("54dbb737eac5007103e729e9ab7ce64a6850a310", "127.0.0.1:6666", "testpeer");
            ArrayList<Peer> peers = new ArrayList<Peer>();
            peers.add(peer);


            babbleConfigDir.WritePeersJsonFiles(targetDir,peers, peers);
            configFile = new File(targetDir, BabbleConfigDir.PEERS_GENESIS_JSON);
            assertTrue(configFile.exists());
            configFile = new File(targetDir, BabbleConfigDir.PEERS_JSON);
            assertTrue(configFile.exists());

        }
        finally
        {
            temporaryFolder.delete();
        }
    }


}
