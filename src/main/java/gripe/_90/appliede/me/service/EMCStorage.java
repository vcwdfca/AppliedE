package gripe._90.appliede.me.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ae2.api.config.Actionable;
import ae2.api.config.PowerMultiplier;
import ae2.api.features.IPlayerRegistry;
import ae2.api.networking.energy.IEnergyService;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.MEStorage;
import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.menu.TransmutationTerminalMenu;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public final class EMCStorage implements MEStorage {
    private final KnowledgeService service;
    private int highestTier = 1;

    EMCStorage(KnowledgeService service) {
        this.service = service;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var emc = service.getEmc();
        var currentTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            out.add(EMCKey.of(currentTier), emc.remainder(AppliedE.TIER_LIMIT).longValue());
            emc = emc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        out.add(EMCKey.of(currentTier), emc.longValue());

        if (highestTier != currentTier) {
            highestTier = currentTier;
            service.updatePatterns();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || !(what instanceof EMCKey emc) || service.getProviders().isEmpty()) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var providers = new ArrayList<>(service.getProviders());
            Collections.shuffle(providers);

            if (emc.getTier() == 1) {
                var quotient = amount / providers.size();
                var remainder = amount % providers.size();

                for (var p = 0; p < providers.size(); p++) {
                    var provider = providers.get(p);
                    setEmc(provider, BigInteger.valueOf(provider.getEmc())
                        .add(BigInteger.valueOf(quotient + (p < remainder ? 1 : 0))));
                }
            } else {
                var toInsert = BigInteger.valueOf(amount).multiply(AppliedE.TIER_LIMIT.pow(emc.getTier() - 1));
                distributeEmc(toInsert, providers);
            }

            service.syncEmc();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || service.getProviders().isEmpty()) {
            return 0;
        }

        if (what instanceof AEItemKey item && source.player().isPresent()) {
            return extractItem(item, amount, mode, source, false);
        }

        if (!(what instanceof EMCKey emc)) {
            return 0;
        }

        var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);
        var rawEmc = BigInteger.valueOf(amount).multiply(multiplier);
        var extracted = BigInteger.ZERO;
        var providers = getProvidersForExtraction(source);

        while (!providers.isEmpty() && extracted.compareTo(rawEmc) < 0) {
            Collections.shuffle(providers);

            var toExtract = rawEmc.subtract(extracted);
            var divisor = BigInteger.valueOf(providers.size());
            var quotient = toExtract.divide(divisor);
            var remainder = toExtract.remainder(divisor).longValue();

            for (int p = 0; p < providers.size(); p++) {
                var provider = providers.get(p);
                var currentEmc = BigInteger.valueOf(provider.getEmc());
                var toExtractFrom = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);

                if (currentEmc.compareTo(toExtractFrom) <= 0) {
                    if (mode == Actionable.MODULATE) {
                        provider.setEmc(0);
                    }

                    extracted = extracted.add(currentEmc);
                    providers.remove(provider);
                    p--;
                } else {
                    if (mode == Actionable.MODULATE) {
                        setEmc(provider, currentEmc.subtract(toExtractFrom));
                    }

                    extracted = extracted.add(toExtractFrom);
                }
            }
        }

        if (mode == Actionable.MODULATE) {
            service.syncEmc();
        }

        return extracted.divide(multiplier).longValue();
    }

    public long insertItem(
        AEItemKey what,
        long amount,
        Actionable mode,
        IActionSource source,
        boolean mayLearn,
        boolean consumePower,
        Runnable onLearn) {
        if (amount <= 0 || service.getProviders().isEmpty()) {
            return 0;
        }

        var stack = what.toStack();
        if ((!mayLearn && !service.getKnownItems().contains(what)) || !ProjectEAPI.getEMCProxy().hasValue(stack)) {
            return 0;
        }

        var player = source.player().orElse(null);
        var machine = source.machine().orElse(null);
        var playerProvider = player != null ? service.getProviderFor(player) : null;
        var machineProvider = machine != null ? service.getProviderFor(machine) : null;

        if (mayLearn) {
            if (player != null && playerProvider == null) {
                return 0;
            }

            if (machine != null && machineProvider == null) {
                return 0;
            }
        }

        if (mode == Actionable.MODULATE) {
            var itemEmc = BigInteger.valueOf(ProjectEAPI.getEMCProxy().getSellValue(stack));
            var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));

            if (consumePower) {
                amount = getAmountAfterPowerExpenditure(totalEmc, itemEmc, service.grid.getEnergyService());
                totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
            }

            if (amount == 0) {
                return 0;
            }

            var providers = new ArrayList<>(service.getProviders());
            Collections.shuffle(providers);
            distributeEmc(totalEmc, providers);
            service.syncEmc();

            if (mayLearn) {
                var resolvedPlayerProvider = playerProvider != null ? playerProvider.get() : null;
                if (player != null && resolvedPlayerProvider != null && !resolvedPlayerProvider.hasKnowledge(stack)) {
                    addKnowledge(what, resolvedPlayerProvider, player);
                    onLearn.run();
                }

                var resolvedMachineProvider = machineProvider != null ? machineProvider.get() : null;
                if (machine != null && resolvedMachineProvider != null && !resolvedMachineProvider.hasKnowledge(stack)) {
                    var node = Objects.requireNonNull(machine.getActionableNode());
                    var owner = IPlayerRegistry.getConnected(node.getLevel().getMinecraftServer(), node.getOwningPlayerId());
                    addKnowledge(what, resolvedMachineProvider, owner);
                    onLearn.run();
                }
            }
        }

        return amount;
    }

    public long insertItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean mayLearn) {
        return insertItem(what, amount, mode, source, mayLearn, true, () -> {});
    }

    public long extractItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean skipStored) {
        if (source.player().isPresent()
            && !(source.player().get().openContainer instanceof TransmutationTerminalMenu)) {
            return 0;
        }

        if (amount <= 0 || !service.getKnownItems().contains(what)) {
            return 0;
        }

        var existingStored = service.grid.getStorageService().getCachedInventory();

        if (!skipStored && existingStored.get(what) > 0) {
            return 0;
        }

        var itemEmc = BigInteger.valueOf(ProjectEAPI.getEMCProxy().getValue(what.toStack()));

        if (itemEmc.signum() <= 0) {
            return 0;
        }

        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var providers = getProvidersForExtraction(source);

        if (providers.isEmpty()) {
            return 0;
        }

        var availableEmc = totalEmc.min(
            providers.equals(service.getProviders()) ? service.getEmc() : BigInteger.valueOf(providers.getFirst().getEmc()));

        amount = availableEmc.divide(itemEmc).longValue();

        if (amount == 0) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            amount = getAmountAfterPowerExpenditure(availableEmc, itemEmc, service.grid.getEnergyService());

            if (amount == 0) {
                return 0;
            }

            availableEmc = itemEmc.multiply(BigInteger.valueOf(amount));
            var withdrawn = BigInteger.ZERO;

            while (!providers.isEmpty() && withdrawn.compareTo(availableEmc) < 0) {
                Collections.shuffle(providers);

                var toWithdraw = availableEmc.subtract(withdrawn);
                var divisor = BigInteger.valueOf(providers.size());
                var quotient = toWithdraw.divide(divisor);
                var remainder = toWithdraw.remainder(divisor).longValue();

                for (int p = 0; p < providers.size(); p++) {
                    var provider = providers.get(p);
                    var currentEmc = BigInteger.valueOf(provider.getEmc());
                    var toWithdrawFrom = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);

                    if (currentEmc.compareTo(toWithdrawFrom) <= 0) {
                        provider.setEmc(0);
                        withdrawn = withdrawn.add(currentEmc);
                        providers.remove(provider);
                        p--;
                    } else {
                        setEmc(provider, currentEmc.subtract(toWithdrawFrom));
                        withdrawn = withdrawn.add(toWithdrawFrom);
                    }
                }
            }

            service.syncEmc();
        }

        return amount;
    }

    private static void distributeEmc(BigInteger totalEmc, ArrayList<IKnowledgeProvider> providers) {
        var divisor = BigInteger.valueOf(providers.size());
        var quotient = totalEmc.divide(divisor);
        var remainder = totalEmc.remainder(divisor).longValue();

        for (var p = 0; p < providers.size(); p++) {
            var provider = providers.get(p);
            var added = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);
            setEmc(provider, BigInteger.valueOf(provider.getEmc()).add(added));
        }
    }

    private List<IKnowledgeProvider> getProvidersForExtraction(IActionSource source) {
        var providers = new ArrayList<IKnowledgeProvider>();

        if (source.player().isPresent() && AppliedEConfig.CONFIG.terminalExtractFromOwnEmcOnly()) {
            var provider = service.getProviderFor(source.player().get());
            if (provider != null && provider.get() != null) {
                providers.add(provider.get());
            }
        } else {
            providers.addAll(service.getProviders());
        }

        return providers;
    }

    private static long getAmountAfterPowerExpenditure(BigInteger maxEmc, BigInteger itemEmc, IEnergyService energy) {
        var multiplier = BigDecimal.valueOf(PowerMultiplier.CONFIG.multiplier)
            .multiply(BigDecimal.valueOf(AppliedEConfig.CONFIG.getTransmutationPowerMultiplier()))
            .divide(BigDecimal.valueOf(EMCKeyType.TYPE.getAmountPerOperation()), 4, RoundingMode.HALF_UP);
        var toExpend = new BigDecimal(maxEmc).multiply(multiplier).min(BigDecimal.valueOf(Double.MAX_VALUE));

        var available = energy.extractAEPower(toExpend.doubleValue(), Actionable.SIMULATE, PowerMultiplier.ONE);
        var expended = Math.min(available, toExpend.doubleValue());
        var amount = BigDecimal.valueOf(available)
            .min(toExpend)
            .divide(multiplier, RoundingMode.HALF_UP)
            .toBigInteger()
            .divide(itemEmc)
            .longValue();

        if (amount > 0) {
            energy.extractAEPower(expended, Actionable.MODULATE, PowerMultiplier.ONE);
        }

        return amount;
    }

    private static void addKnowledge(AEItemKey what, IKnowledgeProvider provider, EntityPlayer player) {
        var stack = what.toStack();
        provider.addKnowledge(stack);

        if (player instanceof EntityPlayerMP serverPlayer) {
            provider.sync(serverPlayer);
        }
    }

    private static void setEmc(IKnowledgeProvider provider, BigInteger value) {
        provider.setEmc(value.min(BigInteger.valueOf(Long.MAX_VALUE)).max(BigInteger.ZERO).longValue());
    }

    int getHighestTier() {
        return highestTier;
    }

    @Override
    public ITextComponent getDescription() {
        return new TextComponentTranslation(AppliedEItems.EMC_MODULE.getTranslationKey() + ".name");
    }
}
