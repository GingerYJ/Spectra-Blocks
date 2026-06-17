package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderCrystalHarmonicResonator extends RenderArcaneShaderTile<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int RING_SEGMENTS = 112;
    private static final int HARMONIC_RING_COUNT = 5;
    private static final int NODE_COUNT = 8;
    private static final int[] CRYSTAL_COLORS = new int[]{
            0xF7FFFF, 0xA8F6FF, 0xC7B7FF, 0xFFFFFF
    };

    @Override
    protected void renderShaderLayers(TileScalableEffect te, float ticks, ShaderProgram shader) {
        drawResonantCrystal(shader, ticks);
        drawHarmonicRings(shader, ticks);
        drawNodeNetwork(shader, ticks);
        drawFineOvertones(shader, ticks);
    }

    private void drawResonantCrystal(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.052D);
        double bob = Math.sin(ticks * 0.055D) * 0.055D;

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, bob, 0.0D);
        GlStateManager.rotate(ticks * 0.30F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.28D + pulse * 0.018D, 1.18D + pulse * 0.045D, 8,
                0xDFFFFF, 0xBFAFFF, 0.34F + pulse * 0.08F,
                1.18F, 22.0F, 17.0F, pulse);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.16D + pulse * 0.012D, 0.84D + pulse * 0.036D, 6,
                0xFFFFFF, 0x9EF8FF, 0.42F + pulse * 0.16F,
                1.55F, 28.0F, 29.0F, pulse);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.18D + pulse * 0.032D, 0xFFFFFF, 0xB7FBFF,
                0.52F + pulse * 0.18F, 1.70F, 20.0F, 41.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 14, 14);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawHarmonicRings(ShaderProgram shader, float ticks) {
        for (int i = 0; i < HARMONIC_RING_COUNT; i++) {
            double progress = i / (double) (HARMONIC_RING_COUNT - 1);
            double y = -0.48D + progress * 0.96D + Math.sin(ticks * 0.043D + i * 1.31D) * 0.030D;
            double radius = 0.64D + i * 0.18D + Math.sin(ticks * 0.049D + i) * 0.035D;
            double width = 0.030D + (i % 2) * 0.012D;
            float ringPulse = wave(ticks * 0.070D + i * 0.74D);
            int primary = CRYSTAL_COLORS[i % CRYSTAL_COLORS.length];
            int secondary = CRYSTAL_COLORS[(i + 2) % CRYSTAL_COLORS.length];

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(ticks * (0.08F + i * 0.018F), 0.0F, 0.0F, 1.0F);
            useAlphaBlend();
            ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius, width,
                    primary, secondary, 0.16F + ringPulse * 0.09F,
                    1.12F, 18.0F, 53.0F + i * 7.0F, ringPulse, RING_SEGMENTS);
            useAdditiveBlend();
            GlStateManager.glLineWidth(1.1F + i * 0.12F);
            ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks, radius, 0.0D,
                    0xFFFFFF, primary, 0.12F + ringPulse * 0.18F,
                    1.42F, 24.0F, 79.0F + i * 11.0F, ringPulse, RING_SEGMENTS);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawNodeNetwork(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.25F);
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + ticks * 0.012D;
            double nodeRadius = 1.14D + (i % 2) * 0.10D + Math.sin(ticks * 0.036D + i) * 0.035D;
            double nodeY = -0.38D + (i % 4) * 0.25D + Math.sin(ticks * 0.052D + i * 0.8D) * 0.045D;
            double coreY = Math.sin(ticks * 0.055D) * 0.055D + (i % 2 == 0 ? 0.18D : -0.18D);
            double x = Math.cos(angle) * nodeRadius;
            double z = Math.sin(angle) * nodeRadius;
            float pulse = wave(ticks * 0.075D + i * 0.67D);
            int color = CRYSTAL_COLORS[(i + 1) % CRYSTAL_COLORS.length];

            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle + 0.15D) * 0.22D, coreY, Math.sin(angle + 0.15D) * 0.22D,
                    x, nodeY, z,
                    color, 0xFFFFFF, 0.10F + pulse * 0.14F,
                    1.35F, 20.0F, 101.0F + i * 5.0F, pulse);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, nodeY, z);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.045D + pulse * 0.010D, color, 0xFFFFFF,
                    0.36F + pulse * 0.24F, 1.55F, 16.0F, 139.0F + i * 13.0F, pulse,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 7, 7);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawFineOvertones(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(0.9F);
        for (int i = 0; i < 4; i++) {
            double phase = ticks * (0.020D + i * 0.004D) + i * TWO_PI / 4.0D;
            double radius = 0.42D + i * 0.17D;
            double y = -0.28D + i * 0.18D;
            float pulse = wave(ticks * 0.088D + i * 0.9D);

            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks, radius, phase,
                    1.16D + i * 0.10D, y, 0.025D + i * 0.010D, 0.010D, 10,
                    i % 2 == 0 ? 0xA8F6FF : 0xC7B7FF, 0xFFFFFF,
                    0.075F + pulse * 0.10F, 1.28F, 18.0F, 173.0F + i * 17.0F, pulse);
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }
}
