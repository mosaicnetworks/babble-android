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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.ConfigDirectory;

class ArchivedGroupsAdapter extends RecyclerView.Adapter<ArchivedGroupsAdapter.ViewHolder> {

    private SelectableData<ConfigDirectory> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView groupNameTextView;
        TextView groupUidTextView;
        private View mView;

        ViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.serviceName);
            groupUidTextView = itemView.findViewById(R.id.groupUid);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mView = itemView;
        }

        void setSelected(Boolean selected) {
            mView.setSelected(selected);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemShortClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) mClickListener.onItemLongClick(view, getAdapterPosition());

            // Return true to indicate the click was handled
            return true;
        }
    }

    public ArchivedGroupsAdapter(Context context, SelectableData<ConfigDirectory> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.service_recyclerview_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConfigDirectory configDirectory = mData.get(position);
        String serviceName = configDirectory.description;
        String groupUid = configDirectory.uniqueId;

        holder.groupNameTextView.setText(serviceName);
        holder.groupUidTextView.setText(groupUid);

        int colourGroupName;
        int colourGroupUid;
        if (configDirectory.isBackup) {
            colourGroupName = R.color.colorArchivedGroup;
            colourGroupUid = R.color.colorArchivedGroup;
        } else {
            colourGroupName = android.R.color.primary_text_light;
            colourGroupUid = android.R.color.secondary_text_light;
        }

        holder.groupNameTextView.setTextColor(mContext.getResources().getColor(colourGroupName));
        holder.groupUidTextView.setTextColor(mContext.getResources().getColor(colourGroupUid));

        if (mData.isSelected(position)) {
            holder.setSelected(true);
        } else {
            holder.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement these methods to respond to click events
    public interface ItemClickListener {

        void onItemShortClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}
