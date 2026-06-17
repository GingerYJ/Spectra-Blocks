package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileSolarCoronaBurst;
import net.minecraft.client.renderer.GlStateManager;

public class RenderSolarCoronaBurst extends RenderCelestialEffectBase<TileSolarCoronaBurst> {

    private static final double CORE_RADIUS = 0.70D;
    private static final double CORONA_RADIUS = 1.42D;
    private static final int PROMINENCE_COUNT = 12;
    private static final int SPARK_COUNT = 64;
    private static final int RING_SEGMENTS = 128;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileSolarCoronaBurst te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawSolarCore(ticks, naturalShader, colorShader);
        drawCoronaShell(ticks, naturalShader, colorShader);
        drawProminences(ticks, naturalShader, colorShader);
        drawExpelledSparks(ticks, naturalShader);
    }

    private void drawSolarCore(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.070D);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORONA_RADIUS + pulse * 0.12D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.8F, 0xFF7A00, 0xFFB31F, 0xFFF3A2,
                0.13F + pulse * 0.050F, pulse, 0.96F, ticks * 0.045F, 17.0F, 28);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS + pulse * 0.055D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.0F, 0xFF9A1A, 0xFFE06F, 0xFFFFFF,
                0.50F + pulse * 0.10F, pulse, 1.40F, ticks * 0.078F, 41.0F, 28);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS * 0.62D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.2F, 0xFFE06F, 0xFFFFFF, 0xFFF8D6,
                0.34F + pulse * 0.12F, pulse, 1.62F, ticks * 0.095F, 73.0F, 22);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.55F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(21.0F, 1.0F, 0.0F, 0.35F);
        RenderNaturalShaderHelper.drawBasicFlatRing(colorShader,
                CORE_RADIUS * 1.12D, CORE_RADIUS * 1.20D,
                0xFF6A00, 0.12F + pulse * 0.05F, RING_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCoronaShell(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.043D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * -0.18F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORONA_RADIUS + pulse * 0.08D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.4F, 0xFF5A00, 0xFFD36A, 0xFFF8D6,
                0.16F + pulse * 0.055F, pulse, 1.05F, ticks * 0.056F, 101.0F, 26);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(76.0F, 1.0F, 0.1F, 0.0F);
        GlStateManager.rotate(ticks * -0.28F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawBasicStarRays(colorShader, CORE_RADIUS * 1.10D,
                CORONA_RADIUS * 1.72D, 28, 0xFF8A19,
                0.12F + pulse * 0.060F, ticks * 0.014D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawProminences(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        useAdditiveBlend();
        for (int i = 0; i < PROMINENCE_COUNT; i++) {
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
            RenderNaturalShaderHelper.drawBasicSphericalArc(colorShader, arcRadius, yaw, sweep,
                    basePitch, pitchWave, ticks * 0.037D + i, color,
                    0.080F + surge * 0.095F, 42);
            GlStateManager.glLineWidth(2.0F);
            RenderNaturalShaderHelper.drawBasicSphericalArc(colorShader, arcRadius * 0.98D,
                    yaw + 0.025D, sweep * 0.94D, basePitch, pitchWave * 0.82D,
                    ticks * 0.046D + i * 1.7D, 0xFFF8D6,
                    0.12F + surge * 0.22F, 42);
            RenderHelper.resetLineWidth();

            if (surge > 0.46F) {
                double tipYaw = yaw + sweep * 0.52D;
                double tipPitch = basePitch + Math.sin(ticks * 0.037D + i + Math.PI) * pitchWave;
                double horizontal = Math.cos(tipPitch) * arcRadius;
                GlStateManager.pushMatrix();
                GlStateManager.translate(Math.cos(tipYaw) * horizontal,
                        Math.sin(tipPitch) * arcRadius,
                        Math.sin(tipYaw) * horizontal);
                RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.045D + surge * 0.055D,
                        RenderNaturalShaderHelper.MODE_SOLAR, 2.0F, 0xFFF4BA, 0xFFFFFF, 0xFF8A19,
                        0.28F * surge, surge, 1.26F, ticks * 0.078F, i * 37.0F, 8);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }

    private void drawExpelledSparks(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(ticks * (0.006D + (i % 5) * 0.0009D) + i * 0.061D);
            double yaw = i * GOLDEN_ANGLE + ticks * (0.004D + (i % 3) * 0.001D);
            double yNorm = -0.88D + (i % 31) * (1.76D / 30.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = CORONA_RADIUS * (0.96D + progress * 1.86D);
            float fade = (float) (1.0D - progress);
            int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD15D : 0xFF5A00);
            float alpha = fade * (0.12F + 0.22F * wave(ticks * 0.064D + i));

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(yaw) * horizontal * radius,
                    yNorm * radius * 0.78D + Math.sin(ticks * 0.05D + i) * 0.06D,
                    Math.sin(yaw) * horizontal * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.020D + fade * 0.046D,
                    RenderNaturalShaderHelper.MODE_SOLAR, 2.6F + (i % 5) * 0.1F,
                    color, 0xFF8A19, 0xFFF8D6, alpha, fade, 1.14F,
                    ticks * 0.062F, 211.0F + i * 11.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
