# CHANGELOG

## TBA (February, 2020)

SECURITY:

FEATURES:

IMPROVEMENTS:

- The NSD component now uses a string of random digits as the advertised group name
  and contains a TXT field with the user defined group name, group UID and app UID.
  This enables a set of advertised services to be partitioned into sets of
  services which represent the same group.

BUG FIXES:

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

