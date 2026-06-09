package gripe._90.appliede.item;

import java.util.function.BiConsumer;

import ae2.container.ISubGui;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.helpers.WirelessTerminalGuiHost;
import ae2.items.tools.powered.WirelessTerminalItem;
import ae2.items.tools.powered.WirelessTerminals;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class WirelessTransmutationTerminalHost extends WirelessTerminalGuiHost<WirelessTerminalItem>
    implements TransmutationTerminalHost {
    private static final String TAG_SHIFT_TO_TRANSMUTE = "shiftToTransmute";

    public WirelessTransmutationTerminalHost(
        WirelessTerminalItem stackItem,
        WirelessTerminalItem terminalItem,
        EntityPlayer player,
        ItemGuiHostLocator locator,
        BiConsumer<EntityPlayer, ISubGui> returnToMainContainer
    ) {
        super(stackItem, terminalItem, player, locator, returnToMainContainer);
    }

    @Override
    public boolean getShiftToTransmute() {
        NBTTagCompound tag = WirelessTerminals.getExistingTerminalData(getItemStack(), getTerminalItem());
        return tag != null && tag.getBoolean(TAG_SHIFT_TO_TRANSMUTE);
    }

    @Override
    public void setShiftToTransmute(boolean shift) {
        WirelessTerminals.getTerminalData(getItemStack(), getTerminalItem())
            .setBoolean(TAG_SHIFT_TO_TRANSMUTE, shift);
    }
}
