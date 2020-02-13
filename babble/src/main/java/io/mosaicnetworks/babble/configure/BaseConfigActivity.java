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

import android.view.View;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.mdns.ResolvedGroup;

/**
 * This activity complements the {@link BabbleService}. It consists of a set of fragments which
 * allow the {@link BabbleService} to be configured. Extend class and override the
 * {@link BaseConfigActivity#getBabbleService()}, {@link BaseConfigActivity#onJoined(String)} and
 * {@link BaseConfigActivity#onStartedNew(String)} methods.
 */
public abstract class BaseConfigActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private FragmentManager mFragmentManager;
    public static final String PREFERENCE_FILE_KEY = "babbleandroid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_config);

        mFragmentManager = getSupportFragmentManager();

        //"When a config change occurs the old Fragment adds itself to the new Activity when it's
        //recreated". - https://stackoverflow.com/questions/8474104/android-fragment-lifecycle-over-orientation-changes
        //Check if fragment is already added to avoid attaching multiple instances of the fragment
        TabsFragment fragment = (TabsFragment) mFragmentManager.findFragmentById(R.id.constraint_layout);
        if (fragment == null) {
            addFragment(TabsFragment.newInstance());
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
    public void onServiceSelected(ResolvedGroup resolvedGroup) {
        JoinGroupFragment mJoinGroupMdnsFragment = JoinGroupFragment.newInstance(resolvedGroup);
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
