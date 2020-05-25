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

package io.mosaicnetworks.babble.fragments.create;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import java.util.Objects;

import androidx.annotation.NonNull;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.fragments.BaseConfigActivity;
import io.mosaicnetworks.babble.fragments.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.service.BabbleService;
import io.mosaicnetworks.babble.service.BabbleServiceBinder;
import io.mosaicnetworks.babble.servicediscovery.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCService;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment enables the user to configure the BabbleService to create a new group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link NewGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class NewGroupFragment extends BabbleServiceBinder {

    private String mConfigDirectory;
    private ProgressDialog mLoadingDialog;
    private GroupDescriptor mGroupDescriptor;
    private String mMoniker;
    private OnFragmentInteractionListener mListener;
    private ServiceAdvertiser mServiceAdvertiser;

    public NewGroupFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment NewGroupServiceFragment.
     */
    public static NewGroupFragment newInstance() {
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

        EditText editGroup = view.findViewById(R.id.edit_group_name);
        editGroup.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imgr).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS); //TODO: fix potential NPE

        return view;
    }

    // called when the user presses the start button
    public void startGroup(View view) {

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
        mGroupDescriptor = new GroupDescriptor(groupName);

        // Get network type
        Switch switchGlobal =  view.findViewById(R.id.switch_global);
        boolean isGlobal = switchGlobal.isChecked();

        if (isGlobal) {
            WebRTCService webRTCService = WebRTCService.getInstance(getContext());
            mServiceAdvertiser = webRTCService;

            String ip = Utils.getIPAddr(getContext());

            ConfigManager configManager = ConfigManager.getInstance(getContext());
            String peersAddr = configManager.getPublicKey();

            configAndStartBabble(peersAddr, ip, BabbleService.NETWORK_GLOBAL);

        } else {
            mServiceAdvertiser = new MdnsAdvertiser(mGroupDescriptor,
                    getContext().getApplicationContext());
            String ipAddr = Utils.getIPAddr(getContext());
            configAndStartBabble(ipAddr, ipAddr, BabbleService.NETWORK_WIFI);
        }
    }

    private void configAndStartBabble(String peersAddr, String babbleAddr, int serviceType) {
        ConfigManager configManager =
                ConfigManager.getInstance(getContext().getApplicationContext());
        mConfigDirectory = configManager.createConfigNewGroup(mGroupDescriptor, mMoniker, peersAddr, babbleAddr, serviceType);
        startBabbleService();
    }

    public void startBabbleService() {
        getActivity().startService(new Intent(getActivity(), BabbleService.class));
        mLoadingDialog = DialogUtils.displayLoadingDialog(getContext());
        mLoadingDialog.show();
        doBindService();
    }

    @Override
    protected void onServiceConnected() {
        try {
            mBoundService.start(mConfigDirectory, mServiceAdvertiser);
            mListener.baseOnStartedNew(mMoniker, mGroupDescriptor.getName());
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
