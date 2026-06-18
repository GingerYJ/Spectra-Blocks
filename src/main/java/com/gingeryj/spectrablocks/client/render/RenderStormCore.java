package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileStormCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderStormCore extends RenderCelestialEffectBase<TileStormCore> {

    private static final double EYE_RADIUS = 0.48D;
    private static final double CLOUD_RADIUS = 2.92D;
    private static final double OUTER_WIND_RADIUS = 3.55D;
    private static final int CLOUD_BLOB_COUNT = 30;
    private static final int WIND_BAND_COUNT = 6;
    private static final int ARC_COUNT = 8;
    private static final int RING_SEGMENTS = 150;
    private static final float CLOUD_ROTATION_SPEED = 0.026F;
    private static final float WIND_ROTATION_SPEED = 0.055F;
    private static final float LIGHTNING_CYCLE_SPEED = 0.020F;
    private static final int[] CLOUD_COLORS = new int[]{
            0x6F7B86, 0x8FA1AF, 0xB7C8D6, 0x4E5B67
    };

    @Override
    protected void renderCelestialEffect(TileStormCore te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawWindEye(ticks, naturalShader);
        drawCloudMass(ticks, naturalShader);
        drawWindBands(ticks, naturalShader);
        drawLightning(ticks, naturalShader);
    }

    private void drawWindEye(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.052D);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, EYE_RADIUS + pulse * 0.035D,
                RenderNaturalShaderHelper.MODE_STORM, 0.0F, 0xF5FBFF, 0x89D6FF, 0xFFFFFF,
                0.46F + pulse * 0.12F, pulse, 1.24F, ticks * 0.058F, 7.0F, 22);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, EYE_RADIUS * 1.78D + pulse * 0.10D,
                RenderNaturalShaderHelper.MODE_STORM, 0.2F, 0x89D6FF, 0x4E5B67, 0xE8F6FF,
                0.20F + pulse * 0.06F, pulse, 0.92F, ticks * 0.044F, 19.0F, 24);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(76.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.54F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderRing(naturalShader, 0.60D, 1.18D + pulse * 0.05D,
                RenderNaturalShaderHelper.MODE_STORM, 2.0F,
                0xD8F4FF, 0x89D6FF, 0xFFFFFF, 0.070F + pulse * 0.035F,
                pulse, 1.04F, ticks * 0.050F, 53.0F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, 1.20D + pulse * 0.04D,
                RenderNaturalShaderHelper.MODE_STORM, 2.4F,
                0xFFFFFF, 0xD8F4FF, 0x89D6FF, 0.14F + pulse * 0.05F,
                pulse, 1.22F, ticks * 0.056F, 67.0F, RING_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCloudMass(float ticks, ShaderProgram naturalShader) {
        useAlphaBlend();
        for (int i = 0; i < CLOUD_BLOB_COUNT; i++) {
            double progress = (i + 0.5D) / CLOUD_BLOB_COUNT;
            double angle = i * 2.399963229728653D + ticks * (CLOUD_ROTATION_SPEED + (i % 4) * 0.002F);
            double yNorm = -0.70D + (i % 15) * (1.40D / 14.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = 1.00D + Math.pow(progress, 0.45D) * (CLOUD_RADIUS - 1.00D);
            double curl = Math.sin(ticks * 0.018D + i * 0.77D) * 0.16D;
            double x = Math.cos(angle) * horizontal * (radius + curl);
            double y = yNorm * radius * 0.52D + Math.cos(angle * 1.6D + ticks * 0.013D) * 0.10D;
            double z = Math.sin(angle) * horizontal * (radius - curl * 0.40D);
            double scaleX = 0.42D + (i % 5) * 0.075D;
            double scaleY = 0.20D + (i % 4) * 0.045D;
            double scaleZ = 0.34D + (i % 6) * 0.060D;
            float alpha = 0.115F + 0.055F * wave(ticks * 0.021D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate((float) (angle * 57.29577951308232D), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(8.0F + (i % 7) * 4.0F, 1.0F, 0.0F, 0.18F);
            GlStateManager.scale(scaleX, scaleY, scaleZ);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 1.0D,
                    RenderNaturalShaderHelper.MODE_STORM, 1.0F + (i % 4) * 0.20F,
                    CLOUD_COLORS[i % CLOUD_COLORS.length], 0xB7C8D6, 0xFFFFFF,
                    alpha, wave(ticks * 0.021D + i), 0.82F, ticks * 0.030F, i * 31.0F, 12);
            GlStateManager.popMatrix();
        }
    }

    private void drawWindBands(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < WIND_BAND_COUNT; i++) {
            double radius = 1.30D + i * 0.36D;
            double startAngle = ticks * (WIND_ROTATION_SPEED + i * 0.007F) + i * 0.83D;
            double sweep = Math.PI * (1.20D + (i % 3) * 0.20D);
            float pulse = wave(ticks * 0.033D + i);
            int color = i % 2 == 0 ? 0xD3E5EE : 0x8FC6DF;
            float alpha = 0.090F + pulse * 0.045F;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(18.0F + i * 9.0F, 1.0F, 0.0F, 0.20F);
            GlStateManager.rotate(i * 24.0F, 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawShaderSpiralRibbon(naturalShader, radius,
                    OUTER_WIND_RADIUS - i * 0.10D, startAngle, sweep,
                    0.080D + pulse * 0.020D, RenderNaturalShaderHelper.MODE_STORM, 3.0F + i * 0.16F,
                    color, 0xE8F6FF, 0x91DFFF, alpha, pulse, 0.98F,
                    ticks * 0.046F, 101.0F + i * 19.0F, 58);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(78.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * -0.17F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, OUTER_WIND_RADIUS * 0.68D,
                RenderNaturalShaderHelper.MODE_STORM, 4.2F,
                0xBFD7E3, 0x8FC6DF, 0xFFFFFF, 0.12F,
                wave(ticks * 0.030D), 1.04F, ticks * 0.038F, 229.0F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, OUTER_WIND_RADIUS * 0.86D,
                RenderNaturalShaderHelper.MODE_STORM, 4.6F,
                0xE8F6FF, 0xBFD7E3, 0x91DFFF, 0.075F,
                wave(ticks * 0.026D + 0.7D), 0.92F, ticks * 0.034F, 251.0F, RING_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawLightning(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < ARC_COUNT; i++) {
            double flashPhase = fract(ticks * LIGHTNING_CYCLE_SPEED + i * 0.173D);
            float flash = (float) Math.max(0.0D, Math.sin(Math.PI * flashPhase));
            double radius = 1.62D + (i % 4) * 0.38D;
            double angle = i * 0.7853981633974483D + ticks * (0.016D + (i % 3) * 0.004D);
            double y = -0.92D + (i % 5) * 0.46D;
            double lift = 0.34D + (i % 3) * 0.16D;
            double length = 0.45D + (i % 4) * 0.13D;

            drawShaderJaggedArc(naturalShader, radius, angle, length, y, lift,
                    0.090D, 7, 0x91DFFF, 0xEAFBFF, flash * 0.16F,
                    flash, ticks, 311 + i * 37, 5.0F + i * 0.12F, 0.026D);
            drawShaderJaggedArc(naturalShader, radius, angle, length, y, lift,
                    0.065D, 7, i % 2 == 0 ? 0xFFFFFF : 0xB7F0FF, 0x91DFFF,
                    flash * 0.48F, flash, ticks, 811 + i * 41, 6.0F + i * 0.12F, 0.012D);

            if ((i & 1) == 0) {
                drawBranch(ticks, i, angle + length * 0.52D, radius, y + lift * 0.40D,
                        flash, naturalShader);
            }
        }
        useAlphaBlend();
    }

    private void drawBranch(float ticks, int index, double angle, double radius, double y,
                            float flash, ShaderProgram naturalShader) {
        double startX = Math.cos(angle) * radius;
        double startZ = Math.sin(angle) * radius;
        double endAngle = angle + Math.sin(ticks * 0.08D + index) * 0.34D;
        double endRadius = radius + 0.42D + (index % 3) * 0.12D;
        double endX = Math.cos(endAngle) * endRadius;
        double endY = y + Math.sin(index * 1.7D) * 0.24D;
        double endZ = Math.sin(endAngle) * endRadius;

        RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_STORM,
                7.0F + index * 0.08F, startX, y, startZ, endX, endY, endZ,
                0xEAFBFF, 0x91DFFF, 0xFFFFFF, flash * 0.24F, flash, 1.28F,
                ticks * 0.060F, 401.0F + index * 17.0F, 0.010D);
    }

    private void drawShaderJaggedArc(ShaderProgram shader, double radius, double startYaw,
                                     double sweepYaw, double y, double lift, double jitter,
                                     int segments, int primaryColor, int secondaryColor,
                                     float alpha, float flash, float ticks, int seed,
                                     float layer, double halfWidth) {
        if (alpha <= 0.005F || radius <= 0.0D || segments < 2) {
            return;
        }

        double previousX = 0.0D;
        double previousY = 0.0D;
        double previousZ = 0.0D;
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double fade = Math.sin(Math.PI * progress);
            double angle = startYaw + sweepYaw * progress
                    + deterministicJitter(seed, i, ticks) * jitter * 0.80D;
            double localRadius = radius + deterministicJitter(seed + 71, i, ticks) * jitter;
            double localY = y + lift * fade
                    + deterministicJitter(seed + 137, i, ticks) * jitter * 0.85D;
            double x = Math.cos(angle) * localRadius;
            double z = Math.sin(angle) * localRadius;

            if (i > 0) {
                double fadeProgress = (i - 0.5D) / segments;
                float segmentAlpha = alpha * (0.18F + 0.82F * (float) Math.sin(Math.PI * fadeProgress));
                RenderNaturalShaderHelper.drawShaderLine(shader, RenderNaturalShaderHelper.MODE_STORM,
                        layer, previousX, previousY, previousZ, x, localY, z,
                        primaryColor, secondaryColor, 0xFFFFFF, segmentAlpha, flash, 1.45F,
                        ticks * 0.070F, seed + i * 0.73F, halfWidth);
            }

            previousX = x;
            previousY = localY;
            previousZ = z;
        }
    }

    private double deterministicJitter(int seed, int step, float ticks) {
        double frame = Math.floor(ticks * 0.35D);
        double value = Math.sin(seed * 12.9898D + step * 78.233D + frame * 0.37D) * 43758.5453D;
        return value - Math.floor(value) - 0.5D;
    }
}
