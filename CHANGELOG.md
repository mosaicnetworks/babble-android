# CHANGELOG

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

