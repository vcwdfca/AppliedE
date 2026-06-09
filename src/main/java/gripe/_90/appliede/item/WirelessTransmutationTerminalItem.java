package gripe._90.appliede.item;

import ae2.container.GuiIds;
import ae2.items.tools.powered.WirelessTerminalItem;

public class WirelessTransmutationTerminalItem extends WirelessTerminalItem {
    public static final String ID = "wireless_transmutation_terminal";

    public WirelessTransmutationTerminalItem(double powerCapacity) {
        super(powerCapacity, ID, GuiIds.GuiKey.WIRELESS_TERMINAL, WirelessTransmutationTerminalItem::newStack,
            WirelessTransmutationTerminalHost::new, ID, 2, false);
    }

    private static net.minecraft.item.ItemStack newStack(WirelessTerminalItem item) {
        return new net.minecraft.item.ItemStack(item);
    }
}
