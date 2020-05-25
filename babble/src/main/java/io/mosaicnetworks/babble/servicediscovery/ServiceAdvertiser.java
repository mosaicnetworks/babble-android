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


import io.mosaicnetworks.babble.service.BabbleService;
import io.mosaicnetworks.babble.servicediscovery.mdns.PeersProvider;

/**
 * This interface defines the methods that an Advertiser passed to {@link BabbleService} would
 * need to implement. {@link io.mosaicnetworks.babble.servicediscovery.mdns.MdnsAdvertiser} and
 * {@link io.mosaicnetworks.babble.servicediscovery.webrtc.WebRTCService} implement this interface.
 */
public interface ServiceAdvertiser {

    //TODO: swap out the "string methods" with the "list methods"

    //boolean advertise(List<Peer> genesisPeers, List<Peer> currentPeers, PeersProvider peersProvider);
    boolean advertise(String genesisPeers, String currentPeers, PeersProvider peersProvider);

    void stopAdvertising();

    //void onPeersChange(List<Peer> newPeers);
    void onPeersChange(String newPeers);
}
