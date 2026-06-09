package gripe._90.appliede.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import ae2.api.stacks.AEKey;

import gripe._90.appliede.me.key.EMCKey;

@Mixin(targets = "ae2.container.me.common.ContainerMEStorage$1")
public abstract class MEStorageMenuMixin {
    // spotless:off
    @ModifyArg(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lae2/api/storage/StorageHelper;poweredExtraction(Lae2/api/networking/energy/IEnergySource;Lae2/api/storage/MEStorage;Lae2/api/stacks/AEKey;JLae2/api/networking/security/IActionSource;Lae2/api/config/Actionable;)J"),
            index = 2)
    // spotless:on
    private AEKey emcExtraction(AEKey what) {
        return what instanceof EMCKey ? EMCKey.BASE : what;
    }
}
