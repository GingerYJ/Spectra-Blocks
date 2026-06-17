package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileSolarCoronaBurst;
import net.minecraft.client.renderer.GlStateManager;

public class RenderSolarCoronaBurst extends RenderCelestialEffectBase<TileSolarCoronaBurst> {

    private static final double CORE_RADIUS = 0.70D;
    private static final double CORONA_RADIUS = 1.42D;
    private static final int PROMINENCE_COUNT = 12;
    private static final int SPARK_COUNT = 90;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileSolarCoronaBurst te, float ticks) {
        drawSolarCore(ticks);
        drawCoronaShell(ticks);
        drawProminences(ticks);
        drawExpelledSparks(ticks);
    }

    private void drawSolarCore(float ticks) {
        float pulse = wave(ticks * 0.070D);

        useAdditiveBlend();
        RenderHelper.drawSphere(CORONA_RADIUS + pulse * 0.12D, 0xFFB31F, 0.105F + pulse * 0.040F, 26, 26);
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.055D, 0xFFE06F, 0.46F + pulse * 0.08F, 24, 24);
        RenderHelper.drawSphere(CORE_RADIUS * 0.62D, 0xFFFFFF, 0.34F + pulse * 0.12F, 20, 20);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.55F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(21.0F, 1.0F, 0.0F, 0.35F);
        drawFlatRing(CORE_RADIUS * 1.12D, CORE_RADIUS * 1.20D, 0xFF6A00, 0.12F + pulse * 0.05F, 96);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCoronaShell(float ticks) {
        float pulse = wave(ticks * 0.043D);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.4F);
        RenderHelper.drawWireframeSphere(CORONA_RADIUS + pulse * 0.08D, 0xFFD36A, 0.13F + pulse * 0.055F, 7, 14);
        RenderHelper.resetLineWidth();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(76.0F, 1.0F, 0.1F, 0.0F);
        GlStateManager.rotate(ticks * -0.28F, 0.0F, 1.0F, 0.0F);
        RenderEnergyEffectHelper.drawStarRays(CORE_RADIUS * 1.10D, CORONA_RADIUS * 1.72D,
                28, 0xFF8A19, 0.12F + pulse * 0.060F, ticks * 0.014D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawProminences(float ticks) {
        useAdditiveBlend();
        int prominenceCount = RenderQuality.detailCount(PROMINENCE_COUNT, 4);
        for (int i = 0; i < prominenceCount; i++) {
            double cycle = fract(ticks * (0.018D + (i % 4) * 0.002D) + i * 0.137D);
            float surge = (float) Math.sin(Math.PI * cycle);
            surge = surge * surge;
            double yaw = i * 0.823D + ticks * (0.007D + (i % 3) * 0.0015D);
            double sweep = 0.68D + (i % 4) * 0.16D + surge * 0.32D;
            double basePitch = -0.54D + (i % 6) * 0.18D;
            double pitchWave = 0.17D + (i % 3) * 0.055D;
            double arcRadius = CORONA_RADIUS * (1.02D + surge * 0.68D);
            int color = i % 3 == 0 ? 0xFFF3A2 : (i % 3 == 1 ? 0xFF8A19 : 0xFF3D00);

            GlStateManager.glLineWidth(5.0F);
            drawSphericalArc(arcRadius, yaw, sweep, basePitch, pitchWave,
                    ticks * 0.037D + i, color, 0.080F + surge * 0.095F, 42);
            GlStateManager.glLineWidth(2.0F);
            drawSphericalArc(arcRadius * 0.98D, yaw + 0.025D, sweep * 0.94D, basePitch, pitchWave * 0.82D,
                    ticks * 0.046D + i * 1.7D, 0xFFF8D6, 0.12F + surge * 0.22F, 42);
            RenderHelper.resetLineWidth();

            if (surge > 0.46F) {
                GlStateManager.pushMatrix();
                double tipYaw = yaw + sweep * 0.52D;
                double tipPitch = basePitch + Math.sin(ticks * 0.037D + i + Math.PI) * pitchWave;
                double horizontal = Math.cos(tipPitch) * arcRadius;
                GlStateManager.translate(Math.cos(tipYaw) * horizontal,
                        Math.sin(tipPitch) * arcRadius,
                        Math.sin(tipYaw) * horizontal);
                RenderHelper.drawSphere(0.045D + surge * 0.055D, 0xFFF4BA, 0.28F * surge, 8, 8);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }

    private void drawExpelledSparks(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        RenderHelper.PointBatch points = RenderQuality.low() ? RenderHelper.beginPointBatch(2.0F) : null;
        for (int i = 0; i < SPARK_COUNT; i += stride) {
            double progress = fract(ticks * (0.006D + (i % 5) * 0.0009D) + i * 0.061D);
            double yaw = i * GOLDEN_ANGLE + ticks * (0.004D + (i % 3) * 0.001D);
            double yNorm = -0.88D + (i % 31) * (1.76D / 30.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = CORONA_RADIUS * (0.96D + progress * 1.86D);
            float fade = (float) (1.0D - progress);
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius * 0.78D + Math.sin(ticks * 0.05D + i) * 0.06D;
            double z = Math.sin(yaw) * horizontal * radius;
            int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD15D : 0xFF5A00);
            float alpha = fade * (0.12F + 0.22F * wave(ticks * 0.064D + i));
            double size = 0.020D + fade * 0.046D;

            if (points != null) {
                points.add(x, y, z, color, alpha * 1.12F);
            } else {
                drawSphereAt(x, y, z, size, color, alpha, 6, 6);
            }
        }
        if (points != null) {
            points.draw();
        }
        useAlphaBlend();
    }
}
