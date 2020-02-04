# CHANGELOG

## TBA (February, 2020)

SECURITY:

FEATURES:

- archive: Amended to show all versions
- configuration: Added global config options to ConfigManager with sample code in comments in
  sample/MainActivity.java
- sample app: Added labels to other person's speech to highlight who said what.
- sample app: Added announcements when people join and leave

IMPROVEMENTS:

- babble library: renamed configuration functions to be less ambiguous

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

