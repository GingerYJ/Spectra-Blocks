package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderNovaBloom extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.34D;
    private static final double BLOOM_RADIUS = 1.10D;
    private static final double SHOCKWAVE_MAX_RADIUS = 2.28D;
    private static final int RING_SEGMENTS = 112;
    private static final int RAY_COUNT = 18;
    private static final int SPARK_COUNT = 42;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawShockwaves(ticks, naturalShader);
        drawStarCore(ticks, naturalShader);
        drawShortRays(ticks, naturalShader);
        drawEjectedStardust(ticks, naturalShader);
    }

    private void drawStarCore(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.086D);
        float flare = wave(ticks * 0.143D);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, BLOOM_RADIUS + pulse * 0.14D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.8F, 0xFF9D2E, 0xFFE8A8, 0xEAFBFF,
                0.14F + pulse * 0.055F, pulse, 1.10F, ticks * 0.042F, 23.0F, 26);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS * 1.82D + pulse * 0.05D,
                RenderNaturalShaderHelper.MODE_STARDUST, 0.2F, 0xFFF0B8, 0xFFFFFF, 0xBFEFFF,
                0.48F + pulse * 0.14F, pulse, 1.48F, ticks * 0.075F, 47.0F, 24);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS + flare * 0.036D,
                RenderNaturalShaderHelper.MODE_SOLAR, 0.0F, 0xFFFFFF, 0xFFF4C8, 0xDDFBFF,
                0.74F + flare * 0.16F, flare, 1.70F, ticks * 0.104F, 71.0F, 20);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.42F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, CORE_RADIUS * 2.08D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.8F, 0xFFF4C8, 0xFFFFFF, 0xBFEFFF,
                0.24F + flare * 0.12F, flare, 1.28F, ticks * 0.085F, 89.0F, 64);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawShockwaves(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < 2; i++) {
            double progress = fract(ticks * 0.0065D + i * 0.50D);
            double eased = 1.0D - (1.0D - progress) * (1.0D - progress);
            double radius = lerp(BLOOM_RADIUS * 0.72D, SHOCKWAVE_MAX_RADIUS, eased);
            float fade = (float) Math.sin(Math.PI * progress);
            float alpha = fade * (0.15F - i * 0.030F);
            int warmColor = i == 0 ? 0xFFB24F : 0xFFE7A6;
            int coolColor = i == 0 ? 0xCFF9FF : 0x85DFFF;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(12.0F + i * 54.0F, 1.0F, 0.0F, 0.24F);
            GlStateManager.rotate(ticks * (0.10F + i * 0.035F), 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawShaderRing(naturalShader, radius * 0.94D, radius,
                    RenderNaturalShaderHelper.MODE_STARDUST, 1.4F + i * 0.25F,
                    warmColor, coolColor, 0xFFFFFF, alpha, fade, 1.04F,
                    ticks * 0.050F, 121.0F + i * 17.0F, RING_SEGMENTS);
            RenderNaturalShaderHelper.drawShaderCircle(naturalShader, radius * 1.016D,
                    RenderNaturalShaderHelper.MODE_STARDUST, 1.7F + i * 0.25F,
                    coolColor, warmColor, 0xFFFFFF, alpha * 0.86F, fade, 1.06F,
                    ticks * 0.058F, 137.0F + i * 17.0F, RING_SEGMENTS);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0D, 0.08D, 1.0D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, radius * 0.72D,
                    RenderNaturalShaderHelper.MODE_STARDUST, 1.2F + i * 0.35F,
                    coolColor, warmColor, 0xFFFFFF, alpha * 0.34F,
                    fade, 0.86F, ticks * 0.038F, 113.0F + i * 37.0F, 22);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawShortRays(float ticks, ShaderProgram naturalShader) {
        float shimmer = wave(ticks * 0.067D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(74.0F, 1.0F, 0.0F, 0.18F);
        GlStateManager.rotate(ticks * 0.22F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderStarRays(naturalShader, CORE_RADIUS * 1.32D,
                BLOOM_RADIUS * (1.20D + shimmer * 0.22D), RAY_COUNT,
                RenderNaturalShaderHelper.MODE_STARDUST, 2.1F, 0xFFF6CE, 0xBFEFFF, 0xFFFFFF,
                0.13F + shimmer * 0.08F, shimmer, 1.24F, ticks * 0.064F, 157.0F, ticks * 0.019D);
        GlStateManager.popMatrix();

        for (int i = 0; i < 8; i++) {
            double blink = Math.max(0.0D, Math.sin(ticks * (0.046D + (i % 3) * 0.007D) + i * 1.91D));
            blink = blink * blink * blink;
            if (blink <= 0.18D) {
                continue;
            }

            double yaw = i * GOLDEN_ANGLE + ticks * 0.012D;
            double pitch = -0.48D + (i % 5) * 0.24D;
            double horizontal = Math.cos(pitch);
            double inner = CORE_RADIUS * (1.55D + blink * 0.20D);
            double outer = BLOOM_RADIUS * (0.98D + blink * 0.46D);
            int color = i % 2 == 0 ? 0xFFFFFF : 0xBFEFFF;

            RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_STARDUST,
                    2.4F + i * 0.08F,
                    Math.cos(yaw) * horizontal * inner, Math.sin(pitch) * inner, Math.sin(yaw) * horizontal * inner,
                    Math.cos(yaw) * horizontal * outer, Math.sin(pitch) * outer, Math.sin(yaw) * horizontal * outer,
                    color, 0xBFEFFF, 0xFFFFFF, (float) blink * 0.24F, (float) blink,
                    1.34F, ticks * 0.075F, 179.0F + i * 13.0F, 0.020D + blink * 0.018D);
        }
        useAlphaBlend();
    }

    private void drawEjectedStardust(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(ticks * (0.010D + (i % 5) * 0.0012D) + i * 0.073D);
            double fadeCurve = Math.sin(Math.PI * progress);
            double yaw = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 4) * 0.001D);
            double pitch = -0.74D + (i % 17) * (1.48D / 16.0D);
            double horizontal = Math.cos(pitch);
            double radius = lerp(CORE_RADIUS * 1.15D, SHOCKWAVE_MAX_RADIUS * 0.94D, progress);
            double ripple = Math.sin(ticks * 0.055D + i * 1.37D) * 0.045D * fadeCurve;
            double size = 0.018D + (1.0D - progress) * 0.038D + fadeCurve * 0.010D;
            float alpha = (float) (0.10D + fadeCurve * 0.28D) * (float) (1.0D - progress * 0.42D);
            int color = i % 6 == 0 ? 0xFFFFFF : (i % 3 == 0 ? 0xFFE3A1 : 0xAEEFFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(yaw) * horizontal * radius,
                    Math.sin(pitch) * radius * 0.78D + ripple,
                    Math.sin(yaw) * horizontal * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_STARDUST, 2.0F + (i % 5) * 0.12F,
                    color, 0x8DDDFF, 0xFFF4C8,
                    alpha, (float) fadeCurve, 1.16F,
                    ticks * 0.060F, 181.0F + i * 9.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
