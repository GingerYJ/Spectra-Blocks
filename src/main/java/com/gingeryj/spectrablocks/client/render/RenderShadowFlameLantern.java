package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderShadowFlameLantern extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.25D;
    private static final double LANTERN_RADIUS = 0.72D;
    private static final double LANTERN_HEIGHT = 1.56D;
    private static final int FLAME_TONGUE_COUNT = 10;
    private static final int SPARK_COUNT = 26;
    private static final int RING_SEGMENTS = 96;

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawShadowAura(shader, ticks);
        drawLanternFrame(shader, ticks);
        drawShadowFlames(shader, ticks);
        drawFloatingSparks(shader, ticks);
        drawCore(shader, ticks);
    }

    private void drawShadowAura(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.88D + pulse * 0.08D, 0x1C062E, 0x6A34B8,
                0.13F + pulse * 0.05F, 1.12F, 12.0F, 4.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.54D + pulse * 0.05D, 0x0B2731, 0x75F4FF,
                0.08F + pulse * 0.04F, 1.24F, 17.0F, 9.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 16, 16);
        useAlphaBlend();
    }

    private void drawLanternFrame(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.026D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.060F, 0.0F, 1.0F, 0.0F);

        GlStateManager.glLineWidth(1.45F);
        drawFrameRing(shader, ticks, LANTERN_RADIUS, -0.56D, 0x6A34B8, 0x99F7FF, 0.20F + pulse * 0.08F, 21.0F);
        drawFrameRing(shader, ticks, LANTERN_RADIUS * 0.88D, 0.54D, 0x6A34B8, 0x99F7FF, 0.18F + pulse * 0.07F, 25.0F);
        drawFrameRing(shader, ticks, LANTERN_RADIUS * 0.48D, 0.82D, 0x89F7FF, 0xFFFFFF, 0.16F + pulse * 0.06F, 29.0F);

        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2.0D * i / 6.0D;
            double x = Math.cos(angle) * LANTERN_RADIUS;
            double z = Math.sin(angle) * LANTERN_RADIUS;
            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    x, -0.56D, z,
                    x * 0.88D, 0.54D, z * 0.88D,
                    i % 2 == 0 ? 0x5D2D9D : 0x7AF5FF, 0xFFFFFF,
                    0.18F + pulse * 0.06F, 1.28F, 16.0F, 33.0F + i, pulse);
        }

        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, 0.82D, 0.0D, 0.0D, 1.08D, 0.0D,
                0x8AF8FF, 0xFFFFFF, 0.22F + pulse * 0.08F, 1.32F, 18.0F, 48.0F, pulse);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFrameRing(ShaderProgram shader, float ticks, double radius, double y,
                               int color, int accentColor, float alpha, float seed) {
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                color, accentColor, alpha, 1.20F, 18.0F, seed, alpha, RING_SEGMENTS);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.030D, color, accentColor, alpha * 0.42F, 1.05F, 13.0F, seed + 2.0F, alpha, RING_SEGMENTS);
        GlStateManager.popMatrix();
    }

    private void drawShadowFlames(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.38D, 0.0D);

        for (int i = 0; i < FLAME_TONGUE_COUNT; i++) {
            double angle = Math.PI * 2.0D * i / FLAME_TONGUE_COUNT + ticks * (0.006D + (i % 3) * 0.001D);
            double length = 0.68D + (i % 4) * 0.080D + wave(ticks * 0.052D + i) * 0.14D;
            double width = 0.115D + (i % 3) * 0.018D;
            double lift = 0.44D + wave(ticks * 0.041D + i * 0.63D) * 0.20D;
            int rootColor = i % 2 == 0 ? 0x160421 : 0x05222B;
            int tipColor = i % 3 == 0 ? 0x83F6FF : 0x7A3CE2;
            float alpha = 0.27F + wave(ticks * 0.060D + i * 0.48D) * 0.18F;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (ticks * 0.38D + i * 9.0D), 0.0F, 1.0F, 0.0F);
            ArcaneShaderEffectRenderer.drawPetalLayer(shader, ticks, angle,
                    length, width, lift, rootColor, tipColor,
                    alpha, 1.42F, 17.0F, 61.0F + i * 3.0F, alpha);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.072D);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS + pulse * 0.035D, 0x1A0428, 0x8C55FF,
                0.58F + pulse * 0.14F, 1.72F, 22.0F, 87.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS * 0.58D + pulse * 0.018D, 0x93FAFF, 0xFFFFFF,
                0.42F + pulse * 0.16F, 1.95F, 26.0F, 91.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 14, 14);
        useAlphaBlend();
    }

    private void drawFloatingSparks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(ticks * (0.010D + (i % 5) * 0.001D) + i * 0.067D);
            double drift = Math.sin(progress * Math.PI);
            double angle = i * GOLDEN_ANGLE + ticks * (0.014D + (i % 3) * 0.0014D);
            double radius = 0.08D + drift * (0.42D + (i % 5) * 0.025D);
            double y = -0.36D + progress * 1.44D + Math.sin(ticks * 0.034D + i) * 0.040D;
            double size = 0.015D + (1.0D - progress) * 0.024D + drift * 0.010D;
            float fade = (float) Math.sin(progress * Math.PI);
            int color = i % 6 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x7AF5FF : 0x7B3DDF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, 0.14F + fade * 0.34F,
                    1.45F, 15.0F, 113.0F + i * 7.0F, fade,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 7, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
