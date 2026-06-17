package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderRadiantSigilField extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double INNER_RADIUS = 0.42D;
    private static final double MIDDLE_RADIUS = 0.78D;
    private static final double OUTER_RADIUS = 1.12D;
    private static final double COLUMN_RADIUS = 0.12D;
    private static final int RING_SEGMENTS = 112;
    private static final int SIGIL_MARKS = 24;
    private static final int RISING_MOTES = 18;
    private static final float RING_ROTATION_SPEED = 0.26F;
    private static final float SIGIL_SEQUENCE_SPEED = 0.045F;
    private static final float MOTE_RISE_SPEED = 0.014F;

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawGroundCircle(shader, ticks);
        drawSigilRings(shader, ticks);
        drawSequentialSigils(shader, ticks);
        drawCenterColumn(shader, ticks);
        drawRisingMotes(shader, ticks);
    }

    private void drawGroundCircle(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.038F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.46D, 0.0D);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.18D, OUTER_RADIUS + 0.16D, 0xF9D985, 0xBDF8FF,
                0.08F + pulse * 0.05F, 1.12F, 13.0F, 5.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.0D, INNER_RADIUS * 0.72D, 0xFFF4C6, 0xFFFFFF,
                0.10F + pulse * 0.06F, 1.35F, 18.0F, 9.0F, pulse, 72);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSigilRings(ShaderProgram shader, float ticks) {
        drawSigilRing(shader, ticks, INNER_RADIUS, 0.020D, 0xFFF5CD, 0xFFFFFF,
                10, 0.82F, 17.0F);
        drawSigilRing(shader, ticks, MIDDLE_RADIUS, 0.034D, 0xBDF8FF, 0xFFF1B5,
                16, -0.48F, 31.0F);
        drawSigilRing(shader, ticks, OUTER_RADIUS, 0.045D, 0xD7A849, 0xFFF7DB,
                SIGIL_MARKS, 0.22F, 47.0F);
    }

    private void drawSigilRing(ShaderProgram shader, float ticks, double radius, double width,
                               int bandColor, int lineColor, int marks, float speedScale, float seed) {
        float pulse = wave(ticks * 0.041F + seed * 0.06F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.435D, 0.0D);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius, width,
                bandColor, lineColor, 0.15F + pulse * 0.07F,
                1.10F, 15.0F, seed, pulse, RING_SEGMENTS);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.3F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                lineColor, 0xFFFFFF, 0.18F + pulse * 0.10F,
                1.32F, 22.0F, seed + 2.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.105D + radius * 0.030D, 0.018D, marks,
                lineColor, 0xFFFFFF, 0.18F + pulse * 0.12F,
                1.38F, 24.0F, seed + 5.0F, pulse);
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawSequentialSigils(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.410D, 0.0D);
        GlStateManager.rotate(ticks * 0.060F, 0.0F, 1.0F, 0.0F);
        GlStateManager.glLineWidth(1.9F);

        for (int i = 0; i < SIGIL_MARKS; i++) {
            double cycle = fract(ticks * SIGIL_SEQUENCE_SPEED + i / (double) SIGIL_MARKS);
            float glow = sigilFlash(cycle);
            if (glow <= 0.025F) {
                continue;
            }

            double angle = TWO_PI * i / SIGIL_MARKS;
            double radius = OUTER_RADIUS - (i % 3) * 0.105D;
            double inner = radius - 0.060D;
            double outer = radius + 0.135D + glow * 0.035D;
            int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFF0B8 : 0xBDF8FF);

            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner,
                    Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                    color, 0xFFFFFF, 0.16F + glow * 0.46F,
                    1.58F, 25.0F, 83.0F + i * 3.0F, glow);

            if (i % 4 == 0) {
                drawSigilNotch(shader, ticks, angle, radius - 0.18D,
                        0.055D + glow * 0.015D, color, 0.10F + glow * 0.30F, 137.0F + i);
            }
        }

        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCenterColumn(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.064F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.18D, 0.0D);
        GlStateManager.scale(0.74D, 3.45D, 0.74D);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                COLUMN_RADIUS + pulse * 0.018D, 0xFFF6CF, 0xC9FBFF,
                0.22F + pulse * 0.12F, 1.62F, 19.0F, 151.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 12, 12);
        GlStateManager.popMatrix();

        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.22D + pulse * 0.035D, 0xFFFFFF, 0xFFF1B5,
                0.48F + pulse * 0.18F, 1.75F, 22.0F, 157.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 14, 14);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.48D + pulse * 0.080D, 0xF4C96A, 0xBDF8FF,
                0.10F + pulse * 0.07F, 1.24F, 13.0F, 163.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 16, 16);
        useAlphaBlend();
    }

    private void drawRisingMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < RISING_MOTES; i++) {
            double progress = fract(ticks * MOTE_RISE_SPEED + i * 0.071D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.010D + (i % 4) * 0.0012D);
            double drift = Math.sin(progress * Math.PI);
            double radius = 0.18D + drift * (0.58D + (i % 5) * 0.045D);
            double height = -0.42D + progress * 1.36D;
            double size = 0.013D + (1.0D - progress) * 0.018D + (i % 3) * 0.003D;
            float fade = (float) Math.sin(progress * Math.PI);
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE9A5 : 0xBDF8FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, 0.14F + fade * 0.34F,
                    1.48F, 14.0F, 181.0F + i * 7.0F, fade,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSigilNotch(ShaderProgram shader, float ticks, double angle, double radius,
                                double halfLength, int color, float alpha, float seed) {
        double tangentX = -Math.sin(angle);
        double tangentZ = Math.cos(angle);
        double centerX = Math.cos(angle) * radius;
        double centerZ = Math.sin(angle) * radius;

        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                centerX - tangentX * halfLength, 0.0D, centerZ - tangentZ * halfLength,
                centerX + tangentX * halfLength, 0.0D, centerZ + tangentZ * halfLength,
                color, 0xFFFFFF, alpha, 1.42F, 23.0F, seed, alpha);
    }

    private static float sigilFlash(double cycle) {
        double wrapped = cycle < 0.5D ? cycle : 1.0D - cycle;
        double shaped = Math.max(0.0D, 1.0D - wrapped * 7.0D);
        return (float) (shaped * shaped);
    }
}
