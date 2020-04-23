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

package io.mosaicnetworks.babble.discovery;

import android.content.Context;

import io.mosaicnetworks.babble.service.ServiceAdvertiser;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroupManager;

public interface DiscoveryDataProvider {

    /**
     * Called from DiscoveryDataController to assign a unique id to this data provider.
     * @param uid
     */
    void setUid(String uid);


    /**
     * Starts the discovery process
     * @param context
     * @param resolvedGroupManager
     */
    void startDiscovery(Context context, ResolvedGroupManager resolvedGroupManager);

    /**
     * Stops the discovery process
     */
    void stopDiscovery();

    /**
     * Called when a ResolvedGroup has been selected. This function applies any requirements
     * specific to this discovery type.
     *
     * @param resolvedGroup
     */
    void selectedDiscoveryResolveGroup(Context context, ResolvedGroup resolvedGroup);

    /**
     * Returns the {@link ServiceAdvertiser} for this Discovery Data Provider
     * @return
     */
    ServiceAdvertiser getAdvertiser();


    /**
     * When creating a new group, we pass a pseudo ResolvedGroup to the Discovery Data Provider
     * so the that Babble creation and advertising code is common between new and joining groups
     *
     * @param resolvedGroup
     */
    void addNewPseudoResolvedGroup(ResolvedGroup resolvedGroup);

    int getNetworkType();

    boolean isArchive();
}
