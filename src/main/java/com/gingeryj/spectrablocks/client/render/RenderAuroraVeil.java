package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileAuroraVeil;
import net.minecraft.client.renderer.GlStateManager;

public class RenderAuroraVeil extends RenderCelestialEffectBase<TileAuroraVeil> {

    private static final double VEIL_WIDTH = 3.70D;
    private static final double VEIL_HEIGHT = 3.55D;
    private static final double VEIL_BASE_Y = -0.42D;
    private static final double VEIL_DEPTH = 0.26D;
    private static final int VEIL_LAYERS = 6;
    private static final int VEIL_SEGMENTS = 44;
    private static final int RAY_COUNT = 15;
    private static final int MOTE_COUNT = 28;
    private static final float DRIFT_SPEED = 0.010F;

    private static final int[] VEIL_COLORS = new int[]{
            0x45FFD6, 0x69F5FF, 0x8C76FF, 0xFF8FD8, 0xB7FF72, 0xFFFFFF
    };

    @Override
    protected void renderCelestialEffect(TileAuroraVeil te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawBaseGlow(ticks, naturalShader);
        drawVeilLayers(ticks, naturalShader);
        drawVerticalRays(ticks, naturalShader);
        drawMotes(ticks, naturalShader);
    }

    private void drawBaseGlow(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0D, 0.05D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 1.92D + pulse * 0.12D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.0F, 0x56FFE0, 0x946DFF, 0xFFFFFF,
                0.13F + pulse * 0.04F, pulse, 0.80F, ticks * 0.025F, 5.0F, 30);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 2.70D + pulse * 0.10D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.2F, 0x946DFF, 0x56FFE0, 0xB7FF72,
                0.060F + pulse * 0.025F, pulse, 0.62F, ticks * 0.018F, 11.0F, 30);
        GlStateManager.popMatrix();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.38D + pulse * 0.05D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.4F, 0xFFFFFF, 0x69F5FF, 0xFF8FD8,
                0.23F + pulse * 0.06F, pulse, 1.05F, ticks * 0.040F, 17.0F, 18);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, 1.92D + pulse * 0.12D,
                RenderNaturalShaderHelper.MODE_AURORA, 2.0F,
                0x56FFE0, 0x946DFF, 0xFFFFFF, 0.15F + pulse * 0.04F,
                pulse, 0.92F, ticks * 0.030F, 31.0F, 128);
        useAlphaBlend();
    }

    private void drawVeilLayers(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < VEIL_LAYERS; i++) {
            float pulse = wave(ticks * (0.020D + i * 0.003D) + i * 0.77D);
            int color = VEIL_COLORS[i % VEIL_COLORS.length];
            int secondary = VEIL_COLORS[(i + 1) % VEIL_COLORS.length];
            int accent = VEIL_COLORS[(i + 3) % VEIL_COLORS.length];
            double angle = i * 31.0D + Math.sin(ticks * 0.004D + i) * 4.5D;
            double zOffset = (i - (VEIL_LAYERS - 1) * 0.5D) * VEIL_DEPTH;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) angle, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, 0.0D, zOffset);
            GlStateManager.scale(1.0D - i * 0.025D, 1.0D + pulse * 0.035D, 1.0D);
            RenderNaturalShaderHelper.drawAuroraSheet(naturalShader, VEIL_WIDTH, VEIL_HEIGHT,
                    VEIL_BASE_Y, VEIL_SEGMENTS, i, color, secondary, accent,
                    0.22F + pulse * 0.075F, pulse, ticks * 0.026F, i * 19.0F);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawVerticalRays(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < RAY_COUNT; i++) {
            double progress = (double) i / (RAY_COUNT - 1);
            double x = (progress - 0.5D) * (VEIL_WIDTH * 0.92D);
            double phase = ticks * 0.023D + i * 0.61D;
            double z = Math.sin(phase) * 0.42D;
            double height = 2.10D + wave(phase) * 1.30D;
            int color = VEIL_COLORS[i % VEIL_COLORS.length];
            float alpha = 0.065F + 0.055F * wave(ticks * 0.031D + i);

            RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_AURORA,
                    2.6F + i * 0.05F,
                    x, VEIL_BASE_Y + 0.05D, z * 0.20D,
                    x + Math.sin(phase * 0.9D) * 0.14D, VEIL_BASE_Y + height, z,
                    color, VEIL_COLORS[(i + 1) % VEIL_COLORS.length], 0xFFFFFF,
                    alpha, wave(phase), 1.05F, ticks * 0.032F, 53.0F + i * 7.0F, 0.022D);
        }
        useAlphaBlend();
    }

    private void drawMotes(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = (i + 0.5D) / MOTE_COUNT;
            double yaw = i * 2.399963229728653D + ticks * DRIFT_SPEED;
            double heightWave = fract(progress + ticks * 0.0023D + (i % 7) * 0.013D);
            double radius = 0.35D + (i % 11) * 0.145D;
            double x = Math.cos(yaw) * radius;
            double y = VEIL_BASE_Y + heightWave * (VEIL_HEIGHT + 0.20D);
            double z = Math.sin(yaw) * radius + Math.sin(ticks * 0.020D + i) * 0.12D;
            double size = 0.026D + (i % 4) * 0.008D;
            float alpha = 0.16F + 0.20F * wave(ticks * 0.037D + i * 0.9D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_AURORA, 2.4F + (i % 4) * 0.15F,
                    VEIL_COLORS[(i + 2) % VEIL_COLORS.length],
                    VEIL_COLORS[(i + 4) % VEIL_COLORS.length], 0xFFFFFF,
                    alpha, (float) heightWave, 1.08F, ticks * 0.037F, i * 23.0F, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
