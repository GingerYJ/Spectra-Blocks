package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileSilentObelisk;
import net.minecraft.client.renderer.GlStateManager;

public class RenderSilentObelisk extends RenderArcaneShaderTile<TileSilentObelisk> {

    private static final int SILENT_MOTES = 12;

    @Override
    protected void renderShaderLayers(TileSilentObelisk te, float ticks, ShaderProgram shader) {
        drawObelisk(shader, ticks);
        drawSilenceShell(shader, ticks);
        drawQuietRings(shader, ticks);
        drawMuteMotes(shader, ticks);
    }

    private void drawObelisk(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.022D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.045F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.32D, 2.45D, 6, 0x08070D, 0x4B5C88,
                0.64F + pulse * 0.06F, 0.95F, 9.0F, 130.0F, pulse);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.12D, 2.05D, 6, 0x9EA8C8, 0xE6E9F2,
                0.08F + pulse * 0.08F, 1.18F, 14.0F, 144.0F, pulse);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSilenceShell(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.022D);

        useAlphaBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                1.22D + pulse * 0.055D, 0x120B1E, 0x4B5C88,
                0.14F + pulse * 0.04F, 0.92F, 7.0F, 150.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.42D, 0x24143A, 0x9EA8C8,
                0.10F + pulse * 0.05F, 1.10F, 10.0F, 156.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 12, 12);
        useAlphaBlend();
    }

    private void drawQuietRings(ShaderProgram shader, float ticks) {
        drawRing(shader, ticks, 0.74D, -0.46D, 0.18F, 0x4B5C88, 0x9EA8C8, 160.0F);
        drawRing(shader, ticks, 1.02D, 0.10D, -0.13F, 0x24143A, 0x9EA8C8, 172.0F);
        drawRing(shader, ticks, 1.34D, 0.56D, 0.09F, 0x4B5C88, 0xE6E9F2, 184.0F);
    }

    private void drawRing(ShaderProgram shader, float ticks, double radius, double y,
                          float speed, int color, int highlight, float seed) {
        float pulse = wave(ticks * 0.020D + seed);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate(72.0F - (float) radius * 6.0F, 1.0F, 0.0F, 0.18F);
        GlStateManager.rotate(ticks * speed, 0.0F, 1.0F, 0.0F);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.030D, color, highlight, 0.13F + pulse * 0.05F,
                0.95F, 9.0F, seed, pulse, 72);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.10D, 0.012D, 10, highlight, color,
                0.12F + pulse * 0.06F, 1.05F, 12.0F, seed + 4.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawMuteMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SILENT_MOTES; i++) {
            double progress = fract(ticks * 0.004D + i * 0.083D);
            double angle = i * 2.399963229728653D + ticks * 0.003D;
            double radius = 0.66D + (i % 4) * 0.18D;
            double y = -0.70D + progress * 1.42D;
            float alpha = 0.10F + (float) Math.sin(progress * Math.PI) * 0.16F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.014D + (i % 3) * 0.004D, i % 2 == 0 ? 0x9EA8C8 : 0x4B5C88,
                    0xE6E9F2, alpha, 1.05F, 8.0F, i * 12.0F,
                    (float) progress, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
