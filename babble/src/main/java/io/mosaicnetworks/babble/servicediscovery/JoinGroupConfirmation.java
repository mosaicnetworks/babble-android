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

package io.mosaicnetworks.babble.servicediscovery;

import io.mosaicnetworks.babble.discovery.DiscoveryDataController;

/**
 * This interface allows additional steps in the workflow between the user selecting a group and
 * the actual join processing
 */
public interface JoinGroupConfirmation {
    /**
     * Called when the user has selected a ResolvedGroup. If the user confirms they wish to join,
     * {@link DiscoveryDataController#joinGroup(ResolvedGroup)} is called, which then effect the
     * NewJoinResolvedService instruction
     *
     * @param discoveryDataController The DiscoveryDataController to call back
     * @param resolvedGroup is the ResolvedGroup selected by the user
     */
    void joinRequested(DiscoveryDataController discoveryDataController, ResolvedGroup resolvedGroup);
}
