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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.configure.mdns.MdnsJoinGroupFragment;
import io.mosaicnetworks.babble.configure.p2p.P2PJoinGroupFragment;
import io.mosaicnetworks.babble.configure.webrtc.WebRTCJoinGroupFragment;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PResolvedGroup;

/**
 * This activity complements the {@link BabbleService}. It consists of a set of fragments which
 * allow the {@link BabbleService} to be configured. Extend class and override the
 * {@link BaseConfigActivity#getBabbleService()}, {@link BaseConfigActivity#onJoined(String, String)} and
 * {@link BaseConfigActivity#onStartedNew(String, String)} methods.
 */
public abstract class BaseConfigActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    /**
     * Key for the bundle used to pass the visibility flag for the mDNS tab to the fragment
     */
    public static final String SHOW_MDNS ="SHOW_MDNS";

    /**
     * Key for the bundle used to pass the visibility flag for the P2P tab to the fragment
     */
    public static final String SHOW_P2P ="SHOW_P2P";

    /**
     * Key for the bundle used to pass the visibility flag for the Global tab to the fragment
     */
    public static final String SHOW_GLOBAL ="SHOW_GLOBAL";

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
    private boolean mShowP2P = true;
    private boolean mShowGlobal = true;
    private boolean mShowArchive = true;
    private boolean mTooLateToChangeShowTabs = false;
    private boolean mShowAllArchiveVersions = true;
    public static final String PREFERENCE_FILE_KEY = "babbleandroid";
    private static final String TAG = "BaseConfigActivity";

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
     * Controls whether to show the Global tab. Must be called before
     * {@link BaseConfigActivity#onCreate(Bundle)} method is called as the parameters are used in
     * {@link BaseConfigActivity#onCreate(Bundle)}.
     * @param showGlobal true to show the global tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected void setShowGlobal(boolean showGlobal) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        mShowGlobal = showGlobal;
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

        mFragmentManager = getSupportFragmentManager();

        //"When a config change occurs the old Fragment adds itself to the new Activity when it's
        //recreated". - https://stackoverflow.com/questions/8474104/android-fragment-lifecycle-over-orientation-changes
        //Check if fragment is already added to avoid attaching multiple instances of the fragment
        Fragment fragment = mFragmentManager.findFragmentById(R.id.constraint_layout);
        if (fragment == null) {
            mTooLateToChangeShowTabs = true;
            Bundle bundle = new Bundle();
            bundle.putBoolean(SHOW_MDNS, mShowmDNS);
            bundle.putBoolean(SHOW_P2P, mShowP2P);
            bundle.putBoolean(SHOW_GLOBAL, mShowGlobal);
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

        if (resolvedGroup instanceof ResolvedGroup) {
            Log.i(TAG, "onServiceSelected: MDNS item selected");
            MdnsJoinGroupFragment mJoinGroupMdnsFragment = MdnsJoinGroupFragment.newInstance(resolvedGroup);
            replaceFragment(mJoinGroupMdnsFragment, true);
        } else if (resolvedGroup instanceof ResolvedGroup) {
            Log.i(TAG, "onServiceSelected: P2P item selected");
            //P2PJoinGroupFragment mJoinGroupP2PFragment = P2PJoinGroupFragment.newInstance(resolvedGroup);
            //replaceFragment(mJoinGroupP2PFragment, true);
        } else if (resolvedGroup instanceof ResolvedGroup) {
            Log.i(TAG, "onServiceSelected: WebRTC item selected");
            WebRTCJoinGroupFragment mJoinGroupWebRTCFragment = WebRTCJoinGroupFragment.newInstance(resolvedGroup);
            replaceFragment(mJoinGroupWebRTCFragment, true);
        }
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

    @Override
    public abstract BabbleService getBabbleService();

    public abstract void onJoined(String moniker, String group);

    public abstract void onStartedNew(String moniker, String group);

    @Override
    public abstract void onArchiveLoaded(String moniker, String group);

}
