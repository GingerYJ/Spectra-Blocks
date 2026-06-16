package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileFireflySwarm;
import net.minecraft.client.renderer.GlStateManager;

public class RenderFireflySwarm extends RenderCelestialEffectBase<TileFireflySwarm> {

    private static final int FIREFLY_COUNT = 44;
    private static final int WISP_COUNT = 18;
    private static final double SWARM_RADIUS = 1.36D;
    private static final double SWARM_HEIGHT = 1.74D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float DRIFT_SPEED = 0.014F;
    private static final float FLICKER_SPEED = 0.115F;
    private static final int[] FIREFLY_COLORS = new int[]{
            0xFFE98A, 0xFFD45D, 0xFFF7B8, 0xBFFF5A
    };

    @Override
    protected void renderCelestialEffect(TileFireflySwarm te, float ticks) {
        drawWarmHaze(ticks);
        drawFireflies(ticks);
        drawSoftTrails(ticks);
    }

    private void drawWarmHaze(float ticks) {
        float pulse = wave(ticks * 0.038F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.22D, 0.0D);
        RenderHelper.drawSphere(0.72D + pulse * 0.08D, 0x6F5B18, 0.045F + pulse * 0.025F, 16, 16);
        RenderCelestialEffectBase.drawFlatRing(0.12D, 1.18D + pulse * 0.10D,
                0xB38A22, 0.050F + pulse * 0.030F, 80);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFireflies(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < FIREFLY_COUNT; i++) {
            double phase = i * 0.619D;
            double yaw = i * GOLDEN_ANGLE + ticks * (DRIFT_SPEED + (i % 5) * 0.0018D);
            double bob = Math.sin(ticks * 0.037D + phase) * 0.18D;
            double radius = (0.30D + (i % 11) * 0.085D)
                    + Math.sin(ticks * 0.021D + i * 1.27D) * 0.12D;
            double y = -0.58D + (i % 17) * (SWARM_HEIGHT / 16.0D) + bob;
            double horizontal = Math.max(0.24D, 1.0D - Math.abs(y) * 0.18D);
            double x = Math.cos(yaw) * radius * horizontal;
            double z = Math.sin(yaw) * radius * horizontal;
            float flicker = wave(ticks * FLICKER_SPEED + phase);
            float alpha = 0.16F + flicker * (0.34F + (i % 3) * 0.035F);
            double coreSize = 0.022D + flicker * 0.020D + (i % 4) * 0.002D;
            int color = FIREFLY_COLORS[i % FIREFLY_COLORS.length];

            drawSphereAt(x, y, z, coreSize * 2.5D, color, alpha * 0.22F, 7, 7);
            drawSphereAt(x, y, z, coreSize, i % 7 == 0 ? 0xFFFFFF : color, alpha, 5, 5);

            if (flicker > 0.82F && (i % 4) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                RenderEnergyEffectHelper.drawSpark(coreSize * 3.2D, color, alpha * 0.46F);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }

    private void drawSoftTrails(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.15F);
        for (int i = 0; i < WISP_COUNT; i++) {
            double phase = i * 0.83D;
            double angle = i * GOLDEN_ANGLE + ticks * (0.010D + (i % 3) * 0.001D);
            double radius = 0.44D + (i % 6) * 0.12D;
            double y = -0.50D + (i % 9) * 0.17D + Math.sin(ticks * 0.030D + phase) * 0.08D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double x2 = Math.cos(angle - 0.22D) * (radius + 0.08D);
            double z2 = Math.sin(angle - 0.22D) * (radius + 0.08D);
            float alpha = 0.060F + 0.055F * wave(ticks * 0.052D + phase);

            RenderHelper.drawLine(x, y, z, x2, y + 0.05D, z2, 0xFFE38A, alpha);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }
}
