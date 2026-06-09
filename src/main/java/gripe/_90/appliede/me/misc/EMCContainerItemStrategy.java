package gripe._90.appliede.me.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.config.Actionable;
import ae2.api.stacks.GenericStack;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

import moze_intel.projecte.api.PESounds;
import moze_intel.projecte.api.item.IItemEmc;

@SuppressWarnings("UnstableApiUsage")
public class EMCContainerItemStrategy implements ContainerItemStrategy<EMCKey, ItemStack> {
    public static final EMCContainerItemStrategy INSTANCE = new EMCContainerItemStrategy();

    private EMCContainerItemStrategy() {}

    @Nullable
    @Override
    public GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IItemEmc handler)) {
            return null;
        }

        return new GenericStack(EMCKey.BASE, handler.getStoredEmc(stack));
    }

    @Nullable
    @Override
    public ItemStack findCarriedContext(EntityPlayer player, Container menu) {
        if (!(menu instanceof TransmutationTerminalMenu)) {
            return null;
        }

        var carried = player.inventory.getItemStack();
        return !carried.isEmpty() && carried.getItem() instanceof IItemEmc ? carried : null;
    }

    @Override
    public long extract(ItemStack context, EMCKey what, long amount, Actionable mode) {
        if (!(context.getItem() instanceof IItemEmc handler)) {
            return 0;
        }

        if (mode.isSimulate()) {
            return Math.min(amount, handler.getStoredEmc(context));
        }

        return handler.extractEmc(context, amount);
    }

    @Override
    public long insert(ItemStack context, EMCKey what, long amount, Actionable mode) {
        if (!(context.getItem() instanceof IItemEmc handler)) {
            return 0;
        }

        long capacity = Math.max(0, handler.getMaximumEmc(context) - handler.getStoredEmc(context));
        if (mode.isSimulate()) {
            return Math.min(amount, capacity);
        }

        return handler.addEmc(context, amount);
    }

    @Override
    public void playFillSound(EntityPlayer player, EMCKey what) {
        player.playSound(PESounds.CHARGE, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(EntityPlayer player, EMCKey what) {
        player.playSound(PESounds.UNCHARGE, 1.0F, 1.0F);
    }

    @Nullable
    @Override
    public GenericStack getExtractableContent(ItemStack context) {
        return getContainedStack(context);
    }
}
