package io.mosaicnetworks.babble.configure;

import io.mosaicnetworks.babble.node.BabbleService;

public interface OnFragmentInteractionListener {

    BabbleService getBabbleService();

    void onStartedNew(String moniker);

    void onJoined(String moniker);

}
