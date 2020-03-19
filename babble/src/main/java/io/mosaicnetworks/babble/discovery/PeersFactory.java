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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This abstract class provides methods to convert a List<Peer> to a JSON String
 * and vice versa
 */
public abstract class PeersFactory {
    private static Gson gson = new Gson();

    /**
     * Exports List of Peers to Json
     *
     * @param peers
     * @return
     */
    public static String toJson(List<Peer> peers) {
            return gson.toJson(peers);
    }

    /**
     * Converts a JSON string into an array of {@link Peer}
     *
     * @param peersJson
     * @return
     */
    public static Peer[] toPeersArray(String peersJson) {
        return gson.fromJson(peersJson, Peer[].class);
    }


    /**
     * Converts a JSON string into a list of {@link Peer}
     *
     * @param peersJson
     * @return
     */
    public static List<Peer> toPeersList(String peersJson) {
        return new ArrayList<>(Arrays.asList(toPeersArray(peersJson)));
    }


}
