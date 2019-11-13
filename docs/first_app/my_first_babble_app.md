# My First Babble App Tutorial

This article is a walkthrough of the process of building your first Android App using the babble-android library.

## Babble

Babble, mobile ad hoc block chain, yadda, yadda, yadda. **//TODO**

## Prerequisites

We will assume that you have installed Android Studio, an appropriate Android SDK (I am using version 29) and Android NDK. 

This tutorial is going to assume deployment to a physical Android device. Thus you will need an Android device (Android device requirements **//TODO**) with the developer options turned on, debugging enabled, and a suitable USB cable. You could use the android emulator, but that is beyond the scope of this article.

## Our First Minimal App

First up we will create a minimal app to test our environment and prove that we have loaded the ``babble-android`` library correctly. Fire up Android Studio and select ``File/New Project``. You are asked to choose a project template.

![ ](./screenshots/create_new_project.png  "Create New Project Screen"){width=80%}

Choose *Empty Activity* from the *Phone and Tablet* tab, and click Next.

![ ](./screenshots/new_project_options.png  "New Project Options"){width=80%}

The options here should be self-explanatory. We would recommend not using spaces in the Package Name or the Save Location. Do not set the Minimum API Level below 19. Click Finish.


![](./screenshots/empty_android_studio.png "Initial Android Studio")

Android Studio will open, and after expanding some of the menus it will look something like above. 

*N.B. the screen will change a few seconds after opening when the initial gradle scripts complete. The status bar at the bottom of the window should tell you this is happening.*

Connect your android device to your computer via a USB cable. First we will test that the Android Debug Bridge (**adb**) can see the device. 
```bash
jon@hpjon:~/Android/MyFirstApp$ adb devices
List of devices attached
4JPNU18709118621	device
jon@hpjon:~/Android/MyFirstApp$ 
```

The command ``adb kill-server`` will reset this connection. 

If you can see a device listed, go back to Android Studio. and in the top right is a target device dropdown. Select your device from the list. 

![](./screenshots/device_selection.png "Select Device")

If you cannot find your device in the list, the ``Troubleshoot device connections`` option on that menu should help.

Then press that little green triangle to the right of the dropdown device menu. Gradle then builds the app, which is then installed onto the physical android device that you selected. The whole process tool about 20 seconds on my laptop (feel free to buy me a quicker one). 

![](./screenshots/mobile_step_1.png "First App Screenshot"){width=50%}

If you look on the device, you should find the app installed, as below:

![](./screenshots/default_icon.png "First App Icon"){width=25%}

Congratulations, you have an app. Now onto Babble...

## Our First Babble App 

We will now integrate the ``babble-android`` library into our skeleton app. We will use it to generate a key pair -- just to prove that we have a working library instance. 

The library is hosted **jcenter**. To make it available, we need to amend some gradle scripts.

![](./screenshots/gradle_scripts.png "First App Icon"){width=50%}

 In Android Studio, double click on the Project ``build.gradle`` as highlighted in the screenshot above. 
 
We then add the 3 line ``maven`` instruction as below:

```gradle 
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url 'https://dl.bintray.com/mosaicnetworks/maven'
        }
    }
} 
```

Which leaves the entire file looking like this:
 
```gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.2'
        
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url 'https://dl.bintray.com/mosaicnetworks/maven'
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
 
```

Next we need to amend the app ``build.gradle`` (it is below the Project ``build.gradle`` in the screenshot above. We add an implementation line to the bottom dependencies section.

```gradle
implementation 'io.mosaicnetworks:babble:0.1.0'
```

This leaves us with this full file:

```gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion 29
    buildToolsVersion "29.0.2"
    defaultConfig {
        applicationId "io.mosaicnetworks.myfirstapp"
        minSdkVersion 19
        targetSdkVersion 29
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'io.mosaicnetworks:babble:0.1.0'
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
}
```


![](./screenshots/sync_message.png "Sync Message")

When you get a message like the above, click the ``Sync Now`` link on the right of the message.

The library should now be included in the project. So lets use it! Open ``MainActivity.java`` as below:

![](./screenshots/main_activity.png "Main Activity")

Add the lines below underneath the last import statement. The lines will appear greyed out, as the import is not yet used. As well as babble we are importing the ``Log`` package to write to the Android logs. 

```java
import io.mosaicnetworks.babble.node.KeyPair;
import android.util.Log;
```

Add the following lines as the last line of the ``onCreate`` function 
```java
KeyPair kp = new KeyPair();
Log.i("Yippee",kp.privateKey);
```

This code generates a key pair and writes the private code to the logs. 

Save all the files and run your app. 

![](./screenshots/logcat.png "LogCat")

The app looks exactly as per the previous iteration, so lets take a look under the hood. Press logcat, as highlighted in gray in the screenshot above. Then type ``yippee`` in the search box at the top of that window to filter the logs. You should have a freshly generated private key in there. 
