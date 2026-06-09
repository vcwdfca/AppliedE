package gripe._90.appliede.me.misc;

import java.util.List;
import java.util.Objects;

import ae2.api.config.Actionable;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGrid;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IManagedGridNode;
import ae2.api.networking.security.IActionHost;
import ae2.api.networking.security.IActionSource;
import ae2.api.networking.ticking.IGridTickable;
import ae2.api.networking.ticking.TickRateModulation;
import ae2.api.networking.ticking.TickingRequest;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.api.storage.MEStorage;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.api.upgrades.UpgradeInventories;
import ae2.me.storage.DelegatingMEInventory;
import ae2.util.ConfigInventory;
import ae2.util.Platform;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.service.EMCStorage;
import gripe._90.appliede.me.service.KnowledgeService;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;

public class EMCInterfaceLogic implements IActionHost, IGridTickable, IUpgradeableObject {
    protected final EMCInterfaceLogicHost host;
    protected final IManagedGridNode mainNode;

    private final ConfigInventory config;
    private final ConfigInventory storage;
    private final IUpgradeInventory upgrades;

    private final MEStorage localInvHandler;
    private final GenericStack[] plannedWork;
    private final IActionSource source = IActionSource.ofMachine(this);

    @Nullable
    private WrappedEMCStorage emcStorage;

