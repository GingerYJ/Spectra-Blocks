package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderHelper {

    private static final int MAX_SPHERE_SEGMENTS = 24;
    private static final int MAX_TEXTURED_SPHERE_SEGMENTS = 28;
    private static final int MAX_CIRCLE_SEGMENTS = 96;
    private static final int MAX_WIREFRAME_SEGMENTS = 16;

    private RenderHelper() {
    }

    public static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }

    public static void drawSphere(double radius, int color, float alpha, int latSegs, int lonSegs) {
        if (alpha <= 0.01F) {
            return;
        }

        latSegs = RenderQuality.scaleSegments(latSegs, 4, MAX_SPHERE_SEGMENTS);
        lonSegs = RenderQuality.scaleSegments(lonSegs, 4, MAX_SPHERE_SEGMENTS);
        alpha *= RenderQuality.alphaMultiplier();
        float[] rgb = unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            double y0 = radius * Math.cos(theta0);
            double y1 = radius * Math.cos(theta1);
            double h0 = radius * Math.sin(theta0);
            double h1 = radius * Math.sin(theta1);
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = 2.0D * Math.PI * lon / lonSegs;
                double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                double cos0 = Math.cos(phi0);
                double sin0 = Math.sin(phi0);
                double cos1 = Math.cos(phi1);
                double sin1 = Math.sin(phi1);

                buffer.pos(h0 * cos0, y0, h0 * sin0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
                buffer.pos(h1 * cos0, y1, h1 * sin0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
                buffer.pos(h0 * cos1, y0, h0 * sin1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();

                buffer.pos(h0 * cos1, y0, h0 * sin1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
                buffer.pos(h1 * cos0, y1, h1 * sin0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
                buffer.pos(h1 * cos1, y1, h1 * sin1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            }
        }
        tessellator.draw();
    }

    public static void drawWireframeSphere(double radius, int color, float alpha, int gridLat, int gridLon) {
        if (alpha <= 0.01F) {
            return;
        }

        gridLat = RenderQuality.scaleSegments(gridLat, 4, MAX_WIREFRAME_SEGMENTS);
        gridLon = RenderQuality.scaleSegments(gridLon, 6, MAX_WIREFRAME_SEGMENTS);
        alpha *= RenderQuality.alphaMultiplier();
        float[] rgb = unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int lat = 1; lat < gridLat; lat++) {
            double theta = Math.PI * lat / gridLat;
            double y = radius * Math.cos(theta);
            double horizontalRadius = radius * Math.sin(theta);
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int lon = 0; lon <= gridLon; lon++) {
                double phi = 2.0D * Math.PI * lon / gridLon;
                buffer.pos(horizontalRadius * Math.cos(phi), y, horizontalRadius * Math.sin(phi))
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
            }
            tessellator.draw();
        }

        for (int lon = 0; lon < gridLon; lon++) {
            double phi = 2.0D * Math.PI * lon / gridLon;
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int lat = 0; lat <= gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                buffer.pos(radius * Math.sin(theta) * Math.cos(phi),
                                radius * Math.cos(theta),
                                radius * Math.sin(theta) * Math.sin(phi))
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
            }
            tessellator.draw();
        }
    }

    public static void drawCircle(double radius, int color, float alpha, int segments) {
        if (alpha <= 0.01F) {
            return;
        }

        segments = RenderQuality.scaleSegments(segments, 8, MAX_CIRCLE_SEGMENTS);
        alpha *= RenderQuality.alphaMultiplier();
        float[] rgb = unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            double angle = 2.0D * Math.PI * i / segments;
            buffer.pos(radius * Math.cos(angle), 0.0D, radius * Math.sin(angle))
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    public static void drawTexturedSphere(double radius, ResourceLocation texture, float alpha, int latSegs, int lonSegs) {
        if (alpha <= 0.01F) {
            return;
        }

        latSegs = RenderQuality.scaleSegments(latSegs, 8, MAX_TEXTURED_SPHERE_SEGMENTS);
        lonSegs = RenderQuality.scaleSegments(lonSegs, 8, MAX_TEXTURED_SPHERE_SEGMENTS);
        alpha *= RenderQuality.alphaMultiplier();
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.enableCull();
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            double y0 = radius * Math.cos(theta0);
            double y1 = radius * Math.cos(theta1);
            double h0 = radius * Math.sin(theta0);
            double h1 = radius * Math.sin(theta1);
            double v0 = (double) lat / latSegs;
            double v1 = (double) (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = 2.0D * Math.PI * lon / lonSegs;
                double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                double u0 = (double) lon / lonSegs;
                double u1 = (double) (lon + 1) / lonSegs;
                double cos0 = Math.cos(phi0);
                double sin0 = Math.sin(phi0);
                double cos1 = Math.cos(phi1);
                double sin1 = Math.sin(phi1);

                buffer.pos(h0 * cos0, y0, h0 * sin0).tex(u0, v0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.pos(h0 * cos1, y0, h0 * sin1).tex(u1, v0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.pos(h1 * cos0, y1, h1 * sin0).tex(u0, v1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();

                buffer.pos(h0 * cos1, y0, h0 * sin1).tex(u1, v0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.pos(h1 * cos1, y1, h1 * sin1).tex(u1, v1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.pos(h1 * cos0, y1, h1 * sin0).tex(u0, v1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            }
        }
        tessellator.draw();
        if (!cullWasEnabled) {
            GlStateManager.disableCull();
        }
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2,
                                int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        alpha *= RenderQuality.alphaMultiplier();
        float[] rgb = unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x2, y2, z2).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        tessellator.draw();
    }

    public static void resetLineWidth() {
        GlStateManager.glLineWidth(1.0F);
    }
}
