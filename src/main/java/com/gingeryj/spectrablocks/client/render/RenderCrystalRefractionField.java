package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileCrystalRefractionField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderCrystalRefractionField extends RenderCelestialEffectBase<TileCrystalRefractionField> {

    private static final int PLANE_COUNT = 9;
    private static final int EDGE_COUNT = 7;
    private static final double INNER_RADIUS = 0.86D;
    private static final double OUTER_RADIUS = 3.42D;
    private static final double PLANE_HEIGHT = 1.86D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final float PLANE_ROTATION_SPEED = 0.090F;
    private static final int[] PLANE_COLORS = new int[]{
            0xD8FFFF, 0x9EF5FF, 0xC2B9FF, 0xFFE2F8
    };

    @Override
    protected void renderCelestialEffect(TileCrystalRefractionField te, float ticks) {
        drawCore(ticks);
        drawPlanes(ticks);
        drawFacetEdges(ticks);
        drawCaustics(ticks);
    }

    private void drawCore(float ticks) {
        float pulse = wave(ticks * 0.044D);

        useAdditiveBlend();
        RenderHelper.drawSphere(0.54D + pulse * 0.04D, 0xFFFFFF, 0.24F, 18, 18);
        RenderHelper.drawSphere(0.96D + pulse * 0.07D, 0xBDFBFF, 0.13F, 18, 18);
        useAlphaBlend();
    }

    private void drawPlanes(float ticks) {
        useAlphaBlend();
        for (int i = 0; i < PLANE_COUNT; i++) {
            double angle = TWO_PI * i / PLANE_COUNT + ticks * PLANE_ROTATION_SPEED * (i % 2 == 0 ? 1.0D : -0.65D);
            double radius = INNER_RADIUS + (i % 3) * 0.24D;
            double farRadius = OUTER_RADIUS - (i % 4) * 0.18D;
            double yShift = Math.sin(ticks * 0.020D + i) * 0.18D;
            double tilt = -0.62D + (i % 5) * 0.31D;
            int color = PLANE_COLORS[i % PLANE_COLORS.length];
            float alpha = 0.090F + 0.052F * wave(ticks * 0.036D + i * 0.61D);

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) Math.toDegrees(tilt), 0.0F, 0.0F, 1.0F);
            drawFacetPlane(radius, farRadius, PLANE_HEIGHT, yShift, 0.34D + (i % 3) * 0.08D, color, alpha);
            GlStateManager.popMatrix();
        }
    }

    private void drawFacetEdges(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < EDGE_COUNT; i++) {
            double angle = TWO_PI * i / EDGE_COUNT - ticks * 0.012D;
            double y0 = -1.30D + (i % 3) * 0.38D;
            double y1 = 1.30D - (i % 2) * 0.34D;
            double radius = 2.04D + (i % 4) * 0.29D;
            int color = PLANE_COLORS[(i + 1) % PLANE_COLORS.length];
            float alpha = 0.18F + 0.08F * wave(ticks * 0.050D + i);

            GlStateManager.glLineWidth(2.1F);
            RenderHelper.drawLine(Math.cos(angle) * radius, y0, Math.sin(angle) * radius,
                    Math.cos(angle + 0.42D) * (radius * 0.66D), y1,
                    Math.sin(angle + 0.42D) * (radius * 0.66D), color, alpha);
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.drawLine(Math.cos(angle) * radius, y0, Math.sin(angle) * radius,
                    Math.cos(angle + 0.42D) * (radius * 0.66D), y1,
                    Math.sin(angle + 0.42D) * (radius * 0.66D), 0xFFFFFF, alpha * 0.52F);
            RenderHelper.resetLineWidth();
        }
        useAlphaBlend();
    }

    private void drawCaustics(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.032F, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < 4; i++) {
            double radius = 1.25D + i * 0.46D;
            float alpha = 0.075F + 0.035F * wave(ticks * 0.042D + i);
            GlStateManager.glLineWidth(1.0F + i * 0.25F);
            RenderHelper.drawCircle(radius, PLANE_COLORS[i % PLANE_COLORS.length], alpha, 80);
        }
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private static void drawFacetPlane(double innerRadius, double outerRadius, double height,
                                       double yShift, double halfWidth, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        addVertex(buffer, innerRadius, yShift - height * 0.30D, -halfWidth, rgb, alpha * 0.48F);
        addVertex(buffer, outerRadius, yShift + height * 0.22D, 0.0D, rgb, alpha);
        addVertex(buffer, innerRadius, yShift + height * 0.48D, halfWidth, rgb, alpha * 0.56F);
        addVertex(buffer, -innerRadius * 0.26D, yShift + height * 0.18D, halfWidth * 0.52D, rgb, alpha * 0.32F);
        addVertex(buffer, innerRadius, yShift - height * 0.30D, -halfWidth, rgb, alpha * 0.45F);
        addVertex(buffer, innerRadius, yShift + height * 0.48D, halfWidth, rgb, alpha * 0.52F);
        tessellator.draw();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
