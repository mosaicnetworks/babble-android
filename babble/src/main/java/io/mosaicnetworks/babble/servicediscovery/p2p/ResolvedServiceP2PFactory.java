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

package io.mosaicnetworks.babble.servicediscovery.p2p;/*
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

import com.google.common.net.InetAddresses;

import java.util.Map;

import io.mosaicnetworks.babble.discovery.PeersFactory;
import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

public abstract class ResolvedServiceP2PFactory {

    public static ResolvedService NewJoinResolvedService(String dataProviderId,  Map<String, String> map) {

        return new ResolvedService(
                dataProviderId,
                InetAddresses.forString((String) map.get(BabbleConstants.DNS_TXT_HOST_LABEL)),
                "",
                BabbleConstants.BABBLE_PORT(),
                Integer.parseInt((String) map.get(BabbleConstants.DNS_TXT_PORT_LABEL)),
                map,
                (String) map.get(BabbleConstants.DNS_TXT_APP_LABEL),
                (String) map.get(BabbleConstants.DNS_TXT_GROUP_LABEL),
                (String) map.get(BabbleConstants.DNS_TXT_GROUP_ID_LABEL),
                PeersFactory.toPeersList((String) map.get(BabbleConstants.DNS_TXT_INITIAL_PEERS_LABEL)),
                PeersFactory.toPeersList((String) map.get(BabbleConstants.DNS_TXT_CURRENT_PEERS_LABEL))
        );   //TODO: JK20Mar review this
    }

}
