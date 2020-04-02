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

package io.mosaicnetworks.sample_custom_ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import io.mosaicnetworks.babble.configure.OnBabbleConfigWritten;
import io.mosaicnetworks.babble.discovery.DiscoveryDataController;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.node.CannotStartBabbleNodeException;
import io.mosaicnetworks.babble.node.ConfigManager;
import io.mosaicnetworks.babble.node.GroupDescriptor;
import io.mosaicnetworks.babble.service.BabbleService2;
import io.mosaicnetworks.babble.service.BabbleServiceBinderActivity;
import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.JoinGroupConfirmation;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;
import io.mosaicnetworks.babble.servicediscovery.ServicesListView;
import io.mosaicnetworks.babble.servicediscovery.mdns.MdnsDataProvider;
import io.mosaicnetworks.babble.servicediscovery.mdns.ResolvedServiceMdnsFactory;
import io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCDataProvider;
import io.mosaicnetworks.babble.utils.DialogUtils;
import io.mosaicnetworks.babble.utils.Utils;

/**
 * This application demonstrates using the babble-android library without using the activities and
 * and fragments that are used in the other sample app. It is braodly functionally equivalent to
 * the other sample app.
 *
 * The workflow in this sample app has been streamlined. The moniker and protocol are predefined.
 * If the user has not set the moniker before (it is stored in SharedPreferences) then the user
 * is prompted on start up to enter it.
 *
 * The protocol defaults to WebRTC.
 *
 * In the Hamburger menu on the Main Activity, the user can change the protocol or moniker. But
 * it has been removed from the regular, new and join process.
 */
