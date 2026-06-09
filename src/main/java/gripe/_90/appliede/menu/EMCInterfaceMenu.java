package gripe._90.appliede.menu;

import ae2.api.stacks.AEItemKey;
import ae2.container.SlotSemantics;
import ae2.container.implementations.UpgradeableContainer;
import ae2.container.slot.AppEngSlot;
import ae2.container.slot.FakeSlot;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

public class EMCInterfaceMenu extends UpgradeableContainer<EMCInterfaceLogicHost> {
    private static final String ACTION_OPEN_SET_AMOUNT = "openSetAmount";

    public EMCInterfaceMenu(InventoryPlayer playerInventory, EMCInterfaceLogicHost host) {
        super(playerInventory, host);
        registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);
    }

    @Override
    protected void setupInventorySlots() {
        var logic = getHost().getInterfaceLogic();
        var config = logic.getConfig().createGuiWrapper();
        var storage = logic.getStorage().createGuiWrapper();

        for (int i = 0; i < config.size(); i++) {
            addSlot(new FakeSlot(config, i, 0, 0), SlotSemantics.CONFIG);
        }

        for (int i = 0; i < storage.size(); i++) {
            addSlot(new AppEngSlot(storage, i, 0, 0), SlotSemantics.STORAGE);
        }
    }

    public void openSetAmountMenu(int configSlot) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
            return;
        }

        var locator = getLocator();
        if (!(getPlayer() instanceof EntityPlayerMP player) || locator == null) {
            return;
        }

        var stack = getHost().getConfig().getStack(configSlot);
        if (stack != null && stack.what() instanceof AEItemKey item) {
            EMCSetStockAmountMenu.remember(player, configSlot, item, (int) stack.amount());
            AppliedEGuiOpener.openInterfaceHostGui(player, AppliedEGuiIds.EMC_SET_STOCK_AMOUNT, getHost(), false);
        }
    }
}
