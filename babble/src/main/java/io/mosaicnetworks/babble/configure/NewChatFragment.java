package io.mosaicnetworks.babble.configure;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewChatFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public NewChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewChatFragment.
     */
    public static NewChatFragment newInstance() {
        return new NewChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_new_chat, container, false);
        final View newChatButton = view.findViewById(R.id.button);
        newChatButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startChat(view);
                    }
                }
        );

        EditText edit = (EditText) view.findViewById(R.id.editText);
        edit.requestFocus();

        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);

        return view;
    }

    // called when the user presses the start chat button
    public void startChat(View view) {
        //TODO: check this is safe
        BabbleService<?> babbleService = mListener.getBabbleService();
        //get moniker
        EditText editText = view.findViewById(R.id.editText);
        String moniker = editText.getText().toString();
        if (moniker.isEmpty()) {
            displayOkAlertDialog(R.string.no_moniker_alert_title, R.string.no_moniker_alert_message);
            return;
        }

        //MessagingService messagingService = MessagingService.getInstance();
        try {
            babbleService.configureNew(moniker, Utils.getIPAddr(getContext()));
        } catch (IllegalStateException ex) {
            //TODO: just catch IOException - this will mean the port is in use
            //we'll assume this is caused by the node taking a while to leave a previous chat,
            //though it could be that another application is using the port - in which case
            //we'll keep getting stuck here until the port is available!
            displayOkAlertDialog(R.string.babble_busy_title, R.string.babble_busy_message);
            return;
        }

        babbleService.start();
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
