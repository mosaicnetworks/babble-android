package io.mosaicnetworks.babble.configure;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.servicediscovery.mdns.ServicesListView;
import io.mosaicnetworks.babble.utils.Utils;

public class HomeMdnsFragment extends Fragment implements ResponseListener {

    private OnFragmentInteractionListener mListener;
    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpGenesisPeerDiscoveryRequest;
    private HttpPeerDiscoveryRequest mHttpCurrentPeerDiscoveryRequest;
    private List<Peer> mGenesisPeers;
    private ServicesListView mServiceListView;
    private LinearLayout mLinearLayoutServiceSearch;
    private SwipeRefreshLayout mSwipeRefreshDiscoveryFailed;
    private SwipeRefreshLayout mSwipeRefreshServicesDisplaying;

    public HomeMdnsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment HomeMdnsFragment.
     */
    public static HomeMdnsFragment newInstance() {
        return new HomeMdnsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoadingDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home_mdns, container, false);

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

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        mMoniker = sharedPref.getString("moniker", "Me");

        return view;
    }

    private void joinGroup(String peerIp) {
        getPeers(peerIp);
    }

    private void getPeers(final String peerIP) {
        /*
        try {
            mHttpGenesisPeerDiscoveryRequest = HttpPeerDiscoveryRequest.createGenesisPeersRequest(peerIP,
                    BabbleService.DEFAULT_DISCOVERY_PORT, new ResponseListener() {
                        @Override
                        public void onReceivePeers(List<Peer> genesisPeers) {
                            mGenesisPeers = genesisPeers;

                            mHttpCurrentPeerDiscoveryRequest =
                                    HttpPeerDiscoveryRequest.createCurrentPeersRequest(
                                            peerIP, BabbleService.DEFAULT_DISCOVERY_PORT,
                                            HomeMdnsFragment.this, getContext());

                            mHttpCurrentPeerDiscoveryRequest.send();
                        }

                        @Override
                        public void onFailure(Error error) {
                            HomeMdnsFragment.this.onFailure(error);
                        }
                    }, getContext());
        } catch (IllegalArgumentException ex) {
            displayOkAlertDialog(R.string.invalid_hostname_alert_title, R.string.invalid_hostname_alert_message);
            return;
        }

         */

        mLoadingDialog.show();
        mHttpGenesisPeerDiscoveryRequest.send();
    }

    @Override
    public void onReceivePeers(List<Peer> currentPeers) {


    }

    @Override
    public void onFailure(io.mosaicnetworks.babble.discovery.ResponseListener.Error error) {
        mLoadingDialog.dismiss();
        int messageId;
        switch (error) {
            case INVALID_JSON:
                messageId = R.string.peers_json_error_alert_message;
                break;
            case CONNECTION_ERROR:
                messageId = R.string.peers_connection_error_alert_message;
                break;
            case TIMEOUT:
                messageId = R.string.peers_timeout_error_alert_message;
                break;
            default:
                messageId = R.string.peers_unknown_error_alert_message;
        }
        displayOkAlertDialog(R.string.peers_error_alert_title, messageId);
    }

    private void initLoadingDialog() {
        mLoadingDialog = new ProgressDialog(getContext());
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setTitle(R.string.loading_title);
        mLoadingDialog.setMessage(getString(R.string.loading_message));
        mLoadingDialog.setIndeterminate(true);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                cancelRequests();
            }});
    }

    private void displayOkAlertDialog(@StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }

    private void cancelRequests() {
        if (mHttpCurrentPeerDiscoveryRequest!=null) {
            mHttpCurrentPeerDiscoveryRequest.cancel();
        }

        if (mHttpGenesisPeerDiscoveryRequest!=null) {
            mHttpGenesisPeerDiscoveryRequest.cancel();
        }
    }

    @Override
    public void onAttach(Context context) {
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
        mServiceListView.startDiscovery(new ServicesListView.ServicesListListener() {

            @Override
            public void onServiceSelectedSuccess(NsdServiceInfo nsdServiceInfo) {
                mListener.onServiceSelected(nsdServiceInfo);
            }

            @Override
            public void onServiceSelectedFailure() {
                displayOkAlertDialog(R.string.service_discovery_resolve_fail_title, R.string.service_discovery_resolve_fail_message);
            }

            @Override
            public void onDiscoveryStartFailure() {
                displayOkAlertDialog(R.string.service_discovery_start_fail_title, R.string.service_discovery_start_fail_message);

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

        //TODO: is this the right place to cancel the requests?
        cancelRequests();
        mServiceListView.stopDiscovery();
    }

}
