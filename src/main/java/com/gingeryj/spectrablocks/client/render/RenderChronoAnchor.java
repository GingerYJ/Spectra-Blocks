package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderChronoAnchor extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.18D;
    private static final double INNER_RING_RADIUS = 0.58D;
    private static final double MIDDLE_RING_RADIUS = 0.82D;
    private static final double OUTER_RING_RADIUS = 1.06D;
    private static final int RING_SEGMENTS = 96;
    private static final int INNER_MARKS = 12;
    private static final int MIDDLE_MARKS = 24;
    private static final int OUTER_MARKS = 36;
    private static final int AFTERIMAGE_COUNT = 10;
    private static final float CORE_PULSE_SPEED = 0.075F;
    private static final float RING_ROTATION_SPEED = 0.52F;

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawAnchorGlow(shader, ticks);
        drawReversedClockRings(shader, ticks);
        drawTickHands(shader, ticks);
        drawAfterimageMotes(shader, ticks);
        drawAnchorCore(shader, ticks);
    }

    private void drawAnchorGlow(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.040F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.72D + pulse * 0.08D, 0x5E3A16, 0x8FDFFF,
                0.11F + pulse * 0.04F, 1.14F, 12.0F, 5.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.36D + pulse * 0.05D, 0xFFD27A, 0xFFFFFF,
                0.17F + pulse * 0.07F, 1.28F, 15.0F, 9.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 14, 14);
        useAlphaBlend();
    }

    private void drawReversedClockRings(ShaderProgram shader, float ticks) {
        drawClockRing(shader, ticks, INNER_RING_RADIUS, 0.08D, 24.0F, 1.0F, 0.0F,
                0xFFD37A, 0xFFFFFF, INNER_MARKS, -1.18F, 19.0F);
        drawClockRing(shader, ticks, MIDDLE_RING_RADIUS, -0.02D, -34.0F, 0.0F, 1.0F,
                0x78D9FF, 0xFFF1C6, MIDDLE_MARKS, -0.72F, 37.0F);
        drawClockRing(shader, ticks, OUTER_RING_RADIUS, 0.04D, 62.0F, 0.75F, 1.0F,
                0xB07B2F, 0xAEEBFF, OUTER_MARKS, -0.38F, 53.0F);
    }

    private void drawClockRing(ShaderProgram shader, float ticks, double radius, double yOffset,
                               float tilt, float axisX, float axisZ, int bandColor,
                               int lineColor, int marks, float speedScale, float seed) {
        float pulse = wave(ticks * 0.033F + seed * 0.05F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.044D + pulse * 0.010D, bandColor, lineColor,
                0.14F + pulse * 0.06F, 1.08F, 16.0F, seed, pulse, RING_SEGMENTS);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.6F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                lineColor, 0xFFFFFF, 0.22F + pulse * 0.11F,
                1.32F, 22.0F, seed + 4.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.105D + radius * 0.018D, 0.018D, marks, lineColor, 0xFFFFFF,
                0.26F + pulse * 0.14F, 1.38F, 24.0F, seed + 8.0F, pulse);
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawTickHands(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.055F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        drawHand(shader, ticks, ticks * -0.020D, 0.12D, 0.70D, 0xFFFFFF, 0xFFD37A,
                0.34F + pulse * 0.16F, 71.0F);
        drawHand(shader, ticks, ticks * -0.006D + 1.5D, 0.10D, 0.48D, 0xAEEBFF, 0xFFFFFF,
                0.26F + pulse * 0.12F, 79.0F);
        drawHand(shader, ticks, ticks * -0.046D + 3.0D, 0.08D, 0.84D, 0xFFD37A, 0xFFFFFF,
                0.18F + pulse * 0.10F, 83.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawHand(ShaderProgram shader, float ticks, double angle, double innerRadius,
                          double outerRadius, int color, int accentColor, float alpha, float seed) {
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                Math.cos(angle) * innerRadius, Math.sin(angle) * innerRadius, 0.0D,
                Math.cos(angle) * outerRadius, Math.sin(angle) * outerRadius, 0.0D,
                color, accentColor, alpha, 1.42F, 18.0F, seed, alpha);
    }

    private void drawAfterimageMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) {
            double phase = fract(ticks * 0.025D + i * 0.137D);
            float flicker = sharpPulse((float) (Math.PI * phase));
            if (flicker <= 0.04F) {
                continue;
            }

            double angle = i * GOLDEN_ANGLE - ticks * (0.012D + (i % 3) * 0.003D);
            double radius = 0.36D + (i % 5) * 0.095D + phase * 0.16D;
            double y = -0.18D + (i % 4) * 0.12D + Math.sin(ticks * 0.021D + i) * 0.035D;
            double size = 0.018D + (i % 3) * 0.006D;
            int color = i % 3 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD37A : 0xAEEBFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks - i * 3.0F, size,
                    color, 0xFFFFFF, 0.18F + flicker * 0.36F, 1.45F, 11.0F,
                    101.0F + i * 7.0F, flicker, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawAnchorCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS + pulse * 0.035D, 0xFFF2C2, 0xFFFFFF,
                0.66F + pulse * 0.20F, 1.72F, 19.0F, 127.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 16, 16);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS * 2.2D + pulse * 0.08D, 0xFFD37A, 0x8FDFFF,
                0.16F + pulse * 0.08F, 1.30F, 14.0F, 131.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 16, 16);
        GlStateManager.glLineWidth(1.4F);
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2.0D * i / 6.0D - ticks * 0.018D;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * 0.14D, 0.0D, Math.sin(angle) * 0.14D,
                    Math.cos(angle) * (0.36D + pulse * 0.05D), 0.0D,
                    Math.sin(angle) * (0.36D + pulse * 0.05D),
                    i % 2 == 0 ? 0xFFD37A : 0xAEEBFF, 0xFFFFFF,
                    0.22F + pulse * 0.14F, 1.38F, 18.0F, 139.0F + i, pulse);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private static float sharpPulse(float value) {
        float pulse = Math.max(0.0F, (float) Math.sin(value));
        return pulse * pulse * pulse;
    }
}
