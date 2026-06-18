package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileRiftAnchorSpire;
import net.minecraft.client.renderer.GlStateManager;

public class RenderRiftAnchorSpire extends RenderArcaneShaderTile<TileRiftAnchorSpire> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int ANCHOR_ARCS = 6;
    private static final int RIFT_MOTES = 14;

    @Override
    protected void renderShaderLayers(TileRiftAnchorSpire te, float ticks, ShaderProgram shader) {
        drawSpire(shader, ticks);
        drawRiftSheets(shader, ticks);
        drawBindingRings(shader, ticks);
        drawAnchorArcs(shader, ticks);
        drawRiftMotes(shader, ticks);
    }

    private void drawSpire(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.036D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.08F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.30D, 1.74D, 6, 0x03020A, 0x74F5FF,
                0.56F + pulse * 0.10F, 1.12F, 15.0F, 44.0F, pulse);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.16D, 1.38D, 6, 0xC65DFF, 0xFFFFFF,
                0.13F + pulse * 0.12F, 1.38F, 20.0F, 61.0F, pulse);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRiftSheets(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < 3; i++) {
            float pulse = wave(ticks * 0.026D + i * 0.7D);
            double angle = i * TWO_PI / 3.0D + Math.sin(ticks * 0.018D + i) * 0.045D;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, 0.02D + i * 0.03D, 0.0D);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            ArcaneShaderEffectRenderer.drawPetalLayer(shader, ticks,
                    0.0D, 1.72D + i * 0.08D, 0.19D + i * 0.030D, 0.82D,
                    i == 0 ? 0x03020A : 0x14051F, i == 1 ? 0xC65DFF : 0x74F5FF,
                    0.20F + pulse * 0.07F, 1.10F, 18.0F, 80.0F + i * 5.0F, pulse);
            GlStateManager.popMatrix();
        }
    }

    private void drawBindingRings(ShaderProgram shader, float ticks) {
        drawRing(shader, ticks, 0.94D, -0.44D, 0.22F, 0x74F5FF, 0xFFFFFF, 70.0F);
        drawRing(shader, ticks, 1.22D, 0.44D, -0.31F, 0xC65DFF, 0xFFFFFF, 91.0F);
    }

    private void drawRing(ShaderProgram shader, float ticks, double radius, double y,
                          float speed, int color, int highlight, float seed) {
        float pulse = wave(ticks * 0.032D + seed);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate(76.0F - (float) (radius * 8.0D), 1.0F, 0.0F, 0.24F);
        GlStateManager.rotate(ticks * speed, 0.0F, 1.0F, 0.0F);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.052D, color, highlight, 0.24F + pulse * 0.12F,
                1.30F, 16.0F, seed, pulse, 80);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.18D, 0.020D, 16, highlight, color,
                0.24F + pulse * 0.12F, 1.25F, 18.0F, seed + 7.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawAnchorArcs(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ANCHOR_ARCS; i++) {
            double phase = fract(ticks * (0.010D + (i % 3) * 0.001D) + i * 0.137D);
            float alpha = (float) Math.sin(phase * Math.PI) * 0.42F;
            if (alpha <= 0.02F) {
                continue;
            }

            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks,
                    0.74D + (i % 3) * 0.16D, i * TWO_PI / ANCHOR_ARCS + ticks * 0.010D,
                    0.36D + (i % 2) * 0.10D, -0.42D + (i % 4) * 0.26D,
                    0.28D + (i % 2) * 0.12D, 0.048D, 7,
                    i % 2 == 0 ? 0x74F5FF : 0xC65DFF, 0xFFFFFF,
                    alpha, 1.45F, 17.0F, 110.0F + i * 8.0F, (float) phase);
        }
        useAlphaBlend();
    }

    private void drawRiftMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < RIFT_MOTES; i++) {
            double phase = fract(ticks * 0.009D + i * 0.089D);
            double angle = i * 2.399963229728653D + ticks * 0.006D;
            double radius = lerp(1.44D, 0.42D, phase);
            double y = -0.64D + phase * 1.28D + Math.sin(ticks * 0.016D + i) * 0.08D;
            float alpha = (float) Math.sin(phase * Math.PI) * 0.38F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.022D + (i % 3) * 0.006D, i % 2 == 0 ? 0x74F5FF : 0xC65DFF,
                    0xFFFFFF, 0.12F + alpha, 1.38F, 12.0F, i * 12.0F,
                    (float) phase, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
