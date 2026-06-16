package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileAuroraVeil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderAuroraVeil extends RenderCelestialEffectBase<TileAuroraVeil> {

    private static final double VEIL_WIDTH = 3.70D;
    private static final double VEIL_HEIGHT = 3.55D;
    private static final double VEIL_BASE_Y = -0.42D;
    private static final double VEIL_DEPTH = 0.26D;
    private static final int VEIL_LAYERS = 6;
    private static final int VEIL_SEGMENTS = 40;
    private static final int RAY_COUNT = 15;
    private static final int MOTE_COUNT = 56;
    private static final float WAVE_SPEED = 0.026F;
    private static final float DRIFT_SPEED = 0.010F;

    private static final int[] VEIL_COLORS = new int[]{
            0x45FFD6, 0x69F5FF, 0x8C76FF, 0xFF8FD8, 0xB7FF72, 0xFFFFFF
    };

    @Override
    protected void renderCelestialEffect(TileAuroraVeil te, float ticks) {
        drawBaseGlow(ticks);
        drawVeilLayers(ticks);
        drawVerticalRays(ticks);
        drawMotes(ticks);
    }

    private void drawBaseGlow(float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        drawFlatRing(0.34D, 1.92D + pulse * 0.12D, 0x56FFE0, 0.12F + pulse * 0.04F, 128);
        drawFlatRing(1.30D, 2.70D + pulse * 0.10D, 0x946DFF, 0.060F + pulse * 0.025F, 128);
        RenderHelper.drawSphere(0.38D + pulse * 0.05D, 0xFFFFFF, 0.22F + pulse * 0.06F, 18, 18);
        useAlphaBlend();
    }

    private void drawVeilLayers(float ticks) {
        for (int i = 0; i < VEIL_LAYERS; i++) {
            float pulse = wave(ticks * (0.020D + i * 0.003D) + i * 0.77D);
            int color = VEIL_COLORS[i % VEIL_COLORS.length];
            double angle = i * 31.0D + Math.sin(ticks * 0.004D + i) * 4.5D;
            double zOffset = (i - (VEIL_LAYERS - 1) * 0.5D) * VEIL_DEPTH;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) angle, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, 0.0D, zOffset);
            GlStateManager.scale(1.0D - i * 0.025D, 1.0D + pulse * 0.035D, 1.0D);
            drawAuroraSheet(ticks, i, color, 0.20F + pulse * 0.075F);
            GlStateManager.popMatrix();
        }
    }

    private void drawAuroraSheet(float ticks, int layer, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= VEIL_SEGMENTS; i++) {
            double progress = (double) i / VEIL_SEGMENTS;
            double centered = progress - 0.5D;
            double edgeFade = Math.sin(Math.PI * progress);
            double x = centered * VEIL_WIDTH;
            double phase = ticks * WAVE_SPEED + progress * Math.PI * 4.2D + layer * 0.84D;
            double sway = Math.sin(phase) * (0.22D + layer * 0.018D);
            double smallWave = Math.sin(phase * 1.9D + layer) * 0.075D;
            double z = sway + smallWave;
            double bottomY = VEIL_BASE_Y + Math.sin(phase + 1.3D) * 0.055D;
            double topY = VEIL_BASE_Y + VEIL_HEIGHT * (0.88D + 0.12D * edgeFade)
                    + Math.sin(phase * 0.78D) * 0.18D;
            float topAlpha = alpha * (float) (0.18D + 0.82D * edgeFade);
            float bottomAlpha = alpha * (float) (0.050D + 0.20D * edgeFade);

            buffer.pos(x, bottomY, z * 0.45D)
                    .color(rgb[0], rgb[1], rgb[2], bottomAlpha)
                    .endVertex();
            buffer.pos(x, topY, z)
                    .color(rgb[0], rgb[1], rgb[2], topAlpha)
                    .endVertex();
        }

        tessellator.draw();
    }

    private void drawVerticalRays(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(2.2F);
        for (int i = 0; i < RAY_COUNT; i++) {
            double progress = (double) i / (RAY_COUNT - 1);
            double x = (progress - 0.5D) * (VEIL_WIDTH * 0.92D);
            double phase = ticks * 0.023D + i * 0.61D;
            double z = Math.sin(phase) * 0.42D;
            double height = 2.10D + wave(phase) * 1.30D;
            int color = VEIL_COLORS[i % VEIL_COLORS.length];
            float alpha = 0.065F + 0.055F * wave(ticks * 0.031D + i);

            RenderHelper.drawLine(x, VEIL_BASE_Y + 0.05D, z * 0.20D,
                    x + Math.sin(phase * 0.9D) * 0.14D, VEIL_BASE_Y + height, z,
                    color, alpha);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawMotes(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = (i + 0.5D) / MOTE_COUNT;
            double yaw = i * 2.399963229728653D + ticks * DRIFT_SPEED;
            double heightWave = fract(progress + ticks * 0.0023D + (i % 7) * 0.013D);
            double radius = 0.35D + (i % 11) * 0.145D;
            double x = Math.cos(yaw) * radius;
            double y = VEIL_BASE_Y + heightWave * (VEIL_HEIGHT + 0.20D);
            double z = Math.sin(yaw) * radius + Math.sin(ticks * 0.020D + i) * 0.12D;
            double size = 0.018D + (i % 4) * 0.006D;
            float alpha = 0.18F + 0.24F * wave(ticks * 0.037D + i * 0.9D);

            drawSphereAt(x, y, z, size, VEIL_COLORS[(i + 2) % VEIL_COLORS.length], alpha, 6, 6);
        }
        useAlphaBlend();
    }
}
