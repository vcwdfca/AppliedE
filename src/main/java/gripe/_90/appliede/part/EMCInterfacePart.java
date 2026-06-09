package gripe._90.appliede.part;

import java.util.List;
import javax.annotation.Nullable;

import ae2.api.inventories.InternalInventory;
import ae2.api.networking.GridHelper;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridNodeListener;
import ae2.api.networking.IManagedGridNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.util.AECableType;
import ae2.core.AppEng;
import ae2.items.parts.PartModels;
import ae2.parts.AEBasePart;
import ae2.parts.PartModel;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import gripe._90.appliede.me.misc.EMCInterfaceLogic;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;

public class EMCInterfacePart extends AEBasePart implements EMCInterfaceLogicHost {
    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_interface");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/interface_has_channel"));

    private static final IGridNodeListener<EMCInterfacePart> NODE_LISTENER = new NodeListener<>() {
        @Override
        public void onGridChanged(EMCInterfacePart host, IGridNode node) {
            super.onGridChanged(host, node);
            host.logic.gridChanged();
        }
    };

    private final EMCInterfaceLogic logic = new EMCInterfaceLogic(getMainNode(), this, AppliedEItems.CABLE_EMC_INTERFACE);

    public EMCInterfacePart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (getMainNode().hasGridBooted()) {
            logic.notifyNeighbours();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        logic.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        logic.writeToNBT(data);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
    }

    @Override
    public EMCInterfaceLogic getInterfaceLogic() {
        return logic;
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!isClientSide()) {
            AppliedEGuiOpener.openPartGui(player, AppliedEGuiIds.EMC_INTERFACE, this);
        }

        return true;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return logic.getUpgrades();
        }
        return super.getSubInventory(id);
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return getPartItem().asItemStack();
    }
}
