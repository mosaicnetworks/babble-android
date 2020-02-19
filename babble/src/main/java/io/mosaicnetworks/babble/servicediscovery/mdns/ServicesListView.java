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

package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServicesListView extends RecyclerView {

    public interface ServicesListListener {

        void onServiceSelectedSuccess(ResolvedGroup resolvedGroup);

        void onServiceSelectedFailure();

        void onDiscoveryStartFailure();

        void onListEmptyStatusChange(boolean empty);
    }

    private List<ResolvedGroup> mServiceInfoList = new ArrayList<>();
    private ServicesListListener mServicesListListener;
    private MdnsDiscovery mMdnsDiscovery;
    private boolean mPrevIsEmpty = true;

    public ServicesListView(Context context) {
        super(context);
        initialize(context);
    }

    public ServicesListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ServicesListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(final Context context) {

        setLayoutManager(new LinearLayoutManager(context));

        final ServicesListAdapter adapter = new ServicesListAdapter(context, mServiceInfoList);
        adapter.setClickListener(new ServicesListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mServicesListListener.onServiceSelectedSuccess(adapter.getItem(position));
            }
        });

        setAdapter(adapter);

        mMdnsDiscovery = new MdnsDiscovery(context, mServiceInfoList, new MdnsDiscovery.ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated(boolean groupCountChange) {
                // let the adapter know
                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Objects.requireNonNull(getAdapter()).notifyDataSetChanged();
                    }
                };
                mainHandler.post(myRunnable);

                // if the service list info's empty status has changed, let the service listener
                // know
                //TODO: can we use the groupCountChange (or a suitable return) to make this easier?
                final boolean curIsEmpty = mServiceInfoList.isEmpty();
                if (mPrevIsEmpty ^ curIsEmpty) {
                    //service list info's empty status has changed

                    Runnable emptyStatus = new Runnable() {
                        @Override
                        public void run() {
                            mServicesListListener.onListEmptyStatusChange(curIsEmpty);
                        }
                    };
                    mainHandler.post(emptyStatus);

                }

                mPrevIsEmpty = curIsEmpty;
            }

            @Override
            public void onStartDiscoveryFailed() {
                mServicesListListener.onDiscoveryStartFailure();
            }
        });
    }

    public void startDiscovery(ServicesListListener servicesListListener) {
        mServicesListListener = servicesListListener;
        //mMdnsDiscovery.discoverServices();
        mServicesListListener.onDiscoveryStartFailure();
    }

    public void stopDiscovery() {
        mMdnsDiscovery.stopDiscovery();
    }

}
