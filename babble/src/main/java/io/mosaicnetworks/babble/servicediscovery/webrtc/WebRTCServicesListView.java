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

package io.mosaicnetworks.babble.servicediscovery.webrtc;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ServiceDiscoveryListener;
import io.mosaicnetworks.babble.servicediscovery.ServicesListListener;

public class WebRTCServicesListView extends RecyclerView {

    private List<ResolvedGroup> mServiceInfoList = new ArrayList<>();
    private ServicesListListener mServicesListListener;
    private WebRTCService mWebRTCService;
    private boolean mPrevIsEmpty = true;

    public WebRTCServicesListView(Context context) {
        super(context);
        initialize(context);
    }

    public WebRTCServicesListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public WebRTCServicesListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(final Context context) {

        setLayoutManager(new LinearLayoutManager(context));

        final WebRTCServicesListAdapter adapter = new WebRTCServicesListAdapter(context, mServiceInfoList);
        adapter.setClickListener(new WebRTCServicesListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mServicesListListener.onServiceSelectedSuccess(adapter.getItem(position));
            }
        });

        setAdapter(adapter);

        // XXX Why are we doing this here? It is also done in ViewModel

        mWebRTCService = WebRTCService.getInstance(context);

        mWebRTCService.setResolvedGroups(mServiceInfoList);

        mWebRTCService.registerServiceDiscoveryListener(new ServiceDiscoveryListener() {
                 @Override
                 public void onServiceListUpdated(boolean groupCountChange) {


                     Log.i("WebRTCServicesListView", "onServiceListUpdated: ");



                     // let the adapter know
                     Handler mainHandler = new Handler(context.getMainLooper());

                     Runnable myRunnable = new Runnable() {
                         @Override
                         public void run() {
                             Log.i("WebRTCServicesListView", "run: ");
                             Objects.requireNonNull(getAdapter()).notifyDataSetChanged();
                             Log.i("WebRTCServicesListView", "ran: ");
                         }
                     };
                     mainHandler.post(myRunnable);

                     // if the service list info's empty status has changed, let the service listener
                     // know
                     //TODO: can we use the groupCountChange (or a suitable return) to make this easier?
                     final boolean curIsEmpty = mServiceInfoList.isEmpty();

                     Log.i("WebRTCServicesListView", "isEmpty: "+curIsEmpty);

                     //TODO: This check has just been commented out. We could be more efficient.
 //                    if (mPrevIsEmpty ^ curIsEmpty) {
                         //service list info's empty status has changed

                         Runnable emptyStatus = new Runnable() {
                             @Override
                             public void run() {
                                 Log.i("WebRTCServicesListView", "emptyStatus: ");
                                 mServicesListListener.onListEmptyStatusChange(curIsEmpty);
                             }
                         };
                         mainHandler.post(emptyStatus);

   //                  }

                     mPrevIsEmpty = curIsEmpty;

                 }

                 @Override
                 public void onStartDiscoveryFailed() {
                     Log.i("WebRTCServicesListView", "onStartDiscoveryFailed");
                     mServicesListListener.onDiscoveryStartFailure();
                 }

        });

    }

    public void startDiscovery(ServicesListListener servicesListListener) {
        mServicesListListener = servicesListListener;
        mWebRTCService.discoverService();
    }

    public void refreshDiscovery() {
        Log.i("WebRTCServicesListView", "refreshDiscovery: ");
        mWebRTCService.discoverService();
    }

    public void stopDiscovery() {
        mWebRTCService.stopDiscoverService();
    }

}
