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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsServicesListAdapter;

public class DiscoverGroupsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRvDiscoveredGroups;
    private SwipeRefreshLayout mSwipeRefreshServiceSearch;
    private SwipeRefreshLayout mSwipeRefreshDiscoveryFailed;
    private SwipeRefreshLayout mSwipeRefreshServicesDisplaying;
    private ConfigManager mConfigManager;
    private DiscoverGroupsViewModel mViewModel;
    private MdnsServicesListAdapter mMdnsServicesListAdapter;
    private SelectableData<ConfigDirectory> mArchivedList = new SelectableData<>();
    private List<MdnsResolvedGroup> mServiceInfoList;

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
        mViewModel = new ViewModelProvider(this, new DiscoverGroupsViewModelFactory(getActivity().getApplication(), mConfigManager)).get(DiscoverGroupsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_mdns_discovery, container, false);

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

                        //do nothing ;)

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

                        //do nothing ;)

                        if (mSwipeRefreshServiceSearch.isRefreshing()) {
                            mSwipeRefreshServiceSearch.setRefreshing(false);
                        }
                    }
                }
        );

        return view;
    }

    /*
    private void startDiscovery() {
        mServiceListView.startDiscovery(new ServicesListListener() {

            @Override
            public void onServiceSelectedSuccess(ResolvedGroup resolvedGroup) {
                mListener.onServiceSelected(resolvedGroup);
            }

            @Override
            public void onServiceSelectedFailure() {
                DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.service_discovery_resolve_fail_title, R.string.service_discovery_resolve_fail_message);
            }

            @Override
            public void onDiscoveryStartFailure() {
                DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.service_discovery_start_fail_title, R.string.service_discovery_start_fail_message);

                mSwipeRefreshServiceSearch.setVisibility(View.GONE);
                mSwipeRefreshDiscoveryFailed.setVisibility(View.VISIBLE);
                mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
            }

            @Override
            public void onListEmptyStatusChange(boolean empty) {
                if (empty) {
                    mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
                } else {
                    mSwipeRefreshServiceSearch.setVisibility(View.GONE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.VISIBLE);
                }
            }
        });

    }

     */

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

        mMdnsServicesListAdapter = new MdnsServicesListAdapter(getContext(), mServiceInfoList);
        mRvDiscoveredGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvDiscoveredGroups.setAdapter(mMdnsServicesListAdapter);

        final Observer<List<MdnsResolvedGroup>> servicesObserver = new Observer<List<MdnsResolvedGroup>>() {
            @Override
            public void onChanged(@Nullable final List<MdnsResolvedGroup> updatedList) {

                if (mServiceInfoList.isEmpty()) {
                    mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
                } else {
                    mSwipeRefreshServiceSearch.setVisibility(View.GONE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.VISIBLE);
                }

                mMdnsServicesListAdapter.notifyDataSetChanged();
            }
        };

        mViewModel.getServiceInfoList().observe(this, servicesObserver);

    }

    @Override
    public void onPause() {

        super.onPause();

        mSwipeRefreshServiceSearch.setVisibility(View.VISIBLE);
        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
        mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
    }
}

