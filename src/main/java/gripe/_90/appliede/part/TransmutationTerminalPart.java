package gripe._90.appliede.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.container.ISubGui;
import ae2.items.parts.PartModels;
import ae2.parts.PartModel;
import ae2.parts.reporting.AbstractTerminalPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;

public class TransmutationTerminalPart extends AbstractTerminalPart implements TransmutationTerminalHost {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppliedE.id("part/transmutation_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = AppliedE.id("part/transmutation_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private boolean shiftToTransmute;

    public TransmutationTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public boolean getShiftToTransmute() {
        return shiftToTransmute;
    }

    @Override
    public void setShiftToTransmute(boolean shift) {
        shiftToTransmute = shift;
        saveChanges();
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        shiftToTransmute = data.getBoolean("shiftToTransmute");
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("shiftToTransmute", shiftToTransmute);
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!player.world.isRemote) {
            AppliedEGuiOpener.openPartGui(player, AppliedEGuiIds.TRANSMUTATION_TERMINAL, this);
        }
        return true;
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        AppliedEGuiOpener.openPartGui(player, AppliedEGuiIds.TRANSMUTATION_TERMINAL, this, true);
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
