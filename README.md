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

```implementation 'io.mosaicnetworks:babble:0.4.3'```

and start coding!

For the very impatient we've added the sample app to the google play store. Install
it to get a quick feel for what babble can be used for.

<a href='https://play.google.com/store/apps/details?id=io.mosaicnetworks.sample&hl=en_GB'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80px'/></a>

## Library structure

At the core of the library is the BabbleNode class. This is a
wrapper around our [golang implementation of Babble](https://github.com/mosaicnetworks/babble).
However we don't recommend you use this class
directly (not initially anyway). We've included a number of ancillary
classes in the library, which can be used as scaffolding so that you can
focus on building your app logic. These components can easily be swapped
out with your own custom implementations later on. Take a look at the
[sample app](https://github.com/mosaicnetworks/babble-android/tree/master/sample)
to see how to use these classes.

## Library Development

If you want to build the library you'll need the Android SDK. This can be installed
as part of an [Android Studio](https://developer.android.com/studio)
install, or alternatively you can download and install the
[command line tools](https://developer.android.com/studio/index.html#command-tools)
only.

The library can then be built, either by calling the relevant [gradle](https://gradle.org/)
commands from the command line or from within Android Studio.
