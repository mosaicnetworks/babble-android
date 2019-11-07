package io.mosaicnetworks.babble.node;

public final class BabbleConfig {

    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
        PANIC
    }

    public static final class Builder {

        private int mHeartbeat = 100;
        private boolean mStore = false;
        private String mLogLevel = "error";
        private int mTcpTimeout = 1000;
        private int mMaxPool = 2;
        private int mCacheSize = 50000;
        private int mSyncLimit = 1000;
        private boolean mEnableFastSync = false;

        public Builder heartbeat(int heartbeat) {
            mHeartbeat = heartbeat;
            return this;
        }

        public Builder store(boolean store) {
            mStore = store;
            return this;
        }

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

        public Builder tcpTimeout(int tcpTimeout) {
            mTcpTimeout = tcpTimeout;
            return this;
        }

        public Builder maxPool(int maxPool) {
            mMaxPool = maxPool;
            return this;
        }

        public Builder cacheSize(int cacheSize) {
            mCacheSize = cacheSize;
            return this;
        }

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

        public BabbleConfig build() {
            return new BabbleConfig(this);
        }
    }

    public final int heartbeat;  //heartbeat in milliseconds
    public final Boolean store;  //use badger store
    public final String logLevel;  //debug, info, warn, error, fatal, panic
    public final int tcpTimeout;  //TCP timeout in milliseconds
    public final int maxPool;  //max number of pooled connections
    public final int cacheSize;  //number of items in LRU cache
    public final int syncLimit;  //max Events per sync
    public final Boolean enableFastSync;  //enable fast sync

    private BabbleConfig(Builder builder) {
        heartbeat = builder.mHeartbeat;
        store = builder.mStore;
        logLevel = builder.mLogLevel;
        tcpTimeout = builder.mTcpTimeout;
        maxPool = builder.mMaxPool;
        cacheSize = builder.mCacheSize;
        syncLimit = builder.mSyncLimit;
        enableFastSync = builder.mEnableFastSync;
    }


}
