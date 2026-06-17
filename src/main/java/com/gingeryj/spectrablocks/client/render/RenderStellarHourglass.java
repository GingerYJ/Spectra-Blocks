package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileStellarHourglass;
import net.minecraft.client.renderer.GlStateManager;

public class RenderStellarHourglass extends RenderCelestialEffectBase<TileStellarHourglass> {

    private static final double LOBE_OFFSET = 1.16D;
    private static final double LOBE_RADIUS = 1.42D;
    private static final double FRAME_RADIUS = 2.06D;
    private static final int CLOUD_LAYER_COUNT = 4;
    private static final int DUST_COUNT = 118;
    private static final int STREAM_COUNT = 34;
    private static final int ARC_COUNT = 8;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float CLOUD_ROTATION_SPEED = 0.040F;
    private static final int TOP_COLOR = 0x85D9FF;
    private static final int BOTTOM_COLOR = 0xFFB978;
    private static final int STAR_COLOR = 0xF4F8FF;

    @Override
    protected void renderCelestialEffect(TileStellarHourglass te, float ticks) {
        drawFrame(ticks);
        drawNebulaLobe(ticks, LOBE_OFFSET, TOP_COLOR, 1.0F);
        drawNebulaLobe(ticks, -LOBE_OFFSET, BOTTOM_COLOR, -1.0F);
        drawFallingDust(ticks);
        drawStarArcs(ticks);
    }

    private void drawFrame(float ticks) {
        float pulse = wave(ticks * 0.036D);

        useAdditiveBlend();
        GlStateManager.glLineWidth(2.2F);
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i + ticks * 0.004D;
            RenderHelper.drawLine(Math.cos(angle) * FRAME_RADIUS, 1.70D, Math.sin(angle) * FRAME_RADIUS,
                    Math.cos(angle + Math.PI) * FRAME_RADIUS, -1.70D,
                    Math.sin(angle + Math.PI) * FRAME_RADIUS, 0xBEEBFF, 0.055F + pulse * 0.035F);
        }
        RenderHelper.resetLineWidth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.72D, 0.0D);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.drawCircle(FRAME_RADIUS * 0.82D, 0xDFF7FF, 0.14F, 88);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -1.72D, 0.0D);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.drawCircle(FRAME_RADIUS * 0.82D, 0xFFD6A3, 0.14F, 88);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawNebulaLobe(float ticks, double yOffset, int color, float direction) {
        useAlphaBlend();
        int layers = RenderQuality.low() ? Math.max(2, CLOUD_LAYER_COUNT - 1) : CLOUD_LAYER_COUNT;
        for (int i = 0; i < layers; i++) {
            double radius = LOBE_RADIUS + i * 0.22D;
            float pulse = wave(ticks * (0.026D + i * 0.006D) + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, yOffset, 0.0D);
            GlStateManager.rotate(direction * ticks * (CLOUD_ROTATION_SPEED + i * 0.014F), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(18.0F + i * 9.0F, 1.0F, 0.0F, 0.28F);
            GlStateManager.scale(1.0D + i * 0.05D, 0.46D + i * 0.04D, 1.0D - i * 0.035D);
            RenderHelper.drawSphere(radius + pulse * 0.08D, color, 0.070F + pulse * 0.032F, 22, 22);
            RenderHelper.drawWireframeSphere((radius + pulse * 0.08D) * 1.02D,
                    STAR_COLOR, 0.036F + pulse * 0.018F, 6, 10);
            GlStateManager.popMatrix();
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        RenderHelper.drawSphere(0.34D, STAR_COLOR, 0.42F, 14, 14);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFallingDust(float ticks) {
        useAdditiveBlend();
        int streamStride = RenderQuality.mediumOrLow() ? 2 : 1;
        for (int i = 0; i < STREAM_COUNT; i += streamStride) {
            double progress = fract(ticks * 0.018D + i * 0.071D);
            double y = 1.18D - progress * 2.36D;
            double pinch = Math.abs(y) / 1.18D;
            double radius = 0.07D + pinch * pinch * 0.46D;
            double angle = i * GOLDEN_ANGLE + ticks * 0.016D;
            int color = progress < 0.52D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.22F + 0.22F * wave(ticks * 0.055D + i);

            drawSphereAt(Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                    0.026D + (i % 4) * 0.005D, color, alpha, 6, 6);
        }

        int dustStride = RenderQuality.detailStride();
        for (int i = 0; i < DUST_COUNT; i += dustStride) {
            double top = i < DUST_COUNT / 2 ? 1.0D : -1.0D;
            int local = i % (DUST_COUNT / 2);
            double band = (local + 0.5D) / (DUST_COUNT / 2);
            double angle = local * GOLDEN_ANGLE + top * ticks * 0.007D;
            double radius = 0.28D + Math.pow(band, 0.56D) * 1.42D;
            double y = top * (0.62D + Math.sin(local * 0.53D + ticks * 0.020D) * 0.42D);
            int color = top > 0.0D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.12F + 0.12F * wave(ticks * 0.046D + local);

            drawSphereAt(Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                    0.018D + (local % 3) * 0.004D, color, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private void drawStarArcs(float ticks) {
        useAdditiveBlend();
        int arcCount = RenderQuality.detailCount(ARC_COUNT, 3);
        for (int i = 0; i < arcCount; i++) {
            double radius = 1.42D + (i % 4) * 0.17D;
            double start = i * 0.74D + ticks * (0.006D + (i % 2) * 0.002D);
            double sweep = Math.PI * (0.84D + (i % 3) * 0.18D);
            double y = i < ARC_COUNT / 2 ? 1.10D : -1.10D;
            double sign = y > 0.0D ? 1.0D : -1.0D;
            int color = y > 0.0D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.12F + 0.07F * wave(ticks * 0.044D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.glLineWidth(2.0F);
            drawSphericalArc(radius, start, sweep, sign * 0.24D, 0.15D,
                    ticks * 0.010D + i, color, alpha, 46);
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
