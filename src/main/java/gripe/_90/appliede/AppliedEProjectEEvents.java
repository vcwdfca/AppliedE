package gripe._90.appliede;

import gripe._90.appliede.me.service.KnowledgeService;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class AppliedEProjectEEvents {
    private AppliedEProjectEEvents() {
    }

    @SubscribeEvent
    public static void onEmcRemap(EMCRemapEvent event) {
        KnowledgeService.updateKnownItemsForAllGrids();
    }

    @SubscribeEvent
    public static void onKnowledgeChange(PlayerKnowledgeChangeEvent event) {
        KnowledgeService.updateKnownItemsForAllGrids();
    }
}
