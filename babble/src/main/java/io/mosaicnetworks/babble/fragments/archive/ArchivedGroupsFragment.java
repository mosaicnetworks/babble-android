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

package io.mosaicnetworks.babble.fragments.archive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.configure.OnFragmentInteractionListener;
import io.mosaicnetworks.babble.configure.SelectableData;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.service.BabbleService;
import io.mosaicnetworks.babble.service.BabbleServiceBinder;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This fragment lets the user choose between joining an existing group or creating a new one
 * Use the {@link ArchivedGroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchivedGroupsFragment extends BabbleServiceBinder implements ArchivedGroupsAdapter.ItemClickListener {

    public static boolean reloadArchive = false;
    private OnFragmentInteractionListener mListener;
    private ArchivedGroupsAdapter mArchivedGroupsAdapter;
    private SelectableData<ConfigDirectory> mArchivedList = new SelectableData<>();
    private ConfigManager mConfigManager;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    private ArchivedGroupsViewModel mViewModel;
    private RecyclerView mRvArchivedGroups;
    private LinearLayout mLinearLayoutNoArchives;
    private String mMoniker;
    private ProgressDialog mLoadingDialog;
    private int mBabbleArchivePort = 6666;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
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

        mViewModel = new ViewModelProvider(this, new ArchivedGroupsViewModelFactory(mConfigManager)).get(ArchivedGroupsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_archived_groups, container, false);

        mRvArchivedGroups = view.findViewById(R.id.rv_archived_groups);
        mLinearLayoutNoArchives = view.findViewById(R.id.linearLayout_no_archives);
        
        return view;
    }

    @Override
    public void onItemShortClick(View view, int position) {

        if (mArchivedList.anySelected()) {

            if (mArchivedList.isSelected(position)) {
                mArchivedList.unSelect(position);
                view.setSelected(false);
                if  (!mArchivedList.anySelected()) {
                    mActionMode.finish();
                }
            } else {
                mArchivedList.select(position);
                view.setSelected(true);
            }
        } else {

            ConfigDirectory configDirectory = mArchivedList.get(position);

            mConfigManager.setGroupToArchive(configDirectory, Utils.getIPAddr(Objects.requireNonNull(getContext())), mBabbleArchivePort);
            mMoniker = mConfigManager.getMoniker();
            getActivity().startService(new Intent(getActivity(), BabbleService.class));
            mLoadingDialog = DialogUtils.displayLoadingDialog(getContext());
            mLoadingDialog.show();
            doBindService();
        }
    }

    public void onItemLongClick(View view, int position) {

        if (mActionMode == null) {
            mActionMode = Objects.requireNonNull(getActivity()).startActionMode(mActionModeCallback);
        }

        List<Integer> selectedPositions = new ArrayList<>();
        selectedPositions.addAll(mArchivedList.getAllSelected());

        mArchivedList.unSelectAll();

        for (Integer pos:selectedPositions) {
            mArchivedGroupsAdapter.notifyItemChanged(pos);
        }

        mArchivedList.select(position);
        view.setSelected(true);

    }

    private void initActionModeCallback() {
        mActionModeCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.archive_action, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // nothing is done so return false
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.delete_contact) {

                    for (int selectedPosition:mArchivedList.getAllSelected()) {
                        ConfigDirectory configDirectory = mArchivedList.get(selectedPosition);
                        mConfigManager.deleteDirectoryAndBackups(configDirectory.directoryName, false);
                    }

                    mArchivedList.removeAllSelected();

                    if (mArchivedList.isEmpty()) {
                        mLinearLayoutNoArchives.setVisibility(View.VISIBLE);
                        mRvArchivedGroups.setVisibility(View.GONE);
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
                if (isResumed()) {
                    mArchivedList.unSelectAll();
                    mArchivedGroupsAdapter.notifyDataSetChanged();
                }
                mActionMode = null;
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        try {
            mBoundService.startArchive(mConfigManager.getTomlDir());
            mListener.onArchiveLoaded(mMoniker, "Archived Group");
        } catch (IllegalArgumentException ex) {
            DialogUtils.displayOkAlertDialog(Objects.requireNonNull(getContext()), R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            getActivity().stopService(new Intent(getActivity(), BabbleService.class));
        }

        doUnbindService();
    }

    @Override
    protected void onServiceDisconnected() {

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
        mViewModel.getArchivedList().postValue(mArchivedList);

        if (mActionMode!=null) {
            mActionMode.finish();
        }

        if (mLoadingDialog!=null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (reloadArchive) {
            mViewModel.loadArchiveList();
            reloadArchive = false;
        }

        mArchivedList = mViewModel.getArchivedList().getValue();

        mArchivedGroupsAdapter = new ArchivedGroupsAdapter(getContext(), mArchivedList);
        mArchivedGroupsAdapter.setClickListener(this);

        mRvArchivedGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvArchivedGroups.setAdapter(mArchivedGroupsAdapter);

        if (mArchivedList.anySelected()) {
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog!=null) {
            mLoadingDialog.dismiss();
        }
    }
}
