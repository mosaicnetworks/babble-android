

# Dependencies

Install Android Studio. I installed version 3.5.2.0 via the `Ubuntu Software` app, which installs a `snap` version. This is a 700Mb download, and snap will likely keep 2 or 3 version of it. 

If there is no prepackaged version for your OS, you can download it directly from [here](https://developer.android.com/studio).

Set ANDROID_HOME environment variable. Add it to ``.bashrc`` or ``.profile``. 

Accept the Android SDK licenses:

```bash
jon@hpjon:~/Android$ $ANDROID_HOME/tools/bin/sdkmanager --licenses
Warning: File /home/jon/.android/repositories.cfg could not be loaded.          
5 of 5 SDK package licenses not accepted. 100% Computing updates...             
Review licenses that have not been accepted (y/N)? y

...
```

If you then launch Android Studio, it gives a warning that no SDK has been found and gives you the option to install one. I accepted the defaults and it installed Android 29. 




----

Clone the repository:

```bash
jon@hpjon:~$ mkdir Android
jon@hpjon:~$ cd Android
jon@hpjon:~/Android$ git clone https://github.com/mosaicnetworks/babble-android.git
Cloning into 'babble-android'...
remote: Enumerating objects: 745, done.
remote: Counting objects: 100% (745/745), done.
remote: Compressing objects: 100% (344/344), done.
remote: Total 745 (delta 251), reused 675 (delta 181), pack-reused 0
Receiving objects: 100% (745/745), 35.61 MiB | 7.27 MiB/s, done.
Resolving deltas: 100% (251/251), done.
```

----

Open the babble-android project within Android Studio. 


## Build the aar library

First some dependencies:

Install the Android NDK. Instructions are [here](https://developer.android.com/ndk/guides/index.html).

In Android Studio, Tools/SDK Manager. Under the SDK Tools tab, choose to install LLDB, NDK(Side by side) and CMake. 

In a terminal session, clone Babble. 

```bash
~/Android/Sdk/ndk$ ln -s 20.0.5594570/ latest

$ go get golang.org/x/mobile/cmd/gomobile
$ go get golang.org/x/tools/go/packages
$
$ cd [...babble]/src/mobile

$ export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/latest
$ gomobile init 
$ gomobile bind -v -target=android -tags=mobile .
$ mkdir -p build/test ; mv mobile.aar mobile-sources.jar build/test
``

```bash
jon@hpjon:~/Android/babble-android/babble/libs$ bash ./bbc.sh 
Archive:  mobile.aar
  inflating: mobile.aar.unzip/AndroidManifest.xml  
  inflating: mobile.aar.unzip/proguard.txt  
  inflating: mobile.aar.unzip/classes.jar  
  inflating: mobile.aar.unzip/jni/armeabi-v7a/libgojni.so  
  inflating: mobile.aar.unzip/jni/arm64-v8a/libgojni.so  
  inflating: mobile.aar.unzip/jni/x86/libgojni.so  
  inflating: mobile.aar.unzip/jni/x86_64/libgojni.so  
  inflating: mobile.aar.unzip/R.txt  
   creating: mobile.aar.unzip/res/
```


`

