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

package io.mosaicnetworks.babble.node;

import mobile.Mobile;

/**
 * A public/private key pair for use with a {@link BabbleNode}. A random key pair is generated on
 * construction
 */
public final class KeyPair {

    /**
     * Public key
     */
    public final String publicKey;

    /**
     * Private key
     */
    public final String privateKey;

    /**
     * Constructs a random key pair
     */
    public KeyPair() {
        String keyPair = Mobile.getPrivPublKeys(); //publicKey=!@#@!=privateKey
        String[] separated =  keyPair.split("=!@#@!="); //keys are hex so won't contain these characters!

        publicKey = separated[0].trim();
        privateKey = separated[1].trim();
    }
}
