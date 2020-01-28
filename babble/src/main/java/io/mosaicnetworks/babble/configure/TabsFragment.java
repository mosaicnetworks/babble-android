package io.mosaicnetworks.babble.configure;

import android.content.Context;
import android.os.Bundle;
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

    public TabsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static TabsFragment newInstance() {
        TabsFragment fragment = new TabsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tabs, container, false);
    }

    @Override
    public void onAttach(Context context) {
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

                switch (position) {
                    case 0: tab.setText(R.string.live_tab);
                        break;
                    case 1: tab.setText(R.string.archived_tab);
                }

            }}).attach();
    }

    public static class GroupCollectionAdapter extends FragmentStateAdapter {

        public GroupCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case 0: return MdnsDiscoveryFragment.newInstance();
                case 1: return ArchivedGroupsFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}