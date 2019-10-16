package io.mosaicnetworks.babble.node;

import mobile.Mobile;

public final class KeyPair {

    public final String publicKey;
    public final String privateKey;

    public KeyPair() {
        String keyPair = Mobile.getPrivPublKeys(); //publicKey=!@#@!=privateKey
        String[] separated =  keyPair.split("=!@#@!="); //keys are hex so won't contain these characters!

        publicKey = separated[0].trim();
        privateKey = separated[1].trim();
    }
}
