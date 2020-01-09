package io.mosaicnetworks.babble.node;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BabbleConfigTest {



    @Test
    public void defaultConfigTest() {
        BabbleConfig babbleConfig = new BabbleConfig.Builder()
                .cacheSize(12)
                .logLevel(BabbleConfig.LogLevel.ERROR)
                .build();

        assertEquals(12, babbleConfig.cacheSize);
        assertEquals("error", babbleConfig.logLevel);

    }


// Test the default values of the config
    @Test
    public void allDefaultConfigTest() {
        BabbleConfig babbleConfig = new BabbleConfig.Builder()
                .build();

// These values are set in BabbleConfig.java
        assertEquals(10, babbleConfig.heartbeat);
        assertEquals(10, babbleConfig.slowHeartbeat);
        assertEquals(false, babbleConfig.store);
        assertEquals("error", babbleConfig.logLevel);
        assertEquals(1000, babbleConfig.tcpTimeout);
        assertEquals(2, babbleConfig.maxPool);
        assertEquals(50000, babbleConfig.cacheSize);
        assertEquals(1000, babbleConfig.syncLimit);
        assertEquals(false, babbleConfig.enableFastSync);
        assertEquals(false, babbleConfig.bootstrap);
        assertEquals("", babbleConfig.serviceListen);
        assertEquals("20s", babbleConfig.joinTimeout);
        assertEquals(false, babbleConfig.maintenanceMode);
        assertEquals(300, babbleConfig.suspendLimit);
        assertEquals(true, babbleConfig.loadPeers);
        assertEquals(true, babbleConfig.noService);
    }


    @Test
    public void allValuesConfigTest() {
        BabbleConfig babbleConfig = new BabbleConfig.Builder()
                .heartbeat(50)
                .slowHeartbeat(70)
                .store(true)
                .logLevel(BabbleConfig.LogLevel.INFO)
                .tcpTimeout(2000)
                .maxPool(3)
                .cacheSize(5000)
                .syncLimit(400)
           //     .enableFastSync(true)
                .bootstrap(true)
                .serviceListen("localhost:1234")
                .joinTimeout("10s")
                .maintenanceMode(true)
                .suspendLimit(200)
                .loadPeers(false)
                .noService(false)
                .build();

// These values are set in BabbleConfig.java
        assertEquals(50, babbleConfig.heartbeat);
        assertEquals(70, babbleConfig.slowHeartbeat);
        assertEquals(true, babbleConfig.store);
        assertEquals("info", babbleConfig.logLevel);
        assertEquals(2000, babbleConfig.tcpTimeout);
        assertEquals(3, babbleConfig.maxPool);
        assertEquals(5000, babbleConfig.cacheSize);
        assertEquals(400, babbleConfig.syncLimit);
    //    assertEquals(true, babbleConfig.enableFastSync);
        assertEquals(true, babbleConfig.bootstrap);
        assertEquals("localhost:1234", babbleConfig.serviceListen);
        assertEquals("10s", babbleConfig.joinTimeout);
        assertEquals(true, babbleConfig.maintenanceMode);
        assertEquals(200, babbleConfig.suspendLimit);
        assertEquals(false, babbleConfig.loadPeers);
        assertEquals(false, babbleConfig.noService);
    }



}
