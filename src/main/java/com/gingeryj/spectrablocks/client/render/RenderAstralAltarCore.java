package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileAstralAltarCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderAstralAltarCore extends RenderCelestialEffectBase<TileAstralAltarCore> {

    private static final double INNER_RING_RADIUS = 0.86D;
    private static final double MIDDLE_RING_RADIUS = 1.54D;
    private static final double OUTER_RING_RADIUS = 2.28D;
    private static final double CORE_RADIUS = 0.28D;
    private static final int RING_SEGMENTS = 144;
    private static final int RUNE_MARKS_INNER = 12;
    private static final int RUNE_MARKS_MIDDLE = 20;
    private static final int RUNE_MARKS_OUTER = 28;
    private static final int STARFIRE_COUNT = 42;
    private static final float RING_ROTATION_SPEED = 0.46F;
    private static final float STARFIRE_RISE_SPEED = 0.018F;
    private static final float CORE_PULSE_SPEED = 0.066F;

    @Override
    protected void renderCelestialEffect(TileAstralAltarCore te, float ticks) {
        drawAltarGlow(ticks);
        drawRuneLayers(ticks);
        drawStarfire(ticks);
        drawCenterSpark(ticks);
    }

    private void drawAltarGlow(float ticks) {
        float pulse = wave(ticks * 0.045F);

        useAdditiveBlend();
        RenderHelper.drawSphere(0.92D + pulse * 0.10D, 0xFFD78A, 0.105F + pulse * 0.055F, 22, 22);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.06D, 0.0D);
        RenderCelestialEffectBase.drawFlatRing(0.18D, OUTER_RING_RADIUS + 0.18D, 0x533572, 0.070F + pulse * 0.030F, RING_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRuneLayers(float ticks) {
        drawRuneLayer(ticks, INNER_RING_RADIUS, 0.02D, 0xBFA2FF, 0xFFF4C4, RUNE_MARKS_INNER, 0.92F);
        drawRuneLayer(ticks, MIDDLE_RING_RADIUS, 0.16D, 0x6A5CFF, 0xDCD2FF, RUNE_MARKS_MIDDLE, -0.54F);
        drawRuneLayer(ticks, OUTER_RING_RADIUS, 0.31D, 0xD8913D, 0xFFE0A3, RUNE_MARKS_OUTER, 0.28F);
    }

    private void drawRuneLayer(float ticks, double radius, double yOffset, int bandColor,
                               int lineColor, int marks, float speedScale) {
        float pulse = wave(ticks * 0.036F + radius);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        RenderEnergyEffectHelper.drawFlatBand(radius, 0.035D + pulse * 0.010D,
                bandColor, 0.12F + pulse * 0.055F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.9F);
        RenderHelper.drawCircle(radius, lineColor, 0.28F + pulse * 0.13F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.1F);
        RenderEnergyEffectHelper.drawRuneMarks(radius, 0.18D + radius * 0.025D, marks,
                lineColor, 0.34F + pulse * 0.16F, ticks * 0.010D * speedScale);
        if (marks >= RUNE_MARKS_MIDDLE) {
            RenderEnergyEffectHelper.drawStarRays(radius * 0.32D, radius * 0.86D,
                    marks / 4, lineColor, 0.13F + pulse * 0.08F, ticks * 0.007D * speedScale);
        }
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawStarfire(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STARFIRE_COUNT; i++) {
            double progress = fract(ticks * STARFIRE_RISE_SPEED + i * 0.037D);
            double angle = i * 2.399963229728653D + ticks * (0.025D + (i % 5) * 0.0018D);
            double radius = (1.0D - progress) * (0.42D + (i % 6) * 0.035D);
            double height = -0.02D + progress * 1.72D;
            double size = 0.020D + (1.0D - progress) * 0.035D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE6A3 : 0xCDBBFF);

            drawSphereAt(Math.cos(angle) * radius, height, Math.sin(angle) * radius,
                    size, color, 0.25F + fade * 0.48F, 6, 6);
        }
        useAlphaBlend();
    }

    private void drawCenterSpark(float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.045D, 0xFFF8DA, 0.62F + pulse * 0.20F, 18, 18);
        RenderHelper.drawSphere(CORE_RADIUS * 2.2D + pulse * 0.10D, 0xFFD28A, 0.12F + pulse * 0.06F, 20, 20);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderEnergyEffectHelper.drawStarRays(0.22D, 0.88D + pulse * 0.10D, 8,
                0xFFF3C4, 0.38F + pulse * 0.20F, ticks * 0.012D);
        GlStateManager.popMatrix();
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }
}
