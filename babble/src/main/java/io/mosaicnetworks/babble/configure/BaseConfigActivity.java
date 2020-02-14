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

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;

/**
 * This activity complements the {@link BabbleService}. It consists of a set of fragments which
 * allow the {@link BabbleService} to be configured. Extend class and override the
 * {@link BaseConfigActivity#getBabbleService()}, {@link BaseConfigActivity#onJoined(String)} and
 * {@link BaseConfigActivity#onStartedNew(String)} methods.
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
     * Key for the bundle used to pass the visibility flag for the Archive Tab to the fragment
     */
    public static final String SHOW_ARCHIVE ="SHOW_ARCHIVE";


    /**
     * Key for the bundle used to pass the flag for showing all versions in the Archive Tab to the fragment
     */
    public static final String SHOW_ALL_ARCHIVE ="SHOW_ALL_ARCHIVE";


    private FragmentManager mFragmentManager;
    public static final String PREFERENCE_FILE_KEY = "babbleandroid";


    /**
     * Controls whether to show the mDNS tab. Much be called in the constructor of the instance that
     * implements BaseConfigActivity as the parameter are used in BaseConfigActivity.onCreate.
     * @param showmDNS true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected static void setShowmDNS(boolean showmDNS) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        BaseConfigActivity.mShowmDNS = showmDNS;
    }

    /**
     * Controls whether to show the P2P tab. Much be called in the constructor of the instance that
     * implements BaseConfigActivity as the parameter are used in BaseConfigActivity.onCreate.
     * @param showP2P true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected static void setShowP2P(boolean showP2P) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        BaseConfigActivity.mShowP2P = showP2P;
    }

    /**
     * Controls whether to show the Archive tab. Much be called in the constructor of the instance that
     * implements BaseConfigActivity as the parameter are used in BaseConfigActivity.onCreate.
     * @param showArchive true to show the mDNS tab, false to hide
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    protected static void setShowArchive(boolean showArchive) throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        BaseConfigActivity.mShowArchive = showArchive;
    }

    /**
     * Controls whether to show all archive versions of group. Much be called in the constructor of the instance that
     * implements BaseConfigActivity as the parameter are used in BaseConfigActivity.onCreate.
     * @param showAllArchiveVersions true to show all archive versions of group, false for just the latest ones
     * @throws IllegalStateException if the OnCreate event handler has already been run
     */
    public static void setShowAllArchiveVersions(boolean showAllArchiveVersions)  throws IllegalStateException {
        if (mTooLateToChangeShowTabs) throw new IllegalStateException();
        BaseConfigActivity.mShowAllArchiveVersions = showAllArchiveVersions;
    }




    private static boolean mShowmDNS = true;
    private static boolean mShowP2P = true;
    private static boolean mShowArchive = true;
    private static boolean mTooLateToChangeShowTabs = false;

    private static boolean mShowAllArchiveVersions = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_config);

        mFragmentManager = getSupportFragmentManager();


        mTooLateToChangeShowTabs = true;
        Bundle bundle = new Bundle();
        bundle.putBoolean(SHOW_MDNS, mShowmDNS);
        bundle.putBoolean(SHOW_P2P, mShowP2P);
        bundle.putBoolean(SHOW_ARCHIVE, mShowArchive);
        bundle.putBoolean(SHOW_ALL_ARCHIVE, mShowAllArchiveVersions);

        TabsFragment mHomeFragment = TabsFragment.newInstance(bundle);

        addFragment(mHomeFragment);
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

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.constraint_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // called when the user presses the new group (plus) button
    public void newGroup(View view) {
        NewGroupFragment mNewGroupFragment = NewGroupFragment.newInstance();
        replaceFragment(mNewGroupFragment);
    }

    @Override
    public void onServiceSelected(NsdServiceInfo serviceInfo) {
        JoinGroupFragment mJoinGroupMdnsFragment = JoinGroupFragment.newInstance(serviceInfo);
        replaceFragment(mJoinGroupMdnsFragment);
    }

    @Override
    public abstract BabbleService getBabbleService();

    @Override
    public abstract void onJoined(String moniker);

    @Override
    public abstract void onStartedNew(String moniker);

    @Override
    public abstract void onArchiveLoaded(String moniker);

}
