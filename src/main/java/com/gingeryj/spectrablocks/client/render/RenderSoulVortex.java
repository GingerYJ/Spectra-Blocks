package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileSoulVortex;
import net.minecraft.client.renderer.GlStateManager;

public class RenderSoulVortex extends RenderArcaneShaderTile<TileSoulVortex> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double VORTEX_HEIGHT = 2.18D;
    private static final double BASE_RADIUS = 1.48D;
    private static final double TOP_RADIUS = 0.38D;
    private static final int SOUL_COUNT = 72;
    private static final int HELIX_COUNT = 4;
    private static final int HELIX_SEGMENTS = 72;
    private static final int RING_SEGMENTS = 112;
    private static final float VORTEX_ROTATION_SPEED = 0.055F;
    private static final float SOUL_RISE_SPEED = 0.012F;
    private static final float CORE_PULSE_SPEED = 0.060F;

    @Override
    protected void renderShaderLayers(TileSoulVortex te, float ticks, ShaderProgram shader) {
        drawBaseWell(shader, ticks);
        drawHelices(shader, ticks);
        drawSouls(shader, ticks);
        drawCenterColumn(shader, ticks);
    }

    private void drawBaseWell(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.050F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.04D, 0.0D);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.16D, BASE_RADIUS + pulse * 0.06D, 0x095C63, 0x6AFFD2,
                0.16F + pulse * 0.06F, 1.18F, 13.0F, 11.0F, pulse, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.7F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, BASE_RADIUS, 0.0D,
                0x6AFFD2, 0xD6FFF5, 0.24F + pulse * 0.12F,
                1.30F, 20.0F, 13.0F, pulse, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawHelices(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < HELIX_COUNT; i++) {
            double phase = TWO_PI * i / HELIX_COUNT + ticks * VORTEX_ROTATION_SPEED;
            int color = i % 2 == 0 ? 0x51FFD0 : 0x8CFBFF;
            GlStateManager.glLineWidth(i % 2 == 0 ? 2.2F : 1.4F);
            ArcaneShaderEffectRenderer.drawHelixLayer(shader, ticks, phase,
                    BASE_RADIUS, TOP_RADIUS, VORTEX_HEIGHT, 1.85D, HELIX_SEGMENTS,
                    color, 0xD6FFF5, 0.25F, 1.45F, 18.0F, i * 23.0F, 0.8F);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawSouls(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SOUL_COUNT; i++) {
            double progress = fract(ticks * SOUL_RISE_SPEED + i * 0.023D);
            double angle = i * GOLDEN_ANGLE + progress * TWO_PI * 1.85D + ticks * VORTEX_ROTATION_SPEED;
            double radius = lerp(BASE_RADIUS, TOP_RADIUS, progress) + Math.sin(ticks * 0.034D + i) * 0.045D;
            double height = progress * VORTEX_HEIGHT - 0.08D;
            double bob = Math.sin(ticks * 0.070D + i * 0.73D) * 0.035D;
            double size = 0.026D + Math.sin(progress * Math.PI) * 0.032D;
            float fade = 0.28F + (float) Math.sin(Math.PI * progress) * 0.50F;
            int color = i % 6 == 0 ? 0xD6FFF5 : (i % 2 == 0 ? 0x55FFC8 : 0x46D6FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height + bob, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, fade, 1.55F, 12.0F, i * 7.0F, (float) progress,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            if (i % 9 == 0) {
                drawSpark(shader, ticks, size * 2.6D, color, fade * 0.55F, i * 17.0F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawCenterColumn(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.54D + pulse * 0.08D, 0x1DE6C2, 0x8CFBFF,
                0.15F + pulse * 0.06F, 1.20F, 12.0F, 71.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 20, 20);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.12D, 0.0D);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.28D + pulse * 0.035D, 0xBFFFF2, 0xFFFFFF,
                0.24F + pulse * 0.12F, 1.45F, 16.0F, 73.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 16, 16);
        GlStateManager.popMatrix();
        GlStateManager.glLineWidth(1.3F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, 1.02D,
                -0.22D + pulse * 0.06D, 0x4DFFD0, 0xD6FFF5,
                0.20F, 1.2F, 15.0F, 79.0F, pulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, 0.82D,
                0.34D, 0x8CFBFF, 0xFFFFFF,
                0.17F + pulse * 0.08F, 1.2F, 15.0F, 83.0F, pulse, RING_SEGMENTS);
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
                    color, 0xFFFFFF, alpha, 1.3F, 18.0F, seed + i, alpha);
        }
    }
}
