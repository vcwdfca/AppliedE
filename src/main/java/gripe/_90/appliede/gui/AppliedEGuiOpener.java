package gripe._90.appliede.gui;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import ae2.core.gui.locator.BaublesItemLocator;
import ae2.core.gui.locator.ItemGuiHostLocator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ae2.parts.AEBasePart;

import java.util.Objects;

public final class AppliedEGuiOpener {
    private AppliedEGuiOpener() {
    }

    public static void openTileGui(EntityPlayer player, AppliedEGuiIds gui, TileEntity tile) {
        openTileGui(player, gui, tile, false);
    }

    public static void openTileGui(EntityPlayer player, AppliedEGuiIds gui, TileEntity tile, boolean returnedFromSubScreen) {
        if (player == null || gui == null || tile == null || tile.getWorld() == null) {
            return;
        }
        BlockPos pos = tile.getPos();
        player.openGui(AppliedE.INSTANCE, gui.id(returnedFromSubScreen), tile.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static void openPartGui(EntityPlayer player, AppliedEGuiIds gui, AEBasePart part) {
        openPartGui(player, gui, part, false);
    }

    public static void openPartGui(EntityPlayer player, AppliedEGuiIds gui, AEBasePart part, boolean returnedFromSubScreen) {
        if (player == null || gui == null || part == null || part.getHost() == null) {
            return;
        }
        BlockPos hostPos = part.getHost().getLocation().getPos();
        EnumFacing side = Objects.requireNonNull(part.getSide(), "Part GUI requires a sided part");
        World world = player.world;
        int encodedY = (side.ordinal() << 8) | (hostPos.getY() & 255);
        player.openGui(AppliedE.INSTANCE, gui.id(returnedFromSubScreen), world, hostPos.getX(), encodedY, hostPos.getZ());
    }

    public static void openInterfaceHostGui(
        EntityPlayer player,
        AppliedEGuiIds gui,
        EMCInterfaceLogicHost host,
        boolean returnedFromSubScreen
    ) {
        if (host instanceof AEBasePart part) {
            openPartGui(player, gui, part, returnedFromSubScreen);
        } else {
            openTileGui(player, gui, host.getTileEntity(), returnedFromSubScreen);
        }
    }

    public static void openItemGui(EntityPlayer player, AppliedEGuiIds gui, int slot) {
        openItemGui(player, gui, slot, false);
    }

    public static void openItemGui(EntityPlayer player, AppliedEGuiIds gui, int slot, boolean returnedFromSubScreen) {
        if (player == null || gui == null) {
            return;
        }
        player.openGui(AppliedE.INSTANCE, gui.id(returnedFromSubScreen), player.world, slot, 0, 0);
    }

    public static boolean openItemGui(
        EntityPlayer player,
        AppliedEGuiIds gui,
        ItemGuiHostLocator locator,
        boolean returnedFromSubScreen
    ) {
        if (player == null || gui == null || locator == null) {
            return false;
        }

        Integer slot = locator.getPlayerInventorySlot();
        if (slot != null) {
            openItemGui(player, gui, slot, returnedFromSubScreen);
            return true;
        }

        if (locator instanceof BaublesItemLocator baublesLocator) {
            player.openGui(AppliedE.INSTANCE, gui.id(returnedFromSubScreen), player.world,
                encodeBaublesLocator(baublesLocator), 0, 0);
            return true;
        }

        return false;
    }

    public static int encodeBaublesLocator(BaublesItemLocator locator) {
        return -1 - locator.baubleSlot();
    }
}
