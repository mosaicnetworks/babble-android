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

public class ServicesListView extends RecyclerView {

    public interface ServicesListListener {

        void onServiceSelectedSuccess(NsdServiceInfo nsdServiceInfo);

        void onServiceSelectedFailure();

        void onDiscoveryStartFailure();

        void onListEmptyStatusChange(boolean empty);
    }

    private List<NsdDiscoveredService> mServiceInfoList = new ArrayList<>();
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
                adapter.getItem(position);
                mMdnsDiscovery.resolveService(adapter.getItem(position), new MdnsDiscovery.ResolutionListener() {
                    @Override
                    public void onServiceResolved(final NsdServiceInfo service) {
                        Handler mainHandler = new Handler(context.getMainLooper());

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mServicesListListener.onServiceSelectedSuccess(service);
                            }
                        };
                        mainHandler.post(myRunnable);

                    }

                    @Override
                    public void onResolveFailed() {
                        mServicesListListener.onServiceSelectedFailure();
                    }
                });

            }
        });

        setAdapter(adapter);

        mMdnsDiscovery = new MdnsDiscovery(context, mServiceInfoList, new MdnsDiscovery.ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated() {

                // let the adapter know
                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        getAdapter().notifyDataSetChanged();
                    }
                };
                mainHandler.post(myRunnable);

                // if the service list info's empty status has changed, let the service listener
                // know
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
        mMdnsDiscovery.discoverServices();
    }

    public void stopDiscovery() {
        mMdnsDiscovery.stopDiscovery();
    }

}
