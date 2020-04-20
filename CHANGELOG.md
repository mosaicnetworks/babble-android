# CHANGELOG

## UNRELEASED

SECURITY:

FEATURES:

- Added ability to communicate over webrtc

IMPROVEMENTS:

BUG FIXES:

- Eliminated various bugs releated to restarting activities


## v0.4.3 (March 23, 2020)

SECURITY:

FEATURES:

- Sample App: Added hamburger menu to chat screen to show additional information and status 
- Sample App: Added hamburger menu to front screen to display version information
- Parameters: Added babble.xml to allow overriding of some or all parameters in an app. 

IMPROVEMENTS:

- Babble Library: Implemented BabbleService as an Android Service
- Sample App: added title to the chat screen
- Sample App: changed colours from teal

BUG FIXES:


  
## v0.4.2 (March 6, 2020)

SECURITY:

FEATURES:

IMPROVEMENTS:

- archive: Maintain group selections through configuration changes
- archive: Remove contextual action bar when changing to another tab

BUG FIXES:

- archive: Select only one group on long click. Previously when long clicking
  an archived group, every n'th group would be selected

## v0.4.1 (February 28, 2020)

SECURITY:

FEATURES:

- P2P (Wifi Direct) support added

IMPROVEMENTS:

- Added avatars to the sample add
- Hid system user name in sample chat announcements
- Updated versions of library dependencies

BUG FIXES:

- mDNS fix for Android versions lower then 7.0 which could not discover due to an Android bug
- fix bug that could cause a crash on changing orientation in the chat screen of the sample app

## v0.4.0 (February 21, 2020)

SECURITY:

FEATURES:

IMPROVEMENTS:

- group UID is now displayed in the live groups list
- added group UID to archive view and changed text colour of backup archives to light gray
- display a message if archive is empty in Sample app ChatActivity
- display message if no archives found
- add swipe refresh on mDNS service search

BUG FIXES:

- Fix bug when refreshing after mDNS start failure

## v0.3.1 (February 10, 2020)

SECURITY:

FEATURES:

IMPROVEMENTS:

- babble library: renamed configuration functions to be less ambiguous
- discovery: The NSD component now uses a string of random digits as the advertised
  group name and contains a TXT field with the user defined group name, group UID and
  app UID. This enables a set of advertised services to be partitioned into sets of
  services which represent the same group.
- archive: Amended to show all versions
- configuration: Added global config options to ConfigManager with sample code in comments in
  sample/MainActivity.java
- sample app: Added labels to other person's speech to highlight who said what.
- sample app: Added announcements when people join and leave

BUG FIXES:

- babble: Add support for build target API 29 (Android Q) which previously failed


## v0.3.0 (January 31, 2020)

SECURITY:

FEATURES:

- servicediscovery: Added mDNS service discovery and advertising.
- configuration: Modified BabbleService to read configuration from a directory.
- configuration: Added a configuration manager to do CRUD of configuration
  directories.
- archive: Added archive views to allow end users to interact with saved group
  configurations.

IMPROVEMENTS:

- babble: BREAKING CHANGES - CommitHandler, Consumer, and AppState.
          deal with Blocks instead of transactions. 
- babble: configurable slow-heartbeat.

BUG FIXES:

