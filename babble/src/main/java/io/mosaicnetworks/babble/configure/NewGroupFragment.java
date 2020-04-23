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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;

import io.mosaicnetworks.babble.BuildConfig;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.service.BabbleService2;
import io.mosaicnetworks.babble.service.BabbleServiceBinderFragment;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser2;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PService2;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment enables the user to configure the {@link BabbleService2} to create a new group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link NewGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class NewGroupFragment extends BabbleServiceBinderFragment {

    private static boolean mShowmDNS = true;
    private static boolean mShowP2P = true;
    private String mConfigDirectory;
    private ProgressDialog mLoadingDialog;
    private ResolvedGroup mResolvedGroup;
    private String mMoniker;
    private OnFragmentInteractionListener mListener;
    private ServiceAdvertiser mServiceAdvertiser;
    private int mNetworkType = BabbleConstants.NETWORK_NONE;

    public NewGroupFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment NewGroupServiceFragment.
     */
    public static NewGroupFragment newInstance(Bundle args) {
        mShowmDNS = args.getBoolean(BaseConfigActivity.SHOW_MDNS, true);
        mShowP2P = args.getBoolean(BaseConfigActivity.SHOW_P2P, true);
        return new NewGroupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_new_group, container, false);
        final View newGroupButton = view.findViewById(R.id.button_start);
        newGroupButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            startGroup(view);
                    }
                }
        );

        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        EditText edit = view.findViewById(R.id.edit_moniker);
        edit.setText(sharedPref.getString("moniker", "Me"));

        // If only one discovery tab is shown then set and then disable the toggle switch to only
        // allow a valid selection.
        Switch switchP2P =  view.findViewById(R.id.switch_p2p);
        if (mShowmDNS) {
            if (mShowP2P) {
                switchP2P.setChecked(false);
                switchP2P.setEnabled(true);
            } else {
                switchP2P.setChecked(false);
                switchP2P.setEnabled(false);
                switchP2P.setVisibility(View.GONE);
            }
        } else {
            if (mShowP2P) {
                switchP2P.setChecked(true);
                switchP2P.setEnabled(false);
            }
        }

        EditText editGroup = view.findViewById(R.id.edit_group_name);
        editGroup.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imgr).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS); //TODO: fix potential NPE

        return view;
    }

    // called when the user presses the start button
    public void startGroup(View view){

        //get moniker
        EditText editMoniker = view.findViewById(R.id.edit_moniker);
        mMoniker = editMoniker.getText().toString();
        if (mMoniker.isEmpty()) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        // Store moniker entered
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("moniker", mMoniker);
        editor.apply();

        //get group name
        EditText editGroupName = view.findViewById(R.id.edit_group_name);
        String groupName = editGroupName.getText().toString();
        if (groupName.isEmpty()) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.no_group_name_alert_title, R.string.no_group_name_alert_message);
            return;
        }


  //      mGroupDescriptor = new GroupDescriptor(groupName);

        // Get network type
        Switch switchP2P =  view.findViewById(R.id.switch_p2p);
        boolean isP2P = switchP2P.isChecked();

        if (isP2P) {

            //TODO: P2P - Need change the workflow here. We are launching a new instance, but we rely
            //      on having started Wifi Direct up.
            mNetworkType = BabbleConstants.NETWORK_P2P;
            P2PService2 p2PService = P2PService2.getInstance(getContext());
            p2PService.registerOnNetworkInitialised(new OnNetworkInitialised() {
                @Override
                public void onNetworkInitialised(String ip){
                    configAndStartBabble(ip, ip);
                }
            });

            // This will call back onNetworkInitialised
            p2PService.startRegistration(mMoniker, groupName, BuildConfig.BabbleVersion, false);
            //TODO: Turned off discovery for the lead peer, but may need to re-enable to trigger discovery.

            //TODO: finish this line: mServiceAdvertiser =

        } else {

            mNetworkType = BabbleConstants.NETWORK_WIFI;
            mServiceAdvertiser = new MdnsAdvertiser2(
                    getContext().getApplicationContext(),
                    null
                    );  //TODO: JK27March  Add MDNS ResolvedService new group factory method
            String ip = Utils.getIPAddr(getContext());
            configAndStartBabble(ip, ip);
        }
    }

    public void onNetworkInitialised(String peersAddr, String babbleAddr){
        Log.i("startGroup", "onNetworkInitialised: "+ peersAddr + " " + babbleAddr);
        configAndStartBabble(peersAddr, babbleAddr);
    }

    private void configAndStartBabble(String peersAddr, String babbleAddr)  {
        ConfigManager configManager =
                ConfigManager.getInstance(getContext().getApplicationContext());
        /*
       try {

           //TODO: JK02Apr restore this line - will need to use ResolvedGroup
                      mConfigDirectory = configManager.createConfigNewGroup(mGroupDescriptor, peersAddr, babbleAddr, mNetworkType);
       } catch (CannotStartBabbleNodeException|IOException ex) {
           //TODO: think about this error handling
       }
       */
        startBabbleService();
    }

    public void startBabbleService() {
        getActivity().startService(new Intent(getActivity(), BabbleService2.class));
        mLoadingDialog = DialogUtils.displayLoadingDialog(getContext());
        mLoadingDialog.show();
        doBindService();
    }

    @Override
    protected void onServiceConnected() {
        try {
            mBoundService.start(mConfigDirectory, mResolvedGroup, mServiceAdvertiser, false);
            mListener.baseOnStartedNew(mMoniker, mResolvedGroup.getGroupName());
        } catch (IllegalArgumentException ex) {
            // we'll assume this is caused by the node taking a while to leave a previous group,
            // though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            mLoadingDialog.dismiss();
            getActivity().stopService(new Intent(getActivity(), BabbleService2.class));
        }
        doUnbindService();
    }

    @Override
    protected void onServiceDisconnected() {
        //Do nothing
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
    public void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog!=null) {
            mLoadingDialog.dismiss();
        }
        doUnbindService();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
