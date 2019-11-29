package io.mosaicnetworks.babble.configure;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoinGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinGroupFragment extends Fragment implements ResponseListener {

    private OnFragmentInteractionListener mListener;
    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpGenesisPeerDiscoveryRequest;
    private HttpPeerDiscoveryRequest mHttpCurrentPeerDiscoveryRequest;
    private List<Peer> mGenesisPeers;

    public JoinGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment JoinGroupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JoinGroupFragment newInstance() {
        return new JoinGroupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoadingDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_join_group, container, false);
        final View joinGroupButton = view.findViewById(R.id.buttonJoin);
        joinGroupButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinGroup(view);
                    }
                }
        );

        EditText edit = (EditText) view.findViewById(R.id.editMoniker);
        edit.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);

        return view;

    }

    // called when the user presses the join button
    public void joinGroup(View view) {
        //get moniker
        EditText editText = view.findViewById(R.id.editMoniker);
        mMoniker = editText.getText().toString();
        if (mMoniker.isEmpty()) {
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        //get peer IP address
        EditText editIP = view.findViewById(R.id.editHost);
        final String peerIP = editIP.getText().toString();
        if (peerIP.isEmpty()) {
            displayOkAlertDialog(R.string.no_hostname_alert_title, R.string.no_hostname_alert_message);
            return;
        }

        getPeers(peerIP);
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
                                            JoinGroupFragment.this, getContext());

                            mHttpCurrentPeerDiscoveryRequest.send();
                        }

                        @Override
                        public void onFailure(Error error) {
                            JoinGroupFragment.this.onFailure(error);
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
        } catch (IllegalStateException ex) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            mLoadingDialog.dismiss();
            displayOkAlertDialog(R.string.babble_busy_title, R.string.babble_busy_message);
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
                cancelRequets();
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

    private void cancelRequets() {
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
    public void onPause() {
        //TODO: is this the right place to cancel the requests?
        cancelRequets();
        super.onPause();
    }

}
