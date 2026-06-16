package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileCollapsingStar;
import net.minecraft.client.renderer.GlStateManager;

public class RenderCollapsingStar extends RenderCelestialEffectBase<TileCollapsingStar> {

    private static final double CORE_RADIUS = 0.88D;
    private static final double RING_INNER_RADIUS = 1.10D;
    private static final double RING_OUTER_RADIUS = 3.35D;
    private static final int RING_SEGMENTS = 192;
    private static final int INFALL_PARTICLE_COUNT = 68;
    private static final int FLASH_PERIOD = 230;
    private static final int FLASH_LENGTH = 18;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileCollapsingStar te, float ticks) {
        drawAccretionRing(ticks);
        drawCore(ticks);
        drawInfallParticles(ticks);
        drawFlash(ticks);
    }

    private void drawCore(float ticks) {
        float pulse = wave(ticks * 0.055D);

        useAlphaBlend();
        RenderHelper.drawSphere(CORE_RADIUS * (1.00D + pulse * 0.035D), 0x000000, 0.94F, 26, 26);
        useAdditiveBlend();
        RenderHelper.drawSphere(CORE_RADIUS * (1.45D + pulse * 0.10D), 0xFFB24A, 0.105F, 26, 26);
        RenderHelper.drawWireframeSphere(CORE_RADIUS * (1.62D + pulse * 0.08D), 0xFFE0A0, 0.145F, 8, 16);
        useAlphaBlend();
    }

    private void drawAccretionRing(float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(67.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.185F, 0.0F, 1.0F, 0.0F);

        useAdditiveBlend();
        drawFlatRing(RING_INNER_RADIUS, RING_OUTER_RADIUS, 0xFF7A2E, 0.135F, RING_SEGMENTS);
        drawFlatRing(1.48D, 2.70D, 0xFFE5A3, 0.165F, RING_SEGMENTS);
        drawSpiralRibbon(1.04D, 3.18D, ticks * 0.010D,
                Math.PI * 1.65D, 0.18D, 0xFFB34F, 0.185F, 96);
        drawSpiralRibbon(1.20D, 3.32D, Math.PI + ticks * 0.012D,
                Math.PI * 1.48D, 0.13D, 0xFFFFFF, 0.110F, 96);

        GlStateManager.glLineWidth(3.0F);
        RenderHelper.drawCircle(RING_OUTER_RADIUS * 1.012D, 0xFF9B42, 0.090F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.4F);
        RenderHelper.drawCircle(RING_INNER_RADIUS * 0.98D, 0xFFECC8, 0.190F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawInfallParticles(float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(67.0F, 1.0F, 0.0F, 0.0F);
        useAdditiveBlend();

        for (int i = 0; i < INFALL_PARTICLE_COUNT; i++) {
            double progress = fract(i * 0.071D + ticks * 0.0105D);
            double radius = lerp(RING_OUTER_RADIUS * 0.98D, CORE_RADIUS * 0.72D, progress);
            double angle = i * GOLDEN_ANGLE - progress * 4.70D + ticks * 0.010D;
            double tailProgress = Math.max(0.0D, progress - 0.045D);
            double tailRadius = lerp(RING_OUTER_RADIUS * 0.98D, CORE_RADIUS * 0.72D, tailProgress);
            double tailAngle = i * GOLDEN_ANGLE - tailProgress * 4.70D + ticks * 0.010D;
            double y = Math.sin(i * 1.13D + ticks * 0.038D) * 0.060D;
            double headX = Math.cos(angle) * radius;
            double headZ = Math.sin(angle) * radius;
            double tailX = Math.cos(tailAngle) * tailRadius;
            double tailZ = Math.sin(tailAngle) * tailRadius;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD27D : 0xFF6F38);

            GlStateManager.glLineWidth(1.6F);
            RenderHelper.drawLine(headX, y, headZ, tailX, y * 0.65D, tailZ,
                    color, 0.14F * fade);
            drawSphereAt(headX, y, headZ, 0.028D + 0.050D * (1.0D - progress),
                    color, 0.42F * fade, 6, 6);
        }

        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFlash(float ticks) {
        int cycle = Math.floorMod((int) ticks, FLASH_PERIOD);
        if (cycle >= FLASH_LENGTH) {
            return;
        }

        float progress = cycle / (float) FLASH_LENGTH;
        float fade = (float) Math.sin(Math.PI * progress);

        useAdditiveBlend();
        RenderHelper.drawSphere(1.25D + progress * 2.35D, 0xFFFFFF, 0.24F * fade, 28, 28);
        RenderHelper.drawSphere(1.80D + progress * 1.70D, 0xFFC15D, 0.18F * fade, 24, 24);
        GlStateManager.glLineWidth(4.0F);
        RenderHelper.drawWireframeSphere(1.55D + progress * 2.20D, 0xFFF1C4, 0.16F * fade, 8, 14);
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }
}
