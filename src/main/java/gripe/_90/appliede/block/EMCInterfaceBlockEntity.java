package gripe._90.appliede.block;

import java.util.List;

import ae2.api.AECapabilities;
import ae2.api.networking.GridHelper;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridNodeListener;
import ae2.api.networking.IManagedGridNode;
import ae2.api.util.AECableType;
import ae2.tile.grid.AENetworkedTile;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.misc.EMCInterfaceLogic;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public class EMCInterfaceBlockEntity extends AENetworkedTile implements EMCInterfaceLogicHost {
    private static final IGridNodeListener<EMCInterfaceBlockEntity> NODE_LISTENER = new IGridNodeListener<>() {
        @Override
        public void onSaveChanges(EMCInterfaceBlockEntity host, IGridNode node) {
            host.saveChanges();
        }

        @Override
        public void onGridChanged(EMCInterfaceBlockEntity host, IGridNode node) {
            host.logic.gridChanged();
        }

        @Override
        public void onStateChanged(EMCInterfaceBlockEntity host, IGridNode node, State state) {
            host.onMainNodeStateChanged(state);
        }
    };

    private final EMCInterfaceLogic logic = new EMCInterfaceLogic(getMainNode(), this, AppliedEItems.EMC_INTERFACE);

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public EMCInterfaceLogic getInterfaceLogic() {
        return logic;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (getMainNode().hasGridBooted()) {
            logic.notifyNeighbours();
        }
    }

    @Override
    public void saveAdditional(NBTTagCompound data) {
        super.saveAdditional(data);
        logic.writeToNBT(data);
    }

    @Override
    public void loadTag(NBTTagCompound data) {
        super.loadTag(data);
        logic.readFromNBT(data);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        super.addAdditionalDrops(drops);
        logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(AppliedEItems.EMC_INTERFACE);
    }

    @Override
    public AECableType getCableConnectionType(EnumFacing dir) {
        return AECableType.SMART;
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return new ItemStack(AppliedEItems.EMC_INTERFACE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == AECapabilities.ME_STORAGE) {
            return (T) logic.getInventory();
        }
        if (capability == AECapabilities.GENERIC_INTERNAL_INV) {
            return (T) logic.getStorage();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == AECapabilities.ME_STORAGE || capability == AECapabilities.GENERIC_INTERNAL_INV) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }
}
