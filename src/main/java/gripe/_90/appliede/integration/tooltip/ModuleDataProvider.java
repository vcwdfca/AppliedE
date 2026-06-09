package gripe._90.appliede.integration.tooltip;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import ae2.api.integrations.igtooltip.TooltipBuilder;
import ae2.api.integrations.igtooltip.TooltipContext;
import ae2.api.integrations.igtooltip.providers.BodyProvider;
import ae2.api.integrations.igtooltip.providers.ServerDataProvider;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.part.EMCModulePart;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
public class ModuleDataProvider implements BodyProvider<EMCModulePart>, ServerDataProvider<EMCModulePart> {
    static final ModuleDataProvider INSTANCE = new ModuleDataProvider();

    private ModuleDataProvider() {}

    @Override
    public void provideServerData(EntityPlayer player, EMCModulePart module, NBTTagCompound serverData) {
        var node = Objects.requireNonNull(module.getGridNode());
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            var profileCache = node.getLevel().getMinecraftServer().getPlayerProfileCache();

            if (profileCache != null) {
                var profile = profileCache.getProfileByUUID(uuid);
                if (profile != null) {
                    serverData.setString("owner", profile.getName());
                }
            }
        }
    }

    @Override
    public void buildTooltip(EMCModulePart module, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();

        if (serverData.hasKey("owner")) {
            var owner = serverData.getString("owner");
            var label = new TextComponentTranslation("tooltip." + AppliedE.MODID + ".owner");
            label.appendSibling(new TextComponentString(": " + owner));
            tooltip.addLine(label);
        }
    }
}
