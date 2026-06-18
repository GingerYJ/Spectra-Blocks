package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileGravityTideEngine;
import net.minecraft.client.renderer.GlStateManager;

public class RenderGravityTideEngine extends RenderArcaneShaderTile<TileGravityTideEngine> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int TIDE_MOTES = 18;

    @Override
    protected void renderShaderLayers(TileGravityTideEngine te, float ticks, ShaderProgram shader) {
        drawGravityField(shader, ticks);
        drawTideRings(shader, ticks);
        drawTideArms(shader, ticks);
        drawInfallMotes(shader, ticks);
    }

    private void drawGravityField(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.044D);
        float fieldPulse = wave(ticks * 0.028D);

        useAlphaBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                1.95D + fieldPulse * 0.08D, 0x0B1F42, 0x62E6FF,
                0.16F + fieldPulse * 0.04F, 1.05F, 9.0F, 15.0F, fieldPulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 22, 22);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.86D + pulse * 0.04D, 0x1A2D66, 0x7A63FF,
                0.20F + pulse * 0.06F, 1.10F, 12.0F, 25.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 20, 20);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.34D + pulse * 0.035D, 0x020612, 0x62E6FF,
                0.68F + pulse * 0.10F, 1.35F, 16.0F, 35.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 20, 20);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.18D + pulse * 0.020D, 0xE8FCFF, 0x62E6FF,
                0.22F + pulse * 0.18F, 1.55F, 20.0F, 45.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 14, 14);
        useAlphaBlend();
    }

    private void drawTideRings(ShaderProgram shader, float ticks) {
        drawRing(shader, ticks, 0.85D, -0.22D, 0.055F, 0x62E6FF, 0xE8FCFF, 11.0F);
        drawRing(shader, ticks, 1.35D, 0.06D, -0.038F, 0x7A63FF, 0xE8FCFF, 23.0F);
        drawRing(shader, ticks, 2.05D, 0.30D, 0.027F, 0x365AB8, 0x62E6FF, 37.0F);
    }

    private void drawRing(ShaderProgram shader, float ticks, double radius, double y,
                          float speed, int color, int highlight, float seed) {
        float pulse = wave(ticks * 0.030D + seed);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate(64.0F - (float) radius * 7.0F, 1.0F, 0.0F, 0.25F);
        GlStateManager.rotate(ticks * speed, 0.0F, 1.0F, 0.0F);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.050D, color, highlight, 0.22F + pulse * 0.10F,
                1.22F, 14.0F, seed, pulse, 96);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.16D, 0.018D, 18, highlight, color,
                0.18F + pulse * 0.10F, 1.15F, 16.0F, seed + 6.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawTideArms(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 6; i++) {
            double phase = ticks * 0.014D + i * TWO_PI / 6.0D;
            float pulse = wave(ticks * 0.038D + i * 0.53D);
            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks,
                    1.02D + (i % 3) * 0.22D, phase, 0.74D,
                    -0.20D + (i % 3) * 0.18D, 0.30D + (i % 2) * 0.12D,
                    0.040D, 8, i % 2 == 0 ? 0x62E6FF : 0x7A63FF, 0xE8FCFF,
                    0.16F + pulse * 0.24F, 1.35F, 14.0F, 60.0F + i * 5.0F, pulse);
        }
        useAlphaBlend();
    }

    private void drawInfallMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < TIDE_MOTES; i++) {
            double progress = fract(ticks * 0.009D + i * 0.067D);
            double angle = i * 2.399963229728653D + ticks * 0.006D;
            double radius = lerp(2.10D, 0.34D, progress);
            double y = Math.sin(angle * 1.7D + ticks * 0.018D) * (0.46D * (1.0D - progress));
            float fade = (float) Math.sin(progress * Math.PI);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.020D + (i % 3) * 0.006D, i % 2 == 0 ? 0x62E6FF : 0x7A63FF,
                    0xE8FCFF, 0.12F + fade * 0.36F, 1.40F, 11.0F,
                    i * 8.0F, fade, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
