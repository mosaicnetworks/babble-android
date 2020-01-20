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
import android.widget.ProgressBar;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
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
    private ProgressBar mProgressView;

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
        mProgressView = view.findViewById(R.id.progressBar);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        mMoniker = sharedPref.getString("moniker", "Me");

        return view;
    }

    private void joinGroup(String peerIp) {
        getPeers(peerIp);
    }

    private void getPeers(final String peerIP) {
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

        mLoadingDialog.show();
        mHttpGenesisPeerDiscoveryRequest.send();
    }

    @Override
    public void onReceivePeers(List<Peer> currentPeers) {
        //TODO: check this is safe
        BabbleService<?> babbleService = mListener.getBabbleService();

        try {
            babbleService.configureJoin(mGenesisPeers, currentPeers, mMoniker, Utils.getIPAddr(getContext()));
        } catch (IllegalStateException | CannotStartBabbleNodeException ex) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            mLoadingDialog.dismiss();
            displayOkAlertDialog(R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        }

        mLoadingDialog.dismiss();
        babbleService.start();
        mListener.onJoined(mMoniker);
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

    @Override
    public void onResume() {
        super.onResume();
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
            }

            @Override
            public void onListEmptyStatusChange(boolean empty) {
                if (empty) {
                    mProgressView.setVisibility(View.VISIBLE);
                    mServiceListView.setVisibility(View.GONE);
                } else {
                    mProgressView.setVisibility(View.GONE);
                    mServiceListView.setVisibility(View.VISIBLE);
                }


            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: is this the right place to cancel the requests?
        cancelRequests();
        mServiceListView.stopDiscovery();
    }

}
