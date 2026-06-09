package gripe._90.appliede.client;

import java.awt.Color;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import ae2.api.client.AEKeyRenderHandler;
import ae2.client.gui.style.Blitter;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;

public final class EMCRenderer implements AEKeyRenderHandler<EMCKey> {
    public static final EMCRenderer INSTANCE = new EMCRenderer();

    private final Supplier<TextureAtlasSprite> sprite = () -> Minecraft.getMinecraft()
            .getTextureMapBlocks()
            .getAtlasSprite(AppliedE.id("item/dummy_emc_item").toString());

    private EMCRenderer() {}

    @Override
    public void drawInGui(Minecraft minecraft, int x, int y, EMCKey stack) {
        var hueIntervals = AppliedEClientConfig.CONFIG.getEmcTierColours();
        var hue = ((stack.getTier() - 1) * 360F / hueIntervals) / 360;

        Blitter.sprite(sprite.get())
                .blending(false)
                .dest(x, y, 16, 16)
                .colorRgb(Color.HSBtoRGB(hue, stack.getTier() == 1 ? 0 : 0.6F, 1))
                .blit();
    }

    @Override
    public void drawOnBlockFace(EMCKey what, float scale, int combinedLight, World level) {
        // Supergiant's block-face rendering is not needed for this compile-only port.
    }

    @Override
    public ITextComponent getDisplayName(EMCKey stack) {
        return stack.getDisplayName();
    }
}
