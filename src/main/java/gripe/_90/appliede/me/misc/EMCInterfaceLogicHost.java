package gripe._90.appliede.me.misc;

import ae2.api.storage.ISubGuiHost;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.container.ISubGui;
import ae2.helpers.IConfigInvHost;
import ae2.helpers.externalstorage.GenericStackInv;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import ae2.parts.AEBasePart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface EMCInterfaceLogicHost extends IConfigInvHost, ISubGuiHost, IUpgradeableObject {
    TileEntity getTileEntity();

    void saveChanges();

    EMCInterfaceLogic getInterfaceLogic();

    @Override
    default GenericStackInv getConfig() {
        return getInterfaceLogic().getConfig();
    }

    default GenericStackInv getStorage() {
        return getInterfaceLogic().getStorage();
    }

    @Override
    default IUpgradeInventory getUpgrades() {
        return getInterfaceLogic().getUpgrades();
    }

    @Override
    default void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        AppliedEGuiOpener.openInterfaceHostGui(player, AppliedEGuiIds.EMC_INTERFACE, this, true);
    }
}
