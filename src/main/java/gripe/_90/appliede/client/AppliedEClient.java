package gripe._90.appliede.client;

import ae2.api.client.AEKeyRendering;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;

public class AppliedEClient {
    private static boolean registered;

    public static void init() {
        if (!registered) {
            AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, EMCRenderer.INSTANCE);
            registered = true;
        }
    }
}
