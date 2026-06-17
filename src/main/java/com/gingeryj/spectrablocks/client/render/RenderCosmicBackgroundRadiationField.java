package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileCosmicBackgroundRadiationField;
import net.minecraft.client.renderer.GlStateManager;

public class RenderCosmicBackgroundRadiationField
        extends RenderCelestialEffectBase<TileCosmicBackgroundRadiationField> {

    private static final double FIELD_RADIUS = 5.90D;
    private static final int SHELL_SEGMENTS = 34;
    private static final int NOISE_POINT_COUNT = 158;
    private static final int CONTOUR_COUNT = 13;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float DRIFT_SPEED = 0.006F;

    @Override
    protected void renderCelestialEffect(TileCosmicBackgroundRadiationField te, float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * DRIFT_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(11.0F + ticks * 0.002F, 1.0F, 0.0F, 0.25F);
        drawFieldShell(ticks);
        drawContours(ticks);
        drawNoise(ticks);
        GlStateManager.popMatrix();
    }

    private void drawFieldShell(float ticks) {
        float pulse = wave(ticks * 0.008D);

        useAlphaBlend();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        RenderHelper.drawSphere(FIELD_RADIUS, 0xC9DEFF, 0.050F + pulse * 0.018F,
                SHELL_SEGMENTS, SHELL_SEGMENTS);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();

        useAdditiveBlend();
        RenderHelper.drawWireframeSphere(FIELD_RADIUS * 1.004D, 0xEAF4FF,
                0.025F + pulse * 0.014F, 10, 18);
        useAlphaBlend();
    }

    private void drawContours(float ticks) {
        useAdditiveBlend();
        int contourCount = RenderQuality.detailCount(CONTOUR_COUNT, 5);
        for (int i = 0; i < contourCount; i++) {
            double band = -0.82D + i * (1.64D / (contourCount - 1));
            double y = band * FIELD_RADIUS;
            int color = i % 3 == 0 ? 0xFFD6AC : (i % 3 == 1 ? 0xD9FFE9 : 0xBFD8FF);
            float alpha = 0.045F + 0.026F * wave(ticks * 0.010D + i * 0.61D);

            GlStateManager.glLineWidth(i % 4 == 0 ? 2.0F : 1.0F);
            drawLatitudeCircle(FIELD_RADIUS * (0.990D + (i % 2) * 0.004D), y,
                    color, alpha, 160);
            RenderHelper.resetLineWidth();
        }

        int arcCount = RenderQuality.detailCount(8, 3);
        for (int i = 0; i < arcCount; i++) {
            double startYaw = i * Math.PI * 0.31D + ticks * 0.0015D;
            double basePitch = -0.58D + i * 0.165D;
            int color = i % 2 == 0 ? 0xF3E9FF : 0xBEE9FF;

            GlStateManager.glLineWidth(1.2F);
            drawSphericalArc(FIELD_RADIUS * 1.006D, startYaw, Math.PI * 1.18D,
                    basePitch, 0.050D, ticks * 0.006D + i,
                    color, 0.060F, 64);
            RenderHelper.resetLineWidth();
        }
        useAlphaBlend();
    }

    private void drawNoise(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        RenderHelper.PointBatch points = RenderQuality.low() ? RenderHelper.beginPointBatch(2.0F) : null;
        for (int i = 0; i < NOISE_POINT_COUNT; i += stride) {
            double yaw = i * GOLDEN_ANGLE + ticks * (0.0009D + (i % 7) * 0.00008D);
            double yNorm = -0.98D + (i % 53) * (1.96D / 52.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double mottling = Math.sin(i * 12.9898D + ticks * 0.006D) * 0.5D
                    + Math.cos(i * 4.141D - ticks * 0.004D) * 0.5D;
            double radius = FIELD_RADIUS * (0.955D + mottling * 0.010D);
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius;
            double z = Math.sin(yaw) * horizontal * radius;
            int color;
            if (mottling > 0.38D) {
                color = 0xFFD7B5;
            } else if (mottling < -0.32D) {
                color = 0xB8D5FF;
            } else {
                color = 0xE6FFF0;
            }

            float alpha = 0.055F + 0.035F * wave(ticks * 0.011D + i * 0.43D);
            double size = 0.010D + (i % 4) * 0.003D;
            if (points != null) {
                points.add(x, y, z, color, alpha * 1.20F);
            } else {
                drawSphereAt(x, y, z, size, color, alpha, 5, 5);
            }
        }
        if (points != null) {
            points.draw();
        }
        useAlphaBlend();
    }
}
