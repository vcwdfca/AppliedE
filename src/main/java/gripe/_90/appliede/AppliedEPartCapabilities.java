package gripe._90.appliede;

import ae2.api.AECapabilities;
import ae2.api.parts.RegisterPartCapabilitiesEvent;
import gripe._90.appliede.part.EMCInterfacePart;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AppliedE.MODID)
public final class AppliedEPartCapabilities {
    private AppliedEPartCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterPartCapabilitiesEvent event) {
        event.register(AECapabilities.GENERIC_INTERNAL_INV,
            (part, side) -> part.getInterfaceLogic().getStorage(),
            EMCInterfacePart.class);
        event.register(AECapabilities.ME_STORAGE,
            (part, side) -> part.getInterfaceLogic().getInventory(),
            EMCInterfacePart.class);
    }
}
