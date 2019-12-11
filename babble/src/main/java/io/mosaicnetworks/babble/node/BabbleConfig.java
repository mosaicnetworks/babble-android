package io.mosaicnetworks.babble.node;

/**
 * An immutable class which holds the configuration passed to a node during it's construction
 */
public final class BabbleConfig {

    /**
     * The level at which the node should log
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
        PANIC
    }

    /**
     * A builder pattern is used to construct the config
     */
    public static final class Builder {

        private int mHeartbeat = 10;
        private int mSlowHeartbeat = 10;
        private boolean mStore = false;
        private String mLogLevel = "error";
        private int mTcpTimeout = 1000;
        private int mMaxPool = 2;
        private int mCacheSize = 50000;
        private int mSyncLimit = 1000;
        private boolean mEnableFastSync = false;

        /**
         * Set the heartbeat
         * @param heartbeat interval between node syncing in milliseconds
         * @return modified builder
         */
        public Builder heartbeat(int heartbeat) {
            mHeartbeat = heartbeat;
            return this;
        }

        /**
         * Set the slow-heartbeat
         * @param slowHeartbeat interval between node syncing in milliseconds when there is nothing
         *                      to gossip about
         * @return modified builder
         */
        public Builder slowHeartbeat(int slowHeartbeat) {
            mSlowHeartbeat = slowHeartbeat;
            return this;
        }

        /**
         * Set store
         * @param store store the blockchain data on disk
         * @return modified builder
         */
        public Builder store(boolean store) {
            mStore = store;
            return this;
        }

        /**
         * Set the log level
         * @param logLevel the level at which the node should log
         * @return modified builder
         */
        public Builder logLevel(LogLevel logLevel) {
            switch (logLevel) {
                case DEBUG:
                    mLogLevel = "debug";
                    break;
                case INFO:
                    mLogLevel = "info";
                    break;
                case WARN:
                    mLogLevel = "warn";
                    break;
                case ERROR:
                    mLogLevel = "error";
                    break;
                case FATAL:
                    mLogLevel = "fatal";
                    break;
                case PANIC:
                    mLogLevel = "panic";
                    break;
            }

            return this;
        }

        /**
         * Set TCP timeout
         * @param tcpTimeout TCP timeout in milliseconds
         * @return modified builder
         */
        public Builder tcpTimeout(int tcpTimeout) {
            mTcpTimeout = tcpTimeout;
            return this;
        }

        /**
         * Set the max pool
         * @param maxPool max pool value
         * @return modified builder
         */
        public Builder maxPool(int maxPool) {
            mMaxPool = maxPool;
            return this;
        }

        /**
         * Set the cache size
         * @param cacheSize the maximum number of events to store in the cache
         * @return modified builder
         */
        public Builder cacheSize(int cacheSize) {
            mCacheSize = cacheSize;
            return this;
        }

        /**
         * Set the sync limit
         * @param syncLimit the maximum number of events over which fast sync is performed
         * @return modified builder
         */
        public Builder syncLimit(int syncLimit) {
            mSyncLimit = syncLimit;
            return this;
        }

        /*
        //temporarily disable method
        public Builder enableFastSync(boolean enableFastSync) {
            mEnableFastSync = enableFastSync;
            return this;
        }
         */

        /**
         * Builds the config object
         * @return the built {@link BabbleConfig}
         */
        public BabbleConfig build() {
            return new BabbleConfig(this);
        }
    }

    /**
     * Interval between node syncing in milliseconds when there is something to gossip about
     */
    public final int heartbeat;  //heartbeat in milliseconds

    /**
     * Interval between node syncing in milliseconds when there is nothing to gossip about
     */
    public final int slowHeartbeat;  //heartbeat in milliseconds

    /**
     * Store the blockchain data on disk
     */
    public final Boolean store;  //use badger store

    /**
     * The level at which the node should log
     */
    public final String logLevel;  //debug, info, warn, error, fatal, panic

    /**
     * TCP timeout in milliseconds
     */
    public final int tcpTimeout;  //TCP timeout in milliseconds

    /**
     * Max pool value
     */
    public final int maxPool;  //max number of pooled connections

    /**
     * The maximum number of events to store in the cache
     */
    public final int cacheSize;  //number of items in LRU cache

    /**
     * The maximum number of events over which fast sync is performed
     */
    public final int syncLimit;  //max Events per sync

    /**
     * Enable fast sync
     */
    public final Boolean enableFastSync;  //enable fast sync

    private BabbleConfig(Builder builder) {
        heartbeat = builder.mHeartbeat;
        slowHeartbeat = builder.mSlowHeartbeat;
        store = builder.mStore;
        logLevel = builder.mLogLevel;
        tcpTimeout = builder.mTcpTimeout;
        maxPool = builder.mMaxPool;
        cacheSize = builder.mCacheSize;
        syncLimit = builder.mSyncLimit;
        enableFastSync = builder.mEnableFastSync;
    }


}
