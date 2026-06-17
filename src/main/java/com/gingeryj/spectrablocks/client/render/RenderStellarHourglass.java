package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileStellarHourglass;
import net.minecraft.client.renderer.GlStateManager;

public class RenderStellarHourglass extends RenderCelestialEffectBase<TileStellarHourglass> {

    private static final double LOBE_OFFSET = 1.16D;
    private static final double LOBE_RADIUS = 1.42D;
    private static final double FRAME_RADIUS = 2.06D;
    private static final int CLOUD_LAYER_COUNT = 4;
    private static final int DUST_COUNT = 62;
    private static final int STREAM_COUNT = 28;
    private static final int ARC_COUNT = 8;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float CLOUD_ROTATION_SPEED = 0.040F;
    private static final int TOP_COLOR = 0x85D9FF;
    private static final int BOTTOM_COLOR = 0xFFB978;
    private static final int STAR_COLOR = 0xF4F8FF;

    @Override
    protected void renderCelestialEffect(TileStellarHourglass te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawFrame(ticks, colorShader);
        drawNebulaLobe(ticks, LOBE_OFFSET, TOP_COLOR, 1.0F, naturalShader, colorShader);
        drawNebulaLobe(ticks, -LOBE_OFFSET, BOTTOM_COLOR, -1.0F, naturalShader, colorShader);
        drawFallingDust(ticks, naturalShader);
        drawStarArcs(ticks, colorShader);
    }

    private void drawFrame(float ticks, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.036D);

        useAdditiveBlend();
        GlStateManager.glLineWidth(2.2F);
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i + ticks * 0.004D;
            RenderNaturalShaderHelper.drawBasicLine(colorShader,
                    Math.cos(angle) * FRAME_RADIUS, 1.70D, Math.sin(angle) * FRAME_RADIUS,
                    Math.cos(angle + Math.PI) * FRAME_RADIUS, -1.70D,
                    Math.sin(angle + Math.PI) * FRAME_RADIUS, 0xBEEBFF, 0.055F + pulse * 0.035F);
        }
        RenderHelper.resetLineWidth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.72D, 0.0D);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, FRAME_RADIUS * 0.82D, 0xDFF7FF, 0.14F, 88);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -1.72D, 0.0D);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, FRAME_RADIUS * 0.82D, 0xFFD6A3, 0.14F, 88);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawNebulaLobe(float ticks, double yOffset, int color, float direction,
                                ShaderProgram naturalShader, ShaderProgram colorShader) {
        useAlphaBlend();
        for (int i = 0; i < CLOUD_LAYER_COUNT; i++) {
            double radius = LOBE_RADIUS + i * 0.22D;
            float pulse = wave(ticks * (0.026D + i * 0.006D) + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, yOffset, 0.0D);
            GlStateManager.rotate(direction * ticks * (CLOUD_ROTATION_SPEED + i * 0.014F), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(18.0F + i * 9.0F, 1.0F, 0.0F, 0.28F);
            GlStateManager.scale(1.0D + i * 0.05D, 0.46D + i * 0.04D, 1.0D - i * 0.035D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, radius + pulse * 0.08D,
                    RenderNaturalShaderHelper.MODE_HOURGLASS, i * 0.25F,
                    color, direction > 0.0F ? BOTTOM_COLOR : TOP_COLOR, STAR_COLOR,
                    0.080F + pulse * 0.034F, pulse, 0.94F,
                    ticks * 0.030F, 17.0F + i * 13.0F + direction * 3.0F, 24);
            GlStateManager.glLineWidth(1.0F);
            RenderNaturalShaderHelper.drawBasicWireSphere(colorShader, (radius + pulse * 0.08D) * 1.02D,
                    STAR_COLOR, 0.036F + pulse * 0.018F, 6, 10);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.34D,
                RenderNaturalShaderHelper.MODE_HOURGLASS, 1.4F,
                STAR_COLOR, color, 0xFFFFFF, 0.44F, wave(ticks * 0.036D),
                1.28F, ticks * 0.058F, direction > 0.0F ? 83.0F : 97.0F, 16);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFallingDust(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_COUNT; i++) {
            double progress = fract(ticks * 0.018D + i * 0.071D);
            double y = 1.18D - progress * 2.36D;
            double pinch = Math.abs(y) / 1.18D;
            double radius = 0.07D + pinch * pinch * 0.46D;
            double angle = i * GOLDEN_ANGLE + ticks * 0.016D;
            int color = progress < 0.52D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.22F + 0.20F * wave(ticks * 0.055D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.034D + (i % 4) * 0.006D,
                    RenderNaturalShaderHelper.MODE_HOURGLASS, 2.0F + (i % 5) * 0.10F,
                    color, STAR_COLOR, 0xFFFFFF, alpha, (float) progress, 1.10F,
                    ticks * 0.052F, i * 19.0F, 8);
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < DUST_COUNT; i++) {
            double top = i < DUST_COUNT / 2 ? 1.0D : -1.0D;
            int local = i % (DUST_COUNT / 2);
            double band = (local + 0.5D) / (DUST_COUNT / 2);
            double angle = local * GOLDEN_ANGLE + top * ticks * 0.007D;
            double radius = 0.28D + Math.pow(band, 0.56D) * 1.42D;
            double y = top * (0.62D + Math.sin(local * 0.53D + ticks * 0.020D) * 0.42D);
            int color = top > 0.0D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.12F + 0.12F * wave(ticks * 0.046D + local);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.026D + (local % 3) * 0.005D,
                    RenderNaturalShaderHelper.MODE_HOURGLASS, 3.0F + (local % 4) * 0.12F,
                    color, STAR_COLOR, 0xFFFFFF, alpha, (float) band, 0.95F,
                    ticks * 0.046F, 211.0F + local * 7.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawStarArcs(float ticks, ShaderProgram colorShader) {
        useAdditiveBlend();
        for (int i = 0; i < ARC_COUNT; i++) {
            double radius = 1.42D + (i % 4) * 0.17D;
            double start = i * 0.74D + ticks * (0.006D + (i % 2) * 0.002D);
            double sweep = Math.PI * (0.84D + (i % 3) * 0.18D);
            double y = i < ARC_COUNT / 2 ? 1.10D : -1.10D;
            double sign = y > 0.0D ? 1.0D : -1.0D;
            int color = y > 0.0D ? TOP_COLOR : BOTTOM_COLOR;
            float alpha = 0.12F + 0.07F * wave(ticks * 0.044D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.glLineWidth(2.0F);
            RenderNaturalShaderHelper.drawBasicSphericalArc(colorShader, radius, start, sweep,
                    sign * 0.24D, 0.15D, ticks * 0.010D + i, color, alpha, 46);
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
