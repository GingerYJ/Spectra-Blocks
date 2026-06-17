package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileAstralAltarCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderAstralAltarCore extends RenderArcaneShaderTile<TileAstralAltarCore> {

    private static final double INNER_RING_RADIUS = 0.86D;
    private static final double MIDDLE_RING_RADIUS = 1.54D;
    private static final double OUTER_RING_RADIUS = 2.28D;
    private static final double CORE_RADIUS = 0.28D;
    private static final int RING_SEGMENTS = 144;
    private static final int RUNE_MARKS_INNER = 12;
    private static final int RUNE_MARKS_MIDDLE = 20;
    private static final int RUNE_MARKS_OUTER = 28;
    private static final int STARFIRE_COUNT = 42;
    private static final float RING_ROTATION_SPEED = 0.46F;
    private static final float STARFIRE_RISE_SPEED = 0.018F;
    private static final float CORE_PULSE_SPEED = 0.066F;

    @Override
    protected void renderShaderLayers(TileAstralAltarCore te, float ticks, ShaderProgram shader) {
        drawAltarGlow(shader, ticks);
        drawRuneLayers(shader, ticks);
        drawStarfire(shader, ticks);
        drawCenterSpark(shader, ticks);
    }

    private void drawAltarGlow(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.92D + pulse * 0.10D, 0xFFD78A, 0xBFA2FF,
                0.14F + pulse * 0.06F, 1.22F, 12.0F, 7.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 22, 22);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.06D, 0.0D);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.18D, OUTER_RING_RADIUS + 0.18D, 0x533572, 0xFFE0A3,
                0.10F + pulse * 0.04F, 1.05F, 14.0F, 17.0F, pulse, RING_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRuneLayers(ShaderProgram shader, float ticks) {
        drawRuneLayer(shader, ticks, INNER_RING_RADIUS, 0.02D, 0xBFA2FF, 0xFFF4C4,
                RUNE_MARKS_INNER, 0.92F, 31.0F);
        drawRuneLayer(shader, ticks, MIDDLE_RING_RADIUS, 0.16D, 0x6A5CFF, 0xDCD2FF,
                RUNE_MARKS_MIDDLE, -0.54F, 47.0F);
        drawRuneLayer(shader, ticks, OUTER_RING_RADIUS, 0.31D, 0xD8913D, 0xFFE0A3,
                RUNE_MARKS_OUTER, 0.28F, 67.0F);
    }

    private void drawRuneLayer(ShaderProgram shader, float ticks, double radius, double yOffset,
                               int bandColor, int lineColor, int marks, float speedScale, float seed) {
        float pulse = wave(ticks * 0.036F + radius);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.070D + pulse * 0.020D, bandColor, lineColor,
                0.17F + pulse * 0.07F, 1.10F, 16.0F, seed, pulse, RING_SEGMENTS);
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.9F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                lineColor, 0xFFFFFF, 0.29F + pulse * 0.13F,
                1.35F, 22.0F, seed + 2.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.18D + radius * 0.025D, 0.024D, marks, lineColor, 0xFFFFFF,
                0.35F + pulse * 0.16F, 1.4F, 26.0F, seed + 5.0F, pulse);
        if (marks >= RUNE_MARKS_MIDDLE) {
            drawStarRays(shader, ticks, radius * 0.32D, radius * 0.86D,
                    marks / 4, lineColor, 0xFFFFFF, 0.14F + pulse * 0.08F, seed + 9.0F);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawStarfire(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STARFIRE_COUNT; i++) {
            double progress = fract(ticks * STARFIRE_RISE_SPEED + i * 0.037D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.025D + (i % 5) * 0.0018D);
            double radius = (1.0D - progress) * (0.42D + (i % 6) * 0.035D);
            double height = -0.02D + progress * 1.72D;
            double size = 0.020D + (1.0D - progress) * 0.035D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE6A3 : 0xCDBBFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, 0.25F + fade * 0.48F, 1.55F, 13.0F,
                    i * 11.0F, fade, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawCenterSpark(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS + pulse * 0.045D, 0xFFF8DA, 0xFFFFFF,
                0.64F + pulse * 0.18F, 1.65F, 20.0F, 71.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS * 2.2D + pulse * 0.10D, 0xFFD28A, 0xBFA2FF,
                0.15F + pulse * 0.07F, 1.25F, 14.0F, 73.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 20, 20);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        drawStarRays(shader, ticks, 0.22D, 0.88D + pulse * 0.10D, 8,
                0xFFF3C4, 0xFFFFFF, 0.38F + pulse * 0.20F, 79.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawStarRays(ShaderProgram shader, float ticks, double innerRadius,
                              double outerRadius, int rays, int color, int accentColor,
                              float alpha, float seed) {
        if (rays <= 0) {
            return;
        }

        GlStateManager.glLineWidth(2.0F);
        for (int i = 0; i < rays; i++) {
            double angle = Math.PI * 2.0D * i / rays + ticks * 0.010D;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                    Math.cos(angle) * outerRadius, 0.0D, Math.sin(angle) * outerRadius,
                    color, accentColor, alpha, 1.45F, 19.0F, seed + i * 2.0F, alpha);
        }
        GlStateManager.glLineWidth(1.0F);
    }
}
