package io.mosaicnetworks.babble.configure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.utils.Utils;


/**
 * This fragment enables the user to configure the {@link BabbleService} to create a new group.
 * Activities that contain this fragment must implement the {@link OnFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link NewGroupFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class NewGroupFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public NewGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment NewGroupFragment.
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

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        EditText edit = view.findViewById(R.id.edit_moniker);
        edit.setText(sharedPref.getString("moniker", "Me"));

        EditText editGroup = view.findViewById(R.id.edit_group_name);
        editGroup.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS); //TODO: fix potential NPE

        return view;
    }

    // called when the user presses the start button
    public void startGroup(View view) {
        //TODO: check this is safe

        Log.i("startGroup", "Staring Group ");
        BabbleService<?> babbleService = mListener.getBabbleService();

        ConfigManager configManager = ConfigManager.getInstance(getContext().getApplicationContext());

        //get moniker
        EditText editMoniker = view.findViewById(R.id.edit_moniker);
        String moniker = editMoniker.getText().toString();
        if (moniker.isEmpty()) {
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



        Log.i("startGroup", "Staring Group " + groupName);

        String configDirectory;
        try {
            configDirectory = configManager.configureNew(groupName, moniker, Utils.getIPAddr(getContext()));
            Log.i("startGroup", "configDirectory: " + configDirectory);
            //babbleService.configureNew(moniker, Utils.getIPAddr(getContext()));
        } catch (IllegalArgumentException | CannotStartBabbleNodeException ex) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous group,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            displayOkAlertDialog(R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            return;
        }

        Log.i("startGroup", "configDirectory: " + configDirectory);


        // Store moniker entered
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                BaseConfigActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("moniker", moniker);
        editor.commit();

        babbleService.start(configDirectory, groupName);
        mListener.onStartedNew(moniker);

    }

    private void displayOkAlertDialog(@StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
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
}
