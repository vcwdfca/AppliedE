package gripe._90.appliede.mixin.crafting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ae2.api.crafting.IPatternDetails;
import ae2.api.networking.IGridNode;
import ae2.api.networking.crafting.ICraftingService;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.KeyCounter;
import ae2.crafting.CraftingCalculation;
import ae2.crafting.CraftingTreeNode;
import ae2.crafting.CraftingTreeProcess;
import ae2.crafting.inv.CraftingSimulationState;

import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {
    @Unique
    private long appliede$requestedAmount;

    @Inject(method = "request", at = @At("HEAD"))
    private void trackRequested(
            CraftingSimulationState inv, long requestedAmount, KeyCounter containerItems, CallbackInfo ci) {
        appliede$requestedAmount = requestedAmount;
    }

    // spotless:off
    @WrapOperation(
            method = "buildChildPatterns",
            at = @At(
                    value = "NEW",
                    target = "(Lae2/api/networking/crafting/ICraftingService;Lae2/crafting/CraftingCalculation;Lae2/api/crafting/IPatternDetails;Lae2/crafting/CraftingTreeNode;)Lae2/crafting/CraftingTreeProcess;"))
    // spotless:on
    private CraftingTreeProcess recalculatePattern(
            ICraftingService craftingService,
            CraftingCalculation job,
            IPatternDetails details,
            CraftingTreeNode node,
            Operation<CraftingTreeProcess> original,
            @Local IGridNode gridNode) {
        if (details instanceof TransmutationPattern) {
            if (details.getOutputs().getFirst().what() instanceof AEItemKey item) {
                details = new TransmutationPattern(item, appliede$requestedAmount, job.hashCode());
            }

            gridNode.grid().getService(KnowledgeService.class).addTemporaryPattern(details);
        }

        return original.call(craftingService, job, details, node);
    }
}
