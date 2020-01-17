package io.mosaicnetworks.babble.configure;

import android.net.nsd.NsdServiceInfo;

import io.mosaicnetworks.babble.node.BabbleService;

/**
 * This interface defines listeners for the {@link JoinGroupFragment} and {@link NewGroupFragment}
 * fragments
 */
public interface OnFragmentInteractionListener {

    /**
     * Your app will need an instance of the {@link BabbleService}. This method provides acccess to
     * the service for the fragments in this activity.
     *
     * @return Your apps {@link BabbleService}.
     */
    BabbleService getBabbleService();

    /**
     * This method will be called when the {@link BabbleService} has successfully joined a group.
     *
     * @param moniker the moniker chosen by the user
     */
    void onJoined(String moniker);

    /**
     * This method will be called when the {@link BabbleService} has successfully started a new
     * group.
     *
     * @param moniker the moniker chosen by the user
     */
    void onStartedNew(String moniker);

    void onServiceSelected(NsdServiceInfo serviceInfo);

    void onArchiveSelected(String groupId);

}
