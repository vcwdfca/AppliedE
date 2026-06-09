package gripe._90.appliede.mixin.misc;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.Item;

import ae2.api.features.P2PTunnelAttunement;

@Mixin(P2PTunnelAttunement.class)
public interface P2PTunnelAttunementAccessor {
    @Accessor
    static Map<String, Item> getTagTunnels() {
        throw new AssertionError();
    }
}
