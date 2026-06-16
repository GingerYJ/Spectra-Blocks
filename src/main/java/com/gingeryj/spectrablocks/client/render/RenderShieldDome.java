package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileShieldDome;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderShieldDome extends RenderCelestialEffectBase<TileShieldDome> {

    private static final double DOME_RADIUS = 2.18D;
    private static final double INNER_RADIUS = 1.96D;
    private static final double DOME_BASE_Y = -0.42D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int DOME_LAT_SEGMENTS = 18;
    private static final int DOME_LON_SEGMENTS = 42;
    private static final int SCAN_LINE_COUNT = 7;
    private static final int RIB_COUNT = 12;
    private static final int RIB_SEGMENTS = 48;
    private static final int GLINT_COUNT = 18;
    private static final int CIRCLE_SEGMENTS = 112;
    private static final float PULSE_SPEED = 0.030F;
    private static final float SHELL_SHIMMER_SPEED = 0.026F;
    private static final float SCAN_SPEED = 0.006F;
    private static final float RIB_ROTATION_SPEED = 0.055F;
    private static final float GLINT_SPEED = 0.082F;
    private static final float OUTER_SHELL_ALPHA = 0.125F;
    private static final float INNER_SHELL_ALPHA = 0.060F;
    private static final float SCAN_ALPHA = 0.38F;
    private static final float RIB_ALPHA = 0.30F;
    private static final float RIM_ALPHA = 0.48F;
    private static final float GLINT_ALPHA = 0.62F;
    private static final int SHIELD_COLOR = 0x5DEBFF;
    private static final int INNER_COLOR = 0xB8FFF8;
    private static final int EDGE_COLOR = 0xFFFFFF;
    private static final int WARNING_COLOR = 0x91C7FF;

    @Override
    protected void renderCelestialEffect(TileShieldDome te, float ticks) {
        drawDomeShells(ticks);
        drawRotatingRibs(ticks);
        drawScanLines(ticks);
        drawPulseRim(ticks);
        drawEdgeGlints(ticks);
    }

    private void drawDomeShells(float ticks) {
        float pulse = wave(ticks * PULSE_SPEED);

        useAlphaBlend();
        drawDomeSurface(DOME_RADIUS + pulse * 0.035D, SHIELD_COLOR,
                OUTER_SHELL_ALPHA + pulse * 0.025F, ticks, 0.0D);
        drawDomeSurface(INNER_RADIUS - pulse * 0.018D, INNER_COLOR,
                INNER_SHELL_ALPHA, ticks, 1.7D);
    }

    private void drawRotatingRibs(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.25F);
        for (int i = 0; i < RIB_COUNT; i++) {
            double phi = TWO_PI * i / RIB_COUNT + ticks * RIB_ROTATION_SPEED * 0.01D;
            float alpha = RIB_ALPHA * (0.62F + 0.38F * wave(ticks * 0.025D + i * 0.58D));
            drawDomeRib(phi, SHIELD_COLOR, alpha);
        }
        RenderHelper.resetLineWidth();
    }

    private void drawScanLines(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SCAN_LINE_COUNT; i++) {
            double progress = fract(ticks * SCAN_SPEED + (double) i / SCAN_LINE_COUNT);
            double theta = 0.08D + progress * (Math.PI * 0.5D - 0.11D);
            double y = DOME_BASE_Y + Math.cos(theta) * DOME_RADIUS;
            double radius = Math.sin(theta) * (DOME_RADIUS + 0.018D);
            float fade = (float) Math.sin(progress * Math.PI);
            float alpha = SCAN_ALPHA * (0.20F + 0.80F * fade);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.glLineWidth(2.2F);
            RenderHelper.drawCircle(radius, EDGE_COLOR, alpha * 0.46F, CIRCLE_SEGMENTS);
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.drawCircle(radius * 0.996D, SHIELD_COLOR, alpha, CIRCLE_SEGMENTS);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
    }

    private void drawPulseRim(float ticks) {
        float pulse = wave(ticks * PULSE_SPEED + 1.2D);
        double rimRadius = DOME_RADIUS + pulse * 0.065D;

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, DOME_BASE_Y, 0.0D);
        GlStateManager.glLineWidth(3.0F);
        RenderHelper.drawCircle(rimRadius, EDGE_COLOR, RIM_ALPHA * (0.38F + pulse * 0.62F), CIRCLE_SEGMENTS);
        GlStateManager.glLineWidth(1.2F);
        RenderHelper.drawCircle(DOME_RADIUS * 0.88D, WARNING_COLOR, RIM_ALPHA * 0.36F, CIRCLE_SEGMENTS);
        drawFlatRing(DOME_RADIUS * 0.88D, rimRadius + 0.10D, SHIELD_COLOR, 0.050F + pulse * 0.035F, CIRCLE_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawEdgeGlints(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < GLINT_COUNT; i++) {
            double phase = ticks * GLINT_SPEED + i * 0.81D;
            double blink = Math.max(0.0D, Math.sin(phase));
            if (blink < 0.68D) {
                continue;
            }

            double angle = TWO_PI * i / GLINT_COUNT + Math.sin(ticks * 0.012D + i) * 0.08D;
            double radius = DOME_RADIUS + 0.035D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double lift = 0.14D + blink * 0.16D;
            float alpha = GLINT_ALPHA * (float) ((blink - 0.68D) / 0.32D);

            GlStateManager.glLineWidth(1.4F);
            RenderHelper.drawLine(x * 0.985D, DOME_BASE_Y + 0.03D, z * 0.985D,
                    x * 1.015D, DOME_BASE_Y + lift, z * 1.015D, EDGE_COLOR, alpha);
            RenderHelper.resetLineWidth();
        }
    }

    private void drawDomeSurface(double radius, int color, float alpha, float ticks, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int lat = 0; lat < DOME_LAT_SEGMENTS; lat++) {
            double theta0 = (Math.PI * 0.5D) * lat / DOME_LAT_SEGMENTS;
            double theta1 = (Math.PI * 0.5D) * (lat + 1) / DOME_LAT_SEGMENTS;
            for (int lon = 0; lon < DOME_LON_SEGMENTS; lon++) {
                double phi0 = TWO_PI * lon / DOME_LON_SEGMENTS;
                double phi1 = TWO_PI * (lon + 1) / DOME_LON_SEGMENTS;
                addDomeVertex(buffer, radius, theta0, phi0, rgb, alpha, ticks, phase);
                addDomeVertex(buffer, radius, theta1, phi0, rgb, alpha, ticks, phase);
                addDomeVertex(buffer, radius, theta0, phi1, rgb, alpha, ticks, phase);
                addDomeVertex(buffer, radius, theta0, phi1, rgb, alpha, ticks, phase);
                addDomeVertex(buffer, radius, theta1, phi0, rgb, alpha, ticks, phase);
                addDomeVertex(buffer, radius, theta1, phi1, rgb, alpha, ticks, phase);
            }
        }
        tessellator.draw();
    }

    private void drawDomeRib(double phi, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= RIB_SEGMENTS; i++) {
            double theta = (Math.PI * 0.5D) * i / RIB_SEGMENTS;
            double horizontal = Math.sin(theta) * DOME_RADIUS;
            double y = DOME_BASE_Y + Math.cos(theta) * DOME_RADIUS;
            float fade = (float) (0.22D + 0.78D * Math.sin(theta));
            buffer.pos(Math.cos(phi) * horizontal, y, Math.sin(phi) * horizontal)
                    .color(rgb[0], rgb[1], rgb[2], alpha * fade)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void addDomeVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                      float[] rgb, float alpha, float ticks, double phase) {
        double horizontal = Math.sin(theta) * radius;
        double x = Math.cos(phi) * horizontal;
        double y = DOME_BASE_Y + Math.cos(theta) * radius;
        double z = Math.sin(phi) * horizontal;
        double edgeFade = 0.34D + 0.66D * Math.sin(theta);
        double shimmer = 0.82D + 0.18D * Math.sin(phi * 6.0D + theta * 7.0D - ticks * SHELL_SHIMMER_SPEED + phase);

        buffer.pos(x, y, z)
                .color(rgb[0], rgb[1], rgb[2], alpha * (float) (edgeFade * shimmer))
                .endVertex();
    }
}
