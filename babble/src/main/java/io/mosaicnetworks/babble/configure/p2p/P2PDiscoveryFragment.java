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

package io.mosaicnetworks.babble.configure.p2p;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Objects;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.configure.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.servicediscovery.InterfaceResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ServicesListListener;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PServicesListView;
import io.mosaicnetworks.babble.utils.DialogUtils;

public class P2PDiscoveryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private P2PServicesListView mServiceListView;
    private LinearLayout mLinearLayoutServiceSearch;
    private SwipeRefreshLayout mSwipeRefreshDiscoveryFailed;
    private SwipeRefreshLayout mSwipeRefreshServicesDisplaying;

    public P2PDiscoveryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MdnsDiscoveryFragment.
     */
    public static P2PDiscoveryFragment newInstance() {
        return new P2PDiscoveryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_p2p_discovery, container, false);

        mServiceListView = view.findViewById(R.id.servicesListView);
        mLinearLayoutServiceSearch = view.findViewById(R.id.linearLayout_service_search);
        mSwipeRefreshDiscoveryFailed = view.findViewById(R.id.swiperefresh_discovery_failed);
        mSwipeRefreshServicesDisplaying = view.findViewById(R.id.swiperefresh_services_displaying);

        mSwipeRefreshDiscoveryFailed.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mLinearLayoutServiceSearch.setVisibility(View.VISIBLE);
                        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                        mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);

                        startDiscovery();

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

    private void startDiscovery() {
        mServiceListView.startDiscovery(new ServicesListListener() {

            @Override
            public void onServiceSelectedSuccess(ResolvedGroup resolvedGroup) {
                mListener.onServiceSelected(resolvedGroup);
            }

            @Override
            public void onServiceSelectedFailure() {
                DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.service_p2p_discovery_resolve_fail_title, R.string.service_discovery_resolve_fail_message);
            }

            @Override
            public void onDiscoveryStartFailure() {
                DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.service_p2p_discovery_start_fail_title, R.string.service_discovery_start_fail_message);

                mLinearLayoutServiceSearch.setVisibility(View.GONE);
                mSwipeRefreshDiscoveryFailed.setVisibility(View.VISIBLE);
                mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
            }

            @Override
            public void onListEmptyStatusChange(boolean empty) {
                if (empty) {
                    mLinearLayoutServiceSearch.setVisibility(View.VISIBLE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);
                } else {
                    mLinearLayoutServiceSearch.setVisibility(View.GONE);
                    mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
                    mSwipeRefreshServicesDisplaying.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        startDiscovery();
    }

    @Override
    public void onPause() {

        super.onPause();

        mLinearLayoutServiceSearch.setVisibility(View.VISIBLE);
        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
        mSwipeRefreshServicesDisplaying.setVisibility(View.GONE);

        mServiceListView.stopDiscovery();
    }

}
