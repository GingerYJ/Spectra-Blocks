package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class RenderHelper {

    private RenderHelper() {
    }

    public static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }

    public static void resetLineWidth() {
        GlStateManager.glLineWidth(1.0F);
    }

    public static void drawTexturedLine(double x0, double y0, double z0,
                                        double x1, double y1, double z1,
                                        double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addTexturedLine(buffer, x0, y0, z0, x1, y1, z1, width);
        tessellator.draw();
    }

    public static void drawTexturedCircle(double radius, int segments, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            addTexturedRingSegment(buffer, radius, width, u0, u1);
        }
        tessellator.draw();
    }

    public static void drawColorLine(double x0, double y0, double z0,
                                     double x1, double y1, double z1,
                                     double width, int color, float alphaStart, float alphaEnd) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        addColorLine(buffer, x0, y0, z0, x1, y1, z1, width, color, alphaStart, alphaEnd);
        tessellator.draw();
    }

    public static void drawColorCircle(double radius, int segments, double width,
                                       int color, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            double angle0 = Math.PI * 2.0D * u0;
            double angle1 = Math.PI * 2.0D * u1;
            double innerRadius = Math.max(0.001D, radius - width * 0.5D);
            double outerRadius = radius + width * 0.5D;
            addColorVertex(buffer, Math.cos(angle0) * innerRadius, 0.0D, Math.sin(angle0) * innerRadius,
                    color, alpha);
            addColorVertex(buffer, Math.cos(angle0) * outerRadius, 0.0D, Math.sin(angle0) * outerRadius,
                    color, alpha);
            addColorVertex(buffer, Math.cos(angle1) * outerRadius, 0.0D, Math.sin(angle1) * outerRadius,
                    color, alpha);
            addColorVertex(buffer, Math.cos(angle0) * innerRadius, 0.0D, Math.sin(angle0) * innerRadius,
                    color, alpha);
            addColorVertex(buffer, Math.cos(angle1) * outerRadius, 0.0D, Math.sin(angle1) * outerRadius,
                    color, alpha);
            addColorVertex(buffer, Math.cos(angle1) * innerRadius, 0.0D, Math.sin(angle1) * innerRadius,
                    color, alpha);
        }
        tessellator.draw();
    }

    public static void addTexturedLine(BufferBuilder buffer, double x0, double y0, double z0,
                                       double x1, double y1, double z1, double width) {
        double[] offset = lineOffset(x0, y0, z0, x1, y1, z1, width);
        if (offset == null) {
            return;
        }

        double px = offset[0];
        double py = offset[1];
        double pz = offset[2];
        addTexturedVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addTexturedVertex(buffer, x0 - px, y0 - py, z0 - pz, 0.0D, 0.0D, -px, -py, -pz);
        addTexturedVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addTexturedVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addTexturedVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addTexturedVertex(buffer, x1 + px, y1 + py, z1 + pz, 1.0D, 1.0D, px, py, pz);
    }

    public static void addColorLine(BufferBuilder buffer, double x0, double y0, double z0,
                                    double x1, double y1, double z1, double width,
                                    int color, float alphaStart, float alphaEnd) {
        double[] offset = lineOffset(x0, y0, z0, x1, y1, z1, width);
        if (offset == null) {
            return;
        }

        double px = offset[0];
        double py = offset[1];
        double pz = offset[2];
        addColorVertex(buffer, x0 + px, y0 + py, z0 + pz, color, alphaStart);
        addColorVertex(buffer, x0 - px, y0 - py, z0 - pz, color, alphaStart);
        addColorVertex(buffer, x1 - px, y1 - py, z1 - pz, color, alphaEnd);
        addColorVertex(buffer, x0 + px, y0 + py, z0 + pz, color, alphaStart);
        addColorVertex(buffer, x1 - px, y1 - py, z1 - pz, color, alphaEnd);
        addColorVertex(buffer, x1 + px, y1 + py, z1 + pz, color, alphaEnd);
    }

    public static void addTexturedRingSegment(BufferBuilder buffer, double radius, double width,
                                              double u0, double u1) {
        double angle0 = Math.PI * 2.0D * u0;
        double angle1 = Math.PI * 2.0D * u1;
        double innerRadius = Math.max(0.001D, radius - width * 0.5D);
        double outerRadius = radius + width * 0.5D;
        addTexturedFlatVertex(buffer, Math.cos(angle0) * innerRadius, 0.0D, Math.sin(angle0) * innerRadius,
                u0, 0.0D);
        addTexturedFlatVertex(buffer, Math.cos(angle0) * outerRadius, 0.0D, Math.sin(angle0) * outerRadius,
                u0, 1.0D);
        addTexturedFlatVertex(buffer, Math.cos(angle1) * outerRadius, 0.0D, Math.sin(angle1) * outerRadius,
                u1, 1.0D);
        addTexturedFlatVertex(buffer, Math.cos(angle0) * innerRadius, 0.0D, Math.sin(angle0) * innerRadius,
                u0, 0.0D);
        addTexturedFlatVertex(buffer, Math.cos(angle1) * outerRadius, 0.0D, Math.sin(angle1) * outerRadius,
                u1, 1.0D);
        addTexturedFlatVertex(buffer, Math.cos(angle1) * innerRadius, 0.0D, Math.sin(angle1) * innerRadius,
                u1, 0.0D);
    }

    private static double[] lineOffset(double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.0001D) {
            return null;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        double refX = Math.abs(dy) > 0.88D ? 1.0D : 0.0D;
        double refY = Math.abs(dy) > 0.88D ? 0.0D : 1.0D;
        double refZ = 0.0D;
        double px = dy * refZ - dz * refY;
        double py = dz * refX - dx * refZ;
        double pz = dx * refY - dy * refX;
        double pLength = Math.sqrt(px * px + py * py + pz * pz);
        if (pLength < 0.0001D) {
            px = 1.0D;
            py = 0.0D;
            pz = 0.0D;
            pLength = 1.0D;
        }

        double half = width * 0.5D / pLength;
        return new double[]{px * half, py * half, pz * half};
    }

    private static void addTexturedVertex(BufferBuilder buffer, double x, double y, double z,
                                          double u, double v,
                                          double normalX, double normalY, double normalZ) {
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (normalLength < 0.0001D) {
            normalX = 0.0D;
            normalY = 1.0D;
            normalZ = 0.0D;
            normalLength = 1.0D;
        }
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) (normalX / normalLength), (float) (normalY / normalLength),
                        (float) (normalZ / normalLength))
                .endVertex();
    }

    private static void addTexturedFlatVertex(BufferBuilder buffer, double x, double y, double z,
                                              double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void addColorVertex(BufferBuilder buffer, double x, double y, double z,
                                       int color, float alpha) {
        float[] rgb = unpackRGB(color);
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
