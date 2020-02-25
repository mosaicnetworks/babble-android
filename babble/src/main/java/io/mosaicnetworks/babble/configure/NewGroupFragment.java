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
import android.widget.Switch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.servicediscovery.p2p.P2PService;
import io.mosaicnetworks.babble.utils.Utils;





/**
 * This fragment enables the user to configure the {@link BabbleService} to create a new group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link NewGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class NewGroupFragment extends Fragment implements OnNetworkInitialised {

    private static boolean mShowmDNS = true;
    private static boolean mShowP2P = true;


    //TODO: This variable is not currently exposed. It controls the initial value of the P2P / MDNS switch
    // true for P2P, false for MDNS.
    private static boolean mInitSwitchP2P = false;



    GroupDescriptor mGroupDescriptor;
    int mNetworkType;
    String mMoniker;

    private OnFragmentInteractionListener mListener;

    public NewGroupFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment NewGroupFragment.
     */
    public static NewGroupFragment newInstance(Bundle args) {
        mShowmDNS = args.getBoolean(BaseConfigActivity.SHOW_MDNS, true);
        mShowP2P = args.getBoolean(BaseConfigActivity.SHOW_P2P, true);
//        setArguments(args);
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
            if ( mShowP2P) {
                switchP2P.setChecked(mInitSwitchP2P);
                switchP2P.setEnabled(true);
            } else {
                    switchP2P.setChecked(false);
                    switchP2P.setEnabled(false);
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
    public void startGroup(View view) {
        Log.i("startGroup", "Starting Group ");

        //get moniker
        EditText editMoniker = view.findViewById(R.id.edit_moniker);
        mMoniker = editMoniker.getText().toString();
        if (mMoniker.isEmpty()) {
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        //get group name
        EditText editGroupName = view.findViewById(R.id.edit_group_name);
        String groupName = editGroupName.getText().toString();
        if (groupName.isEmpty()) {
            displayOkAlertDialog(R.string.no_group_name_alert_title, R.string.no_group_name_alert_message);
            return;
        }

        // Get network type
        Switch switchP2P =  view.findViewById(R.id.switch_p2p);
        boolean isP2P = switchP2P.isChecked();
        int networkType;

        if (isP2P) {
            mNetworkType = BabbleService.NETWORK_P2P;
        } else {
            mNetworkType = BabbleService.NETWORK_WIFI;
        }

        mGroupDescriptor = new GroupDescriptor(groupName);

        //TODO: P2P - Need change the workflow here. We are launching a new instance, but we rely
        //      on having started Wifi Direct up.


        String ip = "";

        switch (mNetworkType) {
            case BabbleService.NETWORK_P2P:
                P2PService p2PService = P2PService.getInstance(getContext());
                p2PService.registerOnNetworkInitialised(this);

                // This will call back onNetworkInitialised
                Log.i("startGroup", "Start Broadcasting P2P");
                p2PService.startRegistration(mMoniker, groupName, BabbleService.BABBLE_VERSION, false);
                //TODO: Turned off discovery for the lead peer, but may need to re-enable to trigger discovery.


                break;
            default:
                ip = Utils.getIPAddr(getContext());

                Log.i("startGroup", "mDNS");

                // N.B. mDNS does not require a callback, so we call configAndStartBabble directly.

                onNetworkInitialised(ip);
        }


        // Store moniker entered
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("moniker", mMoniker);
        editor.apply();



    }



    public void onNetworkInitialised(String ip){
        Log.i("startGroup", "onNetworkInitialised: "+ ip);
        configAndStartBabble(ip);
    }

    private void configAndStartBabble(String ip) {
        BabbleService<?> babbleService = mListener.getBabbleService();
        ConfigManager configManager;

        String configDirectory;


        try {
            configManager = ConfigManager.getInstance(Objects.requireNonNull(getContext()).getApplicationContext());

            Log.v("startGroup", "Got ConfigManager ");

        } catch (FileNotFoundException ex) {
            //This error is thrown by ConfigManager when it fails to read / create a babble root dir.
            //This is probably a fatal error.
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot write configuration. Aborting.");
            throw new IllegalStateException();  // Throws a runtime exception that is deliberately not caught
            // The app will terminate. But babble is unstartable from here.
        }




        try {
            configDirectory = configManager.createConfigNewGroup(mGroupDescriptor, mMoniker, ip);
            Log.i("startGroup", "configDirectory: " + configDirectory);
            //babbleService.createConfigNewGroup(moniker, Utils.getIPAddr(getContext()));
        } catch (IllegalArgumentException | CannotStartBabbleNodeException| IOException ex) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            displayOkAlertDialog(R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        }

        Log.i("startGroup", "configDirectory: " + configDirectory);


        try {
            babbleService.start(configDirectory, mGroupDescriptor, mNetworkType);
        } catch (IllegalArgumentException ex) {
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            displayOkAlertDialog(R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        } catch (Exception ex) {
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage() );
            throw ex;
        }

        mListener.baseOnStartedNew(mMoniker);

    }






    //TODO: Review if we need both.
    private void displayOkAlertDialogText(@StringRes int titleId, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(titleId)
                .setMessage(message)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }




    private void displayOkAlertDialog(@StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
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
}
