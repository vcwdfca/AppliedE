package gripe._90.appliede.me.key;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;

public final class EMCKeyType extends AEKeyType {
    public static final EMCKeyType TYPE = new EMCKeyType();
    private static final ITextComponent EMC = new TextComponentTranslation("key." + AppliedE.MODID + ".emc");

    private EMCKeyType() {
        super(AppliedE.id("emc"), EMCKey.class, EMC);
    }

    @Nullable
    @Override
    public AEKey readFromPacket(PacketBuffer input) {
        return EMCKey.of(input.readVarInt());
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(NBTTagCompound tag) {
        return EMCKey.fromTag(tag);
    }

    @Override
    public int getAmountPerByte() {
        return AppliedEConfig.CONFIG.getEmcPerByte();
    }

    @Override
    public int getAmountPerOperation() {
        return 2000;
    }

    @Override
    public ITextComponent getDescription() {
        return EMC;
    }
}
