package gripe._90.appliede.mixin.tooltip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.InventoryPlayer;

import ae2.api.networking.IGridNode;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.AEKeyFilter;
import ae2.api.storage.ITerminalHost;
import ae2.container.AEBaseContainer;
import ae2.container.me.common.ContainerMEStorage;
import ae2.container.me.common.IncrementalUpdateHelper;
import ae2.core.network.clientbound.MEInventoryUpdatePacket;

import gripe._90.appliede.me.reporting.TransmutablePacketBuilder;
import gripe._90.appliede.me.service.KnowledgeService;

@Mixin(ContainerMEStorage.class)
public abstract class MEStorageMenuMixin extends AEBaseContainer {
    @Shadow
    @Final
    private IncrementalUpdateHelper updateHelper;

    @Unique
    private Set<AEItemKey> appliede$transmutables = new HashSet<>();

    @Unique
    private Set<AEItemKey> appliede$previousTransmutables = new HashSet<>();

    public MEStorageMenuMixin(InventoryPlayer playerInventory, ITerminalHost host) {
        super(playerInventory, host);
    }

    @Shadow
    protected abstract boolean showsCraftables();

    @Shadow
    public abstract @Nullable IGridNode getGridNode();

    @Unique
    private Set<AEItemKey> appliede$getTransmutablesFromGrid() {
        if (!showsCraftables()) {
            return Collections.emptySet();
        }

        return getGridNode() != null && getGridNode().isActive()
                ? getGridNode().grid().getService(KnowledgeService.class).getKnownItems()
                : Collections.emptySet();
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(value = "INVOKE", target = "Lae2/container/me/common/IncrementalUpdateHelper;hasChanges()Z"))
    private void addTransmutables(CallbackInfo ci) {
        appliede$transmutables = appliede$getTransmutablesFromGrid();
        Sets.difference(appliede$previousTransmutables, appliede$transmutables).forEach(updateHelper::addChange);
        Sets.difference(appliede$transmutables, appliede$previousTransmutables).forEach(updateHelper::addChange);
    }

    // spotless:off
    @ModifyReceiver(
            method = "broadcastChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lae2/core/network/clientbound/MEInventoryUpdatePacket$Builder;setFilter(Lae2/api/storage/AEKeyFilter;)V"))
    // spotless:on
    private MEInventoryUpdatePacket.Builder addTransmutables(
            MEInventoryUpdatePacket.Builder builder, AEKeyFilter filter) {
        ((TransmutablePacketBuilder) builder).appliede$addTransmutables(appliede$transmutables);
        return builder;
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(value = "INVOKE", target = "Lae2/container/AEBaseContainer;broadcastChanges()V"))
    private void addPreviousTransmutables(CallbackInfo ci) {
        appliede$previousTransmutables = ImmutableSet.copyOf(appliede$transmutables);
    }
}
