package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileSoulVortex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSoulVortex extends RenderCelestialEffectBase<TileSoulVortex> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double VORTEX_HEIGHT = 2.18D;
    private static final double BASE_RADIUS = 1.48D;
    private static final double TOP_RADIUS = 0.38D;
    private static final int SOUL_COUNT = 72;
    private static final int HELIX_COUNT = 4;
    private static final int HELIX_SEGMENTS = 72;
    private static final int RING_SEGMENTS = 112;
    private static final float VORTEX_ROTATION_SPEED = 0.055F;
    private static final float SOUL_RISE_SPEED = 0.012F;
    private static final float CORE_PULSE_SPEED = 0.060F;

    @Override
    protected void renderCelestialEffect(TileSoulVortex te, float ticks) {
        drawBaseWell(ticks);
        drawHelices(ticks);
        drawSouls(ticks);
        drawCenterColumn(ticks);
    }

    private void drawBaseWell(float ticks) {
        float pulse = wave(ticks * 0.050F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.04D, 0.0D);
        RenderCelestialEffectBase.drawFlatRing(0.16D, BASE_RADIUS + pulse * 0.06D,
                0x095C63, 0.12F + pulse * 0.050F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.6F);
        RenderHelper.drawCircle(BASE_RADIUS, 0x6AFFD2, 0.23F + pulse * 0.12F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawHelices(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < HELIX_COUNT; i++) {
            double phase = TWO_PI * i / HELIX_COUNT + ticks * VORTEX_ROTATION_SPEED;
            int color = i % 2 == 0 ? 0x51FFD0 : 0x8CFBFF;
            GlStateManager.glLineWidth(i % 2 == 0 ? 2.2F : 1.4F);
            drawHelix(phase, color, 0.24F);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawSouls(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SOUL_COUNT; i++) {
            double progress = fract(ticks * SOUL_RISE_SPEED + i * 0.023D);
            double angle = i * 2.399963229728653D + progress * TWO_PI * 1.85D + ticks * VORTEX_ROTATION_SPEED;
            double radius = lerp(BASE_RADIUS, TOP_RADIUS, progress) + Math.sin(ticks * 0.034D + i) * 0.045D;
            double height = progress * VORTEX_HEIGHT - 0.08D;
            double bob = Math.sin(ticks * 0.070D + i * 0.73D) * 0.035D;
            double size = 0.026D + Math.sin(progress * Math.PI) * 0.032D;
            float fade = 0.28F + (float) Math.sin(Math.PI * progress) * 0.50F;
            int color = i % 6 == 0 ? 0xD6FFF5 : (i % 2 == 0 ? 0x55FFC8 : 0x46D6FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height + bob, Math.sin(angle) * radius);
            RenderHelper.drawSphere(size, color, fade, 6, 6);
            if (i % 9 == 0) {
                RenderEnergyEffectHelper.drawSpark(size * 2.6D, color, fade * 0.55F);
            }
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawCenterColumn(float ticks) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        RenderHelper.drawSphere(0.54D + pulse * 0.08D, 0x1DE6C2, 0.12F + pulse * 0.055F, 20, 20);
        drawSphereAt(0.0D, 1.12D, 0.0D, 0.28D + pulse * 0.035D, 0xBFFFF2, 0.22F + pulse * 0.12F, 16, 16);
        GlStateManager.glLineWidth(1.3F);
        RenderCelestialEffectBase.drawLatitudeCircle(1.02D, -0.22D + pulse * 0.06D, 0x4DFFD0, 0.20F, RING_SEGMENTS);
        RenderCelestialEffectBase.drawLatitudeCircle(0.82D, 0.34D, 0x8CFBFF, 0.16F + pulse * 0.08F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawHelix(double phase, int color, float alpha) {
        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= HELIX_SEGMENTS; i++) {
            double progress = (double) i / HELIX_SEGMENTS;
            double angle = phase + progress * TWO_PI * 1.85D;
            double radius = lerp(BASE_RADIUS, TOP_RADIUS, progress);
            double height = progress * VORTEX_HEIGHT - 0.08D;
            float pointAlpha = alpha * (0.20F + 0.80F * (float) Math.sin(Math.PI * progress));
            buffer.pos(Math.cos(angle) * radius, height, Math.sin(angle) * radius)
                    .color(rgb[0], rgb[1], rgb[2], pointAlpha)
                    .endVertex();
        }
        tessellator.draw();
    }
}
