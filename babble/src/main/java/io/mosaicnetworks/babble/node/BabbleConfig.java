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

        //TODO: change variable names to m prefix
        private int heartbeat = 100;
        private boolean store = false;
        private String logLevel = "error";
        private int tcpTimeout = 1000;
        private int maxPool = 2;
        private int cacheSize = 50000;
        private int syncLimit = 1000;
        private boolean enableFastSync = true;

        public Builder heartbeat(int heartbeat) {
            this.heartbeat = heartbeat;
            return this;
        }

        public Builder store(boolean store) {
            this.store = store;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            switch (logLevel) {
                case DEBUG:
                    this.logLevel = "debug";
                    break;
                case INFO:
                    this.logLevel = "info";
                    break;
                case WARN:
                    this.logLevel = "warn";
                    break;
                case ERROR:
                    this.logLevel = "error";
                    break;
                case FATAL:
                    this.logLevel = "fatal";
                    break;
                case PANIC:
                    this.logLevel = "panic";
                    break;
            }

            return this;
        }

        public Builder tcpTimeout(int tcpTimeout) {
            this.tcpTimeout = tcpTimeout;
            return this;
        }

        public Builder maxPool(int maxPool) {
            this.maxPool = maxPool;
            return this;
        }

        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder syncLimit(int syncLimit) {
            this.syncLimit = syncLimit;
            return this;
        }

        public Builder enableFastSync(boolean enableFastSync) {
            this.enableFastSync = enableFastSync;
            return this;
        }

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
        heartbeat = builder.heartbeat;
        store = builder.store;
        logLevel = builder.logLevel;
        tcpTimeout = builder.tcpTimeout;
        maxPool = builder.maxPool;
        cacheSize = builder.cacheSize;
        syncLimit = builder.syncLimit;
        enableFastSync = builder.enableFastSync;
    }


}
