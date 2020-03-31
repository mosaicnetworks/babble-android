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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.mosaicnetworks.babble.R;

public class TabsFragment extends Fragment {

    GroupCollectionAdapter groupCollectionAdapter;
    ViewPager2 viewPager;


    private static boolean mShowCombined = true;
    private static boolean mShowArchive = true;
    private static boolean mShowAllArchiveVersions = true;

    private static int mTabArchive;
    private static int mTabCombined;

    private static final String TAG="TabsFragment";


    public TabsFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @param args Bundle of arguments for this fragment
     * @return A new instance of fragment HomeFragment.
     */
    public static TabsFragment newInstance(Bundle args) {
        TabsFragment fragment = new TabsFragment();

        mShowCombined = args.getBoolean(BaseConfigActivity.SHOW_COMBINED, true);
        mShowArchive = args.getBoolean(BaseConfigActivity.SHOW_ARCHIVE, true);
        mShowAllArchiveVersions = args.getBoolean(BaseConfigActivity.SHOW_ALL_ARCHIVE, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int idx = 0;

        if (mShowCombined) { mTabCombined = idx; idx++;} else { mTabCombined = -1;}
        if (mShowArchive) { mTabArchive = idx; idx++;}  else {mTabArchive = -1;}


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tabs, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        groupCollectionAdapter = new GroupCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(groupCollectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {


                        if (position == mTabArchive) {
                            tab.setText(R.string.archived_tab);
                        } else {
                            if (position == mTabCombined) {
                                tab.setText(R.string.live_tab);
                            }
                        }

            }}).attach();
    }

    public static class GroupCollectionAdapter extends FragmentStateAdapter {

        public GroupCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {


                    if (position == mTabArchive) {
                        Log.i(TAG, "createFragment:  Create Archive tab");
                        Bundle args = new Bundle();
                        args.putBoolean(BaseConfigActivity.SHOW_ALL_ARCHIVE, mShowAllArchiveVersions);
                        return ArchivedGroupsFragment.newInstance(args);
                    } else {
                        if (position == mTabCombined) {
                            Log.i(TAG, "createFragment:  Create Combined tab");
                            return DiscoveryFragment.newInstance();
                        }
                    }

            return null;  //TODO: This function is defined as @NonNull - should we throw an exception instead?
        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (mShowArchive) itemCount++;
            if (mShowCombined) itemCount++;
            return itemCount;
        }
    }

}