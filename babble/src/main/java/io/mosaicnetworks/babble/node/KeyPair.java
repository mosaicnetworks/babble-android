package io.mosaicnetworks.babble.node;

import mobile.Mobile;

public class KeyPair {

    public String publicKey;
    public String privateKey;

    public KeyPair() {
        String keyPair = Mobile.getPrivPublKeys();    //publicKey[!@#$%^]privateKey
        String[] separated =  keyPair.split("=!@#@!=");

        this.publicKey = separated[0].trim();
        this.privateKey = separated[1].trim();
    }

}
