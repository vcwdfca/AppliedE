package gripe._90.appliede.part;

import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import ae2.api.config.Actionable;
import ae2.api.crafting.IPatternDetails;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridNodeListener;
import ae2.api.networking.crafting.ICraftingProvider;
import ae2.api.networking.security.IActionSource;
import ae2.api.networking.ticking.IGridTickable;
import ae2.api.networking.ticking.TickRateModulation;
import ae2.api.networking.ticking.TickingRequest;
import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.IStorageMounts;
import ae2.api.storage.IStorageProvider;
import ae2.container.GuiIds;
import ae2.container.ISubGui;
import ae2.core.gui.GuiOpener;
import ae2.helpers.IPriorityHost;
import ae2.items.parts.PartModels;
import ae2.parts.AEBasePart;
import ae2.parts.PartModel;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.TransmutationPattern;

public final class EMCModulePart extends AEBasePart
        implements IStorageProvider, ICraftingProvider, IPriorityHost, IGridTickable {
    @PartModels
    private static final IPartModel MODEL = new PartModel(AppliedE.id("part/emc_module"));

    private final Object2LongMap<AEKey> outputs = new Object2LongOpenHashMap<>();

    private int priority = 0;

    public EMCModulePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IStorageProvider.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(AppliedEConfig.CONFIG.getModuleEnergyUsage());
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("priority", priority);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        priority = data.getInteger("priority");
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        IStorageProvider.requestUpdate(getMainNode());
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        var grid = getMainNode().getGrid();

        if (grid != null) {
            mounts.mount(grid.getService(KnowledgeService.class).getStorage(getMainNode()));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(KnowledgeService.class).getPatterns(getMainNode()) : List.of();
    }

    @Override
    public int getPatternPriority() {
        return priority;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder, int multiplier) {
        if (!getMainNode().isActive() || !(patternDetails instanceof TransmutationPattern pattern)) {
            return false;
        }

        GenericStack output = pattern.getPrimaryOutput();
        outputs.merge(output.what(), output.amount() * multiplier, Long::sum);

        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        return true;
    }

    @Override
    public boolean canMergePatternPush(IPatternDetails patternDetails) {
        return patternDetails instanceof TransmutationPattern;
    }

    @Override
    public int getMaxPatternPushMultiplier(IPatternDetails patternDetails, int maxMultiplier) {
        return patternDetails instanceof TransmutationPattern ? maxMultiplier : 0;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, outputs.isEmpty());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var storage = node.grid().getStorageService().getInventory();

        for (var output : new Object2LongOpenHashMap<>(outputs).object2LongEntrySet()) {
            var what = output.getKey();
            var amount = output.getLongValue();
            var inserted = storage.insert(what, amount, Actionable.MODULATE, IActionSource.ofMachine(this));

            if (inserted >= amount) {
                outputs.removeLong(what);
            } else if (inserted > 0) {
                outputs.put(what, amount - inserted);
            }
        }

        return TickRateModulation.URGENT;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3, 3, 12, 13, 13, 16);
        bch.addBox(5, 5, 11, 11, 11, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!isClientSide()) {
            GuiOpener.openPartGui(player, GuiIds.GuiKey.PRIORITY, this);
        }

        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newPriority) {
        priority = newPriority;
        getHost().markForSave();
        IStorageProvider.requestUpdate(getMainNode());
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        player.closeScreen();
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return new ItemStack(AppliedEItems.EMC_MODULE);
    }
}
