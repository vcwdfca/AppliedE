package gripe._90.appliede.mixin.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.EntityPlayer;

import ae2.api.networking.IGrid;
import ae2.api.networking.crafting.ICraftingPlan;
import ae2.api.networking.crafting.ICraftingSubmitResult;
import ae2.container.implementations.ContainerCraftConfirm;

import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(ContainerCraftConfirm.class)
public abstract class CraftConfirmMenuMixin {
    @Shadow
    private ICraftingPlan result;

    @Unique
    private boolean appliede$submitted = false;

    @Shadow
    private IGrid getGrid() {
        throw new AssertionError();
    }

    // spotless:off
    @Redirect(
            method = "startJob(Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lae2/api/networking/crafting/ICraftingSubmitResult;successful()Z"))
    // spotless:on
    private boolean setSubmitted(ICraftingSubmitResult submitResult) {
        appliede$submitted = submitResult.successful();
        return appliede$submitted;
    }

    @Inject(method = "onContainerClosed", at = @At("TAIL"))
    private void clearTemporaryPatterns(EntityPlayer player, CallbackInfo ci) {
        if (getGrid() != null && result != null && !appliede$submitted) {
            for (var pattern : result.patternTimes().keySet()) {
                if (pattern instanceof TransmutationPattern) {
                    getGrid().getService(KnowledgeService.class).removeTemporaryPattern(pattern);
                }
            }
        }
    }
}
