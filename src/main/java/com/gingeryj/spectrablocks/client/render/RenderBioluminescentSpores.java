package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileBioluminescentSpores;
import net.minecraft.client.renderer.GlStateManager;

public class RenderBioluminescentSpores extends RenderCelestialEffectBase<TileBioluminescentSpores> {

    private static final int SPORE_COUNT = 72;
    private static final int VEIL_COUNT = 5;
    private static final double SPORE_RADIUS = 1.34D;
    private static final double SPORE_HEIGHT = 2.12D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float RISE_SPEED = 0.010F;
    private static final float GLOW_SPEED = 0.058F;
    private static final int[] SPORE_COLORS = new int[]{
            0x7BFFF2, 0x4FD8FF, 0x8B6BFF, 0xB56BFF, 0xC8FFF2
    };

    @Override
    protected void renderCelestialEffect(TileBioluminescentSpores te, float ticks) {
        drawCaveGlow(ticks);
        drawSporeVeils(ticks);
        drawRisingSpores(ticks);
    }

    private void drawCaveGlow(float ticks) {
        float pulse = wave(ticks * 0.046F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.44D, 0.0D);
        RenderCelestialEffectBase.drawFlatRing(0.16D, 1.18D + pulse * 0.08D,
                0x1F9AC7, 0.060F + pulse * 0.030F, 96);
        RenderHelper.drawSphere(0.74D + pulse * 0.10D, 0x3EE6D8, 0.040F + pulse * 0.030F, 16, 16);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSporeVeils(float ticks) {
        useAlphaBlend();
        for (int i = 0; i < VEIL_COUNT; i++) {
            float pulse = wave(ticks * (0.028F + i * 0.004F) + i * 0.67D);
            int color = i % 2 == 0 ? 0x236B8F : 0x542E92;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, -0.22D + i * 0.20D, 0.0D);
            GlStateManager.rotate(ticks * (0.034F + i * 0.006F), 0.25F, 1.0F, 0.35F);
            GlStateManager.scale(1.0D + i * 0.06D, 0.42D + i * 0.035D, 0.86D + i * 0.04D);
            RenderHelper.drawSphere(0.86D + pulse * 0.12D, color, 0.034F + pulse * 0.026F, 16, 16);
            GlStateManager.popMatrix();
        }
    }

    private void drawRisingSpores(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPORE_COUNT; i++) {
            double progress = fract(ticks * (RISE_SPEED + (i % 6) * 0.0007D) + i * 0.041D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.004D + (i % 4) * 0.0008D);
            double radius = SPORE_RADIUS * (0.20D + 0.80D * fract(i * 0.173D))
                    + Math.sin(ticks * 0.023D + i) * 0.08D;
            double fadeCurve = Math.sin(Math.PI * progress);
            double x = Math.cos(angle) * radius * (0.82D + fadeCurve * 0.18D);
            double y = -0.76D + progress * SPORE_HEIGHT;
            double z = Math.sin(angle) * radius * (0.82D + fadeCurve * 0.18D);
            double swirl = Math.sin(ticks * 0.031D + i * 0.53D) * 0.08D;
            double size = 0.018D + (i % 5) * 0.005D + fadeCurve * 0.018D;
            float pulse = wave(ticks * GLOW_SPEED + i * 0.47D);
            float alpha = 0.12F + (float) fadeCurve * 0.28F + pulse * 0.10F;
            int color = SPORE_COLORS[i % SPORE_COLORS.length];

            x += Math.cos(angle + Math.PI * 0.5D) * swirl;
            z += Math.sin(angle + Math.PI * 0.5D) * swirl;

            drawSphereAt(x, y, z, size * 3.3D, color, alpha * 0.14F, 7, 7);
            drawSphereAt(x, y, z, size, i % 9 == 0 ? 0xFFFFFF : color, alpha, 5, 5);

            if ((i % 12) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                RenderEnergyEffectHelper.drawSpark(size * 2.6D, color, alpha * 0.28F);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }
}
