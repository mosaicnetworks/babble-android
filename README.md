# BABBLE-ANDROID

## This repo is currently in the process of being published
## It may be unstable during this process

**NOTE**:
This is alpha software. Breaking changes are likely to be made as the interfaces
are refined.

Babble-Android allows developers to easily integrate
[Babble](https://github.com/mosaicnetworks/babble) into their own apps.

## Quickstart

This section gives a brief overview of how the library can be used. To see the
code snippets in context, take a look at the sample app in this repo. A complete
API reference can be found in the documentation. 

Add the following to your apps `build.gradle` file:

```implementation location/on/jcenter```

### Library Components

#### Babble Node

The core component of the library is the BabbleNode class. This is a wrapper
around our golang implementation of [Babble](https://github.com/mosaicnetworks/babble
). To start a node create a BabbleNode instance, then call run:

```
BabbleNode babbleNode = new BabbleNode(peersJSON, privateKeyHex, netAddr,
	moniker, new BabbleNodeListeners() {
    @Override
    public void onException(String msg) {
    	//do something if babble throws an exception
    }

    @Override
    public byte[] onReceiveTransactions(byte[][] transactions) {
    	//process transactions which have gone through consensus

    	//return the state hash
        return new byte[0];
    }
});

babbleNode.run();
```

The `peersJSON` String is a list of the group's current validators. It has the
form:

```
[{
	"NetAddr": "192.168.1.1:6666",
	"PubKeyHex": "0X04362B55F78A2614DC1B5FD3AC90A3162E213CC0F07925AC99E420722CDF3C656AE7BB88A0FEDF01DDD8669E159F9DC20CC5F253AC11F8B5AC2E10A30D0654873B",
	"Moniker": "mosaic"
}]
```

If you're starting a new group then the ```peersJSON``` string should contain a
single entry with your node's parameters. If you're joining a pre existing
group, then the list should consist of the current group validators (do not
include your node in the list), Babble will then handle joining the group.

All being well, the node will be running and transactions (a byte array) can be
submitted to the node by a simple call:

```
babbleNode.submitTx(tx)
```

Once the Babble nodes come to consensus on a set of transactions the
```onReceiveTransactions``` callback will be run. This method returns the
"stateHash" as an array of bytes. The state hash is used to verify the state of
the app. It should have all the properties of a [cryptographic hash function](https://en.wikipedia.org/wiki/Cryptographic_hash_function).
If you're not concerned with verifying the state of the app, then the state hash
can essentially be ignored, for example, you could return a constant byte.

Once you're finished with the Babble node, shut it down, this will ensure it
releases all it's resources:

```babbleNode.shutdown()```


TODO: Which thread callbacks run

#### KeyPair

The KeyPair class allows you to generate Babble keys:

```
keyPair = new KeyPair();
String publicKey = keyPair.publicKey;
String privateKey = keyPair.privateKey;
```

#### Peer Discovery

It is up to the app developer to decide how the app will discover the current
list of validators. So for example, one app may choose to hard code the list and
have a fixed set of validators, another app might choose to share the list over
bluetooth while another may choose to advertise the list on the Monet hub.

Although the choice of how to discover peers is left to the developer, we have
included an HTTP discovery procedure as a simple way to get started.

##### HTTP Discovery Server

A discovery server sets up a RESTful web service with a single, `/peers`,
endpoint which can be queried by other devices to get the peersJSON string.

```
HTTPDiscoveryServer httpDiscoveryServer = new HTTPDiscoveryServer(hostname,
        port, new PeersGetter() {
    @Override
    public String getPeers() {
        return null;
    }
});

httpDiscoveryServer.start()
```

Call ```httpDiscoveryServer.stop()``` to stop the service and release the
resources.

TODO: Which thread callbacks run

##### HTTP Discovery Request

The HTTPDiscoveryRequest class complements the HTTPDiscoveryServer by providing
a wrapper around an HTTP request to a `/peers` endpoint.

```
HTTPDiscoveryRequest httpDiscoveryRequest = new HTTPDiscoveryRequest(url, 
        new ResponseListener() {
    @Override
    public void onReceivePeers(Peer[] peers) {
        //do something with the array of peers
    }
}, new FailureListener() {
	@Override
    public void onFailure(int code) {
        //handle failure to get peers list
    }
});
        
httpDiscoveryRequest.send();
```

Where the url should be that of the peers resource on the discovery server e.g.
if the discovery server has the hostname `192.168.2.1` then the url of the
peers resource is `http://192.168.2.1/peers`

TODO: Which thread callbacks run

## Library Development

If you'd like to develop on and build the library locally, you'll need to
install the android SDK then clone this repository.

To build the library either use a suitable IDE, such as Android Studio or run
gradlew from the command line, with the appropriate sub-commands.
