package io.mosaicnetworks.babble.node;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NodeConfigTest {



    @Test
    public void defaultConfigTest() {
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .cacheSize(12)
                .logLevel(NodeConfig.LogLevel.ERROR)
                .build();

        assertEquals(12, nodeConfig.cacheSize);
        assertEquals("error", nodeConfig.logLevel);

    }


// Test the default values of the config
    @Test
    public void allDefaultConfigTest() {
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .build();

// These values are set in NodeConfig.java
        assertEquals(10, nodeConfig.heartbeat);
        assertEquals(10, nodeConfig.slowHeartbeat);
        assertEquals(false, nodeConfig.store);
        assertEquals("error", nodeConfig.logLevel);
        assertEquals(1000, nodeConfig.tcpTimeout);
        assertEquals(2, nodeConfig.maxPool);
        assertEquals(50000, nodeConfig.cacheSize);
        assertEquals(1000, nodeConfig.syncLimit);
        assertEquals(false, nodeConfig.enableFastSync);
        assertEquals(false, nodeConfig.bootstrap);
        assertEquals("", nodeConfig.serviceListen);
        assertEquals("20s", nodeConfig.joinTimeout);
        assertEquals(false, nodeConfig.maintenanceMode);
        assertEquals(300, nodeConfig.suspendLimit);
        assertEquals(true, nodeConfig.noService);
    }


    @Test
    public void allValuesConfigTest() {
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .heartbeat(50)
                .slowHeartbeat(70)
                .store(true)
                .logLevel(NodeConfig.LogLevel.INFO)
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
                .noService(false)
                .build();

// These values are set in NodeConfig.java
        assertEquals(50, nodeConfig.heartbeat);
        assertEquals(70, nodeConfig.slowHeartbeat);
        assertEquals(true, nodeConfig.store);
        assertEquals("info", nodeConfig.logLevel);
        assertEquals(2000, nodeConfig.tcpTimeout);
        assertEquals(3, nodeConfig.maxPool);
        assertEquals(5000, nodeConfig.cacheSize);
        assertEquals(400, nodeConfig.syncLimit);
    //    assertEquals(true, nodeConfig.enableFastSync);
        assertEquals(true, nodeConfig.bootstrap);
        assertEquals("localhost:1234", nodeConfig.serviceListen);
        assertEquals("10s", nodeConfig.joinTimeout);
        assertEquals(true, nodeConfig.maintenanceMode);
        assertEquals(200, nodeConfig.suspendLimit);
        assertEquals(false, nodeConfig.noService);
    }



}
