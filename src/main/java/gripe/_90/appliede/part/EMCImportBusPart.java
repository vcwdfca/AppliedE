package gripe._90.appliede.part;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import ae2.api.config.Actionable;
import ae2.api.networking.IGrid;
import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKeyType;
import ae2.api.storage.StorageHelper;
import ae2.core.AppEng;
import ae2.core.definitions.AEItems;
import ae2.core.settings.TickRates;
import ae2.items.parts.PartModels;
import ae2.me.storage.ExternalStorageFacade;
import ae2.container.GuiIds;
import ae2.parts.PartModel;
import ae2.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.service.KnowledgeService;

import moze_intel.projecte.api.tile.IEmcProvider;

public class EMCImportBusPart extends IOBusPart {
    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_import_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_has_channel"));

    public EMCImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, Set.of(AEKeyType.items(), EMCKeyType.TYPE), partItem);
    }

    @Override
    protected GuiIds.GuiKey getGuiKey() {
        return GuiIds.GuiKey.IMPORT_BUS;
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        TileEntity adjacent = getAdjacentTile();
        EnumFacing facing = getSide().getOpposite();

        var doneWork = false;

        var networkEmc = grid.getService(KnowledgeService.class).getStorage();
        var remaining = new AtomicInteger(getOperationsPerTick());

        if (adjacent instanceof IEmcProvider provider) {
            if (getFilter().isEmpty() || getFilter().isListed(EMCKey.BASE) != isUpgradedWith(AEItems.INVERTER_CARD)) {
                var emcRemaining = remaining.get() * EMCKeyType.TYPE.getAmountPerOperation();
                var inserted = StorageHelper.poweredInsert(
                        grid.getEnergyService(),
                        grid.getStorageService().getInventory(),
                        EMCKey.BASE,
                        Math.min(emcRemaining, provider.getStoredEmc()),
                        source,
                        Actionable.MODULATE);
                provider.provideEMC(facing, inserted);
                remaining.addAndGet((int) -Math.max(1, inserted / EMCKeyType.TYPE.getAmountPerOperation()));
            }
        }

        IItemHandler handler = getItemHandler(adjacent, facing);
        if (handler != null) {
            var adjacentStorage = ExternalStorageFacade.of(handler);

            for (var slot = 0; slot < handler.getSlots() && remaining.get() > 0; slot++) {
                var item = AEItemKey.of(handler.getStackInSlot(slot));

                if (item == null) {
                    continue;
                }

                if (!getFilter().isEmpty() && getFilter().isListed(item) == isUpgradedWith(AEItems.INVERTER_CARD)) {
                    continue;
                }

                var amount = adjacentStorage.extract(item, remaining.get(), Actionable.SIMULATE, source);

                if (amount > 0) {
                    var mayLearn = isLearningCardInstalled();
                    amount = networkEmc.insertItem(item, amount, Actionable.MODULATE, source, mayLearn);
                    adjacentStorage.extract(item, amount, Actionable.MODULATE, source);
                    remaining.addAndGet(-(int) amount);
                }
            }
        }

        if (remaining.get() < getOperationsPerTick()) {
            doneWork = true;
        }

        return doneWork;
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

    private boolean isLearningCardInstalled() {
        return getUpgrades().isInstalled(AppliedEItems.LEARNING_CARD);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
