package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileArcaneStarRing;
import net.minecraft.client.renderer.GlStateManager;

public class RenderArcaneStarRing extends RenderArcaneShaderTile<TileArcaneStarRing> {

    private static final double CORE_RADIUS = 0.46D;
    private static final double HALO_RADIUS = 1.34D;
    private static final double INNER_RING_RADIUS = 1.32D;
    private static final double MIDDLE_RING_RADIUS = 2.02D;
    private static final double OUTER_RING_RADIUS = 2.72D;
    private static final int RING_SEGMENTS = 144;
    private static final int STAR_COUNT = 54;
    private static final int STAR_LINK_COUNT = 12;
    private static final float CORE_PULSE_SPEED = 0.052F;
    private static final float RING_ROTATION_SPEED = 0.52F;
    private static final float STAR_ORBIT_SPEED = 0.018F;

    @Override
    protected void renderShaderLayers(TileArcaneStarRing te, float ticks, ShaderProgram shader) {
        drawCore(shader, ticks);
        drawRuneRings(shader, ticks);
        drawOrbitStars(shader, ticks);
        drawStarLinks(shader, ticks);
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                HALO_RADIUS + pulse * 0.12D, 0xFFD176, 0xBFA2FF,
                0.16F + pulse * 0.06F, 1.28F, 10.0F, 5.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 26, 26);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS + pulse * 0.045D, 0xFFF5CC, 0xFFE7A3,
                0.78F, 1.52F, 18.0F, 9.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 24, 24);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS * 0.55D, 0xFFFFFF, 0xFFF2C1,
                0.56F + pulse * 0.18F, 1.9F, 24.0F, 13.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        drawStarRays(shader, ticks, 0.34D, 1.05D + pulse * 0.12D, 8,
                0xFFF2C1, 0xFFFFFF, 0.45F + pulse * 0.20F, 19.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRuneRings(ShaderProgram shader, float ticks) {
        drawRuneRing(shader, ticks, INNER_RING_RADIUS, 42.0F, 0.0F, 1.0F, 0.16F,
                0xEED28C, 0xFFF2C1, 14, 0.82F, 23.0F);
        drawRuneRing(shader, ticks, MIDDLE_RING_RADIUS, -27.0F, 1.0F, 0.0F, -0.11F,
                0xBFA2FF, 0xF2E8FF, 20, -0.48F, 41.0F);
        drawRuneRing(shader, ticks, OUTER_RING_RADIUS, 68.0F, 0.35F, 1.0F, 0.07F,
                0xFFD176, 0xFFF7D5, 28, 0.28F, 61.0F);
    }

    private void drawRuneRing(ShaderProgram shader, float ticks, double radius, float tilt,
                              float axisX, float axisZ, float offsetY, int bandColor,
                              int lineColor, int marks, float speedScale, float seed) {
        float pulse = wave(ticks * 0.037F + radius);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, offsetY, 0.0D);
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.065D, bandColor, lineColor, 0.19F + pulse * 0.08F,
                1.10F, 16.0F, seed, pulse, RING_SEGMENTS);
        useAdditiveBlend();
        GlStateManager.glLineWidth(2.0F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                lineColor, 0xFFFFFF, 0.28F + pulse * 0.12F,
                1.30F, 21.0F, seed + 3.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.20D, 0.024D, marks, lineColor, 0xFFFFFF,
                0.34F + pulse * 0.15F, 1.35F, 26.0F, seed + 7.0F, pulse);
        if (marks >= 20) {
            drawStarRays(shader, ticks, radius * 0.30D, radius * 0.82D,
                    marks / 4, lineColor, 0xFFFFFF, 0.12F + pulse * 0.08F, seed + 11.0F);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawOrbitStars(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STAR_COUNT; i++) {
            double angle = i * GOLDEN_ANGLE + ticks * (STAR_ORBIT_SPEED + (i % 5) * 0.0018D);
            double wave = Math.sin(ticks * 0.035D + i * 0.77D);
            double radius = 1.55D + (i % 11) * 0.135D + wave * 0.10D;
            double height = Math.sin(angle * 1.7D + i) * 0.52D;
            double starX = Math.cos(angle) * radius;
            double starZ = Math.sin(angle) * radius;
            double size = 0.028D + (i % 4) * 0.006D;
            float starAlpha = 0.32F + (float) (0.5D + 0.5D * wave) * 0.42F;
            int color = i % 6 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE6A3 : 0xD9C7FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(starX, height, starZ);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, starAlpha, 1.6F, 12.0F, i * 17.0F, starAlpha,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 7, 7);
            if (i % 9 == 0) {
                drawSpark(shader, ticks, size * 2.8D, color, starAlpha * 0.55F, i * 19.0F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawStarLinks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.6F);
        for (int i = 0; i < STAR_LINK_COUNT; i++) {
            double angle = i * 0.947D + ticks * 0.014D;
            double angleB = angle + 0.24D + (i % 3) * 0.07D;
            double radiusA = 1.18D + (i % 4) * 0.42D;
            double radiusB = radiusA + 0.34D;
            double yA = Math.sin(ticks * 0.030D + i) * 0.34D;
            double yB = yA + Math.cos(ticks * 0.025D + i * 1.4D) * 0.18D;
            float linkFade = wave(ticks * 0.075F + i * 0.9F);

            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * radiusA, yA, Math.sin(angle) * radiusA,
                    Math.cos(angleB) * radiusB, yB, Math.sin(angleB) * radiusB,
                    i % 2 == 0 ? 0xFFF6CF : 0xDCCBFF, 0xFFFFFF,
                    0.16F + linkFade * 0.20F, 1.25F, 18.0F, i * 29.0F, linkFade);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawSpark(ShaderProgram shader, float ticks, double radius,
                           int color, float alpha, float seed) {
        GlStateManager.glLineWidth(1.0F);
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.25D * i;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    -Math.cos(angle) * radius, -Math.sin(angle) * radius, 0.0D,
                    Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0D,
                    color, 0xFFFFFF, alpha, 1.35F, 20.0F, seed + i, alpha);
        }
    }

    private void drawStarRays(ShaderProgram shader, float ticks, double innerRadius,
                              double outerRadius, int rays, int color, int accentColor,
                              float alpha, float seed) {
        if (rays <= 0) {
            return;
        }

        GlStateManager.glLineWidth(2.0F);
        for (int i = 0; i < rays; i++) {
            double angle = Math.PI * 2.0D * i / rays + ticks * 0.008D;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                    Math.cos(angle) * outerRadius, 0.0D, Math.sin(angle) * outerRadius,
                    color, accentColor, alpha, 1.5F, 18.0F, seed + i * 3.0F, alpha);
        }
        GlStateManager.glLineWidth(1.0F);
    }
}
