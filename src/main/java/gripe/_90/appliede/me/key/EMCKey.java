package gripe._90.appliede.me.key;

import java.util.List;
import java.util.Objects;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import gripe._90.appliede.AppliedE;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class EMCKey extends AEKey {
    public static final EMCKey BASE = new EMCKey(1);

    private final int tier;

    private EMCKey(int tier) {
        if (tier <= 0) {
            throw new IllegalArgumentException("Tier must be positive");
        }

        this.tier = tier;
    }

    public static EMCKey of(int tier) {
        return tier == 1 ? BASE : new EMCKey(tier);
    }

    static EMCKey fromTag(NBTTagCompound tag) {
        return of(tag.getInteger("tier"));
    }

    public int getTier() {
        return tier;
    }

    @Override
    public AEKeyType getType() {
        return EMCKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("tier", tier);
        return tag;
    }

    @Override
    public Object getPrimaryKey() {
        return tier;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("projecte", "emc_" + tier);
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
        data.writeVarInt(tier);
    }

    @Nullable
    @Override
    public Object getReadOnlyStack() {
        return null;
    }

    @Override
    protected ITextComponent computeDisplayName() {
        return new TextComponentTranslation(
            "key." + AppliedE.MODID + ".emc" + (tier == 1 ? "" : "_tiered"),
            tier);
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, World level, BlockPos pos) {
    }

    @Override
    public boolean isTagged(String tag) {
        return false;
    }

    @Nullable
    @Override
    public NBTBase get(String componentId) {
        return null;
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EMCKey key && tier == key.tier;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tier);
    }
}
