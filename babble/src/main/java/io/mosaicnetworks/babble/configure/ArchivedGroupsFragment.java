package io.mosaicnetworks.babble.configure;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;


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
    public void onItemClick(ConfigDirectory configDirectory) {

        String configDir = mConfigManager.getRootDir() + File.separator + ConfigManager.BABBLE_ROOTDIR + File.separator + configDirectory.directoryName; //TODO: clean up!
        Log.d("MY-TAG", "Config directory name: " + configDir);
        BabbleService<?> babbleService = mListener.getBabbleService();
        babbleService.start(configDir);
        mListener.onArchiveLoaded(""); //TODO: fix moniker
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
        mArchivedList.addAll(mConfigManager.getDirectories());
        mArchivedGroupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mArchivedList.clear();
    }
}

class ArchivedGroupsAdapter extends RecyclerView.Adapter<ArchivedGroupsAdapter.ViewHolder> {

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView genericTextView;

        ViewHolder(View itemView) {
            super(itemView);
            genericTextView = itemView.findViewById(R.id.serviceName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(mData.get(getAdapterPosition()));
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
        void onItemClick(ConfigDirectory configDirectory);
    }


}