public class MainActivity extends BabbleServiceBinderActivity implements JoinGroupConfirmation,
        OnBabbleConfigWritten {

    private final String TAG = "MainActivity";
    private int mProtocol = BabbleConstants.NETWORK_GLOBAL;
    private String mMoniker = "";
    private String mConfigDirectory;
    private boolean mIsArchive = false;
    private ProgressDialog mLoadingDialog;
    private GroupDescriptor mGroupDescriptor;


   public static final String PREFERENCE_FILE_KEY = "babbleandroidcustomui";


    // Babble Section
    private DiscoveryDataController mDiscoveryDataController;
    private ResolvedGroupManager mResolvedGroupManager;
    private ServiceAdvertiser mServiceAdvertiser;

    // This will become the ServicesListView List when we wire it up
    private List<ResolvedGroup> mResolvedGroups;

    public SwipeRefreshLayout mSwipeRefreshServiceSearch;
    private SwipeRefreshLayout mSwipeRefreshDiscoveryFailed;
    private SwipeRefreshLayout mSwipeRefreshServicesDisplaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        BabbleService2.setAppState(new ChatState());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate: Started");
        initMoniker();

        setUpUI();

        setUpBabble();




        if (mMoniker.equals("")) {
            editMonikerClick(null);
        }

    }

    private void setUpUI() {
        mSwipeRefreshServiceSearch = findViewById(io.mosaicnetworks.babble.R.id.swipeRefresh_service_search);
        mSwipeRefreshDiscoveryFailed = findViewById(io.mosaicnetworks.babble.R.id.swiperefresh_discovery_failed);
        mSwipeRefreshServicesDisplaying = findViewById(io.mosaicnetworks.babble.R.id.swiperefresh_services_displaying);
        mSwipeRefreshServiceSearch.setVisibility(View.GONE);
        mSwipeRefreshDiscoveryFailed.setVisibility(View.GONE);
        mSwipeRefreshServicesDisplaying.setVisibility(View.VISIBLE);

        //TODO: JK25Mar amend this to work properly - currently it is forcing the display of results.


    }


    /**
     * method to initial Babble and set up all its dependencies
     */
    private void setUpBabble() {

        //TODO: split initialisation and screen rotation events


        // Initialise BabbleConstants
        try {
            BabbleConstants.initialise(this);

        } catch (RuntimeException ex) {
            //Do nothing
        }

        ServicesListView servicesListView = findViewById(R.id.servicesListView);
        mResolvedGroups =  servicesListView.getResolvedGroupList();

        Log.i(TAG, "  Create ResolvedGroupManager");
        mResolvedGroupManager = new ResolvedGroupManager(this, mResolvedGroups);

        Log.i(TAG, "  Register ServicesListView for updates");
        mResolvedGroupManager.registerServicesListUpdater(servicesListView);

        Log.i(TAG, "  Create DiscoveryDataController");
        mDiscoveryDataController = new DiscoveryDataController(this, mResolvedGroupManager);

        Log.i(TAG, "  Register with DiscoveryTestActivity as JoinGroupConfirmation");
        mDiscoveryDataController.registerJoinGroupConfirmation(this);

        Log.i(TAG, "  Register with DiscoveryTestActivity as OnFragmentInteractionListener(");
        mDiscoveryDataController.registerOnBabbleConfigWritten(this);

        mDiscoveryDataController.setMoniker(mMoniker);

        Log.i(TAG, "  Register DiscoveryDataController as ServicesListListener");
        servicesListView.registerServicesListListener(mDiscoveryDataController);


        MdnsDataProvider mdnsDataProvider = new MdnsDataProvider(this);
        String uidMdns = mDiscoveryDataController.registerDiscoveryProvider(mdnsDataProvider);

        WebRTCDataProvider webRTCDataProvider = new WebRTCDataProvider(this,
                BabbleConstants.DISCO_DISCOVERY_ADDRESS(), BabbleConstants.DISCO_DISCOVERY_PORT(),
                BabbleConstants.DISCO_DISCOVERY_ENDPOINT(), BabbleConstants.DISCO_DISCOVERY_POLLING_INTERVAL());


        String uidWebRTC = mDiscoveryDataController.registerDiscoveryProvider(webRTCDataProvider);

        mDiscoveryDataController.startDiscovery();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }




    public void newGroup(MenuItem menuItem) {

        ResolvedService resolvedService = null;
        String dataProviderId = mDiscoveryDataController.getDiscoveryDataProviderByProtocol(mProtocol);


        // First we create a resolved group
        switch (mProtocol) {
            case BabbleConstants.NETWORK_NONE:
                break;

            case BabbleConstants.NETWORK_WIFI:
/*
                String dataProviderId,
                InetAddress inetAddress,
                String inetString,
                int babblePort,
                int discoveryPort,
                String groupName,
                String groupUID,
                List<Peer> initialPeers,
                List<Peer> currentPeers,
                String moniker
                        */

                InetAddress ip = Utils.getIPAddr(this);

                resolvedService = ResolvedServiceMdnsFactory.NewNewResolvedService(
                        dataProviderId,
                        Utils.getIPAddr(this),

                );
                break;

            case BabbleConstants.NETWORK_GLOBAL:
                break;

            case BabbleConstants.NETWORK_P2P:
                break;

        }

        // Then we make an associated ResolvedGroup
        ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);

        // And register it with a DiscoveryDataProvider
        mDiscoveryDataController.addNewPseudoResolvedGroup(dataProviderId, resolvedGroup);
    }



    @Override
    public void joinRequested(DiscoveryDataController discoveryDataController, final ResolvedGroup resolvedGroup) {
        // Add a simple confirmation dialog to demonstrate the addition of UX steps in the NewJoinResolvedService process

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.join)
                .setMessage("NewJoinResolvedService group " + resolvedGroup.getGroupName() + "?")
                .setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDiscoveryDataController.joinGroup(resolvedGroup);
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .create();
        alertDialog.show();
    }



    @Override
    protected void onServiceDisconnected() {
        //Do nothing
    }

    private void configAndStartBabble(String peersAddr, String babbleAddr)  {
        ConfigManager configManager =
                ConfigManager.getInstance(getApplicationContext());
        try {
            mConfigDirectory = configManager.createConfigNewGroup(mGroupDescriptor, peersAddr, babbleAddr, mProtocol);
        } catch (CannotStartBabbleNodeException | IOException ex) {
            //TODO: think about this error handling
        }
        startBabbleService();
    }

    public void startBabbleService() {
        startService(new Intent(this, BabbleService2.class));
        mLoadingDialog = DialogUtils.displayLoadingDialog(this);
        mLoadingDialog.show();
        doBindService();
    }

    @Override
    protected void onServiceConnected() {
        try {
            mBoundService.start(mConfigDirectory, mGroupDescriptor, mServiceAdvertiser);
            startChatActivity();
        } catch (IllegalArgumentException ex) {
            // we'll assume this is caused by the node taking a while to leave a previous group,
            // though it could be that another application is using the port or WiFi is turned off -
            // in which case we'll keep getting stuck here until the port is available or WiFi is
            // turned on!
            DialogUtils.displayOkAlertDialog(this, R.string.babble_init_fail_title, R.string.babble_init_fail_message);
            mLoadingDialog.dismiss();
            stopService(new Intent(this, BabbleService2.class));
        }
        doUnbindService();
    }


    @Override
    public void startBabbleService(String configDir, GroupDescriptor groupDescriptor, boolean isArchive, ServiceAdvertiser serviceAdvertiser) {
        // Needs
        // mConfigDirectory, mGroupDescriptor, mServiceAdvertiser, mMoniker
        mConfigDirectory = configDir;
        mGroupDescriptor = groupDescriptor;
        mIsArchive = isArchive;
        mServiceAdvertiser = serviceAdvertiser;

        startBabbleService();
    }

    public void startChatActivity() {
        Intent intent = new Intent(this, ChatActivityAndroidService.class);
        intent.putExtra("MONIKER", mMoniker);
        intent.putExtra("ARCHIVE_MODE", mIsArchive);
        intent.putExtra("GROUP", mGroupDescriptor.getName());
        startActivity(intent);
    }


