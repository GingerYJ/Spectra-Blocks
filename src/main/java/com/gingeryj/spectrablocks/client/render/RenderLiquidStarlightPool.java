package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderLiquidStarlightPool extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double SURFACE_Y = -0.36D;
    private static final double POOL_RADIUS = 1.34D;
    private static final int SURFACE_LAYERS = 3;
    private static final int STAR_COUNT = 34;
    private static final int SPARK_COUNT = 9;
    private static final int RIPPLE_COUNT = 6;
    private static final int RING_SEGMENTS = 112;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawLiquidSurface(ticks, naturalShader, colorShader);
        drawDriftingStars(ticks, naturalShader);
        drawSoftRipples(ticks, colorShader);
        drawRisingSparks(ticks, naturalShader, colorShader);
    }

    private void drawLiquidSurface(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.030D);

        useAlphaBlend();
        for (int i = 0; i < SURFACE_LAYERS; i++) {
            float layerPulse = wave(ticks * (0.020D + i * 0.006D) + i * 0.73D);
            double radius = POOL_RADIUS - i * 0.20D + layerPulse * 0.035D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, SURFACE_Y + i * 0.010D, 0.0D);
            GlStateManager.rotate(ticks * (0.018F + i * 0.010F), 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(1.0D, 0.032D + i * 0.010D, 1.0D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, radius,
                    RenderNaturalShaderHelper.MODE_STARDUST, i * 0.42F,
                    0xF8FFFF, 0x9BE8FF, 0xFFE8A8,
                    0.11F + layerPulse * 0.030F, layerPulse, 0.72F + i * 0.10F,
                    ticks * (0.017F + i * 0.004F), 23.0F + i * 19.0F, 24);
            GlStateManager.popMatrix();
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, SURFACE_Y + 0.018D, 0.0D);
        GlStateManager.glLineWidth(1.45F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, POOL_RADIUS * 1.02D,
                0xDDF8FF, 0.22F + pulse * 0.07F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawBasicFlatRing(colorShader, POOL_RADIUS * 0.86D, POOL_RADIUS * 1.05D,
                0xB8EEFF, 0.045F + pulse * 0.018F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawBasicStarRays(colorShader, 0.16D, POOL_RADIUS * 0.78D, 14,
                0xFFF3C4, 0.045F + pulse * 0.030F, ticks * 0.005D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawDriftingStars(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < STAR_COUNT; i++) {
            double band = fract(i * 0.38196601125D);
            double radius = POOL_RADIUS * (0.12D + Math.pow(band, 0.58D) * 0.78D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 5) * 0.0012D);
            double driftX = Math.sin(ticks * 0.013D + i * 1.41D) * 0.040D;
            double driftZ = Math.cos(ticks * 0.011D + i * 1.17D) * 0.040D;
            double y = SURFACE_Y + 0.045D + Math.sin(ticks * 0.025D + i) * 0.018D;
            double size = 0.016D + (i % 4) * 0.004D;
            float twinkle = wave(ticks * (0.050D + (i % 6) * 0.006D) + i * 0.83D);
            int color = i % 5 == 0 ? 0xFFE8A8 : (i % 2 == 0 ? 0xF8FFFF : 0x9BE8FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius + driftX, y, Math.sin(angle) * radius + driftZ);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_STARDUST, 1.2F + (i % 5) * 0.13F,
                    color, 0xBCEEFF, 0xFFFFFF, 0.22F + twinkle * 0.28F,
                    twinkle, 1.18F, ticks * 0.040F, 71.0F + i * 5.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSoftRipples(float ticks, ShaderProgram colorShader) {
        if (colorShader == null) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, SURFACE_Y + 0.028D, 0.0D);
        GlStateManager.glLineWidth(1.15F);
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            double progress = fract(ticks * (0.009D + i * 0.0008D) + i * 0.173D);
            double radius = lerp(0.28D, POOL_RADIUS * 1.14D, progress);
            float fade = (float) ((1.0D - progress) * Math.sin(Math.PI * progress));
            int color = i % 3 == 0 ? 0xFFF0B8 : (i % 2 == 0 ? 0xEFFFFF : 0xA9E8FF);

            RenderNaturalShaderHelper.drawBasicCircle(colorShader, radius, color, 0.16F * fade, 88);
            if ((i & 1) == 0) {
                RenderNaturalShaderHelper.drawBasicFlatRing(colorShader, radius * 0.96D, radius * 1.03D,
                        color, 0.028F * fade, 88);
            }
        }
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRisingSparks(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.0F);
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(ticks * (0.006D + (i % 3) * 0.001D) + i * 0.211D);
            double rise = Math.sin(Math.PI * progress);
            double angle = i * GOLDEN_ANGLE + ticks * 0.010D;
            double radius = POOL_RADIUS * (0.18D + 0.58D * fract(i * 0.274D));
            double x = Math.cos(angle) * radius + Math.sin(progress * Math.PI * 2.0D + i) * 0.045D;
            double z = Math.sin(angle) * radius + Math.cos(progress * Math.PI * 2.0D + i) * 0.045D;
            double y = SURFACE_Y + 0.06D + progress * 0.68D;
            float alpha = (float) (rise * 0.34D);
            int color = i % 3 == 0 ? 0xFFF0B8 : 0xEFFFFF;

            RenderNaturalShaderHelper.drawBasicLine(colorShader, x, y - 0.10D * rise, z,
                    x, y + 0.045D, z, color, alpha * 0.46F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.020D + rise * 0.020D,
                    RenderNaturalShaderHelper.MODE_STARDUST, 2.2F + (i % 4) * 0.18F,
                    color, 0x9BE8FF, 0xFFFFFF, alpha,
                    (float) progress, 1.24F, ticks * 0.046F, 137.0F + i * 11.0F, 7);
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }
}
