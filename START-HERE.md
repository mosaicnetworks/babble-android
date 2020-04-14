# Start Here

This document should be deleted if it makes it into the develop/master branch.

Whilst I know you will ignore me, for the moment, I would recommend looking at the ``sample_custom_ui`` app. This is a respin on the sample app with some complexities removed.

Moniker and discovery protocol are set in the hamburger menu. The new group button in the toolbar just uses the values set there with no opportunity to change. 

There is only a single live discovery panel --- there is not need for any more.

Archive is in another panel --- it is not currently wired up. 

I am wiring up the original sample app to use the same services etc, but I would really recommend just using the new app until the old one is similarly modified.


# Appendix

These notes were written mid development: 



These are rough notes to aid development of the refactored Babble-Android. They will need writing up properly. 

## BabbleServiceBinderActivity

Any Activity that uses the Babble Service will extend ``BabbleServiceBinderActivity``. This will include for the Sample App. 

Previously the sample app included the interface to the Babble service within the Fragment that was called - there was a different one for each discovery protocol. The reconciliation of the protocol code means that the invocation of Babble is the same for all protocols - the configuration files may differ - but that is encapsulated within the DiscoveryDataProvider objects. As such it seems sensible to pull the BabbleService2 interaction back to a single place on the Activity.  

Extending ``BabbleServiceBinderActivity`` gives you the heavy lifting for interfacing with the Babble Android Service. 

## sample_custom_ui

This is a new app without the baggage of the fragments that complicated the initial implementation. 

This app differs from the standard sample app by having a simpler UI. Both live and archive ServiceListViews are shown on screen at the same time. The New and Join Group screens have been removed. There is an option under the hamburger menu to set the moniker / change the protocol for a New Group. This removed the need to show a form before starting babble - it has just been replaced with a confirmation dialog. With the removal of the multiple forms, the need to replace fragments has been removed - so there are no fragments at all. 

The Moniker is stored as a shared preference. If none is set the set moniker screen is shown on start up. 

There is not floating plus button for a new group - it has been moved into the toolbar. 


## Initialisation

We need to initialise BabbleConstants. It has some default values that require a context- so it is usually easiest to call it from the Activity. 

```java
BabbleConstants.initialise(this);
```
Assuming the layout XML has been expanded in the onCreate, we need to create a reference to the ``ServicesListView`` component so we can get a reference to its resolvedGroupList. The Resolved Group List reference is unaltered throughout the discovery process - although the list itself will be cleared down and repopulated as required by the discovery protocols. Never assign the group list to a new object instance - it will break the linkage between the discovery components.

```java
ServicesListView servicesListView = findViewById(R.id.servicesListView);
mResolvedGroups =  servicesListView.getResolvedGroupList();
```

We create the ResolvedGroupManager, and link it to the ServicesListView component. The ResolvedGroupManager is responsible for collating the different DiscoveryDataProvider results into a single result set. 
 
```java
mResolvedGroupManager = new ResolvedGroupManager(this, mResolvedGroups);
mResolvedGroupManager.registerServicesListUpdater(servicesListView);
```

We create a DiscoveryDataController to manage all of the DiscoveryDataProviders, linking it the ResolvedGroupManager and registering callbacks to the activity. N.B. the moniker is set at this time (if the moniker is changed, it is updated when it is). We set the moniker early because it is cleaner to set it in one place than to pass it through each of the new and joining methods. 

The ``JoinGroupConfirmation`` is a callback to confirm that the user wants to join a group after selecting it. 

The ``OnBabbleConfigWritten`` interface provides a method to start babble after its config has been written as the name suggests.

```java
mDiscoveryDataController = new DiscoveryDataController(this, mResolvedGroupManager);
mDiscoveryDataController.registerJoinGroupConfirmation(this);
mDiscoveryDataController.registerOnBabbleConfigWritten(this);
mDiscoveryDataController.setMoniker(mMoniker);
```

Register the DDC as a ServicesListListener. 
```java
servicesListView.registerServicesListListener(mDiscoveryDataController);
```

Create and register DiscoveryDataProviders. In this case mDNS and WebRTC.
```java
MdnsDataProvider mdnsDataProvider = new MdnsDataProvider(this);
String uidMdns = mDiscoveryDataController.registerDiscoveryProvider(mdnsDataProvider);

WebRTCDataProvider webRTCDataProvider = new WebRTCDataProvider(this,
                BabbleConstants.DISCO_DISCOVERY_ADDRESS(), 
                BabbleConstants.DISCO_DISCOVERY_PORT(),
                BabbleConstants.DISCO_DISCOVERY_ENDPOINT(),
                BabbleConstants.DISCO_DISCOVERY_POLLING_INTERVAL());
String uidWebRTC = mDiscoveryDataController.registerDiscoveryProvider(webRTCDataProvider);
```

Then we start discovery.
```
mDiscoveryDataController.startDiscovery();
```

## DNS TXT records

There are constants defined in ``BabbleConstants.java``. They should be used for all discovery and advertiser processes and this guarantee consistency.

```java
    public final static String DNS_TXT_HOST_LABEL = "host";
    public final static String DNS_TXT_PORT_LABEL = "port";
    public final static String DNS_TXT_MONIKER_LABEL = "moniker";
    public final static String DNS_TXT_DNS_VERSION_LABEL = "textvers";
    public final static String DNS_TXT_BABBLE_VERSION_LABEL = "babblevers";
    public final static String DNS_TXT_GROUP_ID_LABEL = "groupUid";
    public final static String DNS_TXT_APP_LABEL = "appIdentifier";
    public final static String DNS_TXT_GROUP_LABEL = "groupName";
    public final static String DNS_TXT_CURRENT_PEERS_LABEL = "peers";
    public final static String DNS_TXT_INITIAL_PEERS_LABEL = "initpeers";
```

## Discovery

We now have a single ResolvedGroup type, which contains a single ResolvedService type. The use of which has been extended to be used for new groups too -- we create a ResolvedGroup dynamically -- then the workflow matches the Join workflow. Each discovery protocol has a factory class: ``ResolvedServiceXxxxFactory`` where ``Xxxx`` is the protocol name. 

Given that we always now have ResolvedGroup instance available. The GroupDescription object has become redundant and has been removed / replaced by ResolvedGroup. 



