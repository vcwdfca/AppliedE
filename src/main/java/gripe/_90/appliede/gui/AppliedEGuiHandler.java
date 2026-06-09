package gripe._90.appliede.gui;

import java.util.function.Function;

import ae2.client.gui.style.GuiStyleManager;
import ae2.container.AEBaseContainer;
import ae2.core.gui.locator.GuiHostLocator;
import ae2.core.gui.locator.GuiHostLocators;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.core.gui.locator.PartLocator;
import ae2.tile.AEBaseTile;
import gripe._90.appliede.block.EMCInterfaceBlockEntity;
import gripe._90.appliede.client.screen.EMCInterfaceScreen;
import gripe._90.appliede.client.screen.EMCSetStockAmountScreen;
import gripe._90.appliede.client.screen.TransmutationTerminalScreen;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.menu.EMCInterfaceMenu;
import gripe._90.appliede.menu.EMCSetStockAmountMenu;
import gripe._90.appliede.menu.TransmutationTerminalMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

import ae2.parts.AEBasePart;

public final class AppliedEGuiHandler implements IGuiHandler {
    private static @Nullable PartLocator partLocator(int x, int y, int z) {
        int side = y >> 8;
        if (side < 0 || side >= EnumFacing.VALUES.length) {
            return null;
        }
        return new PartLocator(new BlockPos(x, y & 255, z), EnumFacing.VALUES[side]);
    }

    private static ItemGuiHostLocator itemLocator(int x) {
        if (x < 0) {
            return GuiHostLocators.forBaubleSlot(-1 - x);
        }
        return GuiHostLocators.forInventorySlot(x);
    }

    private static <H, C extends AEBaseContainer> @Nullable C createContainer(
        EntityPlayer player,
        GuiHostLocator locator,
        int guiId,
        Class<H> hostType,
        Function<H, C> factory
    ) {
        if (locator == null) {
            return null;
        }

        H host = locator.locate(player, hostType);
        if (host == null) {
            return null;
        }

        return initContainer(factory.apply(host), locator, guiId);
    }

    private static <C extends AEBaseContainer> C initTileContainer(C container, TileEntity te, int guiId) {
        return initContainer(container, GuiHostLocators.forTile(te), guiId);
    }

    private static <C extends AEBaseContainer> C initContainer(C container, GuiHostLocator locator, int guiId) {
        container.setLocator(locator);
        container.setReturnedFromSubScreen(AppliedEGuiIds.isReturnedFromSubScreen(guiId));
        container.setGuiTitle(getDefaultGuiTitle(container.getTarget()));
        return container;
    }

    private static @Nullable ITextComponent getDefaultGuiTitle(Object host) {
        if (host instanceof net.minecraft.world.IWorldNameable nameable) {
            if (nameable.hasCustomName()) {
                return nameable.getDisplayName();
            }
        }
        if (host instanceof AEBaseTile tile) {
            if (tile.hasCustomName()) {
                return customTitle(tile.getCustomName());
            }
        }
        if (host instanceof AEBasePart part) {
            if (part.hasCustomName()) {
                return customTitle(part.getCustomName());
            }
        }
        return null;
    }

    private static @Nullable ITextComponent customTitle(@Nullable String customName) {
        return customName == null || customName.isEmpty() ? null : new TextComponentString(customName);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        AppliedEGuiIds gui = AppliedEGuiIds.fromId(id);
        if (gui == null) {
            return null;
        }

        switch (gui) {
            case TRANSMUTATION_TERMINAL:
                return createContainer(player, partLocator(x, y, z), id, TransmutationTerminalHost.class,
                    host -> new TransmutationTerminalMenu(player.inventory, host));
            case EMC_INTERFACE:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof EMCInterfaceBlockEntity tile) {
                    return initTileContainer(new EMCInterfaceMenu(player.inventory, tile), tile, id);
                }

                return createContainer(player, partLocator(x, y, z), id, EMCInterfaceLogicHost.class,
                    host -> new EMCInterfaceMenu(player.inventory, host));
            case EMC_SET_STOCK_AMOUNT:
                TileEntity stockTe = world.getTileEntity(new BlockPos(x, y, z));
                if (stockTe instanceof EMCInterfaceBlockEntity tile) {
                    return initTileContainer(new EMCSetStockAmountMenu(player.inventory, tile), tile, id);
                }

                return createContainer(player, partLocator(x, y, z), id, EMCInterfaceLogicHost.class,
                    host -> new EMCSetStockAmountMenu(player.inventory, host));
            case WIRELESS_TRANSMUTATION_TERMINAL:
                return createContainer(player, itemLocator(x), id, TransmutationTerminalHost.class,
                    host -> TransmutationTerminalMenu.wireless(player.inventory, host));
            case EMC_EXPORT_BUS:
            case EMC_IMPORT_BUS:
            case EMC_MODULE_PRIORITY:
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        AppliedEGuiIds gui = AppliedEGuiIds.fromId(id);
        if (gui == null) {
            return null;
        }

        switch (gui) {
            case TRANSMUTATION_TERMINAL:
                TransmutationTerminalMenu terminal = createContainer(player, partLocator(x, y, z), id,
                    TransmutationTerminalHost.class, host -> new TransmutationTerminalMenu(player.inventory, host));
                if (terminal != null) {
                    return new TransmutationTerminalScreen(terminal, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/appliede/transmutation_terminal.json"));
                }
                return null;
            case EMC_INTERFACE:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof EMCInterfaceBlockEntity tile) {
                    EMCInterfaceMenu menu = initTileContainer(new EMCInterfaceMenu(player.inventory, tile), tile, id);
                    return new EMCInterfaceScreen(menu, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/appliede/emc_interface.json"));
                }

                EMCInterfaceMenu menu = createContainer(player, partLocator(x, y, z), id, EMCInterfaceLogicHost.class,
                    host -> new EMCInterfaceMenu(player.inventory, host));
                if (menu != null) {
                    return new EMCInterfaceScreen(menu, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/appliede/emc_interface.json"));
                }
                return null;
            case EMC_SET_STOCK_AMOUNT:
                TileEntity stockTe = world.getTileEntity(new BlockPos(x, y, z));
                if (stockTe instanceof EMCInterfaceBlockEntity tile) {
                    EMCSetStockAmountMenu tileStockMenu = initTileContainer(
                        new EMCSetStockAmountMenu(player.inventory, tile), tile, id);
                    return new EMCSetStockAmountScreen(tileStockMenu, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/set_stock_amount.json"));
                }

                EMCSetStockAmountMenu stockMenu = createContainer(player, partLocator(x, y, z), id,
                    EMCInterfaceLogicHost.class, host -> new EMCSetStockAmountMenu(player.inventory, host));
                if (stockMenu != null) {
                    return new EMCSetStockAmountScreen(stockMenu, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/set_stock_amount.json"));
                }
                return null;
            case WIRELESS_TRANSMUTATION_TERMINAL:
                TransmutationTerminalMenu wireless = createContainer(player, itemLocator(x), id,
                    TransmutationTerminalHost.class, host -> TransmutationTerminalMenu.wireless(player.inventory, host));
                if (wireless != null) {
                    return new TransmutationTerminalScreen(wireless, player.inventory, null,
                        GuiStyleManager.loadStyleDoc("/screens/appliede/wireless_transmutation_terminal.json"));
                }
                return null;
            case EMC_EXPORT_BUS:
            case EMC_IMPORT_BUS:
            case EMC_MODULE_PRIORITY:
            default:
                return null;
        }
    }
}
