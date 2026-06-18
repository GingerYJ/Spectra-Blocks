package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderEmberBloom extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.24D;
    private static final double HEAT_HALO_RADIUS = 1.12D;
    private static final double INNER_PETAL_LENGTH = 0.92D;
    private static final double OUTER_PETAL_LENGTH = 1.34D;
    private static final int INNER_PETAL_COUNT = 8;
    private static final int OUTER_PETAL_COUNT = 12;
    private static final int EMBER_COUNT = 32;
    private static final int RING_SEGMENTS = 96;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawHeatHalo(ticks, naturalShader);
        drawFlamePetals(ticks, naturalShader);
        drawBloomCore(ticks, naturalShader);
        drawRisingEmbers(ticks, naturalShader);
    }

    private void drawHeatHalo(float ticks, ShaderProgram naturalShader) {
        float shimmer = wave(ticks * 0.055D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.30D, 0.0D);
        GlStateManager.scale(1.0D, 0.07D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, HEAT_HALO_RADIUS + shimmer * 0.07D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.2F, 0xA82200, 0xFF7A00, 0xFFE08A,
                0.13F + shimmer * 0.055F, shimmer, 0.84F, ticks * 0.040F, 19.0F, 24);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.28D, 0.0D);
        GlStateManager.rotate(ticks * 0.34F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderRing(naturalShader, 0.30D, HEAT_HALO_RADIUS + shimmer * 0.10D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.5F, 0xFF8A19, 0xFF5A00, 0xFFE08A,
                0.11F + shimmer * 0.05F, shimmer, 0.92F, ticks * 0.040F, 31.0F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawShaderStarRays(naturalShader, 0.24D, HEAT_HALO_RADIUS * 0.96D,
                16, RenderNaturalShaderHelper.MODE_SOLAR, 1.8F, 0xFFE08A, 0xFF8A19, 0xFFFFFF,
                0.10F + shimmer * 0.06F, shimmer, 1.02F, ticks * 0.050F, 43.0F, ticks * 0.018D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFlamePetals(float ticks, ShaderProgram naturalShader) {
        float open = wave(ticks * 0.034D);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.16F, 0.0F, 1.0F, 0.0F);
        drawPetalLayer(ticks, naturalShader, OUTER_PETAL_COUNT, OUTER_PETAL_LENGTH,
                0.30D, 0.10D + open * 0.20D, 0.38D, 0xB21F00, 0xFF6A00, 0.42F, 0.0D);

        GlStateManager.rotate(180.0F / INNER_PETAL_COUNT, 0.0F, 1.0F, 0.0F);
        drawPetalLayer(ticks, naturalShader, INNER_PETAL_COUNT, INNER_PETAL_LENGTH,
                0.24D, 0.20D + open * 0.24D, 0.30D, 0xFF5A00, 0xFFC247, 0.52F, 0.5D);
        GlStateManager.popMatrix();
    }

    private void drawPetalLayer(float ticks, ShaderProgram naturalShader,
                                int count, double length, double width, double lift, double curl,
                                int baseColor, int hotColor, float alpha, double phaseOffset) {
        useAdditiveBlend();
        for (int i = 0; i < count; i++) {
            double angle = TWO_PI * i / count + Math.sin(ticks * 0.019D + i * 1.7D) * 0.035D;
            double open = 0.78D + wave(ticks * 0.037D + i * 0.51D + phaseOffset) * 0.22D;
            double yaw = angle + ticks * (0.003D + (i % 3) * 0.0008D);
            double tipRadius = length * open;
            double tipLift = lift + Math.sin(ticks * 0.026D + i) * 0.055D;

            RenderNaturalShaderHelper.drawShaderSpiralRibbon(naturalShader, 0.16D, tipRadius,
                    yaw - 0.12D, 0.24D + curl * 0.22D, width,
                    RenderNaturalShaderHelper.MODE_SOLAR, 1.9F + (i % 3) * 0.12F,
                    baseColor, hotColor, 0xFFF2C2, alpha * 0.28F,
                    (float) open, 1.08F, ticks * 0.052F, 59.0F + i * 11.0F, 18);

            RenderNaturalShaderHelper.drawShaderSphericalArc(naturalShader, tipRadius,
                    yaw - 0.18D, 0.36D, 0.08D + tipLift * 0.22D,
                    0.10D + curl * 0.12D, ticks * 0.030D + i,
                    RenderNaturalShaderHelper.MODE_SOLAR, 2.3F + (i % 4) * 0.12F,
                    hotColor, 0xFFE08A, 0xFFFFFF, alpha * 0.46F,
                    (float) open, 1.18F, ticks * 0.060F, 73.0F + i * 17.0F, 24);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(yaw) * tipRadius * 0.72D, tipLift,
                    Math.sin(yaw) * tipRadius * 0.72D);
            GlStateManager.scale(0.70D, 1.28D + open * 0.32D, 0.70D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.12D + width * 0.10D,
                    RenderNaturalShaderHelper.MODE_SOLAR, 2.0F + (i % 4) * 0.15F,
                    baseColor, hotColor, 0xFFF2C2,
                    alpha * (0.28F + 0.20F * (float) open), (float) open, 1.18F,
                    ticks * 0.055F, 47.0F + i * 13.0F, 10);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawBloomCore(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.074D);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS * 2.10D + pulse * 0.055D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.7F, 0xFF4A00, 0xFFB31F, 0xFFFFFF,
                0.28F + pulse * 0.10F, pulse, 1.20F, ticks * 0.062F, 83.0F, 18);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS + pulse * 0.035D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.1F, 0xFFE08A, 0xFFFFFF, 0xFF7A00,
                0.72F + pulse * 0.16F, pulse, 1.52F, ticks * 0.090F, 109.0F, 18);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, CORE_RADIUS * 1.55D,
                RenderNaturalShaderHelper.MODE_SOLAR, 2.4F,
                0xFFF2C2, 0xFFFFFF, 0xFF7A00, 0.30F + pulse * 0.15F,
                pulse, 1.22F, ticks * 0.080F, 137.0F, 48);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRisingEmbers(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < EMBER_COUNT; i++) {
            double progress = fract(ticks * (0.010D + (i % 5) * 0.0012D) + i * 0.071D);
            double drift = Math.sin(progress * Math.PI);
            double angle = i * GOLDEN_ANGLE + ticks * (0.010D + (i % 4) * 0.0015D);
            double radius = 0.12D + drift * (0.38D + (i % 6) * 0.035D);
            double height = -0.08D + progress * (1.55D + (i % 4) * 0.16D);
            double size = 0.018D + (1.0D - progress) * 0.034D + drift * 0.016D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 3 == 0 ? 0xFFE08A : (i % 2 == 0 ? 0xFF7A00 : 0xD92800));

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius,
                    height + Math.sin(ticks * 0.045D + i) * 0.045D,
                    Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_SOLAR, 3.0F + (i % 4) * 0.16F,
                    color, 0xFF5A00, 0xFFF2C2,
                    0.12F + fade * 0.34F, fade, 1.12F,
                    ticks * 0.072F, 151.0F + i * 17.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
