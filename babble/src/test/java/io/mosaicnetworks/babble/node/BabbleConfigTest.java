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
}
