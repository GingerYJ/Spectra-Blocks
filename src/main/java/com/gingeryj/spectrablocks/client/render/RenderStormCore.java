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
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawWindEye(ticks, naturalShader, colorShader);
        drawCloudMass(ticks, naturalShader);
        drawWindBands(ticks, colorShader);
        drawLightning(ticks, colorShader);
    }

    private void drawWindEye(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
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
        RenderNaturalShaderHelper.drawBasicFlatRing(colorShader, 0.60D, 1.18D + pulse * 0.05D,
                0xD8F4FF, 0.070F + pulse * 0.035F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.6F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.20D + pulse * 0.04D,
                0xFFFFFF, 0.14F + pulse * 0.05F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
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

    private void drawWindBands(float ticks, ShaderProgram colorShader) {
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
            RenderNaturalShaderHelper.drawBasicSpiralRibbon(colorShader, radius,
                    OUTER_WIND_RADIUS - i * 0.10D, startAngle, sweep,
                    0.080D + pulse * 0.020D, color, alpha, 58);
            GlStateManager.popMatrix();
        }

        GlStateManager.glLineWidth(1.8F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(78.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * -0.17F, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, OUTER_WIND_RADIUS * 0.68D,
                0xBFD7E3, 0.12F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, OUTER_WIND_RADIUS * 0.86D,
                0xE8F6FF, 0.075F, RING_SEGMENTS);
        GlStateManager.popMatrix();
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawLightning(float ticks, ShaderProgram colorShader) {
        useAdditiveBlend();
        for (int i = 0; i < ARC_COUNT; i++) {
            double flashPhase = fract(ticks * LIGHTNING_CYCLE_SPEED + i * 0.173D);
            float flash = (float) Math.max(0.0D, Math.sin(Math.PI * flashPhase));
            double radius = 1.62D + (i % 4) * 0.38D;
            double angle = i * 0.7853981633974483D + ticks * (0.016D + (i % 3) * 0.004D);
            double y = -0.92D + (i % 5) * 0.46D;
            double lift = 0.34D + (i % 3) * 0.16D;
            double length = 0.45D + (i % 4) * 0.13D;

            GlStateManager.glLineWidth(3.1F);
            RenderNaturalShaderHelper.drawBasicJaggedArc(colorShader, radius, angle, length, y, lift,
                    0.090D, 7, 0x91DFFF, flash * 0.16F, ticks, 311 + i * 37);
            GlStateManager.glLineWidth(1.3F);
            RenderNaturalShaderHelper.drawBasicJaggedArc(colorShader, radius, angle, length, y, lift,
                    0.065D, 7, i % 2 == 0 ? 0xFFFFFF : 0xB7F0FF,
                    flash * 0.48F, ticks, 811 + i * 41);

            if ((i & 1) == 0) {
                drawBranch(ticks, i, angle + length * 0.52D, radius, y + lift * 0.40D, flash, colorShader);
            }
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawBranch(float ticks, int index, double angle, double radius, double y,
                            float flash, ShaderProgram colorShader) {
        double startX = Math.cos(angle) * radius;
        double startZ = Math.sin(angle) * radius;
        double endAngle = angle + Math.sin(ticks * 0.08D + index) * 0.34D;
        double endRadius = radius + 0.42D + (index % 3) * 0.12D;
        double endX = Math.cos(endAngle) * endRadius;
        double endY = y + Math.sin(index * 1.7D) * 0.24D;
        double endZ = Math.sin(endAngle) * endRadius;

        GlStateManager.glLineWidth(1.2F);
        RenderNaturalShaderHelper.drawBasicLine(colorShader, startX, y, startZ, endX, endY, endZ,
                0xEAFBFF, flash * 0.24F);
    }
}
