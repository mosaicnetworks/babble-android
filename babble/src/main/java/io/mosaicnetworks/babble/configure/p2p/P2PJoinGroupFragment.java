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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.configure.BaseConfigActivity;
import io.mosaicnetworks.babble.configure.mdns.MdnsJoinGroupFragment;
import io.mosaicnetworks.babble.configure.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PConnected;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PResolvedService;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PService;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

//TODO: This could be merged with MdnsJoinGroupFragment.java

/**
 * This fragment enables the user to configure the {@link BabbleService} to join an existing group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link MdnsJoinGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class P2PJoinGroupFragment extends Fragment implements ResponseListener, P2PConnected {


    private static String TAG="P2PJoinFragment";
    private OnFragmentInteractionListener mListener;
    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpGenesisPeerDiscoveryRequest;
    private HttpPeerDiscoveryRequest mHttpCurrentPeerDiscoveryRequest;
    private List<Peer> mGenesisPeers;
    private P2PResolvedGroup mResolvedGroup;
    private P2PResolvedService mResolvedService;

    public P2PJoinGroupFragment() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MdnsJoinGroupFragment.
     */
    public static P2PJoinGroupFragment newInstance(ResolvedGroup resolvedGroup) {
        Log.i(TAG, "newInstance: "+ resolvedGroup.getGroupName());
        P2PJoinGroupFragment p2PJoinGroupFragment = new P2PJoinGroupFragment();
        p2PJoinGroupFragment.mResolvedGroup = (P2PResolvedGroup) resolvedGroup;
        return  p2PJoinGroupFragment;
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
        final View joinGroupButton = view.findViewById(R.id.button_join);
        joinGroupButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinGroup(view);
                    }
                }
        );

        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        EditText edit = view.findViewById(R.id.edit_moniker);
        edit.setText(sharedPref.getString("moniker", "Me"));
        edit.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imgr).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);

        return view;

    }

    // called when the user presses the join button
    public void joinGroup(View view) {
        //get moniker

        Log.i(TAG, "joinGroup: Button Pressed");

        EditText editText = view.findViewById(R.id.edit_moniker);
        mMoniker = editText.getText().toString();
        if (mMoniker.isEmpty()) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        List<ResolvedService> resolvedServices = mResolvedGroup.getResolvedServices();

        if (resolvedServices.size() < 1) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.no_service_alert_title, R.string.no_service_alert_message);
            return;
        }


        // For P2P only one node advertises.
        mResolvedService = (P2PResolvedService) resolvedServices.get(0);
        final String peerIP = mResolvedService.getInetAddress().getHostAddress();
        final int peerPort = mResolvedService.getPort();
        final String deviceAddress = mResolvedService.getGroupUid();

        // Store moniker and host entered
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("moniker", mMoniker);
        editor.putString("host", peerIP);
        editor.apply();

        connectToPeer(deviceAddress, peerIP, peerPort);


    }


    private void connectToPeer(String deviceAddress, String peerIP, int peerPort) {

        P2PService p2PService = P2PService.getInstance(getContext());
        p2PService.registerP2PConnected(this);
        p2PService.connectToPeer(deviceAddress, peerIP, peerPort);

        // Calls back onConnected on Success.
    }

    @Override
    public void onConnected(String peerIP, int peerPort) {
        getPeers(peerIP, peerPort);
    }

    private void getPeers(final String peerIP, final int peerPort) {
        try {


            mHttpGenesisPeerDiscoveryRequest = HttpPeerDiscoveryRequest.createGenesisPeersRequest(peerIP,
                    peerPort, new ResponseListener() {
                        @Override
                        public void onReceivePeers(List<Peer> genesisPeers) {
                            mGenesisPeers = genesisPeers;

                            mHttpCurrentPeerDiscoveryRequest =
                                    HttpPeerDiscoveryRequest.createCurrentPeersRequest(
                                            peerIP, peerPort,
                                            P2PJoinGroupFragment.this, getContext());

                            mHttpCurrentPeerDiscoveryRequest.send();
                        }

                        @Override
                        public void onFailure(Error error) {
                            P2PJoinGroupFragment.this.onFailure(error);
                        }
                    }, getContext());
        } catch (IllegalArgumentException ex) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.invalid_hostname_alert_title, R.string.invalid_hostname_alert_message);
            return;
        }

        mLoadingDialog.show();
        mHttpGenesisPeerDiscoveryRequest.send();
    }

    @Override
    public void onReceivePeers(List<Peer> currentPeers) {

        ConfigManager configManager;
            configManager = ConfigManager.getInstance(Objects.requireNonNull(getContext()).getApplicationContext());


        BabbleService<?> babbleService = mListener.getBabbleService();
        GroupDescriptor groupDescriptor = new GroupDescriptor(mResolvedService.getGroupName(), mResolvedService.getGroupUid());

        try {
            String configDir = configManager.createConfigJoinGroup(mGenesisPeers, currentPeers, groupDescriptor, mMoniker, Utils.getIPAddr(getContext()));
            babbleService.start(configDir, groupDescriptor);
        } catch (IllegalStateException | CannotStartBabbleNodeException| IOException ex ) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            mLoadingDialog.dismiss();
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        } catch (Exception ex) {
            //TODO: Review this. The duplicate dialog function feels overkill.
            mLoadingDialog.dismiss();
            DialogUtils.displayOkAlertDialogText(Objects.requireNonNull(getContext()), R.string.babble_init_fail_title, "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage() );
            throw ex;
        }

        mLoadingDialog.dismiss();
        mListener.baseOnJoined(mMoniker);
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
        DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.peers_error_alert_title, messageId);
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


    private void cancelRequests() {
        if (mHttpCurrentPeerDiscoveryRequest!=null) {
            mHttpCurrentPeerDiscoveryRequest.cancel();
        }

        if (mHttpGenesisPeerDiscoveryRequest!=null) {
            mHttpGenesisPeerDiscoveryRequest.cancel();
        }
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
    public void onPause() {
        super.onPause();
        cancelRequests();
    }

}
