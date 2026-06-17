package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileFrostCrystalMist;
import net.minecraft.client.renderer.GlStateManager;

public class RenderFrostCrystalMist extends RenderCelestialEffectBase<TileFrostCrystalMist> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int MIST_LOBES = 7;
    private static final int ICE_MOTE_COUNT = 34;
    private static final int SNOW_ARC_COUNT = 6;
    private static final int RING_SEGMENTS = 112;

    private static final int FROST_WHITE = 0xF4FFFF;
    private static final int PALE_BLUE = 0xAEEBFF;
    private static final int CRYSTAL_BLUE = 0x62CFFF;

    @Override
    protected void renderCelestialEffect(TileFrostCrystalMist te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawLowMist(ticks, naturalShader, colorShader);
        drawCrystalCore(ticks, naturalShader, colorShader);
        drawFloatingIceMotes(ticks, naturalShader);
        drawSnowflakeArcs(ticks, colorShader);
    }

    private void drawCrystalCore(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.046D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.08D + pulse * 0.025D, 0.0D);
        GlStateManager.rotate(ticks * 0.18F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.72D, 1.30D, 0.72D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.24D + pulse * 0.025D,
                RenderNaturalShaderHelper.MODE_STARDUST, 1.2F, FROST_WHITE, PALE_BLUE, CRYSTAL_BLUE,
                0.48F + pulse * 0.14F, pulse, 1.28F, ticks * 0.032F, 41.0F, 12, 18);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.08D, 0.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.58D + pulse * 0.055D,
                RenderNaturalShaderHelper.MODE_AURORA, 2.0F, 0xD8FAFF, CRYSTAL_BLUE, FROST_WHITE,
                0.090F + pulse * 0.035F, pulse, 0.78F, ticks * 0.018F, 67.0F, 20);

        if (colorShader != null) {
            GlStateManager.glLineWidth(1.2F);
            GlStateManager.rotate(ticks * 0.10F, 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicStarRays(colorShader, 0.18D, 0.48D,
                    6, FROST_WHITE, 0.22F + pulse * 0.08F, ticks * 0.010D);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicStarRays(colorShader, 0.16D, 0.42D,
                    6, PALE_BLUE, 0.16F + pulse * 0.07F, ticks * 0.009D + 0.35D);
            RenderHelper.resetLineWidth();
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawLowMist(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float breath = wave(ticks * 0.030D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.42D + breath * 0.025D, 0.0D);
        GlStateManager.scale(1.0D + breath * 0.025D, 0.105D, 1.0D + breath * 0.025D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 1.35D + breath * 0.09D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.0F, 0xBCEEFF, 0xF4FFFF, 0x79D8FF,
                0.145F + breath * 0.035F, breath, 0.62F, ticks * 0.012F, 13.0F, 24);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 1.88D + breath * 0.11D,
                RenderNaturalShaderHelper.MODE_AURORA, 0.4F, 0x7FCFFF, 0xE9FDFF, 0xFFFFFF,
                0.062F + breath * 0.025F, breath, 0.50F, ticks * 0.010F, 29.0F, 24);
        GlStateManager.popMatrix();

        for (int i = 0; i < MIST_LOBES; i++) {
            float localBreath = wave(ticks * (0.026D + i * 0.002D) + i * 0.71D);
            double angle = i * TWO_PI / MIST_LOBES + ticks * (0.0025D + i * 0.00025D);
            double radius = 0.30D + (i % 4) * 0.18D + localBreath * 0.035D;
            double y = -0.46D + Math.sin(ticks * 0.017D + i) * 0.035D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            GlStateManager.scale(1.08D + (i % 3) * 0.09D, 0.075D + (i % 2) * 0.018D,
                    0.82D + (i % 4) * 0.08D);
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.70D + localBreath * 0.050D,
                    RenderNaturalShaderHelper.MODE_AURORA, 0.8F + i * 0.25F,
                    0xDDFBFF, 0x8EDCFF, FROST_WHITE, 0.055F + localBreath * 0.024F,
                    localBreath, 0.48F, ticks * 0.011F, 73.0F + i * 17.0F, 16);
            GlStateManager.popMatrix();
        }

        if (colorShader != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, -0.43D, 0.0D);
            GlStateManager.glLineWidth(1.1F);
            RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.18D + breath * 0.04D,
                    PALE_BLUE, 0.105F + breath * 0.035F, RING_SEGMENTS);
            RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.64D + breath * 0.06D,
                    FROST_WHITE, 0.050F + breath * 0.020F, RING_SEGMENTS);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFloatingIceMotes(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < ICE_MOTE_COUNT; i++) {
            double progress = fract(i * 0.083D + ticks * (0.0016D + (i % 5) * 0.00012D));
            double heightEase = Math.sin(progress * Math.PI);
            double yaw = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 6) * 0.00045D);
            double swirl = Math.sin(ticks * 0.013D + i * 1.37D) * 0.055D;
            double radius = 0.28D + heightEase * 1.05D + (i % 7) * 0.035D;
            double x = Math.cos(yaw) * (radius + swirl);
            double y = -0.36D + progress * 1.42D + Math.sin(ticks * 0.020D + i) * 0.050D;
            double z = Math.sin(yaw) * (radius - swirl * 0.5D);
            double size = 0.018D + (i % 4) * 0.006D + heightEase * 0.012D;
            float twinkle = wave(ticks * (0.048D + (i % 4) * 0.004D) + i * 0.62D);
            float alpha = (0.15F + twinkle * 0.30F) * (0.38F + (float) heightEase * 0.62F);
            int primary = i % 5 == 0 ? FROST_WHITE : PALE_BLUE;
            int secondary = i % 3 == 0 ? CRYSTAL_BLUE : 0xD7FAFF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate((float) (ticks * 0.55D + i * 37.0D), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(28.0F + (i % 5) * 11.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.72D, 1.85D + (i % 3) * 0.28D, 0.72D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_STARDUST, 3.0F + (i % 6) * 0.12F,
                    primary, secondary, FROST_WHITE, alpha, twinkle, 1.12F,
                    ticks * 0.030F, 101.0F + i * 9.0F, 6, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSnowflakeArcs(float ticks, ShaderProgram colorShader) {
        if (colorShader == null) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.35F);
        for (int i = 0; i < SNOW_ARC_COUNT; i++) {
            float pulse = wave(ticks * (0.024D + i * 0.003D) + i * 0.79D);
            double radius = 0.72D + i * 0.105D + pulse * 0.035D;
            double startYaw = ticks * (0.0048D + i * 0.00055D) + i * TWO_PI / SNOW_ARC_COUNT;
            double sweep = TWO_PI * (0.22D + (i % 3) * 0.035D);
            double pitch = -0.16D + (i % 3) * 0.16D;
            int color = i % 2 == 0 ? FROST_WHITE : PALE_BLUE;
            float alpha = 0.12F + pulse * 0.085F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, 0.02D + (i - 2.5D) * 0.070D, 0.0D);
            GlStateManager.rotate(14.0F + i * 23.0F, 1.0F, 0.0F, 0.25F);
            GlStateManager.rotate((float) (ticks * (0.050D + i * 0.004D) + i * 37.0D),
                    0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicSphericalArc(colorShader, radius, startYaw, sweep,
                    pitch, 0.105D, ticks * 0.018D + i, color, alpha, 28);

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) Math.toDegrees(startYaw + sweep * 0.52D),
                    0.0F, 1.0F, 0.0F);
            GlStateManager.translate(radius * 0.82D, Math.sin(pitch) * radius, 0.0D);
            GlStateManager.scale(0.34D, 0.34D, 0.34D);
            RenderNaturalShaderHelper.drawBasicStarRays(colorShader, 0.035D, 0.155D,
                    6, color, alpha * 0.72F, ticks * 0.012D + i);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }
}
