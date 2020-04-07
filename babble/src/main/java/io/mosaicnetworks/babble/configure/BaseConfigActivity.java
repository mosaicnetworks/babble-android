/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.babble.configure;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.discovery.DiscoveryDataController;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.service.BabbleService2;
import io.mosaicnetworks.babble.service.BabbleServiceBinderActivity;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.JoinGroupConfirmation;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.utils.DialogUtils;

/**
 * This activity complements the {@link BabbleService}. It consists of a set of fragments which
 * allow the {@link BabbleService} to be configured. Extend class and override the
 * {@link BaseConfigActivity#getBabbleService()}, {@link BaseConfigActivity#onJoined(String)} and
 * {@link BaseConfigActivity#onStartedNew(String)} methods.
 */
public abstract class BaseConfigActivity extends BabbleServiceBinderActivity implements OnBabbleConfigWritten, OnFragmentInteractionListener, JoinGroupConfirmation {

    /**
     * Key for the bundle used to pass the visibility flag for the mDNS tab to the fragment
     */
    public static final String SHOW_MDNS ="SHOW_MDNS";


    /**
     * Key for the bundle used to pass the visibility flag for the mDNS tab to the fragment
     */
    public static final String SHOW_COMBINED ="SHOW_COMBINED";



    /**
     * Key for the bundle used to pass the visibility flag for the P2P tab to the fragment
     */
    public static final String SHOW_P2P ="SHOW_P2P";

    /**
     * Key for the bundle used to pass the visibility flag for the Archive Tab to the fragment
     */
    public static final String SHOW_ARCHIVE ="SHOW_ARCHIVE";

    /**
     * Key for the bundle used to pass the flag for showing all versions in the Archive Tab to the fragment
     */
    public static final String SHOW_ALL_ARCHIVE ="SHOW_ALL_ARCHIVE";


    private FragmentManager mFragmentManager;
    private Boolean mFromGroup = false;
    private boolean mShowmDNS = true;
    private boolean mShowP2P = false;
    private int mProtocol = BabbleConstants.NETWORK_GLOBAL;


    private boolean mShowCombined = true;
    private boolean mShowArchive = true;
    private boolean mTooLateToChangeShowTabs = false;
    private boolean mShowAllArchiveVersions = true;
    private List<ResolvedGroup> mResolvedGroups;
    private ResolvedGroupManager mResolvedGroupManager;
    private DiscoveryDataController mDiscoveryDataController;
    private ServiceAdvertiser mServiceAdvertiser;

    private String mConfigDirectory;
    private boolean mIsArchive = false;
    private ProgressDialog mLoadingDialog;
    private ResolvedGroup mResolvedGroup;



    public static final String PREFERENCE_FILE_KEY = "babbleandroid";
    private static final String TAG = "BaseConfigActivity";


    /**
     * Controls whether to show the Combined tab. Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showCombined true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    public void setShowCombined(boolean showCombined) {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        this.mShowCombined = showCombined;
    }



    /**
     * Controls whether to show the mDNS tab. Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showmDNS true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected void setShowmDNS(boolean showmDNS) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        mShowmDNS = showmDNS;
    }

    /**
     * Controls whether to show the P2P tab. Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showP2P true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected void setShowP2P(boolean showP2P) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        mShowP2P = showP2P;
    }

    /**
     * Controls whether to show the Archive tab. Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showArchive true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected void setShowArchive(boolean showArchive) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        mShowArchive = showArchive;
    }

    /**
     * Controls whether to show all archive versions of group.  Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showAllArchiveVersions true to show all archive versions of group, false for just the latest ones
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    public void setShowAllArchiveVersions(boolean showAllArchiveVersions)  throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        mShowAllArchiveVersions = showAllArchiveVersions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_config);

// BEGIN general initialisation block
    // This block contains some code that will need to be run if using the library without
    // the standard UX.
        BabbleConstants.initialise(this);

        // We create an array list which we need to set on the ServiceListView when it is created.
        mResolvedGroups = new ArrayList<>();

        // servicesListView.getResolvedGroupList(mResolvedGroups);


        /*
        ServicesListView servicesListView = findViewById(R.id.servicesListView);
        Log.i(TAG,"  Get ResolvedGroup List from ServicesListView");
        mResolvedGroups =  servicesListView.getResolvedGroupList();
        mResolvedGroupManager.registerServicesListUpdater(servicesListView);
        servicesListView.registerServicesListListener(mDiscoveryDataController);

         */

        Log.i(TAG,"  Create ResolvedGroupManager");
        mResolvedGroupManager = new ResolvedGroupManager(this, mResolvedGroups);
        Log.i(TAG,"  Create DiscoveryDataController");
        mDiscoveryDataController = new DiscoveryDataController(this, mResolvedGroupManager);
        Log.i(TAG,"  Register with DiscoveryTestActivity as JoinGroupConfirmation");
        mDiscoveryDataController.registerJoinGroupConfirmation(this);
        Log.i(TAG,"  Register with DiscoveryTestActivity as OnFragmentInteractionListener(");
        mDiscoveryDataController.registerOnBabbleConfigWritten(this);

