package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TilePrismaticRainfall;
import net.minecraft.client.renderer.GlStateManager;

public class RenderPrismaticRainfall extends RenderCelestialEffectBase<TilePrismaticRainfall> {

    private static final double TOP_Y = 1.62D;
    private static final double BOTTOM_Y = -0.46D;
    private static final double RAIN_HEIGHT = TOP_Y - BOTTOM_Y;
    private static final double RAIN_RADIUS = 1.36D;
    private static final double HALO_RADIUS = 1.48D;
    private static final int DROP_COUNT = 42;
    private static final int RIPPLE_COUNT = 7;
    private static final int HALO_SEGMENTS = 112;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    private static final int[] PRISM_COLORS = new int[]{
            0xFF9FB7, 0xFFD98C, 0xFFF7A6, 0xA9F5C8, 0x8FE9FF, 0xA9B6FF, 0xE3ADFF
    };

    @Override
    protected void renderCelestialEffect(TilePrismaticRainfall te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawUpperHalo(ticks, naturalShader);
        drawFallingDrops(ticks, naturalShader);
        drawBottomRipples(ticks, naturalShader);
    }

    private void drawUpperHalo(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.038D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, TOP_Y, 0.0D);
        GlStateManager.scale(1.0D, 0.045D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, HALO_RADIUS + pulse * 0.06D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.0F, 0xF8FFFF, 0x9FEAFF, 0xFFD7F0,
                0.18F + pulse * 0.05F, pulse, 0.82F, ticks * 0.022F, 11.0F, 28);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, HALO_RADIUS * 0.64D + pulse * 0.04D,
                RenderNaturalShaderHelper.MODE_STARDUST, 0.35F, 0xFFFFFF, 0xB8FFF1, 0xF5C8FF,
                0.15F + pulse * 0.06F, pulse, 0.95F, ticks * 0.030F, 29.0F, 22);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, TOP_Y + 0.01D, 0.0D);
        GlStateManager.rotate(ticks * 0.055F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, HALO_RADIUS * 0.92D,
                RenderNaturalShaderHelper.MODE_AURORA, 3.1F,
                0xDDFBFF, 0x9FEAFF, 0xFFFFFF,
                0.24F + pulse * 0.06F, pulse, 0.92F, ticks * 0.026F, 101.0F, HALO_SEGMENTS);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, HALO_RADIUS * 0.58D,
                RenderNaturalShaderHelper.MODE_AURORA, 3.4F,
                0xFFD7F0, 0xB8FFF1, 0xFFFFFF,
                0.13F + pulse * 0.05F, pulse, 0.86F, ticks * 0.032F, 113.0F, HALO_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFallingDrops(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < DROP_COUNT; i++) {
            double progress = fract(ticks * (0.0068D + (i % 5) * 0.00055D) + i * 0.071D);
            double fallEase = progress * progress * (3.0D - 2.0D * progress);
            double angle = i * GOLDEN_ANGLE + Math.sin(ticks * 0.006D + i) * 0.10D;
            double radius = RAIN_RADIUS * (0.20D + 0.78D * fract(i * 0.38196601125D));
            double drift = Math.sin(ticks * 0.018D + i * 1.37D) * 0.040D;
            double x = Math.cos(angle) * radius + drift;
            double z = Math.sin(angle) * radius + Math.cos(ticks * 0.014D + i) * 0.040D;
            double y = TOP_Y - fallEase * RAIN_HEIGHT;
            double streak = 0.13D + (i % 4) * 0.035D;
            double headSize = 0.018D + (i % 3) * 0.006D;
            float fade = (float) (Math.sin(Math.PI * progress) * (0.36D + (i % 4) * 0.035D));
            int color = PRISM_COLORS[(i + (int) (ticks * 0.014F)) % PRISM_COLORS.length];
            int secondary = PRISM_COLORS[(i + 2) % PRISM_COLORS.length];

            RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_AURORA,
                    4.0F + (i % 6) * 0.08F, x, y + streak * 0.42D, z,
                    x - drift * 0.72D, y - streak, z,
                    color, secondary, 0xFFFFFF, fade * 0.62F, (float) progress,
                    1.12F, ticks * 0.035F, 151.0F + i * 7.0F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, headSize,
                    RenderNaturalShaderHelper.MODE_AURORA, 1.4F + (i % 6) * 0.12F,
                    color, secondary, 0xFFFFFF, fade, (float) progress, 1.10F,
                    ticks * 0.035F, 53.0F + i * 7.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawBottomRipples(float ticks, ShaderProgram naturalShader) {
        float basinPulse = wave(ticks * 0.034D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, BOTTOM_Y - 0.02D, 0.0D);
        GlStateManager.scale(1.0D, 0.035D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.78D + basinPulse * 0.05D,
                RenderNaturalShaderHelper.MODE_STARDUST, 2.6F, 0xCFFFFF, 0xB5FFE2, 0xFFD0F4,
                0.12F + basinPulse * 0.04F, basinPulse, 0.70F, ticks * 0.020F, 83.0F, 22);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, BOTTOM_Y, 0.0D);
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            double progress = fract(ticks * 0.014D + i * 0.147D);
            double radius = lerp(0.20D, 1.58D, progress);
            float alpha = (float) ((1.0D - progress) * Math.sin(Math.PI * progress));
            int color = PRISM_COLORS[(i * 2 + (int) (ticks * 0.020F)) % PRISM_COLORS.length];

            RenderNaturalShaderHelper.drawShaderCircle(naturalShader, radius,
                    RenderNaturalShaderHelper.MODE_AURORA, 5.0F + i * 0.10F,
                    color, 0xB5FFE2, 0xFFFFFF,
                    0.24F * alpha, alpha, 0.82F, ticks * 0.024F, 211.0F + i, 80);
            if ((i & 1) == 0) {
                RenderNaturalShaderHelper.drawShaderRing(naturalShader, radius * 0.94D, radius * 1.02D,
                        RenderNaturalShaderHelper.MODE_AURORA, 5.8F + i * 0.09F,
                        color, 0xFFD0F4, 0xFFFFFF,
                        0.055F * alpha, alpha, 0.76F, ticks * 0.020F, 233.0F + i, 80);
            }
        }
        RenderNaturalShaderHelper.drawShaderStarRays(naturalShader, 0.18D, 1.12D, 12,
                RenderNaturalShaderHelper.MODE_STARDUST, 6.3F,
                0xFFFFFF, 0xB5FFE2, 0xFFD0F4,
                0.055F + basinPulse * 0.035F, basinPulse, 0.95F,
                ticks * 0.028F, 257.0F, ticks * 0.006D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }
}
