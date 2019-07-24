package io.mosaicnetworks.babble.node;

public class BabbleConfig {
    public int heartbeat;  //heartbeat timeout in milliseconds

    public Boolean store;  //use badger store

    public String loglevel;  //debug, info, warn, error, fatal, panic

    public int tcpTimeout;  //TCP timeout in milliseconds

    public int maxPool;  //max number of pooled connections

    public int cacheSize;  //number of items in LRU cache

    public int syncLimit;  //max Events per sync

    public Boolean enableFastSync;  //enable fast sync

    public BabbleConfig() {
        //default config values
        this.heartbeat = 100;
        this.store = false;
        this.loglevel = "debug";
        this.tcpTimeout = 1000;
        this.maxPool = 2;
        this.cacheSize = 50000;
        this.syncLimit = 1000;
        this.enableFastSync = true;
    }
}
