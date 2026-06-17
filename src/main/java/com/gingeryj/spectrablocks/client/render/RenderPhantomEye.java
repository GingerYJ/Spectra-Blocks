package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderPhantomEye extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EYE_RADIUS = 0.34D;
    private static final double IRIS_RADIUS = 0.46D;
    private static final double LID_RADIUS = 0.78D;
    private static final int RING_SEGMENTS = 72;
    private static final int AFTERIMAGE_COUNT = 3;
    private static final int RAY_COUNT = 5;

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawPhantomAura(shader, ticks);
        drawAfterimages(shader, ticks);
        drawEyeCore(shader, ticks);
        drawEyelids(shader, ticks);
        drawSweepRays(shader, ticks);
    }

    private void drawPhantomAura(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.044F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                1.02D + pulse * 0.08D, 0x071733, 0x746CFF,
                0.10F + pulse * 0.05F, 1.14F, 13.0F, 2.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(1.25F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                0.88D + pulse * 0.04D, 0.0D, 0x58D9FF, 0xD7CBFF,
                0.13F + pulse * 0.06F, 1.18F, 17.0F, 8.0F, pulse, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawEyeCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.072F);
        float blink = blink(ticks);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                EYE_RADIUS * 1.55D + pulse * 0.035D, 0xB9F7FF, 0xFFFFFF,
                0.20F + pulse * 0.08F, 1.42F, 19.0F, 17.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 14, 14);
        useAlphaBlend();

        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) Math.sin(ticks * 0.018F) * 5.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.18D, 0.72D + blink * 0.10D, 0.88D);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                EYE_RADIUS, 0x081228, 0xB9F7FF,
                0.74F, 1.12F, 16.0F, 23.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 16, 16);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, 0.0D, -0.245D);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.12D + pulse * 0.010D, IRIS_RADIUS,
                0x22104A, 0x6EEBFF, 0.48F + pulse * 0.14F,
                1.30F, 21.0F, 31.0F, pulse, RING_SEGMENTS);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.13D + pulse * 0.018D, 0x04030D, 0xFFFFFF,
                0.82F, 1.60F, 23.0F, 37.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 10, 10);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawEyelids(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.052F);
        float blink = blink(ticks);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) Math.sin(ticks * 0.015F) * 3.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.glLineWidth(2.2F);
        ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks, LID_RADIUS,
                -2.68D, 2.22D, -0.16D - blink * 0.06D,
                0.23D - blink * 0.05D, 0.012D, 18,
                0x58D9FF, 0xFFFFFF, 0.35F + pulse * 0.12F,
                1.38F, 19.0F, 43.0F, pulse);
        ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks, LID_RADIUS,
                0.46D, 2.22D, 0.16D + blink * 0.06D,
                -0.23D + blink * 0.05D, 0.012D, 18,
                0x8B6CFF, 0xFFFFFF, 0.30F + pulse * 0.11F,
                1.34F, 19.0F, 59.0F, pulse);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawAfterimages(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) {
            float drift = wave(ticks * (0.034F + i * 0.006F) + i * 1.7F);
            double angle = ticks * (0.012D + i * 0.002D) + i * TWO_PI / AFTERIMAGE_COUNT;
            double radius = 0.70D + i * 0.10D + drift * 0.08D;
            double y = -0.10D + i * 0.10D + Math.sin(ticks * 0.026D + i) * 0.08D;
            float alpha = 0.13F + drift * 0.09F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            GlStateManager.rotate((float) (-ticks * 0.62F + i * 41.0F), 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(1.22D, 0.62D, 0.86D);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks - i * 7.0F,
                    0.15D + drift * 0.025D, i == 1 ? 0x8B6CFF : 0x58D9FF, 0xFFFFFF,
                    alpha, 1.24F, 15.0F, 71.0F + i * 13.0F, drift,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 8, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSweepRays(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.15F);
        for (int i = 0; i < RAY_COUNT; i++) {
            float flash = sweepFlash(ticks, i);
            if (flash <= 0.025F) {
                continue;
            }

            double y = -0.32D + i * 0.16D + Math.sin(ticks * 0.021D + i) * 0.035D;
            double skew = -0.12D + i * 0.055D;
            double length = 0.48D + i * 0.045D;

            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    -length, y, -0.34D + skew,
                    length, y + 0.12D, -0.26D - skew,
                    0xFFFFFF, i % 2 == 0 ? 0x58D9FF : 0xA58CFF,
                    0.12F + flash * 0.44F, 1.55F, 24.0F,
                    101.0F + i * 9.0F, flash);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private static float blink(float ticks) {
        double cycle = fract(ticks * 0.011D);
        if (cycle > 0.10D) {
            return 0.0F;
        }

        double shaped = Math.sin(cycle / 0.10D * Math.PI);
        return (float) (shaped * shaped);
    }

    private static float sweepFlash(float ticks, int seed) {
        double cycle = fract(ticks * 0.016D + seed * 0.19D);
        if (cycle > 0.16D) {
            return 0.0F;
        }

        double shaped = Math.sin(cycle / 0.16D * Math.PI);
        return (float) (shaped * shaped);
    }
}
