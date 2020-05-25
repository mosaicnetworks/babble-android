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
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.fragments.join.MdnsJoinGroupFragment;
import io.mosaicnetworks.babble.fragments.join.WebRTCJoinGroupFragment;
import io.mosaicnetworks.babble.fragments.archive.ArchivedGroupsFragment;
import io.mosaicnetworks.babble.fragments.create.NewGroupFragment;
import io.mosaicnetworks.babble.fragments.tabs.TabsFragment;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;

/**
 * This activity complements the BabbleService. It consists of a set of fragments which
 * allow the BabbleService to be configured. Extend class and override the
 *  {@link BaseConfigActivity#onJoined(String, String)} and
 * {@link BaseConfigActivity#onStartedNew(String, String)} methods.
 */
public abstract class BaseConfigActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private FragmentManager mFragmentManager;
    private Boolean mFromGroup = false;
    public static final String PREFERENCE_FILE_KEY = "babbleandroid";

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
            addFragment(TabsFragment.newInstance());
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.constraint_layout, fragment);
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
        NewGroupFragment mNewGroupFragment = NewGroupFragment.newInstance();
        replaceFragment(mNewGroupFragment, true);
    }

    @Override
    public void onServiceSelected(ResolvedGroup resolvedGroup) {

        switch (resolvedGroup.getSource()) {
            case MDNS:
                MdnsJoinGroupFragment mJoinGroupMdnsFragment = MdnsJoinGroupFragment.newInstance(resolvedGroup);
                replaceFragment(mJoinGroupMdnsFragment, true);
                break;
            case WEBRTC:
                WebRTCJoinGroupFragment mJoinGroupWebRTCFragment = WebRTCJoinGroupFragment.newInstance(resolvedGroup);
                replaceFragment(mJoinGroupWebRTCFragment, true);
                break;
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

}
