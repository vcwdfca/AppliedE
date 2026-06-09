package gripe._90.appliede.client;

import java.awt.Color;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import ae2.api.client.AEKeyRenderHandler;

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
        GlStateManager.pushMatrix();
        try {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            renderSprite(stack, x, y, x + 16.0F, y + 16.0F, 0.0F);
        } finally {
            restoreGuiRenderState();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void drawOnBlockFace(EMCKey what, float scale, int combinedLight, World level) {
        float x0 = -scale / 2.0F;
        float y0 = -scale / 2.0F;
        float x1 = scale / 2.0F;
        float y1 = scale / 2.0F;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            renderSprite(what, x0, y0, x1, y1, 0.0001F);
        } finally {
            restoreBlockFaceRenderState();
            GlStateManager.popMatrix();
        }
    }

    private void renderSprite(EMCKey stack, float x0, float y0, float x1, float y1, float z) {
        var hueIntervals = AppliedEClientConfig.CONFIG.getEmcTierColours();
        var hue = ((stack.getTier() - 1) * 360F / hueIntervals) / 360;
        var color = Color.HSBtoRGB(hue, stack.getTier() == 1 ? 0 : 0.6F, 1);
        var red = (color >> 16 & 255) / 255.0F;
        var green = (color >> 8 & 255) / 255.0F;
        var blue = (color & 255) / 255.0F;
        var alpha = 1.0F;
        var icon = sprite.get();

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x0, y1, z).tex(icon.getMinU(), icon.getMaxV()).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z).tex(icon.getMaxU(), icon.getMaxV()).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y0, z).tex(icon.getMaxU(), icon.getMinV()).color(red, green, blue, alpha).endVertex();
        buffer.pos(x0, y0, z).tex(icon.getMinU(), icon.getMinV()).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    private static void restoreGuiRenderState() {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void restoreBlockFaceRenderState() {
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public ITextComponent getDisplayName(EMCKey stack) {
        return stack.getDisplayName();
    }
}
