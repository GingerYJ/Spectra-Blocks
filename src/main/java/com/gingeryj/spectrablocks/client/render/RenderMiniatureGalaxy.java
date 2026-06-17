package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileMiniatureGalaxy;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderMiniatureGalaxy extends RenderCelestialEffectBase<TileMiniatureGalaxy> {

    private static final double GALAXY_RADIUS = 4.05D;
    private static final double CORE_RADIUS = 0.45D;
    private static final int ARM_COUNT = 4;
    private static final int STAR_COUNT = 132;
    private static final int TRAIL_COUNT = 12;
    private static final int DISC_SEGMENTS = 192;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float ROTATION_SPEED = 0.055F;

    @Override
    protected void renderCelestialEffect(TileMiniatureGalaxy te, float ticks) {
        drawCore(ticks);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        drawDisc(ticks);
        drawStars(ticks);
        drawTrailingClusters(ticks);
        GlStateManager.popMatrix();
    }

    private void drawCore(float ticks) {
        float pulse = wave(ticks * 0.031D);

        useAdditiveBlend();
        RenderHelper.drawSphere(CORE_RADIUS * (2.2D + pulse * 0.18D), 0xFFE6A3, 0.16F, 24, 24);
        RenderHelper.drawSphere(CORE_RADIUS * (1.45D + pulse * 0.10D), 0xA9D7FF, 0.20F, 20, 20);
        RenderHelper.drawSphere(CORE_RADIUS * (0.92D + pulse * 0.07D), 0xFFFFFF, 0.70F, 18, 18);
        useAlphaBlend();
    }

    private void drawDisc(float ticks) {
        useAlphaBlend();
        drawFlatRing(0.42D, GALAXY_RADIUS, 0x21344A, 0.055F, DISC_SEGMENTS);
        drawFlatRing(0.58D, GALAXY_RADIUS * 0.78D, 0x6CA8D9, 0.050F, DISC_SEGMENTS);

        useAdditiveBlend();
        for (int i = 0; i < ARM_COUNT; i++) {
            double start = i * Math.PI * 2.0D / ARM_COUNT + ticks * 0.002D;
            int color = i % 2 == 0 ? 0x8CCBFF : 0xFFE0A1;
            drawSpiralRibbon(0.48D, GALAXY_RADIUS * 0.92D, start,
                    Math.PI * 2.25D, 0.22D, color, 0.145F, 116);
            drawSpiralRibbon(0.66D, GALAXY_RADIUS * 0.98D, start + 0.18D,
                    Math.PI * 2.05D, 0.095D, 0xF7F3FF, 0.110F, 116);
        }
        useAlphaBlend();
    }

    private void drawStars(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        RenderHelper.PointBatch points = RenderQuality.low() ? RenderHelper.beginPointBatch(2.0F) : null;
        for (int i = 0; i < STAR_COUNT; i += stride) {
            double band = (i + 0.5D) / STAR_COUNT;
            double radius = 0.58D + Math.pow(band, 0.64D) * (GALAXY_RADIUS - 0.68D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.0038D / (0.45D + radius));
            double armBias = Math.sin(angle * ARM_COUNT - radius * 2.4D);
            double y = Math.sin(i * 1.713D + ticks * 0.010D) * (0.018D + radius * 0.010D);
            radius += armBias * 0.065D;

            int color;
            if (i % 7 == 0) {
                color = 0xFFE3A8;
            } else if (i % 5 == 0) {
                color = 0x9DD6FF;
            } else {
                color = 0xEEF6FF;
            }

            float flicker = 0.65F + 0.35F * wave(ticks * (0.035D + (i % 5) * 0.006D) + i);
            double size = 0.018D + (i % 4) * 0.004D + (i % 13 == 0 ? 0.020D : 0.0D);
            double starX = Math.cos(angle) * radius;
            double starZ = Math.sin(angle) * radius;
            if (points != null) {
                points.add(starX, y, starZ, color, 0.48F * flicker);
            } else {
                drawSphereAt(starX, y, starZ, size, color, 0.42F * flicker, 6, 6);
            }
        }
        if (points != null) {
            points.draw();
        }
        useAlphaBlend();
    }

    private void drawTrailingClusters(float ticks) {
        useAdditiveBlend();
        int trailCount = RenderQuality.detailCount(TRAIL_COUNT, 4);
        for (int i = 0; i < trailCount; i++) {
            double radius = 1.25D + i * 0.205D;
            double angle = i * GOLDEN_ANGLE + ticks * (0.0048D / radius);
            double trailAngle = angle - 0.34D;
            double y = Math.sin(i * 0.77D + ticks * 0.013D) * 0.045D;
            double headX = Math.cos(angle) * radius;
            double headZ = Math.sin(angle) * radius;
            double tailX = Math.cos(trailAngle) * (radius * 1.035D);
            double tailZ = Math.sin(trailAngle) * (radius * 1.035D);
            float pulse = wave(ticks * 0.045D + i * 0.9D);

            GlStateManager.glLineWidth(3.0F);
            RenderHelper.drawLine(headX, y, headZ, tailX, y * 0.35D, tailZ,
                    0x83C8FF, 0.075F + 0.045F * pulse);
            GlStateManager.glLineWidth(1.4F);
            RenderHelper.drawLine(headX, y, headZ, tailX, y * 0.35D, tailZ,
                    0xFFFFFF, 0.155F + 0.070F * pulse);
            RenderHelper.resetLineWidth();
            drawSphereAt(headX, y, headZ, 0.045D + pulse * 0.025D,
                    i % 3 == 0 ? 0xFFE5A8 : 0xDDEEFF, 0.55F, 7, 7);
        }
        useAlphaBlend();
        GlStateManager.glLineWidth(1.0F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
    }
}