        Log.i(TAG,"END setUpBabble()");
// END general initialisation block

        mFragmentManager = getSupportFragmentManager();

        //"When a config change occurs the old Fragment adds itself to the new Activity when it's
        //recreated". - https://stackoverflow.com/questions/8474104/android-fragment-lifecycle-over-orientation-changes
        //Check if fragment is already added to avoid attaching multiple instances of the fragment
        TabsFragment fragment = (TabsFragment) mFragmentManager.findFragmentById(R.id.constraint_layout);
        if (fragment == null) {
            mTooLateToChangeShowTabs = true;
            Bundle bundle = new Bundle();
            bundle.putBoolean(SHOW_MDNS, mShowmDNS);
            bundle.putBoolean(SHOW_P2P, mShowP2P);
            bundle.putBoolean(SHOW_COMBINED, mShowCombined);
            bundle.putBoolean(SHOW_ARCHIVE, mShowArchive);
            bundle.putBoolean(SHOW_ALL_ARCHIVE, mShowAllArchiveVersions);
            addFragment(TabsFragment.newInstance(bundle));
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.constraint_layout, fragment);
        fragmentTransaction.commit();
    }

    private void removeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment, Boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.constraint_layout, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    // called when the user presses the new group (plus) button
    public void newGroup(View view) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SHOW_MDNS, mShowmDNS);
        bundle.putBoolean(SHOW_P2P, mShowP2P);
        NewGroupFragment mNewGroupFragment = NewGroupFragment.newInstance(bundle);
        replaceFragment(mNewGroupFragment, true);
    }

    @Override
    public void onServiceSelected(ResolvedGroup resolvedGroup) {
        // The different join fragments have been replaced by a single fragment //TODO: delete this comment
        Log.i(TAG, "onServiceSelected: Item selected");
        JoinGroupFragment joinGroupFragment = JoinGroupFragment.newInstance(resolvedGroup);
        replaceFragment(joinGroupFragment, true);
    }

    @Override
    public void baseOnJoined(String moniker, String group) {
        onJoined(moniker, group);
        mFromGroup = true;
    }

    @Override
    public void baseOnStartedNew(String moniker, String group) {
        onStartedNew(moniker, group);
        mFromGroup = true;
    }

    /*
    @Override
    protected void onStop() {
        super.onStop();

        if (mFromGroup) {
            mFragmentManager.popBackStack();
        }
    }

     */

    @Override
    protected void onStart() {
        super.onStart();

        if (mFromGroup) {
            mFragmentManager.popBackStack();
            ArchivedGroupsFragment.reloadArchive = true;
            mFromGroup = false;
        }
    }



    public abstract void onJoined(String moniker, String group);

    public abstract void onStartedNew(String moniker, String group);

    @Override
    public abstract void onArchiveLoaded(String moniker, String group);


    @Override
    protected void onServiceDisconnected() {
        //Do nothing
    }




/*
    private void configAndStartBabble(String peersAddr, String babbleAddr)  {
        ConfigManager configManager =
                ConfigManager.getInstance(getApplicationContext());
        try {
            mConfigDirectory = configManager.createConfigNewGroup(mResolvedGroup, peersAddr, babbleAddr, mProtocol);
        } catch (CannotStartBabbleNodeException | IOException ex) {
            //TODO: think about this error handling
        }
        startBabbleAndroidService();
    }
*/


    @Override
    public void startBabbleService(String configDir, ResolvedGroup resolvedGroup, boolean isArchive, ServiceAdvertiser serviceAdvertiser) {

        mConfigDirectory = configDir;
        mResolvedGroup = resolvedGroup;
        mIsArchive = isArchive;
        mServiceAdvertiser = serviceAdvertiser;

        startBabbleAndroidService();
    }


    public void startBabbleAndroidService() {
        startService(new Intent(this, BabbleService2.class));
        mLoadingDialog = DialogUtils.displayLoadingDialog(this);
        mLoadingDialog.show();
        doBindService();
    }

    /**
     * This method launches the activity that actually uses babble
     *
     */
    public abstract void startBabblingActivity();

    @Override
    protected void onServiceConnected() {
        try {
            mBoundService.start(mConfigDirectory, mResolvedGroup, mServiceAdvertiser);
            startBabblingActivity();
        } catch (IllegalArgumentException ex) {
            // we'll assume this is caused by the node taking a while to leave a previous group,
            // though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            DialogUtils.displayOkAlertDialog(this, R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            mLoadingDialog.dismiss();
            stopService(new Intent(this, BabbleService2.class));
        }
        doUnbindService();
    }



    @Override
    public void joinRequested(DiscoveryDataController discoveryDataController, ResolvedGroup resolvedGroup) {
        //TODO: JK25Mar populate this
    }
}
