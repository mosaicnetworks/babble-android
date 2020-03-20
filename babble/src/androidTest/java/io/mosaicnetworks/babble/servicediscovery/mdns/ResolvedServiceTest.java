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

package io.mosaicnetworks.babble.servicediscovery.mdns;

import android.net.nsd.NsdServiceInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.mosaicnetworks.babble.node.BabbleConstants;
import io.mosaicnetworks.babble.servicediscovery.ResolvedGroup;
import io.mosaicnetworks.babble.servicediscovery.ResolvedService;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ResolvedServiceTest {

    @Test
    public void validTxtRecordTest() throws UnknownHostException {

        String serviceType = "test_serviceType";
        String serviceName = "test_serviceName";
        InetAddress inetAddress = InetAddress.getByName("192.168.2.1");
        int port = 0;
        String appIdentifier = "test_appIdentifier";
        String groupName = "test_groupName";
        String groupUid = "test_groupUid";

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceType(serviceType);
        nsdServiceInfo.setServiceName(serviceName);
        nsdServiceInfo.setHost(inetAddress);
        nsdServiceInfo.setPort(port);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_APP_LABEL, appIdentifier);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_LABEL, groupName);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, groupUid);

        ResolvedService resolvedService = ResolvedServiceMdnsFactory.NewJoinResolvedService("mdns", nsdServiceInfo);

        assertEquals(inetAddress, resolvedService.getInetAddress());
        assertEquals(port, resolvedService.getDiscoveryPort());
        assertEquals(appIdentifier, resolvedService.getAppIdentifier());
        assertEquals(groupName, resolvedService.getGroupName());
        assertEquals(groupUid, resolvedService.getGroupUid());
    }

    @Test
    public void assignGroupTest() throws UnknownHostException {

        String serviceType = "test_serviceType";
        String serviceName = "test_serviceName";
        InetAddress inetAddress = InetAddress.getByName("192.168.2.1");
        int port = 0;
        String appIdentifier = "test_appIdentifier";
        String groupName = "test_groupName";
        String groupUid = "test_groupUid";

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceType(serviceType);
        nsdServiceInfo.setServiceName(serviceName);
        nsdServiceInfo.setHost(inetAddress);
        nsdServiceInfo.setPort(port);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_APP_LABEL, appIdentifier);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_LABEL, groupName);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, groupUid);
        ResolvedService resolvedService = ResolvedServiceMdnsFactory.NewJoinResolvedService("dp", nsdServiceInfo);

        ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);

        resolvedService.setResolvedGroup(resolvedGroup);

        assertEquals(resolvedGroup, resolvedService.getResolvedGroup());

    }

    @Test(expected = IllegalStateException.class)
    public void assignGroupTwiceTest() throws UnknownHostException {

        String serviceType = "test_serviceType";
        String serviceName = "test_serviceName";
        InetAddress inetAddress = InetAddress.getByName("192.168.2.1");
        int port = 0;
        String appIdentifier = "test_appIdentifier";
        String groupName = "test_groupName";
        String groupUid = "test_groupUid";

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceType(serviceType);
        nsdServiceInfo.setServiceName(serviceName);
        nsdServiceInfo.setHost(inetAddress);
        nsdServiceInfo.setPort(port);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_APP_LABEL, appIdentifier);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_LABEL, groupName);
        nsdServiceInfo.setAttribute(BabbleConstants.DNS_TXT_GROUP_ID_LABEL, groupUid);
        ResolvedService resolvedService = ResolvedServiceMdnsFactory.NewJoinResolvedService("dp", nsdServiceInfo);

        ResolvedGroup resolvedGroup = new ResolvedGroup(resolvedService);

        resolvedService.setResolvedGroup(resolvedGroup);
        resolvedService.setResolvedGroup(resolvedGroup); //should throw an IllegalStateException
    }
}
