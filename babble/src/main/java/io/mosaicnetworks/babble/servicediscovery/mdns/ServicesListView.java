package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ServicesListView extends RecyclerView {

    private List<NsdServiceInfo> mServiceInfoList = new ArrayList<>();

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

    private void initialize(Context context) {



        setLayoutManager(new LinearLayoutManager(context));

        ServicesListAdapter adapter = new ServicesListAdapter(context, mServiceInfoList);
        adapter.setClickListener(new ServicesListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //do nothing
            }
        });

        setAdapter(adapter);

        MdnsDiscovery mdnsDiscovery = new MdnsDiscovery(context, mServiceInfoList, new MdnsDiscovery.ServiceDiscoveryListener() {
            @Override
            public void onServiceListUpdated() {
                getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onStartDiscoveryFailed() {
                //do nothing
            }
        });

        mdnsDiscovery.discoverServices();
    }

}
