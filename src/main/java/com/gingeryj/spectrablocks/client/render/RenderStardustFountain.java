package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileStardustFountain;
import net.minecraft.client.renderer.GlStateManager;

public class RenderStardustFountain extends RenderCelestialEffectBase<TileStardustFountain> {

    private static final double BASIN_RADIUS = 1.18D;
    private static final double PLUME_HEIGHT = 2.72D;
    private static final double TOP_SPREAD_RADIUS = 1.62D;
    private static final int STREAM_PARTICLE_COUNT = 58;
    private static final int FALLING_PARTICLE_COUNT = 68;
    private static final int RING_SEGMENTS = 128;
    private static final float STREAM_RISE_SPEED = 0.020F;
    private static final float FALL_CYCLE_SPEED = 0.010F;
    private static final float BASIN_ROTATION_SPEED = 0.58F;
    private static final float CORE_PULSE_SPEED = 0.075F;

    @Override
    protected void renderCelestialEffect(TileStardustFountain te, float ticks) {
        drawBasin(ticks);
        drawRisingStream(ticks);
        drawFallingStardust(ticks);
        drawFountainCore(ticks);
    }

    private void drawBasin(float ticks) {
        float pulse = wave(ticks * 0.048F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.04D, 0.0D);
        GlStateManager.rotate(ticks * BASIN_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        RenderCelestialEffectBase.drawFlatRing(0.18D, BASIN_RADIUS + pulse * 0.05D,
                0x285A91, 0.11F + pulse * 0.045F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.8F);
        RenderHelper.drawCircle(BASIN_RADIUS, 0x9AE8FF, 0.27F + pulse * 0.12F, RING_SEGMENTS);
        RenderEnergyEffectHelper.drawStarRays(0.32D, BASIN_RADIUS * 0.92D, 10,
                0xFFF4C6, 0.16F + pulse * 0.08F, ticks * 0.010D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRisingStream(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_PARTICLE_COUNT; i++) {
            double progress = fract(ticks * STREAM_RISE_SPEED + i * 0.031D);
            double angle = i * 2.399963229728653D + ticks * 0.032D;
            double radius = 0.10D + Math.sin(progress * Math.PI) * 0.22D + (i % 4) * 0.010D;
            double height = progress * PLUME_HEIGHT;
            double size = 0.020D + Math.sin(progress * Math.PI) * 0.032D;
            float fade = 0.28F + (float) Math.sin(Math.PI * progress) * 0.52F;
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFEFC2 : 0x95E7FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            RenderHelper.drawSphere(size, color, fade, 6, 6);
            if (i % 8 == 0) {
                RenderEnergyEffectHelper.drawSpark(size * 3.0D, color, fade * 0.55F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFallingStardust(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < FALLING_PARTICLE_COUNT; i++) {
            double progress = fract(ticks * FALL_CYCLE_SPEED + i * 0.019D);
            double angle = i * 2.399963229728653D + ticks * (0.011D + (i % 5) * 0.001D);
            double arc = Math.sin(progress * Math.PI);
            double radius = TOP_SPREAD_RADIUS * arc * (0.74D + (i % 6) * 0.045D);
            double height = PLUME_HEIGHT - progress * 1.72D + arc * 0.28D;
            double size = 0.018D + (1.0D - progress) * 0.026D;
            float fade = 0.18F + (float) arc * 0.46F;
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD68A : 0x78DFFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            RenderHelper.drawSphere(size, color, fade, 6, 6);
            if (i % 11 == 0) {
                RenderEnergyEffectHelper.drawSpark(size * 2.4D, color, fade * 0.48F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFountainCore(float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        RenderHelper.drawSphere(0.30D + pulse * 0.04D, 0xFFFFFF, 0.62F + pulse * 0.20F, 18, 18);
        RenderHelper.drawSphere(0.76D + pulse * 0.10D, 0x8AE6FF, 0.12F + pulse * 0.055F, 20, 20);
        drawSphereAt(0.0D, PLUME_HEIGHT, 0.0D, 0.24D + pulse * 0.045D,
                0xFFF2B8, 0.26F + pulse * 0.16F, 16, 16);
        useAlphaBlend();
    }
}
