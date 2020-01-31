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

package io.mosaicnetworks.babble.configure;

import android.net.nsd.NsdServiceInfo;

import io.mosaicnetworks.babble.node.BabbleService;

/**
 * This interface defines listeners for the join group and new group fragments
 */
public interface OnFragmentInteractionListener {

    /**
     * Your app will need an instance of the {@link BabbleService}. This method provides access to
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

    /**
     * This method will be called when the {@link BabbleService} has loaded an archive group
     * @param moniker the moniker as chosen previous by the user. //TODO - do we need this
     */
    void onArchiveLoaded(String moniker);

    void onServiceSelected(NsdServiceInfo serviceInfo);


}
