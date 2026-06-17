package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderRuneObelisk extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double OBELISK_RADIUS = 0.34D;
    private static final double OBELISK_HEIGHT = 2.35D;
    private static final double COLUMN_RADIUS = 0.20D;
    private static final double CORE_RADIUS = 0.25D;
    private static final int OBELISK_FACETS = 6;
    private static final int RING_SEGMENTS = 112;
    private static final int MOTE_COUNT = 30;
    private static final int SPARK_COUNT = 8;
    private static final float OBELISK_ROTATION_SPEED = 0.22F;
    private static final float RING_ROTATION_SPEED = 0.72F;
    private static final float MOTE_DRIFT_SPEED = 0.020F;
    private static final float CORE_PULSE_SPEED = 0.068F;

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawOuterAura(shader, ticks);
        drawLightColumn(shader, ticks);
        drawObeliskBody(shader, ticks);
        drawTiltedRuneRings(shader, ticks);
        drawFloatingRunes(shader, ticks);
        drawCorePulse(shader, ticks);
    }

    private void drawOuterAura(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.038F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                1.18D + pulse * 0.10D, 0x2D0B55, 0x9B73FF,
                0.11F + pulse * 0.05F, 1.18F, 9.0F, 4.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 22, 22);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.72D + pulse * 0.06D, 0x5131A2, 0xFFD678,
                0.13F + pulse * 0.05F, 1.25F, 13.0F, 8.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        useAlphaBlend();
    }

    private void drawLightColumn(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.050F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.58D, 3.25D, 0.58D);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                COLUMN_RADIUS + pulse * 0.030D, 0x735DFF, 0xFFE7A8,
                0.22F + pulse * 0.10F, 1.65F, 18.0F, 15.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 14, 14);
        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(1.7F);
        for (int i = 0; i < 3; i++) {
            double phase = ticks * (0.018D + i * 0.003D) + i * 2.0943951023931953D;
            ArcaneShaderEffectRenderer.drawHelixLayer(shader, ticks, phase,
                    0.22D, 0.10D, 1.95D, 1.35D, 42,
                    i == 1 ? 0xD7C4FF : 0xFFD782, 0xFFFFFF,
                    0.20F + pulse * 0.13F, 1.35F, 20.0F, 21.0F + i * 5.0F, pulse);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawObeliskBody(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.055F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * OBELISK_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.78D, 1.0D, 0.78D);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks, OBELISK_RADIUS, OBELISK_HEIGHT,
                OBELISK_FACETS, 0x180428, 0x7257F5,
                0.62F + pulse * 0.08F, 1.18F, 14.0F, 30.0F, pulse);
        useAdditiveBlend();
        GlStateManager.scale(1.035D, 1.015D, 1.035D);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks, OBELISK_RADIUS, OBELISK_HEIGHT,
                OBELISK_FACETS, 0x7B3EFF, 0xFFE2A4,
                0.22F + pulse * 0.10F, 1.70F, 24.0F, 36.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawTiltedRuneRings(ShaderProgram shader, float ticks) {
        drawRuneRing(shader, ticks, 0.74D, -0.34D, 34.0F, 1.0F, 0.0F,
                0x5A2CA3, 0xE8D7FF, 14, 0.96F, 43.0F);
        drawRuneRing(shader, ticks, 0.88D, 0.18D, -48.0F, 0.0F, 1.0F,
                0xD59B43, 0xFFE3A8, 18, -0.64F, 59.0F);
        drawRuneRing(shader, ticks, 0.66D, 0.62D, 27.0F, 0.65F, 1.0F,
                0x4E43D8, 0xCFC6FF, 12, 1.20F, 71.0F);
    }

    private void drawRuneRing(ShaderProgram shader, float ticks, double radius, double yOffset,
                              float tilt, float axisX, float axisZ, int bandColor,
                              int lineColor, int marks, float speedScale, float seed) {
        float pulse = wave(ticks * 0.040F + seed * 0.03F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.055D + pulse * 0.010D, bandColor, lineColor,
                0.20F + pulse * 0.08F, 1.14F, 17.0F, seed, pulse, RING_SEGMENTS);
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.9F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                lineColor, 0xFFFFFF, 0.30F + pulse * 0.12F,
                1.32F, 22.0F, seed + 3.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.15D, 0.021D, marks, lineColor, 0xFFFFFF,
                0.35F + pulse * 0.15F, 1.42F, 28.0F, seed + 7.0F, pulse);
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawFloatingRunes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = fract(ticks * MOTE_DRIFT_SPEED + i * 0.047D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.015D + (i % 4) * 0.002D);
            double radius = 0.48D + (i % 7) * 0.055D + Math.sin(ticks * 0.025D + i) * 0.035D;
            double height = -0.72D + progress * 1.62D;
            double size = 0.019D + (i % 3) * 0.006D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 5 == 0 ? 0xFFE1A0 : (i % 2 == 0 ? 0xC9B8FF : 0x7E6CFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, 0.24F + fade * 0.42F, 1.55F, 12.0F,
                    i * 13.0F, fade, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            if (i % 6 == 0) {
                drawRuneSpark(shader, ticks, size * 2.8D, color, 0.18F + fade * 0.24F, i * 17.0F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawCorePulse(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS + pulse * 0.050D, 0xFFF2BE, 0xFFFFFF,
                0.62F + pulse * 0.20F, 1.85F, 23.0F, 93.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.58D + pulse * 0.12D, 0x6C3BFF, 0xFFD98D,
                0.14F + pulse * 0.08F, 1.38F, 15.0F, 99.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);

        GlStateManager.glLineWidth(1.8F);
        for (int i = 0; i < SPARK_COUNT; i++) {
            double angle = Math.PI * 2.0D * i / SPARK_COUNT + ticks * 0.012D;
            double inner = 0.18D + pulse * 0.03D;
            double outer = 0.52D + pulse * 0.10D;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner,
                    Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                    i % 2 == 0 ? 0xFFE1A0 : 0xD7C4FF, 0xFFFFFF,
                    0.28F + pulse * 0.20F, 1.45F, 18.0F, 105.0F + i, pulse);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawRuneSpark(ShaderProgram shader, float ticks, double radius,
                               int color, float alpha, float seed) {
        GlStateManager.glLineWidth(1.0F);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                -radius, 0.0D, 0.0D, radius, 0.0D, 0.0D,
                color, 0xFFFFFF, alpha, 1.35F, 20.0F, seed, alpha);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, -radius, 0.0D, 0.0D, radius, 0.0D,
                color, 0xFFFFFF, alpha, 1.35F, 20.0F, seed + 1.0F, alpha);
    }
}