// About Menu

    public void aboutDialog(MenuItem menuItem) {

        String preBlock = "<table style=\"border-collapse:collapse;border-spacing:0;\">";
        String postBlock = "</table>\n";
        String predata = "<td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#EBF5FF;\">";
        String prelabel = "<tr><td style=\"padding:10px 5px;border-style:solid;border-width:1px;border-color:black;border-color:#9ABAD9;color:#444;background-color:#D2E4FC;\">";
        String postlabel = "</td>";
        String postdata = "</td></tr>\n";
        String databreak = "<tr><td colspan=\"2\" style=\"border-style:none;\">&nbsp;</td></tr>";

        String aboutText = "<hr>"+preBlock
                +prelabel+"App ID"+postlabel+predata + io.mosaicnetworks.sample_custom_ui.BuildConfig.APPLICATION_ID + postdata
                +prelabel+"Version Code"+postlabel+predata + io.mosaicnetworks.sample_custom_ui.BuildConfig.VERSION_CODE +postdata
                +prelabel+"Version Name"+postlabel+predata + io.mosaicnetworks.sample_custom_ui.BuildConfig.VERSION_NAME + postdata
                +prelabel+"Git Hash"+postlabel+predata + io.mosaicnetworks.sample_custom_ui.BuildConfig.GitHash + postdata
                +prelabel+"Git Branch"+postlabel+predata + io.mosaicnetworks.sample_custom_ui.BuildConfig.GitBranch+postdata
                +databreak
                +prelabel+ "Babble Package"+postlabel+predata + io.mosaicnetworks.babble.BuildConfig.LIBRARY_PACKAGE_NAME + postdata
                +prelabel+ "Version Code"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.VERSION_CODE + postdata
                +prelabel+ "Version Name"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.VERSION_NAME+ postdata
                +prelabel+ "Git Hash"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitHash+ postdata
                +prelabel+ "Git Hash Short"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitHashShort+ postdata
                +prelabel+ "Git Branch"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.GitBranch +postdata
                +databreak
                +prelabel+ "Babble Version"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleVersion +postdata
                +prelabel+ "Babble Repo"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleRepo +postdata
                +prelabel+ "Babble Method"+postlabel+predata+ io.mosaicnetworks.babble.BuildConfig.BabbleMethod +postdata

                +databreak
                +prelabel+"IP Address"+postlabel+predata+ Utils.getIPAddr(this)+postdata
                +postBlock + "\n<hr>\n";

        DialogUtils.displayOkAlertDialogHTML(this, R.string.about_title, aboutText);
    }



    /**
     * Reads the moniker from shared preferences and calls {@link #setMonikerLabel()} with the result.
     *
     */
    private void initMoniker() {
        // This has been moved from the New / NewJoinResolvedService workflow because it does not need to be there
        // Which makes the sample app simpler.

        SharedPreferences sharedPref = getSharedPreferences(
                PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        mMoniker = sharedPref.getString("moniker", "");

        setMonikerLabel();
    }

    /**
     * Creates a dialog with an Edit Text field for updating the Moniker. Write the value to
     * private variable mMoniker.
     *
     * @param menuItem
     */
    public void editMonikerClick(MenuItem menuItem) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_moniker, null);

        dialogBuilder.setTitle(R.string.moniker);

        final EditText editMoniker = (EditText) dialogView.findViewById(R.id.edt_moniker);
        editMoniker.setText(mMoniker);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMoniker = editMoniker.getText().toString();
                Log.i(TAG, "onClick: "+ mMoniker);
                setMonikerLabel();

                SharedPreferences sharedPref = getSharedPreferences(
                        PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("moniker", mMoniker);
                editor.apply();



                dialogBuilder.dismiss();
            }


        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }



    /**
     * Creates a dialog with a Radio Group for updating the Protocol. Write the value to
     * private variable mProtocol.
     *
     * @param menuItem
     */
    public void editProtocolClick(MenuItem menuItem) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_protocol, null);
        dialogBuilder.setTitle(R.string.network_type);

        final RadioGroup radioGroup = (RadioGroup)  dialogView.findViewById(R.id.protocolRadioGroup);
        int currentButt = -1;

        switch (mProtocol) {
            case BabbleConstants.NETWORK_WIFI: currentButt = R.id.rbMdns; break;
            case BabbleConstants.NETWORK_GLOBAL: currentButt = R.id.rbGlobal; break;
            case BabbleConstants.NETWORK_P2P: currentButt = R.id.rbP2p; break;
        }

        // Set current selection
        if (currentButt > 0) {
            final RadioButton radioButton = (RadioButton)   dialogView.findViewById(currentButt);
            radioButton.setChecked(true);
        }


        Button button1 = (Button) dialogView.findViewById(R.id.btSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.btCancel);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = radioGroup.getCheckedRadioButtonId();

                if (selected > 0) {
                    switch (selected) {
                        case R.id.rbP2p: mProtocol = BabbleConstants.NETWORK_P2P; break;
                        case R.id.rbMdns: mProtocol = BabbleConstants.NETWORK_WIFI; break;
                        case R.id.rbGlobal: mProtocol = BabbleConstants.NETWORK_GLOBAL; break;
                    }
                    setProtocolLabel();
                }

                dialogBuilder.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }


    private void setMonikerLabel() {
        String title;

        String app = getString(R.string.app_title);

        if (mMoniker.equals("")) {
            title = app;
        } else {
            title = "Welcome to "+app+", " + mMoniker;
        }

        setTitle(title);

        if (mDiscoveryDataController != null) {
            mDiscoveryDataController.setMoniker(mMoniker);
        }
    }

    /**
     * This function updates any visual representation of the protocol. It is called from
     * {@link #editProtocolClick(MenuItem)}
     */
    private void setProtocolLabel() {
        //DO Nothing
       // BabbleConstants.getNetworkDescription(mProtocol);
    }




}
