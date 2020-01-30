# CHANGELOG

## Unreleased

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

