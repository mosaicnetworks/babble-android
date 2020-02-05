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
import android.util.Log;

import java.nio.charset.Charset;

import static android.content.ContentValues.TAG;

public class NsdResolvedService {
    private String mServiceName;
    private String mServiceType;
    private String mGroupName;

    NsdResolvedService(NsdServiceInfo nsdServiceInfo) {
        mServiceName = nsdServiceInfo.getServiceName();
        mServiceType = nsdServiceInfo.getServiceType();

        try {
            Log.d(TAG, "NsdResolvedService: " + nsdServiceInfo.getAttributes());

            mGroupName = new String(nsdServiceInfo.getAttributes().get(MdnsAdvertiser.GROUP_NAME), Charset.forName("UTF-8"));

        } catch (Exception e) {
            mGroupName = "FAILED";
        }


    }

    public String getServiceName() {
        return mServiceName;
    }

    public void setServiceName(String serviceName) {
        mServiceName = serviceName;
    }

    public String getServiceType() {
        return mServiceType;
    }

    public void setServiceType(String serviceType) {
        mServiceType = serviceType;
    }

    public String getGroupName() {
        return mGroupName;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }


        if (!(o instanceof NsdResolvedService)) {
            return false;
        }

        NsdResolvedService service = (NsdResolvedService) o;

        return service.getServiceName().equals(mServiceName);
    }
}
