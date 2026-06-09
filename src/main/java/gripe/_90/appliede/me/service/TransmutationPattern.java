package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ae2.api.crafting.IPatternDetails;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEItems;
import gripe._90.appliede.me.key.EMCKey;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class TransmutationPattern implements IPatternDetails {
    private final AEItemKey output;
    private final long amount;
    private final int tier;
    private final int job;
    private final AEItemKey definition;

    public TransmutationPattern(AEItemKey output, long amount, int job) {
        this.output = output;
        this.amount = amount;
        this.tier = 1;
        this.job = job;
        this.definition = createDefinition(output, amount, tier, job);
    }

    public TransmutationPattern(int tier) {
        this.output = null;
        this.amount = 1;
        this.tier = tier;
        this.job = 0;
        this.definition = createDefinition(null, amount, tier, job);
    }

    private static AEItemKey createDefinition(@Nullable AEItemKey output, long amount, int tier, int job) {
        var definition = new ItemStack(AppliedEItems.DUMMY_EMC_ITEM);
        var tag = new NBTTagCompound();
        tag.setLong("amount", amount);
        tag.setInteger("tier", tier);
        tag.setInteger("job", job);
        if (output != null) {
            tag.setTag("output", output.toTagGeneric());
        }
        definition.setTagCompound(tag);
        return Objects.requireNonNull(AEItemKey.of(definition));
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        if (output == null) {
            return new IInput[] {new Input(1, tier)};
        }

        var inputs = new ArrayList<IInput>();
        var itemEmc = ProjectEAPI.getEMCProxy().getValue(output.toStack());
        var totalEmc = BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(amount));
        var currentTier = 1;

        while (totalEmc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            inputs.add(new Input(totalEmc.remainder(AppliedE.TIER_LIMIT).longValue(), currentTier));
            totalEmc = totalEmc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        inputs.add(new Input(totalEmc.longValue(), currentTier));
        return inputs.toArray(new IInput[0]);
    }

    @Override
    public List<GenericStack> getOutputs() {
        return Collections.singletonList(
            output != null
                ? new GenericStack(output, amount)
                : new GenericStack(EMCKey.of(tier - 1), AppliedE.TIER_LIMIT.longValue()));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransmutationPattern pattern
            && Objects.equals(pattern.output, output)
            && pattern.amount == amount
            && pattern.tier == tier
            && pattern.job == job;
    }

    @Override
    public int hashCode() {
        return Objects.hash(output, amount, tier, job);
    }

    private record Input(long amount, int tier) implements IInput {
        @Override
        public GenericStack[] possibleInputs() {
            return new GenericStack[] {new GenericStack(EMCKey.of(tier), amount)};
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, World level) {
            return input.matches(possibleInputs()[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
