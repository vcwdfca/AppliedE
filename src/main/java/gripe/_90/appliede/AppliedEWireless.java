package gripe._90.appliede;

import ae2.api.implementations.items.AddWirelessTerminalEvent;
import ae2.core.gui.locator.ItemGuiHostLocator;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import gripe._90.appliede.item.WirelessTransmutationTerminalHost;
import gripe._90.appliede.item.WirelessTransmutationTerminalItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class AppliedEWireless {
    private static boolean initialized;

    private AppliedEWireless() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        AddWirelessTerminalEvent.register(event -> event.builder(
                WirelessTransmutationTerminalItem.ID,
                AppliedEItems.WIRELESS_TRANSMUTATION_TERMINAL,
                AppliedEWireless::openWirelessTerminal,
                WirelessTransmutationTerminalHost::new,
                ItemStack::new)
            .hotkeyName(WirelessTransmutationTerminalItem.ID)
            .upgradeSlots(2)
            .addTerminal());
        initialized = true;
    }

    private static boolean openWirelessTerminal(
        ae2.api.implementations.items.WirelessTerminalDefinition definition,
        EntityPlayer player,
        ItemGuiHostLocator locator,
        ItemStack stack,
        boolean returningFromSubmenu
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        return AppliedEGuiOpener.openItemGui(player, AppliedEGuiIds.WIRELESS_TRANSMUTATION_TERMINAL, locator,
            returningFromSubmenu);
    }
}
