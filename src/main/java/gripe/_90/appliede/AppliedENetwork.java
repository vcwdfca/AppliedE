package gripe._90.appliede;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import gripe._90.appliede.me.misc.LearnAllItemsPacket;

public final class AppliedENetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(AppliedE.MODID);

    private static boolean initialized;

    private AppliedENetwork() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        CHANNEL.registerMessage(LearnAllItemsPacket.Handler.class, LearnAllItemsPacket.class, 0, Side.SERVER);
        initialized = true;
    }
}
