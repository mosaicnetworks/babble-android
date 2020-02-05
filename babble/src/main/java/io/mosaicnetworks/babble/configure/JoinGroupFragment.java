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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
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
import io.mosaicnetworks.babble.discovery.HttpPeerDiscoveryRequest;
import io.mosaicnetworks.babble.discovery.Peer;
import io.mosaicnetworks.babble.discovery.ResponseListener;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.utils.Utils;


/**
 * This fragment enables the user to configure the {@link BabbleService} to join an existing group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link JoinGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class JoinGroupFragment extends Fragment implements ResponseListener {

    private OnFragmentInteractionListener mListener;
    private ProgressDialog mLoadingDialog;
    private String mMoniker;
    private HttpPeerDiscoveryRequest mHttpGenesisPeerDiscoveryRequest;
    private HttpPeerDiscoveryRequest mHttpCurrentPeerDiscoveryRequest;
    private List<Peer> mGenesisPeers;
    private NsdServiceInfo mNsdServiceInfo;

    public JoinGroupFragment(NsdServiceInfo nsdServiceInfo) {
        mNsdServiceInfo = nsdServiceInfo;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment JoinGroupFragment.
     */
    public static JoinGroupFragment newInstance(NsdServiceInfo nsdServiceInfo) {
        return new JoinGroupFragment(nsdServiceInfo);
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
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        //get peer IP address
        final String peerIP = mNsdServiceInfo.getHost().getHostAddress();
        final int peerPort = mNsdServiceInfo.getPort();

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

        ConfigManager configManager;
        try {
            configManager = ConfigManager.getInstance(Objects.requireNonNull(getContext()).getApplicationContext());
        } catch (FileNotFoundException ex) {
            //TODO: We cannot rethrow this exception as the overridden method does not throw it.
            //This error is thrown by ConfigManager when it fails to read / create a babble root dir.
            //This is probably a fatal error.
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot write configuration. Aborting.");
            throw new IllegalStateException();  // Throws a runtime exception that is deliberately not caught
            // The app will terminate. But babble is unstartable from here.
        }

        BabbleService<?> babbleService = mListener.getBabbleService();
        GroupDescriptor groupDescriptor = new GroupDescriptor(mNsdServiceInfo.getServiceName(), mNsdServiceInfo.getServiceType());

        try {
            String configDir = configManager.createConfigJoinGroup(mGenesisPeers, currentPeers, groupDescriptor, mMoniker, Utils.getIPAddr(getContext()));
            babbleService.start(configDir, groupDescriptor);
        } catch (IllegalStateException | CannotStartBabbleNodeException| IOException ex ) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            mLoadingDialog.dismiss();
            displayOkAlertDialog(R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        } catch (Exception ex) {
            //TODO: Review this. The duplicate dialog function feels overkill.
            mLoadingDialog.dismiss();
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage() );
            throw ex;
        }

        mLoadingDialog.dismiss();
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
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }



    private void displayOkAlertDialogText(@StringRes int titleId, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(titleId)
                .setMessage(message)
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
