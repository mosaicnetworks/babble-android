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

/**
 * An immutable class which holds the configuration passed to a node during it's construction
 */
public final class NodeConfig {

    /**
     * The level at which the node should log
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
        PANIC,
        TRACE
    }

    /**
     * A builder pattern is used to construct the config
     */
    public static final class Builder {

// NB these defaults are tested in BabbleConfigTest.java allDefaultConfigTest()
// If you change these values, you must change the values in that file.

        private int mHeartbeat = 100;
        private int mSlowHeartbeat = 200;
        private boolean mStore = true;
        private String mLogLevel = "info";
        private int mTcpTimeout = 10000;
        private int mMaxPool = 2;
        private int mCacheSize = 50000;
        private int mSyncLimit = 100;
        private boolean mEnableFastSync = false;
        private boolean mBootstrap = false;   //bootstrap
        private String mServiceListen = "";   //service-listen
        private String mJoinTimeout = "20s";  //join_timeout
        private boolean mMaintenanceMode = false;  //maintenance-mode
        private int mSuspendLimit = 300;      //suspend-limit
        private boolean mLoadPeers = true;    //loadpeers
        private boolean mNoService = true;    //no-service
        private boolean mWebRTC = false;  //webrtc
        private String mSignalAddr = "";  //signal-addr


        /**
         * Set the Database Directory
         * @param databaseDir the location of the badger DB database
         * @return modified builder
         */
        public Builder databaseDir(String databaseDir) {
            //db
            return this;
        }


        /**
         * Turn on webrtc
         * @param webrtc use webrtc
         * @return modified builder
         */
        public Builder webrtc(boolean webrtc) {
            mWebRTC = webrtc;
            return this;
        }

        /**
         * signalAddress host in ip:port format
         * @param signalAddress Signal Server Address
         * @return modified builder
         */
        public Builder signalAddress(String signalAddress) {
            mSignalAddr = signalAddress;
            return this;
        }




        /**
         * Turn on bootstrap
         * @param bootstrap bootstrap from the badger DB if available
         * @return modified builder
         */
        public Builder bootstrap(boolean bootstrap) {
            mBootstrap = bootstrap;
            return this;
        }


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
                case TRACE:
                    mLogLevel = "trace";
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

        /**
         * Set the Service Listen Address. N.B. this is not required if noService is set to true
         * @param serviceListen the address and port where the service listens
         * @return modified builder
         */
        public Builder serviceListen(String serviceListen) {
            mServiceListen = serviceListen;
            return this;
        }


        /**
         * Set the timeout for Join Requests
         * @param joinTimeout the timeout for join requests. You need to include a unit. e.g. "20s" for 20 seconds
         * @return modified builder
         */
        public Builder joinTimeout(String joinTimeout) {
            mJoinTimeout = joinTimeout;
            return this;
        }


        /**
         * Specify Maintenance Mode
         * @param maintenanceMode open babble in maintenance mode
         * @return modified builder
         */
        public Builder maintenanceMode(boolean maintenanceMode) {
            mMaintenanceMode = maintenanceMode;
            return this;
        }


        /**
         * Specify Suspend Limit for undetermined events
         * @param suspendLimit the number of undetermined events required to trigger suspension
         * @return modified builder
         */
        public Builder suspendLimit(int suspendLimit) {
            mSuspendLimit =  suspendLimit;
            return this;
        }



        /**
         * Specified whether babble loads peers from a file. This should always be true
         * @param loadPeers open peers from a file
         * @return modified builder
         */
        public Builder loadPeers(boolean loadPeers) {
            mLoadPeers = loadPeers;
            return this;
        }


        /**
         * Disables the babble service. This should always be true
         * @param noService disable babble service
         * @return modified builder
         */
        public Builder noService(boolean noService) {
            mNoService = noService;
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
         * @return the built {@link NodeConfig}
         */
        public NodeConfig build() {
            return new NodeConfig(this);
        }
    }


    //TODO: JavaDoc
    public final String signalAddr; //signal Address
    public final boolean webrtc; //use webrtc



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
     * Bootstrap Babble from database
     */
    public final boolean bootstrap;   //bootstrap
    /**
     * Address to babble service listens to
     */
    public final String serviceListen;   //service-listen
    /**
     * Timeout for Join Requests
     */
    public final String joinTimeout;  //join_timeout
    /**
     * Set to true to enable Maintenance Mode to start Babble in a non-gossiping
     * suspended state.
     */
    public final boolean maintenanceMode;  //maintenance-mode
    /**
     * Number of undetermined events to trigger a suspend state
     */
    public final int suspendLimit;      //suspend-limit
    /**
     * Enable loading of peers from a file
     */
    public final boolean loadPeers;    //loadpeers
    /**
     * Disable the babble service
     */
    public final boolean noService;    //no-service


    /**
     * Enable fast sync
     */
    public final Boolean enableFastSync;  //enable fast sync

    private NodeConfig(Builder builder) {
        heartbeat = builder.mHeartbeat;
        slowHeartbeat = builder.mSlowHeartbeat;
        store = builder.mStore;
        logLevel = builder.mLogLevel;
        tcpTimeout = builder.mTcpTimeout;
        maxPool = builder.mMaxPool;
        cacheSize = builder.mCacheSize;
        syncLimit = builder.mSyncLimit;
        enableFastSync = builder.mEnableFastSync;
        bootstrap = builder.mBootstrap;
        serviceListen = builder.mServiceListen;
        joinTimeout= builder.mJoinTimeout;
        maintenanceMode = builder.mMaintenanceMode;
        suspendLimit = builder.mSuspendLimit;
        loadPeers = builder.mLoadPeers;
        noService = builder.mNoService;
        webrtc = builder.mWebRTC;
        signalAddr = builder.mSignalAddr;
    }


}
