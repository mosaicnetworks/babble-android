# Requirements

This document is a temporary document to capture requirements. 


## Wifi Direct

[WiFi Direct tutorial](https://developer.android.com/training/connect-devices-wirelessly/wifi-direct)

>In order to use Wi-Fi P2P, add the ACCESS_FINE_LOCATION, CHANGE_WIFI_STATE, ACCESS_WIFI_STATE, and INTERNET permissions to your manifest. Wi-Fi P2P doesn't require an internet connection, but it does use standard Java sockets, which requires the INTERNET permission. So you need the following permissions to use Wi-Fi P2P:

```xml
	<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	    package="com.example.android.nsdchat"
	    ...
	    <uses-permission
	        android:required="true"
	        android:name="android.permission.ACCESS_FINE_LOCATION"/>
	    <uses-permission
	        android:required="true"
	        android:name="android.permission.ACCESS_WIFI_STATE"/>
	    <uses-permission
	        android:required="true"
	        android:name="android.permission.CHANGE_WIFI_STATE"/>
	    <uses-permission
	        android:required="true"
	        android:name="android.permission.INTERNET"/>
	    ...
```
	    