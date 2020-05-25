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

package io.mosaicnetworks.babble.fragments.discover;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.fragments.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;

/**
 * This fragment implements a visual component to allow selection of a discovered group. It includes
 * swipe refresh and a nothing found message
 */
public class DiscoverGroupsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRvDiscoveredGroups;
    private SwipeRefreshLayout mSwipeRefreshServiceSearch;
    private SwipeRefreshLayout mSwipeRefreshDiscoveryFailed;
    private SwipeRefreshLayout mSwipeRefreshServicesDisplaying;
    private ConfigManager mConfigManager;
    private DiscoverGroupsViewModel mViewModel;
    private ServicesListAdapter mServicesListAdapter;
    private List<ResolvedGroup> mServiceInfoList;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment DiscoverGroupsFragment.
     */
    public static DiscoverGroupsFragment newInstance() {
        return new DiscoverGroupsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfigManager = ConfigManager.getInstance(getContext().getApplicationContext());

        mViewModel = new ViewModelProvider(
                this,
                new DiscoverGroupsViewModelFactory(
                        getActivity().getApplication(),
                        mConfigManager
                )
        ).get(DiscoverGroupsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_service_discovery, container, false);

        mRvDiscoveredGroups = view.findViewById(R.id.servicesListView);
        mSwipeRefreshServiceSearch = view.findViewById(R.id.swipeRefresh_service_search);
        mSwipeRefreshDiscoveryFailed = view.findViewById(R.id.swiperefresh_discovery_failed);
        mSwipeRefreshServicesDisplaying = view.findViewById(R.id.swiperefresh_services_displaying);

        mSwipeRefreshDiscoveryFailed.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
                        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                        mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);

                        //startDiscovery(); TODO: do something when refresh after discovery start failure!

                        if (mSwipeRefreshDiscoveryFailed.isRefreshing()) {
                            mSwipeRefreshDiscoveryFailed.setRefreshing(false);
                        }
                    }
                }
        );

        mSwipeRefreshServicesDisplaying.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mViewModel.refreshDiscovery();

                        if (mSwipeRefreshServicesDisplaying.isRefreshing()) {
                            mSwipeRefreshServicesDisplaying.setRefreshing(false);
                        }
                    }
                }
        );

        mSwipeRefreshServiceSearch.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mViewModel.refreshDiscovery();

                        if (mSwipeRefreshServiceSearch.isRefreshing()) {
                            mSwipeRefreshServiceSearch.setRefreshing(false);
                        }
                    }
                }
        );

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        mServiceInfoList = mViewModel.getmServiceInfoList();

        mServicesListAdapter = new ServicesListAdapter(getContext(), mServiceInfoList);
        mRvDiscoveredGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvDiscoveredGroups.setAdapter(mServicesListAdapter);

        mServicesListAdapter.setClickListener(new ServicesListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.onServiceSelected(mServicesListAdapter.getItem(position));
            }
        });

        final Observer<List<ResolvedGroup>> servicesObserver = new Observer<List<ResolvedGroup>>() {
            @Override
            public void onChanged(@Nullable final List<ResolvedGroup> updatedList) {

                if (mServiceInfoList.isEmpty()) {
                    mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
                } else {
                    mSwipeRefreshServiceSearch.setVisibility(View.GONE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.VISIBLE);
                }

                mServicesListAdapter.notifyDataSetChanged();
            }
        };

        mViewModel.getServiceInfoList().observe(this, servicesObserver);

        mViewModel.refreshDiscovery();

    }

    @Override
    public void onPause() {

        super.onPause();

        mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
        mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
    }
}

