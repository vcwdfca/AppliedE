package gripe._90.appliede.menu;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.GenericStack;
import ae2.container.AEBaseContainer;
import ae2.container.ISubGui;
import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.implementations.ContainerSetStockAmount;
import ae2.container.slot.InaccessibleSlot;
import ae2.util.inv.AppEngInternalInventory;
import com.google.common.primitives.Ints;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class EMCSetStockAmountMenu extends AEBaseContainer implements ISubGui {
    private static final ConcurrentMap<UUID, PendingAmount> PENDING_AMOUNTS = new ConcurrentHashMap<>();

    private final EMCInterfaceLogicHost host;
    private final Slot stockedItem;
    private AEItemKey whatToStock;

    @GuiSync(1)
    private int initialAmount = -1;

    @GuiSync(2)
    private int maxAmount = -1;

    @GuiSync(3)
    private int slot = -1;

    public EMCSetStockAmountMenu(InventoryPlayer playerInventory, EMCInterfaceLogicHost host) {
        super(playerInventory, host);
        registerClientAction(ContainerSetStockAmount.ACTION_SET_STOCK_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        this.stockedItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        addSlot(stockedItem, SlotSemantics.MACHINE_OUTPUT);

        if (playerInventory.player instanceof EntityPlayerMP player) {
            PendingAmount pending = PENDING_AMOUNTS.remove(player.getUniqueID());
            if (pending != null) {
                setWhatToStock(pending.slot, pending.whatToStock, pending.initialAmount);
            }
        }
    }

    public static void remember(
        EntityPlayerMP player,
        int slot,
        AEItemKey toStock,
        int initialAmount
    ) {
        PENDING_AMOUNTS.put(player.getUniqueID(), new PendingAmount(slot, toStock, initialAmount));
    }

    @Override
    public EMCInterfaceLogicHost getHost() {
        return host;
    }

    private void setWhatToStock(int slot, AEItemKey whatToStock, int initialAmount) {
        this.slot = slot;
        this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
        this.initialAmount = initialAmount;
        this.maxAmount = Ints.saturatedCast(host.getConfig().getMaxAmount(whatToStock));
        stockedItem.putStack(whatToStock.wrapForDisplayOrFilter());
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void confirm(int amount) {
        if (isClientSide()) {
            sendClientAction(ContainerSetStockAmount.ACTION_SET_STOCK_AMOUNT, amount);
            return;
        }

        var config = host.getConfig();
        if (!Objects.equals(config.getKey(slot), whatToStock)) {
            host.returnToMainContainer(getPlayer(), this);
            return;
        }

        amount = (int) Math.min(amount, config.getMaxAmount(whatToStock));
        config.setStack(slot, amount <= 0 ? null : new GenericStack(whatToStock, amount));
        host.returnToMainContainer(getPlayer(), this);
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    @Nullable
    public AEItemKey getWhatToStock() {
        var stack = GenericStack.fromItemStack(stockedItem.getStack());
        return stack != null && stack.what() instanceof AEItemKey item ? item : null;
    }

    private static final class PendingAmount {
        private final int slot;
        private final AEItemKey whatToStock;
        private final int initialAmount;

        private PendingAmount(int slot, AEItemKey whatToStock, int initialAmount) {
            this.slot = slot;
            this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
            this.initialAmount = initialAmount;
        }
    }
}
