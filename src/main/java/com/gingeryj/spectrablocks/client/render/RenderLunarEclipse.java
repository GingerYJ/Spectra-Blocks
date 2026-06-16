package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileLunarEclipse;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderLunarEclipse extends RenderCelestialEffectBase<TileLunarEclipse> {

    private static final double MOON_RADIUS = 0.82D;
    private static final double SHADOW_RADIUS = 0.92D;
    private static final double SHADOW_TRAVEL = 0.86D;
    private static final double FRONT_PLANE_Z = 0.66D;
    private static final int DISC_SEGMENTS = 112;
    private static final int DUST_COUNT = 36;
    private static final float SHADOW_SPEED = 0.014F;
    private static final float RING_PULSE_SPEED = 0.052F;

    @Override
    protected void renderCelestialEffect(TileLunarEclipse te, float ticks) {
        drawMoonSurface(ticks);
        drawEclipseShadow(ticks);
        drawCopperRing(ticks);
        drawCopperDust(ticks);
    }

    private void drawMoonSurface(float ticks) {
        float pulse = wave(ticks * 0.025D);

        useAlphaBlend();
        RenderHelper.drawSphere(MOON_RADIUS, 0xD8D3C6, 0.58F, 28, 28);
        RenderHelper.drawSphere(MOON_RADIUS * 1.018D, 0xF2EBD9, 0.12F + pulse * 0.05F, 28, 28);

        drawFrontRing(-0.33D, 0.22D, FRONT_PLANE_Z + 0.006D,
                0.055D, 0.090D, 0x918A80, 0.16F, 28);
        drawFrontRing(0.24D, -0.18D, FRONT_PLANE_Z + 0.008D,
                0.070D, 0.115D, 0x7D766F, 0.14F, 28);
        drawFrontRing(0.14D, 0.36D, FRONT_PLANE_Z + 0.010D,
                0.045D, 0.074D, 0xB7B0A6, 0.12F, 24);
        drawFrontRing(-0.06D, -0.40D, FRONT_PLANE_Z + 0.012D,
                0.036D, 0.064D, 0x888078, 0.12F, 24);
    }

    private void drawEclipseShadow(float ticks) {
        double shadowOffset = Math.sin(ticks * SHADOW_SPEED) * SHADOW_TRAVEL;
        double overlap = 1.0D - Math.min(1.0D, Math.abs(shadowOffset) / SHADOW_TRAVEL);
        float shadowAlpha = 0.45F + (float) overlap * 0.26F;

        useAlphaBlend();
        drawFrontDisc(shadowOffset, 0.0D, FRONT_PLANE_Z + 0.030D,
                SHADOW_RADIUS, 0x13080A, shadowAlpha, DISC_SEGMENTS);
        drawFrontDisc(shadowOffset * 0.98D, 0.0D, FRONT_PLANE_Z + 0.034D,
                SHADOW_RADIUS * 0.62D, 0x030203, 0.23F + (float) overlap * 0.18F, DISC_SEGMENTS);
    }

    private void drawCopperRing(float ticks) {
        double shadowOffset = Math.sin(ticks * SHADOW_SPEED) * SHADOW_TRAVEL;
        double overlap = 1.0D - Math.min(1.0D, Math.abs(shadowOffset) / SHADOW_TRAVEL);
        float pulse = wave(ticks * RING_PULSE_SPEED);

        useAdditiveBlend();
        drawFrontRing(0.0D, 0.0D, FRONT_PLANE_Z + 0.052D,
                MOON_RADIUS * 0.96D, MOON_RADIUS * 1.11D,
                0xB85F32, 0.16F + pulse * 0.045F + (float) overlap * 0.10F, DISC_SEGMENTS);
        drawFrontRing(0.0D, 0.0D, FRONT_PLANE_Z + 0.060D,
                MOON_RADIUS * 1.10D, MOON_RADIUS * 1.23D,
                0xFFB66A, 0.065F + pulse * 0.035F + (float) overlap * 0.045F, DISC_SEGMENTS);
        GlStateManager.glLineWidth(1.6F);
        drawFrontCircle(0.0D, 0.0D, FRONT_PLANE_Z + 0.070D,
                MOON_RADIUS * 1.045D, 0xFFD0A2, 0.22F + (float) overlap * 0.15F, DISC_SEGMENTS);
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawCopperDust(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < DUST_COUNT; i++) {
            double progress = (i + 0.5D) / DUST_COUNT;
            double angle = progress * Math.PI * 2.0D + ticks * (0.010D + (i % 5) * 0.001D);
            double radius = MOON_RADIUS * (1.12D + (i % 7) * 0.026D);
            double wobble = Math.sin(ticks * 0.032D + i * 1.7D) * 0.030D;
            double x = Math.cos(angle) * (radius + wobble);
            double y = Math.sin(angle) * (radius - wobble * 0.4D);
            double z = FRONT_PLANE_Z + 0.085D + Math.sin(ticks * 0.019D + i) * 0.020D;
            float alpha = 0.10F + 0.15F * wave(ticks * 0.041D + i);
            int color = i % 4 == 0 ? 0xFFD9A7 : 0xC66B38;

            drawSphereAt(x, y, z, 0.014D + (i % 3) * 0.004D, color, alpha, 6, 6);
        }
        useAlphaBlend();
    }

    private static void drawFrontDisc(double x, double y, double z, double radius,
                                      int color, float alpha, int segments) {
        if (alpha <= 0.01F || radius <= 0.0D) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2.0D * i / segments;
            buffer.pos(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void drawFrontRing(double x, double y, double z, double innerRadius, double outerRadius,
                                      int color, float alpha, int segments) {
        if (alpha <= 0.01F || innerRadius <= 0.0D || outerRadius <= innerRadius) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2.0D * i / segments;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            buffer.pos(x + cos * outerRadius, y + sin * outerRadius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
            buffer.pos(x + cos * innerRadius, y + sin * innerRadius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void drawFrontCircle(double x, double y, double z, double radius,
                                        int color, float alpha, int segments) {
        if (alpha <= 0.01F || radius <= 0.0D) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            double angle = Math.PI * 2.0D * i / segments;
            buffer.pos(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }
}
