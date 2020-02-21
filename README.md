# BABBLE-ANDROID

**NOTE**:
This is alpha software. Breaking changes are likely to be made as the
interfaces are refined.

babble-android allows developers to easily integrate the
[Babble](https://github.com/mosaicnetworks/babble) consensus engine
into their Android apps.

A complete API reference can be found [here](https://javadoc.io/doc/io.mosaicnetworks/babble/latest/index.html)

A tutorial can be found [here](https://android.babble.io)

## Quickstart

The best place to start is [this](https://github.com/mosaicnetworks/babble-android/tree/master/docs/first_app) comprehensive tutorial,
which guides you through building the [sample app](https://github.com/mosaicnetworks/babble-android/tree/master/sample).

For the impatient, add the following to your app's `build.gradle` file:

```implementation 'io.mosaicnetworks:babble:0.4.0'```

and start coding!

## Library structure

At the core of the library is the BabbleNode class. This is a
wrapper around our [golang implementation of Babble](https://github.com/mosaicnetworks/babble).
However we don't recommend you use this class
directly (not initially anyway). We've included a number of ancillary
classes in the library, which can be used as scaffolding so that you can
focus on building your app logic. These components can easily be swapped
out with your own custom implementations later on.

The scaffolding architecture is shown below.

![alt text](https://github.com/mosaicnetworks/babble-android/blob/master/pics/android-architecture.svg "Scaffold app architecture")

There are five classes which you will need to
implement, these will either extend classes or implement interfaces from
the library. For a comprehensive tutorial on writing these classes see
[here](https://github.com/mosaicnetworks/babble-android/tree/master/docs/first_app).

## Library Development

Building the library requires the Android SDK. This can be installed
as part of an [Android Studio](https://developer.android.com/studio)
install, or alternatively you can download and install the
[command line tools](https://developer.android.com/studio/index.html#command-tools)
only.

The library can then be built, either by calling the relevant [gradle](https://gradle.org/)
commands from the command line or from within Android Studio.
