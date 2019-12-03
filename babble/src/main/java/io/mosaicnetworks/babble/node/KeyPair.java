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
