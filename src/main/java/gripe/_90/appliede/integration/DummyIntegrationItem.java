package gripe._90.appliede.integration;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import gripe._90.appliede.Tags;

public class DummyIntegrationItem extends Item {
    private final String addon;

    public DummyIntegrationItem(String addon) {
        this.addon = addon;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void addInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag flag) {
        lines.add(TextFormatting.GRAY + I18n.translateToLocalFormatted(
                "tooltip." + Tags.MOD_ID + ".not_installed", addon));
    }
}
