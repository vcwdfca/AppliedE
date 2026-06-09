package gripe._90.appliede.part;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import ae2.api.config.Actionable;
import ae2.api.config.SchedulingMode;
import ae2.api.config.Settings;
import ae2.api.networking.IGrid;
import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKeyType;
import ae2.api.storage.StorageHelper;
import ae2.api.util.IConfigManagerBuilder;
import ae2.core.AppEng;
import ae2.container.GuiIds;
import ae2.core.settings.TickRates;
import ae2.items.parts.PartModels;
import ae2.parts.PartModel;
import ae2.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.service.KnowledgeService;

import moze_intel.projecte.api.tile.IEmcAcceptor;

public class EMCExportBusPart extends IOBusPart {
    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_export_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_has_channel"));

    private static final Logger LOGGER = LoggerFactory.getLogger(EMCExportBusPart.class);

    private int nextSlot = 0;

    public EMCExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, Set.of(AEKeyType.items(), EMCKeyType.TYPE), partItem);
    }

    @Override
    public void readFromNBT(NBTTagCompound extra) {
        super.readFromNBT(extra);
        nextSlot = extra.getInteger("nextSlot");
    }

    @Override
    public void writeToNBT(NBTTagCompound extra) {
        super.writeToNBT(extra);
        extra.setInteger("nextSlot", nextSlot);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        TileEntity adjacent = getAdjacentTile();
        EnumFacing facing = getSide().getOpposite();

        var doneWork = false;

        var networkEmc = grid.getService(KnowledgeService.class).getStorage();
        var schedulingMode = getConfigManager().getSetting(Settings.SCHEDULING_MODE);
        var remaining = new AtomicInteger(getOperationsPerTick());
        var slot = 0;

        for (slot = 0; slot < availableSlots() && remaining.get() > 0; slot++) {
            var startingSlot =
                    switch (schedulingMode) {
                        case RANDOM -> getLevel().rand.nextInt(availableSlots());
                        case ROUNDROBIN -> (nextSlot + slot) % availableSlots();
                        default -> slot;
            };
            var what = getConfig().getKey(startingSlot);

            if (what == EMCKey.BASE && adjacent instanceof IEmcAcceptor handler) {
                var rem = remaining.get() * EMCKeyType.TYPE.getAmountPerOperation();
                var insertable = Math.min(rem, Math.max(0, handler.getMaximumEmc() - handler.getStoredEmc()));
                var extracted = StorageHelper.poweredExtraction(
                        grid.getEnergyService(),
                        grid.getStorageService().getInventory(),
                        EMCKey.BASE,
                        insertable,
                        source,
                        Actionable.MODULATE);

                if (extracted > 0) {
                    var accepted = handler.acceptEMC(facing, extracted);
                    remaining.addAndGet((int) -Math.max(1, accepted / EMCKeyType.TYPE.getAmountPerOperation()));
                    if (accepted < extracted) {
                        grid.getStorageService()
                                .getInventory()
                                .insert(EMCKey.BASE, extracted - accepted, Actionable.MODULATE, source);
                    }
                }
            } else if (what instanceof AEItemKey item && getItemHandler(adjacent, facing) instanceof IItemHandler handler) {
                var rem = remaining.get();
                var extracted = networkEmc.extractItem(item, rem, Actionable.SIMULATE, source, true);
                var stack = item.toStack((int) extracted);
                var remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                var wasInserted = extracted - remainder.getCount();

                if (wasInserted > 0) {
                    extracted = networkEmc.extractItem(item, wasInserted, Actionable.MODULATE, source, true);
                    stack = item.toStack((int) extracted);
                    remainder = ItemHandlerHelper.insertItem(handler, stack, false);
                    wasInserted = extracted - remainder.getCount();

                    if (wasInserted < extracted) {
                        var leftover = extracted - wasInserted;
                        leftover -= networkEmc.insertItem(
                                item, leftover, Actionable.MODULATE, source, false, false, () -> {});

                        if (leftover > 0) {
                            LOGGER.error(
                                    "Storage export: adjacent block unexpectedly refused insert, voided {}x{}",
                                    leftover,
                                    item);
                        }
                    }
                }

                if (wasInserted > 0) {
                    remaining.addAndGet(-(int) wasInserted);
                }
            }
        }

        if (remaining.get() < getOperationsPerTick()) {
            if (schedulingMode == SchedulingMode.ROUNDROBIN) {
                nextSlot = (nextSlot + slot) % availableSlots();
            }

            doneWork = true;
        }

        return doneWork;
    }

    @Override
    protected GuiIds.GuiKey getGuiKey() {
        return GuiIds.GuiKey.EXPORT_BUS;
    }

    private TileEntity getAdjacentTile() {
        var adjacentPos = getHost().getTileEntity().getPos().offset(getSide());
        return getLevel().getTileEntity(adjacentPos);
    }

    private IItemHandler getItemHandler(TileEntity adjacent, EnumFacing facing) {
        if (adjacent == null || !adjacent.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return null;
        }
        return adjacent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