    private boolean hasConfig;

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host, Item is) {
        this(node, host, is, 9);
    }

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host, Item is, int slots) {
        this.host = host;
        mainNode = node.setFlags(GridFlags.REQUIRE_CHANNEL)
            .addService(IGridTickable.class, this)
            .setIdlePowerUsage(10);

        config = ConfigInventory.configStacks(slots)
            .slotFilter((AEKey what) -> AEItemKey.is(what))
            .changeListener(this::onConfigRowChanged)
            .build();
        storage = ConfigInventory.storage(slots)
            .slotFilter(this::storageFilter)
            .changeListener(this::onStorageChanged)
            .build();
        upgrades = UpgradeInventories.forMachine(is, 1, host::saveChanges);

        localInvHandler = new DelegatingMEInventory(storage);
        plannedWork = new GenericStack[slots];

        config.useRegisteredCapacities();
        storage.useRegisteredCapacities();
    }

    public ConfigInventory getConfig() {
        return config;
    }

    public ConfigInventory getStorage() {
        return storage;
    }

    public MEStorage getInventory() {
        return hasConfig ? localInvHandler : emcStorage;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private boolean storageFilter(AEKey what) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var grid = mainNode.getGrid();
        var node = mainNode.getNode();

        if (grid == null || node == null) {
            return true;
        }

        var knowledge = grid.getService(KnowledgeService.class);
        var providerSupplier = knowledge.getProviderFor(node.getOwningPlayerProfileId());
        return knowledge.getKnownItems().contains(item)
            || (isUpgradedWith(AppliedEItems.LEARNING_CARD)
            && ProjectEAPI.getEMCProxy().hasValue(item.toStack())
            && providerSupplier != null
            && providerSupplier.get() != null);
    }

    public void readFromNBT(NBTTagCompound tag) {
        config.readFromChildTag(tag, "config");
        storage.readFromChildTag(tag, "storage");
        upgrades.readFromNBT(tag, "upgrades");
        readConfig();
    }

    public void writeToNBT(NBTTagCompound tag) {
        config.writeToChildTag(tag, "config");
        storage.writeToChildTag(tag, "storage");
        upgrades.writeToNBT(tag, "upgrades");
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, !hasWorkToDo());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!mainNode.isActive()) {
            return TickRateModulation.SLEEP;
        }

        var couldDoWork = false;

        for (var i = 0; i < plannedWork.length; i++) {
            var work = plannedWork[i];

            if (work != null) {
                couldDoWork = tryUsePlan(i, work.what(), (int) work.amount()) || couldDoWork;

                if (couldDoWork) {
                    updatePlan(i);
                }
            }
        }

        return hasWorkToDo()
            ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
            : TickRateModulation.SLEEP;
    }

    private boolean hasWorkToDo() {
        for (var requiredWork : plannedWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    private void updatePlan() {
        var hadWork = hasWorkToDo();

        for (var i = 0; i < config.size(); i++) {
            updatePlan(i);
        }

        var hasWork = hasWorkToDo();

        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    private void updatePlan(int slot) {
        var req = config.getStack(slot);
        var stored = storage.getStack(slot);

        if (req == null && stored != null) {
            plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
        } else if (req != null) {
            if (stored == null) {
                plannedWork[slot] = req;
            } else if (req.what().equals(stored.what())) {
                plannedWork[slot] = req.amount() != stored.amount()
                    ? new GenericStack(req.what(), req.amount() - stored.amount())
                    : null;
            } else {
                plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
            }
        } else {
            plannedWork[slot] = null;
        }
    }

    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var grid = mainNode.getGrid();

        if (grid == null) {
            return false;
        }

        if (amount < 0) {
            amount = -amount;
            var inSlot = storage.getStack(slot);

            if (!what.matches(inSlot) || inSlot.amount() < amount) {
                return true;
            }

            var depositedItems = grid.getService(KnowledgeService.class)
                .getStorage()
                .insertItem(item, amount, Actionable.MODULATE, source, isUpgradedWith(AppliedEItems.LEARNING_CARD));

            if (depositedItems > 0) {
                storage.extract(slot, what, depositedItems, Actionable.MODULATE);
                return true;
            }
        }

        if (amount > 0) {
            return storage.insert(slot, what, amount, Actionable.SIMULATE) != amount
                || acquireFromNetwork(grid, slot, what, amount);
        }

        return false;
    }

    private boolean acquireFromNetwork(IGrid grid, int slot, AEKey what, long amount) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var acquiredItems = grid.getService(KnowledgeService.class)
            .getStorage()
            .extractItem(item, amount, Actionable.MODULATE, source, true);

        if (acquiredItems > 0) {
            var inserted = storage.insert(slot, what, acquiredItems, Actionable.MODULATE);

            if (inserted < acquiredItems) {
                throw new IllegalStateException("Bad attempt at managing inventory. Voided items: " + inserted);
            }

            return true;
        } else {
            return false;
        }
    }

    private void readConfig() {
        hasConfig = !config.isEmpty();
        updatePlan();
        notifyNeighbours();
    }

    private void onConfigRowChanged() {
        host.saveChanges();
        readConfig();
    }

    private void onStorageChanged() {
        host.saveChanges();
        updatePlan();
    }

    public void notifyNeighbours() {
        mainNode.ifPresent((grid, node) -> {
            if (node.isActive()) {
                grid.getTickManager().wakeDevice(node);
            }
        });

        var tile = host.getTileEntity();

        if (tile != null && tile.getWorld() != null) {
            Platform.notifyBlocksOfNeighbors(tile.getWorld(), tile.getPos());
        }
    }

    public void gridChanged() {
        emcStorage = new WrappedEMCStorage(Objects.requireNonNull(mainNode.getGrid())
            .getService(KnowledgeService.class)
            .getStorage());
        notifyNeighbours();
    }

    public void addDrops(List<ItemStack> drops) {
        for (var i = 0; i < storage.size(); i++) {
            var stack = storage.getStack(i);

            if (stack != null) {
                var tile = host.getTileEntity();
                stack.what().addDrops(stack.amount(), drops, tile.getWorld(), tile.getPos());
            }
        }

        for (var is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    public void clearContent() {
        storage.clear();
        upgrades.clear();
    }

    private class WrappedEMCStorage implements MEStorage {
        private final EMCStorage storage;

        private WrappedEMCStorage(EMCStorage storage) {
            this.storage = storage;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return what instanceof AEItemKey item && mainNode.isActive()
                ? storage.insertItem(item, amount, mode, source, isUpgradedWith(AppliedEItems.LEARNING_CARD))
                : 0;
        }

        @Override
        public ITextComponent getDescription() {
            return new TextComponentTranslation(AppliedEItems.EMC_INTERFACE.getTranslationKey() + ".name");
        }
    }
}
