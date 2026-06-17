package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderMirrorShardField extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final int SHARD_COUNT = 22;
    private static final int ARC_COUNT = 6;
    private static final int GLINT_COUNT = 18;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double FIELD_RADIUS = 1.34D;
    private static final int[] MIRROR_COLORS = new int[]{
            0xF7FFFF, 0xBFEFFF, 0xD7CEFF, 0xEAFBFF
    };

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawPaleCore(shader, ticks);
        drawFloatingShards(shader, ticks);
        drawReflectionArcs(shader, ticks);
        drawEdgeFlashes(shader, ticks);
    }

    private void drawPaleCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045D);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.28D + pulse * 0.035D, 0xFFFFFF, 0xD9FFFF,
                0.44F + pulse * 0.12F, 1.55F, 18.0F, 3.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 16, 16);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.72D + pulse * 0.10D, 0xCFF9FF, 0xD7CEFF,
                0.12F + pulse * 0.06F, 1.18F, 13.0F, 7.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        useAlphaBlend();
    }

    private void drawFloatingShards(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < SHARD_COUNT; i++) {
            double orbit = i * GOLDEN_ANGLE + ticks * (0.010D + (i % 4) * 0.0015D);
            double layer = -0.82D + (i % 11) * 0.164D;
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - layer * layer));
            double radius = FIELD_RADIUS + (i % 5) * 0.095D
                    + Math.sin(ticks * 0.030D + i * 0.73D) * 0.14D;
            double x = Math.cos(orbit) * horizontal * radius;
            double y = layer * 0.96D + Math.sin(ticks * 0.024D + i) * 0.13D;
            double z = Math.sin(orbit) * horizontal * radius;
            double width = 0.15D + (i % 4) * 0.025D;
            double height = 0.30D + (i % 5) * 0.035D;
            int color = MIRROR_COLORS[i % MIRROR_COLORS.length];
            int accent = MIRROR_COLORS[(i + 2) % MIRROR_COLORS.length];
            float shimmer = wave(ticks * 0.060D + i * 0.57D);
            float alpha = 0.18F + shimmer * 0.18F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate((float) (ticks * (1.10D + (i % 3) * 0.18D) + i * 29.0D),
                    0.22F + (i % 2) * 0.30F, 1.0F, 0.48F);
            GlStateManager.rotate((float) (ticks * 0.42D + i * 37.0D), 0.0F, 0.0F, 1.0F);
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks, width, height,
                    color, accent, alpha, 1.24F, 22.0F, 17.0F + i * 5.0F, shimmer);

            if (i % 3 == 0) {
                useAdditiveBlend();
                drawShardEdge(shader, ticks, width, height, color,
                        (0.08F + shimmer * 0.18F) * flash(ticks, i), 53.0F + i);
                useAlphaBlend();
            }
            GlStateManager.popMatrix();
        }
    }

    private void drawReflectionArcs(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ARC_COUNT; i++) {
            double phase = ticks * (0.014D + i * 0.001D) + i * TWO_PI / ARC_COUNT;
            double radius = 0.82D + (i % 3) * 0.24D;
            double yOffset = -0.44D + (i % 4) * 0.28D;
            float pulse = wave(ticks * 0.038D + i * 0.81D);
            float alpha = 0.13F + pulse * 0.13F;

            GlStateManager.glLineWidth(1.0F + (i % 2) * 0.55F);
            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks, radius, phase,
                    0.36D + (i % 2) * 0.10D, yOffset,
                    0.06D + (i % 3) * 0.025D, 0.018D, 8,
                    MIRROR_COLORS[(i + 1) % MIRROR_COLORS.length], 0xFFFFFF,
                    alpha, 1.36F, 20.0F, 89.0F + i * 11.0F, pulse);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawEdgeFlashes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < GLINT_COUNT; i++) {
            float flash = flash(ticks, i);
            if (flash <= 0.035F) {
                continue;
            }

            double angle = i * GOLDEN_ANGLE - ticks * (0.012D + (i % 5) * 0.001D);
            double radius = 0.48D + (i % 6) * 0.18D;
            double y = -0.62D + (i % 7) * 0.20D + Math.sin(ticks * 0.032D + i) * 0.05D;
            double length = 0.12D + (i % 4) * 0.025D;
            int color = MIRROR_COLORS[(i + 3) % MIRROR_COLORS.length];

            GlStateManager.glLineWidth(1.1F);
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                    Math.cos(angle + 0.10D) * (radius + length), y + length * 0.36D,
                    Math.sin(angle + 0.10D) * (radius + length),
                    0xFFFFFF, color, 0.12F + flash * 0.48F,
                    1.55F, 25.0F, 131.0F + i * 7.0F, flash);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawShardEdge(ShaderProgram shader, float ticks, double width,
                               double height, int color, float alpha, float seed) {
        if (alpha <= 0.01F) {
            return;
        }

        GlStateManager.glLineWidth(1.0F);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, height * 0.58D, 0.0D,
                width * 0.36D, -height * 0.18D, width * 0.16D,
                0xFFFFFF, color, alpha, 1.42F, 24.0F, seed, alpha);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, height * 0.58D, 0.0D,
                -width * 0.42D, -height * 0.34D, width * 0.10D,
                0xFFFFFF, color, alpha * 0.80F, 1.42F, 24.0F, seed + 2.0F, alpha);
    }

    private static float flash(float ticks, int seed) {
        double cycle = fract(ticks * 0.018D + seed * 0.137D);
        if (cycle > 0.18D) {
            return 0.0F;
        }

        double shaped = Math.sin(cycle / 0.18D * Math.PI);
        return (float) (shaped * shaped);
    }
}
