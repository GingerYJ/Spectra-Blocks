package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public final class RenderHelper {

    private static final int MAX_SPHERE_SEGMENTS = 24;
    private static final int MAX_TEXTURED_SPHERE_SEGMENTS = 28;
    private static final int MAX_CIRCLE_SEGMENTS = 96;
    private static final int MAX_WIREFRAME_SEGMENTS = 16;
    private static final Map<Integer, float[]> COLOR_CACHE = new HashMap<Integer, float[]>();
    private static final Map<Integer, CircleTable> CIRCLE_TABLES = new HashMap<Integer, CircleTable>();
    private static final Map<Integer, SphereTable> SPHERE_TABLES = new HashMap<Integer, SphereTable>();

    private RenderHelper() {
    }

    public static float[] unpackRGB(int color) {
        float[] rgb = COLOR_CACHE.get(color);
        if (rgb == null) {
            rgb = new float[]{
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F
            };
            COLOR_CACHE.put(color, rgb);
        }
        return rgb;
    }

    public static void drawSphere(double radius, int color, float alpha, int latSegs, int lonSegs) {
        if (alpha <= 0.01F) {
            return;
        }

        latSegs = RenderQuality.scaleSegments(latSegs, 4, MAX_SPHERE_SEGMENTS);
        lonSegs = RenderQuality.scaleSegments(lonSegs, 4, MAX_SPHERE_SEGMENTS);
        alpha *= RenderQuality.alphaMultiplier();
        float[] rgb = unpackRGB(color);
        SphereTable table = sphereTable(latSegs, lonSegs);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int lat = 0; lat < latSegs; lat++) {
            double y0 = radius * table.latCos[lat];
            double y1 = radius * table.latCos[lat + 1];
            double h0 = radius * table.latSin[lat];
            double h1 = radius * table.latSin[lat + 1];
            for (int lon = 0; lon < lonSegs; lon++) {
                double cos0 = table.lonCos[lon];
                double sin0 = table.lonSin[lon];
                double cos1 = table.lonCos[lon + 1];
                double sin1 = table.lonSin[lon + 1];

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
        SphereTable table = sphereTable(gridLat, gridLon);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int lat = 1; lat < gridLat; lat++) {
            double y = radius * table.latCos[lat];
            double horizontalRadius = radius * table.latSin[lat];
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int lon = 0; lon <= gridLon; lon++) {
                buffer.pos(horizontalRadius * table.lonCos[lon], y, horizontalRadius * table.lonSin[lon])
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
            }
            tessellator.draw();
        }

        for (int lon = 0; lon < gridLon; lon++) {
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int lat = 0; lat <= gridLat; lat++) {
                buffer.pos(radius * table.latSin[lat] * table.lonCos[lon],
                                radius * table.latCos[lat],
                                radius * table.latSin[lat] * table.lonSin[lon])
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
        CircleTable table = circleTable(segments);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            buffer.pos(radius * table.cos[i], 0.0D, radius * table.sin[i])
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
        SphereTable table = sphereTable(latSegs, lonSegs);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int lat = 0; lat < latSegs; lat++) {
            double y0 = radius * table.latCos[lat];
            double y1 = radius * table.latCos[lat + 1];
            double h0 = radius * table.latSin[lat];
            double h1 = radius * table.latSin[lat + 1];
            double v0 = (double) lat / latSegs;
            double v1 = (double) (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double u0 = (double) lon / lonSegs;
                double u1 = (double) (lon + 1) / lonSegs;
                double cos0 = table.lonCos[lon];
                double sin0 = table.lonSin[lon];
                double cos1 = table.lonCos[lon + 1];
                double sin1 = table.lonSin[lon + 1];

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

    private static CircleTable circleTable(int segments) {
        CircleTable table = CIRCLE_TABLES.get(segments);
        if (table == null) {
            table = new CircleTable(segments);
            CIRCLE_TABLES.put(segments, table);
        }
        return table;
    }

    private static SphereTable sphereTable(int latSegs, int lonSegs) {
        int key = (latSegs << 16) | lonSegs;
        SphereTable table = SPHERE_TABLES.get(key);
        if (table == null) {
            table = new SphereTable(latSegs, lonSegs);
            SPHERE_TABLES.put(key, table);
        }
        return table;
    }

    private static final class CircleTable {
        private final double[] cos;
        private final double[] sin;

        private CircleTable(int segments) {
            this.cos = new double[segments + 1];
            this.sin = new double[segments + 1];
            for (int i = 0; i <= segments; i++) {
                double angle = 2.0D * Math.PI * i / segments;
                cos[i] = Math.cos(angle);
                sin[i] = Math.sin(angle);
            }
        }
    }

    private static final class SphereTable {
        private final double[] latCos;
        private final double[] latSin;
        private final double[] lonCos;
        private final double[] lonSin;

        private SphereTable(int latSegs, int lonSegs) {
            this.latCos = new double[latSegs + 1];
            this.latSin = new double[latSegs + 1];
            this.lonCos = new double[lonSegs + 1];
            this.lonSin = new double[lonSegs + 1];
            for (int lat = 0; lat <= latSegs; lat++) {
                double theta = Math.PI * lat / latSegs;
                latCos[lat] = Math.cos(theta);
                latSin[lat] = Math.sin(theta);
            }
            for (int lon = 0; lon <= lonSegs; lon++) {
                double phi = 2.0D * Math.PI * lon / lonSegs;
                lonCos[lon] = Math.cos(phi);
                lonSin[lon] = Math.sin(phi);
            }
        }
    }
}
