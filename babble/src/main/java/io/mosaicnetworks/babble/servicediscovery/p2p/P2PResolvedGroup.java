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

package io.mosaicnetworks.babble.servicediscovery.p2p;

import java.util.ArrayList;
import java.util.List;

import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

/**
 * This represents a group of resolved services with the same group UID. Services with a matching
 * group UID and group name can be added to the group as they're discovered. Services can be removed
 * from the group as and when they are lost.
 */
public final class P2PResolvedGroup {

    private final String mGroupName;
    private final String mGroupUid;
    private final List<P2PResolvedService> mResolvedServices = new ArrayList<>();

    /**
     * Constructor, the group is initialised from a resolved service
     * @param resolvedService a resolved service used to initialise the group
     */
    public P2PResolvedGroup(P2PResolvedService resolvedService) {
        mGroupName = resolvedService.getGroupName();
        mGroupUid = resolvedService.getGroupUid();
        mResolvedServices.add(resolvedService);
    }

    /**
     * Adds a resolved service to this group's list of resolved services
     * @param resolvedService
     */
    public void addService(P2PResolvedService resolvedService) {
        if (mResolvedServices.contains(resolvedService)) {
            throw new IllegalArgumentException("Cannot add service: Group already contains this service");
        }

        if (!resolvedService.getGroupUid().equals(mGroupUid)) {
            throw new IllegalArgumentException("Cannot add service: Service group UID does not match this group's UID");
        }

        if (!resolvedService.getGroupName().equals(mGroupName)) {
            throw new IllegalArgumentException("Cannot add service: Service group name does not match this group's name");
        }

        mResolvedServices.add(resolvedService);
    }

    /**
     * Removes a resolved service from this group's list of resolved services
     * @param resolvedService the service to be removed
     * @return
     */
    public boolean removeService(ResolvedService resolvedService) {

        if (!mResolvedServices.contains(resolvedService)) {
            throw new IllegalArgumentException("Cannot remove service: Group does not contain this service");
        }

        mResolvedServices.remove(resolvedService);

        return mResolvedServices.isEmpty();
    }

    /**
     * Get the list of resolved services associated with this group
     * @return a shallow copy of the list of services associated with this group
     */
    public List<P2PResolvedService> getResolvedServices() {
        return new ArrayList<>(mResolvedServices);
    }

    /**
     * Get the group name
     * @return the group name
     */
    public String getGroupName() {
        return mGroupName;
    }

    /**
     * Get the group UID
     * @return the group UID
     */
    public String getGroupUid() {
        return mGroupUid;
    }
}
