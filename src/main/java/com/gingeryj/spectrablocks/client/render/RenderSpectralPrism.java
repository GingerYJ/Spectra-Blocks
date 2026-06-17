package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileSpectralPrism;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSpectralPrism extends RenderCelestialEffectBase<TileSpectralPrism> {

    private static final double PRISM_RADIUS = 0.58D;
    private static final double PRISM_HEIGHT = 1.42D;
    private static final double INNER_GLOW_RADIUS = 0.72D;
    private static final int PRISM_FACETS = 6;
    private static final int BEAM_COUNT = 12;
    private static final double BEAM_INNER_RADIUS = 0.34D;
    private static final double BEAM_OUTER_RADIUS = 4.42D;
    private static final double BEAM_WIDTH = 0.13D;
    private static final int DUST_COUNT = 96;
    private static final double DUST_RADIUS = 3.85D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float PRISM_ROTATION_SPEED = 0.62F;
    private static final float BEAM_ROTATION_SPEED = 0.055F;

    private static final int[] SPECTRUM_COLORS = new int[]{
            0xFF5A78, 0xFF9B45, 0xFFE86B, 0x8AFF7C, 0x59F3FF, 0x7A82FF, 0xD27CFF
    };

    @Override
    protected void renderCelestialEffect(TileSpectralPrism te, float ticks) {
        drawPrism(ticks);
        drawBeams(ticks);
        drawDust(ticks);
    }

    private void drawPrism(float ticks) {
        float pulse = wave(ticks * 0.050D);

        useAdditiveBlend();
        RenderHelper.drawSphere(INNER_GLOW_RADIUS + pulse * 0.06D, 0xF8FEFF, 0.20F, 18, 18);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * PRISM_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        RenderEnergyEffectHelper.drawFacetedCrystal(PRISM_RADIUS, PRISM_HEIGHT,
                0xC8FFFF, 0xFFB8F4, 0.32F + pulse * 0.06F, PRISM_FACETS);
        GlStateManager.glLineWidth(2.0F);
        RenderEnergyEffectHelper.drawCrystalEdges(PRISM_RADIUS * 1.01D, PRISM_HEIGHT * 1.01D,
                0xFFFFFF, 0.52F, PRISM_FACETS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * 0.22F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(1.5F);
        RenderHelper.drawCircle(1.18D + pulse * 0.04D, 0xFFFFFF, 0.18F, 72);
        RenderHelper.drawCircle(1.46D, 0x82F6FF, 0.11F, 72);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawBeams(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * BEAM_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        int beamCount = RenderQuality.detailCount(BEAM_COUNT, 4);
        for (int i = 0; i < beamCount; i++) {
            double angle = TWO_PI * i / beamCount;
            double tilt = Math.sin(ticks * 0.018D + i * 0.74D) * 0.28D;
            int color = SPECTRUM_COLORS[i % SPECTRUM_COLORS.length];
            float alpha = 0.12F + 0.06F * wave(ticks * 0.040D + i);

            drawBeam(angle, tilt, BEAM_INNER_RADIUS, BEAM_OUTER_RADIUS, BEAM_WIDTH, color, alpha);
            if (!RenderQuality.low() && (i & 1) == 0) {
                drawBeam(angle + 0.055D, -tilt * 0.72D, 0.48D, BEAM_OUTER_RADIUS * 0.82D,
                        BEAM_WIDTH * 0.56D, 0xFFFFFF, alpha * 0.42F);
            }
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawDust(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        RenderHelper.PointBatch points = RenderQuality.low() ? RenderHelper.beginPointBatch(2.0F) : null;
        for (int i = 0; i < DUST_COUNT; i += stride) {
            double band = (i + 0.5D) / DUST_COUNT;
            double angle = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 4) * 0.0005D);
            double radius = 1.20D + Math.pow(band, 0.62D) * DUST_RADIUS;
            double y = Math.sin(i * 0.83D + ticks * 0.024D) * 0.78D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            int color = SPECTRUM_COLORS[(i + (int) (ticks * 0.025F)) % SPECTRUM_COLORS.length];
            float alpha = 0.16F + 0.16F * wave(ticks * 0.055D + i * 0.51D);
            double size = 0.018D + (i % 4) * 0.005D;

            if (points != null) {
                points.add(x, y, z, color, alpha * 1.08F);
            } else {
                drawSphereAt(x, y, z, size, color, alpha, 6, 6);
            }
        }
        if (points != null) {
            points.draw();
        }
        useAlphaBlend();
    }

    private static void drawBeam(double angle, double tilt, double innerRadius, double outerRadius,
                                 double halfWidth, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double sideX = -sin * halfWidth;
        double sideZ = cos * halfWidth;
        double innerY = tilt * innerRadius;
        double outerY = tilt * outerRadius;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(cos * innerRadius - sideX, innerY, sin * innerRadius - sideZ)
                .color(rgb[0], rgb[1], rgb[2], alpha * 0.90F)
                .endVertex();
        buffer.pos(cos * innerRadius + sideX, innerY, sin * innerRadius + sideZ)
                .color(rgb[0], rgb[1], rgb[2], alpha * 0.90F)
                .endVertex();
        buffer.pos(cos * outerRadius - sideX, outerY, sin * outerRadius - sideZ)
                .color(rgb[0], rgb[1], rgb[2], alpha * 0.05F)
                .endVertex();
        buffer.pos(cos * outerRadius + sideX, outerY, sin * outerRadius + sideZ)
                .color(rgb[0], rgb[1], rgb[2], alpha * 0.05F)
                .endVertex();
        tessellator.draw();
    }
}
