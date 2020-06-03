# BABBLE-ANDROID

**NOTE**:
This is alpha software. Breaking changes are likely to be made as the
interfaces are refined. Also there are a few areas which need some
attention:

* Documentation - only parts of the API have been documented.
* Code quality - some parts of the code need tidying up.
* Test coverage - we need to work towards much higher test coverage.

babble-android allows developers to easily integrate the
[Babble](https://github.com/mosaicnetworks/babble) consensus engine
into their Android apps.

A partial API reference can be found [here](https://javadoc.io/doc/io.mosaicnetworks/babble/latest/index.html)

## Quickstart

The best place to start is the [sample app](https://github.com/mosaicnetworks/babble-android/tree/master/sample). This is a demo app which shows how the
components in the library can be used to build a simple chat app.

For the impatient, add the following to your app's `build.gradle` file:

```implementation 'io.mosaicnetworks:babble:0.5.1'```

and start coding!

For the very impatient we've added the sample app to the google play store. Install
it to get a quick feel for what babble can be used for.

<a href='https://play.google.com/store/apps/details?id=io.mosaicnetworks.sample&hl=en_GB'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80px'/></a>

## Library Components

At the core of the library is the BabbleNode class. This is a
wrapper around our [golang implementation of Babble](https://github.com/mosaicnetworks/babble).
This class can be used directly or alternatively the BabbleService class can be used which essentially
wraps the BabbleNode class inside an Android Service.

Where devices have a publicly accessible IP address they can be configured to use these addresses
to communicate directly. However when this is not the case, in particular when devices are behind
a NAT and do not have publicly accessible IP addresses, the nodes can be configured to use WebRTC
In this case the BabbleNode instance is provided with the public keys of the peer nodes and the
address of a signaling server.

Whether using WebRTC or public IP addresses, the devices which make up a network will need to be passed to
the BabbleNode instance.
Unless you intend to hard code this information into your app (which works fine for a fixed set of static groups),
the app will need to discover groups and retrieve this information. We've included an MdnsDiscovery
class to enable devices on the same local network to discover
each other. MDNS is not suitable in use cases where WebRTC would be used, in this case we have a
WebRTCService class which uses a discovery server to discover groups.

Once the discovery procedure has retrieved the peer information this needs to be written to
configuration files on the device. A ConfigManager class has been included which takes care
of writing the necessary configuration files.

We've also included some pre-baked UI components that essentially wrap up the two
discovery mechanisms allowing you to focus on building your specific app logic.

Take a look at the
[sample app](https://github.com/mosaicnetworks/babble-android/tree/master/sample)
to see one way the various components can be used to build a real app.


## Library Development

If you want to build the library you'll need the Android SDK. This can be installed
as part of an [Android Studio](https://developer.android.com/studio)
install, or alternatively you can download and install the
[command line tools](https://developer.android.com/studio/index.html#command-tools)
only.

The library can then be built, either by calling the relevant [gradle](https://gradle.org/)
commands from the command line or from within Android Studio.
