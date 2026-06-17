package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileAbyssalCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderAbyssalCore extends RenderCelestialEffectBase<TileAbyssalCore> {

    private static final double CORE_RADIUS = 0.62D;
    private static final double INNER_GLOW_RADIUS = 1.18D;
    private static final double WATER_SHELL_RADIUS = 2.72D;
    private static final double OUTER_PLANKTON_RADIUS = 3.45D;
    private static final int SPHERE_SEGMENTS = 28;
    private static final int RING_SEGMENTS = 144;
    private static final int PLANKTON_COUNT = 104;
    private static final int WAVE_RING_COUNT = 6;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float CORE_PULSE_SPEED = 0.050F;
    private static final float CURRENT_SPEED = 0.006F;

    @Override
    protected void renderCelestialEffect(TileAbyssalCore te, float ticks) {
        drawCore(ticks);
        drawWaterShell(ticks);
        drawWaveRings(ticks);
        drawPlankton(ticks);
        drawRisingBubbles(ticks);
    }

    private void drawCore(float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        RenderHelper.drawSphere(INNER_GLOW_RADIUS + pulse * 0.12D, 0x3AF7DF, 0.22F + pulse * 0.06F,
                SPHERE_SEGMENTS, SPHERE_SEGMENTS);
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.045D, 0xE6FFF8, 0.58F + pulse * 0.12F,
                SPHERE_SEGMENTS, SPHERE_SEGMENTS);
        RenderHelper.drawSphere(CORE_RADIUS * 0.64D, 0x64FFE4, 0.62F, 20, 20);
        useAlphaBlend();
    }

    private void drawWaterShell(float ticks) {
        float pulse = wave(ticks * 0.022D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.030F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.25F);
        GlStateManager.scale(1.0D, 0.78D + pulse * 0.035D, 1.0D);
        RenderHelper.drawSphere(WATER_SHELL_RADIUS + pulse * 0.07D, 0x0A6F78, 0.145F + pulse * 0.030F,
                SPHERE_SEGMENTS, SPHERE_SEGMENTS);
        RenderHelper.drawSphere(WATER_SHELL_RADIUS * 0.76D, 0x1DCDC1, 0.065F + pulse * 0.025F,
                SPHERE_SEGMENTS, SPHERE_SEGMENTS);
        GlStateManager.glLineWidth(1.3F);
        RenderHelper.drawWireframeSphere(WATER_SHELL_RADIUS * 0.98D, 0x57FFF0, 0.075F + pulse * 0.030F,
                9, 16);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawWaveRings(float ticks) {
        useAdditiveBlend();
        int ringCount = RenderQuality.detailCount(WAVE_RING_COUNT, 3);
        for (int i = 0; i < ringCount; i++) {
            double progress = (i + 1.0D) / ringCount;
            double y = -1.16D + i * 0.46D + Math.sin(ticks * 0.026D + i) * 0.055D;
            double baseRadius = 0.68D + progress * 2.10D;
            float pulse = wave(ticks * (0.028D + i * 0.003D) + i * 0.81D);
            int color = i % 2 == 0 ? 0x69FFF0 : 0x2AA9FF;
            float alpha = 0.10F + pulse * 0.070F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(7.0F + i * 9.0F, 1.0F, 0.0F, 0.25F);
            GlStateManager.rotate(ticks * (0.035F + i * 0.006F), 0.0F, 1.0F, 0.0F);
            drawFlatRing(baseRadius - 0.035D, baseRadius + 0.035D + pulse * 0.025D,
                    color, alpha * 0.44F, RING_SEGMENTS);
            GlStateManager.glLineWidth(1.6F);
            RenderHelper.drawCircle(baseRadius + pulse * 0.045D, color, alpha, RING_SEGMENTS);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawPlankton(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        RenderHelper.PointBatch points = RenderQuality.low() ? RenderHelper.beginPointBatch(2.0F) : null;
        for (int i = 0; i < PLANKTON_COUNT; i += stride) {
            double band = (i + 0.5D) / PLANKTON_COUNT;
            double yaw = i * GOLDEN_ANGLE + ticks * (CURRENT_SPEED + (i % 5) * 0.0007D);
            double yNorm = -0.88D + (i % 41) * (1.76D / 40.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double current = Math.sin(ticks * 0.018D + i * 0.53D) * 0.16D;
            double radius = 1.05D + Math.pow(band, 0.58D) * (OUTER_PLANKTON_RADIUS - 1.05D);
            double x = Math.cos(yaw) * horizontal * (radius + current);
            double y = yNorm * radius * 0.72D + Math.sin(yaw * 1.7D + ticks * 0.010D) * 0.12D;
            double z = Math.sin(yaw) * horizontal * (radius - current * 0.45D);
            double size = 0.018D + (i % 5) * 0.005D;
            int color = i % 7 == 0 ? 0xE9FFF8 : (i % 3 == 0 ? 0x48FFE2 : 0x2CC8FF);
            float alpha = 0.13F + 0.28F * wave(ticks * 0.036D + i);

            if (points != null) {
                points.add(x, y, z, color, alpha * 1.08F);
            } else {
                drawSphereAt(x, y, z, size, color, alpha, 6, 6);
            }
            if (points == null && (i & 15) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                RenderEnergyEffectHelper.drawSpark(size * 3.2D, color, alpha * 0.55F);
                GlStateManager.popMatrix();
            }
        }
        if (points != null) {
            points.draw();
        }
        useAlphaBlend();
    }

    private void drawRisingBubbles(float ticks) {
        useAlphaBlend();
        int bubbleCount = RenderQuality.detailCount(22, 7);
        for (int i = 0; i < bubbleCount; i++) {
            double progress = fract(i * 0.137D + ticks * 0.0036D);
            double yaw = i * GOLDEN_ANGLE + Math.sin(ticks * 0.010D + i) * 0.20D;
            double radius = 0.26D + (i % 6) * 0.19D;
            double x = Math.cos(yaw) * radius;
            double y = -1.45D + progress * 2.96D;
            double z = Math.sin(yaw) * radius;
            double bubble = 0.025D + (i % 4) * 0.008D;
            float alpha = (float) Math.sin(Math.PI * progress) * 0.16F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawWireframeSphere(bubble, 0xB9FFF5, alpha, 4, 6);
            RenderHelper.drawSphere(bubble * 0.80D, 0x8BFFF0, alpha * 0.36F, 5, 5);
            GlStateManager.popMatrix();
        }
    }
}
