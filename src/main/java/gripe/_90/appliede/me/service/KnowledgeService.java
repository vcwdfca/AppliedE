package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import ae2.api.crafting.IPatternDetails;
import ae2.api.networking.IGrid;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridService;
import ae2.api.networking.IGridServiceProvider;
import ae2.api.networking.IManagedGridNode;
import ae2.api.networking.crafting.ICraftingProvider;
import ae2.api.networking.security.IActionHost;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.IStorageProvider;
import ae2.api.storage.MEStorage;
import ae2.me.storage.NullInventory;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.part.EMCModulePart;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private static final int TICKS_PER_SYNC = AppliedEConfig.CONFIG.getSyncThrottleInterval();
    private static final Set<KnowledgeService> SERVICES = Collections.synchronizedSet(new HashSet<>());

    private final List<IManagedGridNode> moduleNodes = new ArrayList<>();
    private final Map<UUID, Supplier<IKnowledgeProvider>> providers = new HashMap<>();
    private final EMCStorage storage = new EMCStorage(this);
    private final Set<IPatternDetails> temporaryPatterns = new HashSet<>();

    final IGrid grid;
    private Set<AEItemKey> knownItemCache;
    private boolean needsSync;
    private int ticksSinceLastSync;

    public KnowledgeService(IGrid grid) {
        this.grid = grid;
        SERVICES.add(this);
    }

    public static void updateKnownItemsForAllGrids() {
        synchronized (SERVICES) {
            SERVICES.forEach(KnowledgeService::updateKnownItems);
        }
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable NBTTagCompound savedData) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            knownItemCache = null;
            moduleNodes.add(module.getMainNode());
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                addProvider(uuid);
            }

            updatePatterns();
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            knownItemCache = null;
            moduleNodes.remove(module.getMainNode());
            providers.clear();

            for (var mainNode : moduleNodes) {
                var node = mainNode.getNode();

                if (node != null) {
                    var uuid = node.getOwningPlayerProfileId();

                    if (uuid != null) {
                        addProvider(uuid);
                    }
                }
            }

            moduleNodes.forEach(IStorageProvider::requestUpdate);
            updatePatterns();
        }

        if (moduleNodes.isEmpty()) {
            SERVICES.remove(this);
        }
    }

    @Override
    public void onServerStartTick() {
        if (ticksSinceLastSync < TICKS_PER_SYNC) {
            ticksSinceLastSync++;
        }

        if (needsSync && ticksSinceLastSync == TICKS_PER_SYNC) {
            syncTrackedProviders();
            needsSync = false;
            ticksSinceLastSync = 0;
        }
    }

    private void addProvider(UUID playerUUID) {
        providers.putIfAbsent(playerUUID, retrieveProvider(playerUUID));
    }

    static Supplier<IKnowledgeProvider> retrieveProvider(UUID playerUUID) {
        return () -> ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(playerUUID);
    }

    List<IKnowledgeProvider> getProviders() {
        return providers.values().stream().map(Supplier::get).filter(provider -> provider != null).toList();
    }

    public Supplier<IKnowledgeProvider> getProviderFor(UUID uuid) {
        return providers.get(uuid);
    }

    Supplier<IKnowledgeProvider> getProviderFor(EntityPlayer player) {
        return getProviderFor(player.getUniqueID());
    }

    Supplier<IKnowledgeProvider> getProviderFor(IActionHost host) {
        var node = host.getActionableNode();

        if (node != null) {
            var uuid = node.getOwningPlayerProfileId();
            return uuid != null ? getProviderFor(uuid) : null;
        }

        return null;
    }

    public EMCStorage getStorage() {
        return storage;
    }

    public MEStorage getStorage(IManagedGridNode node) {
        return !moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive()
            ? storage
            : NullInventory.of();
    }

    public Set<AEItemKey> getKnownItems() {
        if (knownItemCache == null) {
            knownItemCache = new HashSet<>();

            for (var provider : getProviders()) {
                for (var item : provider.getKnowledge()) {
                    if (!ProjectEAPI.getEMCProxy().hasValue(item)) {
                        continue;
                    }

                    var key = AEItemKey.of(item);

                    if (key != null) {
                        knownItemCache.add(key);
                    }
                }
            }
        }

        return knownItemCache;
    }

    private void updateKnownItems() {
        knownItemCache = null;
        updateStorage();
        updatePatterns();
    }

    public List<IPatternDetails> getPatterns(IManagedGridNode node) {
        if (!moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive()) {
            var patterns = new ArrayList<IPatternDetails>();

            for (var tier = storage.getHighestTier(); tier > 1; tier--) {
                patterns.add(new TransmutationPattern(tier));
            }

            for (var item : getKnownItems()) {
                patterns.add(new TransmutationPattern(item, 1, 0));
            }

            patterns.addAll(temporaryPatterns);
            return patterns;
        }

        return Collections.emptyList();
    }

    public void addTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.add(pattern);
        updatePatterns();
    }

    public void removeTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.remove(pattern);
        updatePatterns();
    }

    void updatePatterns() {
        moduleNodes.forEach(ICraftingProvider::requestUpdate);
    }

    private void updateStorage() {
        moduleNodes.forEach(IStorageProvider::requestUpdate);
    }

    BigInteger getEmc() {
        var emc = BigInteger.ZERO;

        for (var providerSupplier : providers.values()) {
            var provider = providerSupplier.get();
            if (provider != null) {
                emc = emc.add(BigInteger.valueOf(provider.getEmc()));
            }
        }

        return emc;
    }

    public boolean isTrackingPlayer(EntityPlayer player) {
        var uuid = player.getUniqueID();
        return providers.containsKey(uuid);
    }

    void syncEmc() {
        needsSync = true;
    }

    private void syncTrackedProviders() {
        var server = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null) {
            return;
        }

        providers.forEach((uuid, providerSupplier) -> {
            var id = ae2.api.features.IPlayerRegistry.getMapping(server).getPlayerId(uuid);
            var player = ae2.api.features.IPlayerRegistry.getConnected(server, id);
            var provider = providerSupplier.get();

            if (player != null && provider != null) {
                provider.sync(player);
            }
        });
    }
}
