package io.mosaicnetworks.babble.configure;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment lets the user choose between joining an existing group or creating a new one
 * Use the {@link ArchivedGroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchivedGroupsFragment extends Fragment implements ArchivedGroupsAdapter.ItemClickListener {

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRvArchivedGroups;
    private ArchivedGroupsAdapter mArchivedGroupsAdapter;
    private List<ConfigDirectory> mArchivedList = new ArrayList<>();
    private ConfigManager mConfigManager;
    private View previousLongClickView;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;

    public ArchivedGroupsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment ArchivedGroupsFragment.
     */
    public static ArchivedGroupsFragment newInstance() {
        return new ArchivedGroupsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfigManager = ConfigManager.getInstance(getContext().getApplicationContext());
        initActionModeCallback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_archived_groups, container, false);
        mRvArchivedGroups = view.findViewById(R.id.rv_archived_groups);
        mRvArchivedGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        mArchivedGroupsAdapter = new ArchivedGroupsAdapter(getContext(), mArchivedList);
        mArchivedGroupsAdapter.setClickListener(this);
        mRvArchivedGroups.setAdapter(mArchivedGroupsAdapter);
        return view;
    }

    @Override
    public void onItemShortClick(ConfigDirectory configDirectory) {


        Log.i("onItemClick", configDirectory.directoryName);


        try {
            mConfigManager.configureArchive(configDirectory, Utils.getIPAddr(getContext()), ConfigManager.DEFAULT_BABBLING_PORT);

        } catch (IOException e)
        {
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot load configuration: "+  e.getMessage() );
            return;
        } catch (Exception e)
        {
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot load configuration: "+ e.getClass().getCanonicalName()+": "+ e.getMessage() );
            throw e;
        }



        try {

            String configDir = mConfigManager.getTomlDir();
            Log.d("MY-TAG", "Config directory name: " + configDir);
            BabbleService<?> babbleService = mListener.getBabbleService();
            babbleService.start(configDir, configDir); //TODO: need to NOT advertise mDNS
            mListener.onArchiveLoaded(mConfigManager.getMoniker());

        } catch (IllegalArgumentException ex) {
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: Illegal Argument: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage() );
        } catch (Exception ex) {
            //TODO: Some sensible error handling here.
            //Errors on starting the babble service were untrapped and killing the app
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage() );
            throw ex;
  //          Toast.makeText(getContext(), "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    /** Called when the user long clicks a group in the archive list*/
    public void onItemLongClick(View view, int position) {

        // un-highlight previously selected view
        if (previousLongClickView != null) {
            previousLongClickView.setSelected(false);
        }

        // highlight selected
        view.setSelected(true);
        previousLongClickView = view;

        // update selected contact
        //selectedContact = adapter.getItem(position);

        // Start CAB if not already started
        if (mActionMode == null) {
            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        }
    }

    private void initActionModeCallback() {
        mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.archive_action, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                /*switch (item.getItemId()) {
                    case R.id.delete_contact:

                        if (db.userConfigDao().getAll().get(0).getPubKeyHex().equals(selectedContact.getPubKeyHex())) {
                            // contact is the user do not precede
                            AlertDialog alertDialog = new AlertDialog.Builder(ContactsActivity.this).create();
                            alertDialog.setTitle("Cannot delete contact");
                            alertDialog.setMessage("You can't delete yourself!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        } else {


                            List<GroupEntry> groups = db.groupContactJoinDao().getGroupsForContacts(selectedContact.getPubKeyHex());

                            if (groups.isEmpty()) {
                                //safe to delete - check user intention
                                AlertDialog alertDialog = new AlertDialog.Builder(ContactsActivity.this).create();
                                alertDialog.setTitle("Delete contact?");
                                alertDialog.setMessage("Are you sure you want to delete this contact?");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.contactDao().delete(selectedContact);
                                                //refresh the contacts list by restarting the activity
                                                Intent intent = new Intent(ContactsActivity.this, ContactsActivity.class);
                                                startActivity(intent);
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();



                            } else {
                                //contact is a member of at least one group - do not delete
                                AlertDialog alertDialog = new AlertDialog.Builder(ContactsActivity.this).create();
                                alertDialog.setTitle("Cannot delete contact");
                                alertDialog.setMessage("The contact is a member of at least one group. Delete all groups they are members of first.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }

                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }*/

                return true;
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // un-highlight previously selected view
                if (previousLongClickView != null) {
                    previousLongClickView.setSelected(false);
                }

                mActionMode = null;
            }
        };
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
    public void onStart() {
        super.onStart();


     // This code trims all backups from the folder list
        ArrayList<ConfigDirectory> configFolders = mConfigManager.getDirectories();
        for (ConfigDirectory temp : configFolders) {
            if (! temp.isBackup) {
                mArchivedList.add(temp);
            }
        }


     // If you want all backups - with multiple versions of a single archive, the block above can be replaced by the line below.
  //      mArchivedList.addAll(mConfigManager.getDirectories());




        mArchivedGroupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mArchivedList.clear();
        mArchivedGroupsAdapter.notifyDataSetChanged();
    }


    //TODO: Review if we need these functions in Archive, Join and New fragments.
    private void displayOkAlertDialog(@StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }



    private void displayOkAlertDialogText(@StringRes int titleId, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(titleId)
                .setMessage(message)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }
}

class ArchivedGroupsAdapter extends RecyclerView.Adapter<ArchivedGroupsAdapter.ViewHolder> {

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView genericTextView;

        ViewHolder(View itemView) {
            super(itemView);
            genericTextView = itemView.findViewById(R.id.serviceName);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemShortClick(mData.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) mClickListener.onItemLongClick(view, getAdapterPosition());
            // Return true to indicate the click was handled
            return true;
        }
    }

    private List<ConfigDirectory> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public ArchivedGroupsAdapter(Context context, List<ConfigDirectory> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.service_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String serviceName = mData.get(position).description;

        holder.genericTextView.setText(serviceName);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }

    // convenience method for getting data at click position
    public ConfigDirectory getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {

        void onItemShortClick(ConfigDirectory configDirectory);

        void onItemLongClick(View view, int position);
    }

}
