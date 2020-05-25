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

package io.mosaicnetworks.babble.fragments.join;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.fragments.BaseConfigActivity;
import io.mosaicnetworks.babble.fragments.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.servicediscovery.mdns.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.node.Peer;
import io.mosaicnetworks.babble.servicediscovery.mdns.ResponseListener;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.service.BabbleService;
import io.mosaicnetworks.babble.service.BabbleServiceBinder;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment enables the user to configure the BabbleService to join an existing group that was
 * discovered with mDNS.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link MdnsJoinGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class MdnsJoinGroupFragment extends BabbleServiceBinder implements ResponseListener {

    private static String TAG="MdnsJoinFragment";
    private OnFragmentInteractionListener mListener;
    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpGenesisPeerDiscoveryRequest;
    private HttpPeerDiscoveryRequest mHttpCurrentPeerDiscoveryRequest;
    private List<Peer> mGenesisPeers;
    private ResolvedGroup mResolvedGroup;
    private ResolvedService mResolvedService;
    private static Random randomGenerator = new Random();
    private GroupDescriptor mGroupDescriptor;
    private String mConfigDirectory;

    public MdnsJoinGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MdnsJoinGroupFragment.
     */
    public static MdnsJoinGroupFragment newInstance(ResolvedGroup resolvedGroup) {
        Log.i(TAG, "newInstance: "+ resolvedGroup.getGroupName());
        MdnsJoinGroupFragment mdnsJoinGroupFragment = new MdnsJoinGroupFragment();
        mdnsJoinGroupFragment.mResolvedGroup = resolvedGroup;
        return  mdnsJoinGroupFragment;
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

        //We are choosing a random resolved service - if we try again we may get a different
        //service.
        mResolvedService = resolvedServices.get(randomGenerator.nextInt(resolvedServices.size()));

        final String peerIP = mResolvedService.getInetAddress().getHostAddress();
        final int peerPort = mResolvedService.getPort();

        // Store moniker and host entered
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("moniker", mMoniker);
        editor.putString("host", peerIP);
        editor.apply();

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
                                            MdnsJoinGroupFragment.this, getContext());

                            mHttpCurrentPeerDiscoveryRequest.send();
                        }

                        @Override
                        public void onFailure(Error error) {
                            MdnsJoinGroupFragment.this.onFailure(error);
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
    public void onFailure(ResponseListener.Error error) {
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

    @Override
    public void onReceivePeers(List<Peer> currentPeers) {

        mGroupDescriptor = new GroupDescriptor(mResolvedService.getGroupName(), mResolvedService.getGroupUid());

        ConfigManager configManager =
                ConfigManager.getInstance(getContext().getApplicationContext());

        mConfigDirectory = configManager.createConfigJoinGroup(mGenesisPeers, currentPeers, mGroupDescriptor, mMoniker, Utils.getIPAddr(getContext()), BabbleService.NETWORK_WIFI);

        startBabbleService();
    }

    public void startBabbleService() {
        getActivity().startService(new Intent(getActivity(), BabbleService.class));
        doBindService();
    }

    @Override
    protected void onServiceConnected() {

        ServiceAdvertiser serviceAdvertiser = new MdnsAdvertiser(mGroupDescriptor,
                getContext().getApplicationContext());

        try {
            mBoundService.start(mConfigDirectory, serviceAdvertiser);
            mLoadingDialog.dismiss();
            mListener.baseOnJoined(mMoniker, mGroupDescriptor.getName());
        } catch (IllegalStateException ex) {
            // we'll assume this is caused by the node taking a while to leave a previous group,
            // though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            mLoadingDialog.dismiss();
            getActivity().stopService(new Intent(getActivity(), BabbleService.class));
        }
        doUnbindService();
    }

    @Override
    protected void onServiceDisconnected() {
        //Do nothing
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
