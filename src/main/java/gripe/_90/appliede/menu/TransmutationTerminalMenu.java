package gripe._90.appliede.menu;

import ae2.api.config.Actionable;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.IGridNode;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.container.GuiIds;
import ae2.container.SlotSemantic;
import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.me.common.ContainerMEStorage;
import ae2.container.slot.FakeSlot;
import ae2.helpers.InventoryAction;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TransmutationTerminalMenu extends ContainerMEStorage {
    protected static final SlotSemantic TRANSMUTE = SlotSemantics.register("APPLIEDE_TRANSMUTE", false);
    protected static final SlotSemantic UNLEARN = SlotSemantics.register("APPLIEDE_UNLEARN", false);

    private static final String ACTION_SET_SHIFT = "setShiftDestination";
    private static final String ACTION_HIDE_LEARNED = "hideLearnedText";
    private static final String ACTION_HIDE_UNLEARNED = "hideUnlearnedText";

    private final TransmutationTerminalHost host;
    private final Slot transmuteSlot = new FakeSlot(InternalInventory.empty(), 0, 0, 0);
    private final Slot unlearnSlot = new FakeSlot(InternalInventory.empty(), 0, 0, 0);

    @GuiSync(1)
    public boolean shiftToTransmute;

    @GuiSync(2)
    public int learnedLabelTicks;

    @GuiSync(3)
    public int unlearnedLabelTicks;

    public TransmutationTerminalMenu(InventoryPlayer ip, TransmutationTerminalHost host) {
        this(GuiIds.GuiKey.ME_STORAGE_TERMINAL, ip, host, true);
    }

    protected TransmutationTerminalMenu(
        GuiIds.GuiKey guiKey,
        InventoryPlayer ip,
        TransmutationTerminalHost host,
        boolean bindInventory
    ) {
        super(guiKey, ip, host, bindInventory);
        this.host = host;
        registerClientAction(ACTION_SET_SHIFT, Boolean.class, host::setShiftToTransmute);
        registerClientAction(ACTION_HIDE_LEARNED, () -> learnedLabelTicks--);
        registerClientAction(ACTION_HIDE_UNLEARNED, () -> unlearnedLabelTicks--);
        addSlot(transmuteSlot, TRANSMUTE);
        addSlot(unlearnSlot, UNLEARN);
    }

    public static TransmutationTerminalMenu wireless(InventoryPlayer ip, TransmutationTerminalHost host) {
        return new TransmutationTerminalMenu(GuiIds.GuiKey.WIRELESS_TERMINAL, ip, host, true);
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        super.doAction(player, action, slot, id);

        if (slot < 0 || slot >= inventorySlots.size()) {
            return;
        }

        var targetSlot = getSlot(slot);
        if (targetSlot == transmuteSlot && !getCarried().isEmpty()) {
            int transmuted = transmuteItem(getCarried(), action == InventoryAction.SPLIT_OR_PLACE_SINGLE, player);
            ItemStack reduced = getCarried().copy();
            reduced.shrink(transmuted);
            setCarried(reduced.isEmpty() ? ItemStack.EMPTY : reduced);
        }

        if (targetSlot == unlearnSlot && !getCarried().isEmpty()) {
            IGridNode node = host.getActionableNode();
            if (node == null) {
                return;
            }

            KnowledgeService knowledge = node.grid().getService(KnowledgeService.class);
            if (!knowledge.isTrackingPlayer(player)) {
                return;
            }

            var providerSupplier = knowledge.getProviderFor(player.getUniqueID());
            var provider = providerSupplier != null ? providerSupplier.get() : null;
            if (provider != null && provider.hasKnowledge(getCarried())) {
                provider.removeKnowledge(getCarried());
                provider.sync(player);
                unlearnedLabelTicks = 300;
                learnedLabelTicks = 0;
                broadcastChanges();
            }
        }
    }

    private int transmuteItem(ItemStack stack, boolean singleItem, EntityPlayer player) {
        if (stack.isEmpty()) {
            return 0;
        }

        IGridNode node = host.getActionableNode();
        if (node == null) {
            return 0;
        }

        KnowledgeService knowledge = node.grid().getService(KnowledgeService.class);
        if (!knowledge.isTrackingPlayer(player)) {
            return 0;
        }

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) {
            return 0;
        }

        long inserted = knowledge.getStorage().insertItem(
            key,
            singleItem ? 1 : stack.getCount(),
            Actionable.MODULATE,
            IActionSource.ofPlayer(player, host),
            true,
            true,
            this::showLearned
        );
        return (int) inserted;
    }

    public void setShiftToTransmute(boolean transmute) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_SHIFT, transmute);
            return;
        }

        shiftToTransmute = transmute;
    }

    public void showLearned() {
        learnedLabelTicks = 300;
        unlearnedLabelTicks = 0;
        broadcastChanges();
    }

    public void decrementLearnedTicks() {
        if (isClientSide()) {
            sendClientAction(ACTION_HIDE_LEARNED);
        }
    }

    public void decrementUnlearnedTicks() {
        if (isClientSide()) {
            sendClientAction(ACTION_HIDE_UNLEARNED);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        if (shiftToTransmute && idx >= 0 && idx < inventorySlots.size()) {
            Slot slot = inventorySlots.get(idx);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                int transmuted = transmuteItem(stack, false, player);
                ItemStack remaining = stack.copy();
                remaining.shrink(transmuted);
                slot.putStack(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
            return ItemStack.EMPTY;
        }

        return super.transferStackInSlot(player, idx);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            shiftToTransmute = host.getShiftToTransmute();
        }
    }

    @Override
    public TransmutationTerminalHost getHost() {
        return host;
    }
}
