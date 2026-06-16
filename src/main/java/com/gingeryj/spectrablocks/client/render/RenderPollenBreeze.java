package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TilePollenBreeze;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderPollenBreeze extends RenderCelestialEffectBase<TilePollenBreeze> {

    private static final int BREEZE_BAND_COUNT = 4;
    private static final int BAND_SEGMENTS = 56;
    private static final int POLLEN_COUNT = 86;
    private static final double BAND_LENGTH = 3.05D;
    private static final double BAND_WIDTH = 0.18D;
    private static final double BAND_SWAY = 0.30D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float BAND_FLOW_SPEED = 0.025F;
    private static final float POLLEN_FLOW_SPEED = 0.013F;
    private static final int[] POLLEN_COLORS = new int[]{
            0xFFF1A4, 0xD8FF7A, 0xB9F25C, 0xFFE06E
    };

    @Override
    protected void renderCelestialEffect(TilePollenBreeze te, float ticks) {
        drawBreezeBands(ticks);
        drawPollen(ticks);
        drawLeafyMotes(ticks);
    }

    private void drawBreezeBands(float ticks) {
        useAlphaBlend();
        for (int i = 0; i < BREEZE_BAND_COUNT; i++) {
            double phase = ticks * BAND_FLOW_SPEED + i * 1.37D;
            double y = -0.38D + i * 0.25D;
            int color = i % 2 == 0 ? 0xD8FF7A : 0xFFF0A0;
            float alpha = 0.060F + 0.030F * wave(ticks * 0.041D + i);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(-8.0F + i * 4.0F, 0.0F, 1.0F, 0.0F);
            drawWindRibbon(y, phase, color, alpha);
            GlStateManager.popMatrix();
        }
    }

    private void drawPollen(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < POLLEN_COUNT; i++) {
            double progress = fract(ticks * (POLLEN_FLOW_SPEED + (i % 5) * 0.0009D) + i * 0.037D);
            double x = -BAND_LENGTH * 0.5D + progress * BAND_LENGTH;
            double phase = i * 0.71D + ticks * 0.035D;
            double band = (i % BREEZE_BAND_COUNT) - 1.5D;
            double y = -0.36D + (i % 9) * 0.13D + Math.sin(phase) * 0.075D;
            double z = Math.sin(progress * Math.PI * 2.0D + phase) * (0.30D + Math.abs(band) * 0.05D);
            double curl = Math.sin(progress * Math.PI) * 0.18D;
            double size = 0.016D + (i % 4) * 0.004D + curl * 0.014D;
            float fade = (float) Math.sin(Math.PI * progress);
            float alpha = 0.12F + fade * 0.30F;
            int color = POLLEN_COLORS[i % POLLEN_COLORS.length];

            drawSphereAt(x, y, z, size * 2.6D, color, alpha * 0.12F, 6, 6);
            drawSphereAt(x, y, z, size, color, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private void drawLeafyMotes(float ticks) {
        useAlphaBlend();
        GlStateManager.glLineWidth(1.2F);
        for (int i = 0; i < 16; i++) {
            double progress = fract(ticks * 0.009D + i * 0.071D);
            double angle = i * GOLDEN_ANGLE + ticks * 0.010D;
            double x = -1.35D + progress * 2.70D;
            double y = -0.30D + (i % 7) * 0.16D + Math.sin(ticks * 0.026D + i) * 0.05D;
            double z = Math.sin(angle) * (0.22D + (i % 4) * 0.045D);
            float alpha = 0.070F + 0.090F * (float) Math.sin(Math.PI * progress);

            RenderHelper.drawLine(x - 0.04D, y, z, x + 0.04D, y + 0.015D, z + 0.02D,
                    0xB8F45C, alpha);
        }
        RenderHelper.resetLineWidth();
    }

    private static void drawWindRibbon(double yOffset, double phase, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= BAND_SEGMENTS; i++) {
            double progress = (double) i / BAND_SEGMENTS;
            double x = -BAND_LENGTH * 0.5D + progress * BAND_LENGTH;
            double wave = Math.sin(progress * Math.PI * 2.0D + phase) * BAND_SWAY;
            double y = yOffset + Math.sin(progress * Math.PI * 3.0D + phase * 0.7D) * 0.055D;
            double width = BAND_WIDTH * (0.35D + 0.65D * Math.sin(Math.PI * progress));
            float pointAlpha = alpha * (float) Math.sin(Math.PI * progress);

            buffer.pos(x, y, wave - width).color(rgb[0], rgb[1], rgb[2], pointAlpha * 0.40F).endVertex();
            buffer.pos(x, y, wave + width).color(rgb[0], rgb[1], rgb[2], pointAlpha).endVertex();
        }
        tessellator.draw();
    }
}
