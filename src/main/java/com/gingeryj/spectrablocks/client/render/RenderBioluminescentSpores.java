package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileBioluminescentSpores;
import net.minecraft.client.renderer.GlStateManager;

public class RenderBioluminescentSpores extends RenderCelestialEffectBase<TileBioluminescentSpores> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double CLOUD_RADIUS = 1.42D;
    private static final double CLOUD_HEIGHT = 1.86D;
    private static final int CLOUD_LAYERS = 5;
    private static final int SPORE_COUNT = 46;
    private static final int WISP_COUNT = 7;
    private static final int RING_SEGMENTS = 96;
    private static final float ASCENT_SPEED = 0.0028F;
    private static final float ORBIT_SPEED = 0.010F;

    private static final int[] SPORE_COLORS = new int[]{
            0xA8FF8A, 0x5FFFD2, 0x79E8FF, 0xC9A8FF, 0xF2FFF0
    };

    @Override
    protected void renderCelestialEffect(TileBioluminescentSpores te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawBreathingCloud(ticks, naturalShader, colorShader);
        drawSpiralWisps(ticks, colorShader);
        drawFloatingSpores(ticks, naturalShader);
        drawLivingCore(ticks, naturalShader);
    }

    private void drawBreathingCloud(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float breath = wave(ticks * 0.035D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.18D + breath * 0.035D, 0.0D);
        GlStateManager.scale(1.0D + breath * 0.035D, 0.66D + breath * 0.025D, 1.0D + breath * 0.035D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CLOUD_RADIUS + breath * 0.08D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.0F, 0x4DFF92, 0x58FFE3, 0xCBA7FF,
                0.115F + breath * 0.040F, breath, 0.74F, ticks * 0.018F, 9.0F, 26);
        GlStateManager.popMatrix();

        for (int i = 0; i < CLOUD_LAYERS; i++) {
            float localBreath = wave(ticks * (0.026D + i * 0.004D) + i * 0.84D);
            double angle = i * TWO_PI / CLOUD_LAYERS + ticks * (0.004D + i * 0.0007D);
            double layerRadius = 0.22D + i * 0.145D + localBreath * 0.045D;
            double y = -0.18D + i * 0.22D + Math.sin(ticks * 0.018D + i) * 0.045D;
            int primary = SPORE_COLORS[i % SPORE_COLORS.length];
            int secondary = SPORE_COLORS[(i + 1) % SPORE_COLORS.length];
            int accent = SPORE_COLORS[(i + 3) % SPORE_COLORS.length];

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * layerRadius, y, Math.sin(angle) * layerRadius);
            GlStateManager.scale(1.18D - i * 0.055D, 0.44D + i * 0.030D, 1.18D - i * 0.055D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.86D + localBreath * 0.07D,
                    RenderNaturalShaderHelper.MODE_AURORA, 0.5F + i * 0.35F,
                    primary, secondary, accent, 0.070F + localBreath * 0.035F,
                    localBreath, 0.62F, ticks * 0.015F, 23.0F + i * 17.0F, 22);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.48D, 0.0D);
        GlStateManager.scale(1.0D, 0.035D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 1.12D + breath * 0.08D,
                RenderNaturalShaderHelper.MODE_AURORA, 3.1F, 0x254B32, 0x6BFFB0, 0x8EFFE9,
                0.100F + breath * 0.035F, breath, 0.55F, ticks * 0.014F, 113.0F, 24);
        GlStateManager.popMatrix();

        if (colorShader != null) {
            GlStateManager.glLineWidth(1.2F);
            RenderNaturalShaderHelper.drawBasicCircle(colorShader, 0.96D + breath * 0.04D,
                    0x72FFC0, 0.12F + breath * 0.05F, RING_SEGMENTS);
            RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.30D + breath * 0.07D,
                    0x8EFFE9, 0.060F + breath * 0.035F, RING_SEGMENTS);
            RenderHelper.resetLineWidth();
        }
        useAlphaBlend();
    }

    private void drawSpiralWisps(float ticks, ShaderProgram colorShader) {
        if (colorShader == null) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.5F);
        for (int i = 0; i < WISP_COUNT; i++) {
            double phase = i * TWO_PI / WISP_COUNT + ticks * (0.009D + i * 0.0008D);
            double startRadius = 0.32D + (i % 3) * 0.055D;
            double endRadius = 1.04D + (i % 4) * 0.045D;
            double sweep = TWO_PI * (0.34D + (i % 3) * 0.055D);
            int color = SPORE_COLORS[(i + 1) % SPORE_COLORS.length];
            float alpha = 0.075F + 0.070F * wave(ticks * 0.041D + i * 0.73D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, -0.30D + i * 0.17D, 0.0D);
            GlStateManager.rotate((float) (ticks * 0.28D + i * 51.0D), 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicSpiralRibbon(colorShader, startRadius, endRadius,
                    phase, sweep, 0.018D + (i % 2) * 0.006D, color, alpha, 34);
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawFloatingSpores(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < SPORE_COUNT; i++) {
            double progress = fract(i * 0.071D + ticks * ASCENT_SPEED * (0.72D + (i % 5) * 0.12D));
            double heightEase = Math.sin(progress * Math.PI);
            double yaw = i * GOLDEN_ANGLE + ticks * (ORBIT_SPEED + (i % 6) * 0.0009D)
                    + Math.sin(ticks * 0.015D + i) * 0.12D;
            double radius = (0.20D + heightEase * 0.98D) * (0.78D + (i % 8) * 0.045D);
            double x = Math.cos(yaw) * radius + Math.sin(ticks * 0.017D + i * 1.7D) * 0.035D;
            double y = -0.62D + progress * CLOUD_HEIGHT + Math.sin(ticks * 0.024D + i) * 0.075D;
            double z = Math.sin(yaw) * radius + Math.cos(ticks * 0.014D + i * 1.3D) * 0.035D;
            double size = 0.020D + (i % 5) * 0.006D + heightEase * 0.018D;
            float pulse = wave(ticks * (0.052D + (i % 4) * 0.005D) + i * 0.61D);
            float alpha = (0.16F + pulse * 0.28F) * (0.42F + (float) heightEase * 0.58F);
            int primary = SPORE_COLORS[i % SPORE_COLORS.length];
            int secondary = SPORE_COLORS[(i + 2) % SPORE_COLORS.length];

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_AURORA, 4.0F + (i % 5) * 0.12F,
                    primary, secondary, 0xF4FFF7, alpha, pulse, 1.08F,
                    ticks * 0.032F, 47.0F + i * 11.0F, 7);

            if (i % 4 == 0) {
                RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size * 2.65D,
                        RenderNaturalShaderHelper.MODE_AURORA, 5.0F + (i % 3) * 0.2F,
                        secondary, primary, 0xFFFFFF, alpha * 0.25F, pulse, 0.82F,
                        ticks * 0.020F, 157.0F + i * 7.0F, 8);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawLivingCore(float ticks, ShaderProgram naturalShader) {
        float breath = wave(ticks * 0.048D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.16D + breath * 0.035D, 0.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.26D + breath * 0.040D,
                RenderNaturalShaderHelper.MODE_AURORA, 6.0F, 0xF4FFF7, 0x85FFB8, 0xBBA2FF,
                0.36F + breath * 0.16F, breath, 1.15F, ticks * 0.042F, 211.0F, 16);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.58D + breath * 0.060D,
                RenderNaturalShaderHelper.MODE_AURORA, 6.4F, 0x72FFC0, 0x64F7FF, 0xCBA7FF,
                0.105F + breath * 0.045F, breath, 0.78F, ticks * 0.024F, 229.0F, 18);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }
}
