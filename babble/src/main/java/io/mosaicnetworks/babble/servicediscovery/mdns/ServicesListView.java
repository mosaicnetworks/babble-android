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
    }

    private List<NsdDiscoveredService> mServiceInfoList = new ArrayList<>();
    private ServicesListListener mServicesListListener;
    private MdnsDiscovery mMdnsDiscovery;

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

                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        getAdapter().notifyDataSetChanged();
                    }
                };
                mainHandler.post(myRunnable);
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
