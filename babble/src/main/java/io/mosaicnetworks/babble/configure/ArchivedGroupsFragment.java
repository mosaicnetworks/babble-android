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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment lets the user choose between joining an existing group or creating a new one
 * Use the {@link ArchivedGroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchivedGroupsFragment extends Fragment implements ArchivedGroupsAdapter.ItemClickListener {

    private OnFragmentInteractionListener mListener;
    private ArchivedGroupsAdapter mArchivedGroupsAdapter;
    private List<ConfigDirectory> mArchivedList = new ArrayList<>();
    private ConfigManager mConfigManager;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    private List<ConfigDirectory> mSelectedGroupsConfigs = new ArrayList<>();
    private List<View> mSelectedGroupsViews = new ArrayList<>();
    private ArchivedGroupsViewModel mViewModel;
    private RecyclerView mRvArchivedGroups;
    private LinearLayout mLinearLayoutArchiveLoading;
    private Boolean mSelected;


    //TODO: either expose this switch or remove it.
    /**
     * This switch controls whether all archive versions are displayed or just the "Live" ones.
     */
    private boolean mShowAllArchiveVersion = true;


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
        try {
            mConfigManager = ConfigManager.getInstance(Objects.requireNonNull(getContext()).getApplicationContext());
        } catch (FileNotFoundException ex) {
            //TODO: We cannot rethrow this exception as the overridden method does not throw it.
            //This error is thrown by ConfigManager when it fails to read / create a babble root dir.
            //This is probably a fatal error.
            displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot write configuration. Aborting.");
            throw new IllegalStateException();  // Throws a runtime exception that is deliberately not caught
                                                // The app will terminate. But babble is unstartable from here.
        }
        initActionModeCallback();

        mViewModel = ViewModelProviders.of(this, new ArchivedGroupsViewModelFactory(mListener.getBabbleService())).get(ArchivedGroupsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_archived_groups, container, false);
        mArchivedGroupsAdapter = new ArchivedGroupsAdapter(getContext(), mArchivedList);
        mArchivedGroupsAdapter.setClickListener(this);
        mRvArchivedGroups = view.findViewById(R.id.rv_archived_groups);
        mRvArchivedGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvArchivedGroups.setAdapter(mArchivedGroupsAdapter);

        mLinearLayoutArchiveLoading = view.findViewById(R.id.linearLayout_archive_loading);

        final Observer<ArchivedGroupsViewModel.State> viewModelState = new Observer<ArchivedGroupsViewModel.State>() {
            @Override
            public void onChanged(@Nullable final ArchivedGroupsViewModel.State newState) {
                onViewModelStateChanged(newState);
            }
        };

        mViewModel.getState().observe(this, viewModelState);

        return view;
    }

    private void onViewModelStateChanged(ArchivedGroupsViewModel.State state) {

        switch (state) {
            case LIST:
                mRvArchivedGroups.setVisibility(View.VISIBLE);
                mLinearLayoutArchiveLoading.setVisibility(View.GONE);
                break;
            case LOADING:
                mRvArchivedGroups.setVisibility(View.GONE);
                mLinearLayoutArchiveLoading.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                //If it loads when we're on a different tab then abort. This does mean it's possible
                //for a configuration change to cause an abort
                if (isResumed()) {
                    mListener.onArchiveLoaded("made up moniker"); //TODO: fix moniker
                } else {
                    mListener.getBabbleService().leave(null);
                    mViewModel.getState().setValue(ArchivedGroupsViewModel.State.LIST);
                }
                break;
            case FAILED:
                displayOkAlertDialog(R.string.archive_load_fail_title, R.string.archive_load_fail_message);
                mViewModel.getState().setValue(ArchivedGroupsViewModel.State.LIST);
        }
    }

    @Override
    public void onItemShortClick(View view, ConfigDirectory configDirectory) {

        if (mSelectedGroupsViews.isEmpty()) {

            try {
                mConfigManager.setGroupToArchive(configDirectory, Utils.getIPAddr(Objects.requireNonNull(getContext())), ConfigManager.DEFAULT_BABBLING_PORT);

            } catch (IOException e) {
                displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot load configuration: " + e.getMessage());
                return;
            } catch (Exception e) {
                displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot load configuration: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
                throw e;
            }

            try {
                String configDir = mConfigManager.getTomlDir();
                mViewModel.loadArchive(configDir, new GroupDescriptor("Archived Group")); //TODO: need to get a proper group descriptor

            } catch (IllegalArgumentException ex) {
                displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: Illegal Argument: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
            } catch (Exception ex) {
                //TODO: Some sensible error handling here.
                //Errors on starting the babble service were untrapped and killing the app
                displayOkAlertDialogText(R.string.babble_init_fail_title, "Cannot start babble: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
                throw ex;
                //          Toast.makeText(getContext(), "Cannot start babble: "+ ex.getClass().getCanonicalName()+": "+ ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            mSelectedGroupsConfigs.add(configDirectory);
            mSelectedGroupsViews.add(view);
            view.setSelected(true);

            //TODO: need to un select if clicked again?
        }
    }

    /** Called when the user long clicks a group in the archive list*/
    public void onItemLongClick(View view, ConfigDirectory configDirectory) {

        // un-highlight previously selected views
        for (View viewS:mSelectedGroupsViews) {
            viewS.setSelected(false);
        }
        // highlight selected
        view.setSelected(true);

        mSelectedGroupsConfigs = new ArrayList<>();
        mSelectedGroupsConfigs.add(configDirectory);

        mSelectedGroupsViews = new ArrayList<>();
        mSelectedGroupsViews.add(view);

        if (mActionMode == null) {
            // Start the CAB
            mActionMode = Objects.requireNonNull(getActivity()).startActionMode(mActionModeCallback);
        }
    }

    private void initActionModeCallback() {
        mActionModeCallback = new ActionMode.Callback() {

            // Called when startActionMode() is called
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
                return false; // nothing is done so return false
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.delete_contact) {

                    for (ConfigDirectory configDirectory: mSelectedGroupsConfigs) {
                        mConfigManager.deleteDirectoryAndBackups(configDirectory.directoryName, false);
                        mArchivedList.remove(configDirectory);
                    }

                    mArchivedGroupsAdapter.notifyDataSetChanged();
                    mode.finish();

                    return true;
                }

                return false;
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // un-highlight previously selected views
                for (View lView:mSelectedGroupsViews) {
                    lView.setSelected(false);
                }

                mSelectedGroupsViews.clear();
                mSelectedGroupsConfigs.clear();

                mActionMode = null;
            }
        };
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
    public void onStart() {
        super.onStart();

        //TODO: This is currently a hardcoded boolean. But it is coded this way to preserve both
        //methods, and potentially it will be switchable in the future
        if (mShowAllArchiveVersion) {
            mArchivedList.addAll(mConfigManager.getDirectories());
        } else {
            // This code trims all backups from the folder list
            ArrayList<ConfigDirectory> configFolders = mConfigManager.getDirectories();
            for (ConfigDirectory temp : configFolders) {
                if (!temp.isBackup) {
                    mArchivedList.add(temp);
                }
            }
        }

        mArchivedGroupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mArchivedList.clear();
        mArchivedGroupsAdapter.notifyDataSetChanged();

        if (mViewModel.getState().getValue() == ArchivedGroupsViewModel.State.LOADED) {
            mViewModel.getState().setValue(ArchivedGroupsViewModel.State.LIST);
        }
    }

    //TODO: Review if we need these functions in Archive, Join and New fragments.
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
            if (mClickListener != null) mClickListener.onItemShortClick(view, mData.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) mClickListener.onItemLongClick(view, mData.get(getAdapterPosition()));
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
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        void onItemShortClick(View view, ConfigDirectory configDirectory);

        void onItemLongClick(View view, ConfigDirectory configDirectory);
    }

}
