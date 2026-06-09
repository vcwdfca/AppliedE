package gripe._90.appliede;

import gripe._90.appliede.block.EMCInterfaceBlockEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class AppliedEBlockEntities {
    private static boolean initialized;

    private AppliedEBlockEntities() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        GameRegistry.registerTileEntity(EMCInterfaceBlockEntity.class, AppliedE.id("emc_interface"));
    }
}
