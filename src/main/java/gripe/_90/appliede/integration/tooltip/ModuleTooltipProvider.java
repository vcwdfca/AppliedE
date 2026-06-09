package gripe._90.appliede.integration.tooltip;

import ae2.api.integrations.igtooltip.PartTooltips;
import ae2.api.integrations.igtooltip.TooltipProvider;

import gripe._90.appliede.part.EMCModulePart;

@SuppressWarnings("UnstableApiUsage")
public class ModuleTooltipProvider implements TooltipProvider {
    static {
        PartTooltips.addBody(EMCModulePart.class, ModuleDataProvider.INSTANCE);
        PartTooltips.addServerData(EMCModulePart.class, ModuleDataProvider.INSTANCE);
    }
}
