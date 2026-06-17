package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public final class RenderHelper {

    private static final double[] GLOW_POINT_COS = new double[8];
    private static final double[] GLOW_POINT_SIN = new double[8];
    private static final Map<Long, SphereMesh> SPHERE_MESH_CACHE = new HashMap<>();

    static {
        for (int i = 0; i < GLOW_POINT_COS.length; i++) {
            double angle = Math.PI * 2.0D * i / GLOW_POINT_COS.length;
            GLOW_POINT_COS[i] = Math.cos(angle);
            GLOW_POINT_SIN[i] = Math.sin(angle);
        }
    }

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

        float[] rgb = unpackRGB(color);
        SphereMesh mesh = sphereMesh(latSegs, lonSegs);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < mesh.vertexCount; i++) {
            buffer.pos(mesh.x[i] * radius, mesh.y[i] * radius, mesh.z[i] * radius)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    public static void drawWireframeSphere(double radius, int color, float alpha, int gridLat, int gridLon) {
        if (alpha <= 0.01F) {
            return;
        }

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

        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        SphereMesh mesh = sphereMesh(latSegs, lonSegs);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int i = 0; i < mesh.vertexCount; i++) {
            buffer.pos(mesh.x[i] * radius, mesh.y[i] * radius, mesh.z[i] * radius)
                    .tex(mesh.u[i], mesh.v[i])
                    .color(1.0F, 1.0F, 1.0F, alpha)
                    .endVertex();
        }
        tessellator.draw();
        GlStateManager.cullFace(previousCullFace == GL11.GL_FRONT
                ? GlStateManager.CullFace.FRONT
                : GlStateManager.CullFace.BACK);
        if (!cullWasEnabled) {
            GlStateManager.disableCull();
        }
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2,
                                int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x2, y2, z2).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        tessellator.draw();
    }

    public static BillboardPoint[] createBillboardPoints(int count) {
        BillboardPoint[] points = new BillboardPoint[count];
        for (int i = 0; i < count; i++) {
            points[i] = new BillboardPoint();
        }
        return points;
    }

    public static void drawBillboardGlowPoints(BillboardPoint[] points, int count) {
        if (count <= 0) {
            return;
        }

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        double yaw = Math.toRadians(renderManager.playerViewY);
        double pitch = Math.toRadians(renderManager.playerViewX);
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
            pitch = -pitch;
        }

        double rightX = Math.cos(yaw);
        double rightZ = -Math.sin(yaw);
        double upX = Math.sin(yaw) * Math.sin(pitch);
        double upY = Math.cos(pitch);
        double upZ = Math.cos(yaw) * Math.sin(pitch);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < count; i++) {
            BillboardPoint point = points[i];
            if (point.alpha <= 0.01F || point.size <= 0.0D) {
                continue;
            }
            addBillboardGlowPoint(buffer, point, rightX, rightZ, upX, upY, upZ);
        }
        tessellator.draw();
    }

    private static void addBillboardGlowPoint(BufferBuilder buffer, BillboardPoint point,
                                              double rightX, double rightZ,
                                              double upX, double upY, double upZ) {
        float red = ((point.color >> 16) & 0xFF) / 255.0F;
        float green = ((point.color >> 8) & 0xFF) / 255.0F;
        float blue = (point.color & 0xFF) / 255.0F;
        double size = point.size;

        for (int i = 0; i < GLOW_POINT_COS.length; i++) {
            int next = (i + 1) % GLOW_POINT_COS.length;
            addGlowTriangle(buffer, point,
                    point.x + (rightX * GLOW_POINT_COS[i] + upX * GLOW_POINT_SIN[i]) * size,
                    point.y + upY * GLOW_POINT_SIN[i] * size,
                    point.z + (rightZ * GLOW_POINT_COS[i] + upZ * GLOW_POINT_SIN[i]) * size,
                    point.x + (rightX * GLOW_POINT_COS[next] + upX * GLOW_POINT_SIN[next]) * size,
                    point.y + upY * GLOW_POINT_SIN[next] * size,
                    point.z + (rightZ * GLOW_POINT_COS[next] + upZ * GLOW_POINT_SIN[next]) * size,
                    red, green, blue);
        }
    }

    private static void addGlowTriangle(BufferBuilder buffer, BillboardPoint point,
                                        double ax, double ay, double az,
                                        double bx, double by, double bz,
                                        float red, float green, float blue) {
        buffer.pos(point.x, point.y, point.z).color(red, green, blue, point.alpha).endVertex();
        buffer.pos(ax, ay, az).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(bx, by, bz).color(red, green, blue, 0.0F).endVertex();
    }

    public static void resetLineWidth() {
        GlStateManager.glLineWidth(1.0F);
    }

    private static SphereMesh sphereMesh(int latSegs, int lonSegs) {
        long key = (((long) latSegs) << 32) ^ (lonSegs & 0xFFFFFFFFL);
        SphereMesh mesh = SPHERE_MESH_CACHE.get(key);
        if (mesh == null) {
            mesh = new SphereMesh(latSegs, lonSegs);
            SPHERE_MESH_CACHE.put(key, mesh);
        }
        return mesh;
    }

    public static final class BillboardPoint {
        private double x;
        private double y;
        private double z;
        private double size;
        private int color;
        private float alpha;

        public void set(double x, double y, double z, double size, int color, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.color = color;
            this.alpha = alpha;
        }
    }

    private static final class SphereMesh {
        private final int vertexCount;
        private final double[] x;
        private final double[] y;
        private final double[] z;
        private final double[] u;
        private final double[] v;

        private SphereMesh(int latSegs, int lonSegs) {
            this.vertexCount = latSegs * lonSegs * 6;
            this.x = new double[vertexCount];
            this.y = new double[vertexCount];
            this.z = new double[vertexCount];
            this.u = new double[vertexCount];
            this.v = new double[vertexCount];
            int index = 0;
            for (int lat = 0; lat < latSegs; lat++) {
                double theta0 = Math.PI * lat / latSegs;
                double theta1 = Math.PI * (lat + 1) / latSegs;
                double v0 = (double) lat / latSegs;
                double v1 = (double) (lat + 1) / latSegs;
                for (int lon = 0; lon < lonSegs; lon++) {
                    double phi0 = 2.0D * Math.PI * lon / lonSegs;
                    double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                    double u0 = (double) lon / lonSegs;
                    double u1 = (double) (lon + 1) / lonSegs;

                    index = addSphereVertex(index, theta0, phi0, u0, v0);
                    index = addSphereVertex(index, theta1, phi0, u0, v1);
                    index = addSphereVertex(index, theta0, phi1, u1, v0);
                    index = addSphereVertex(index, theta0, phi1, u1, v0);
                    index = addSphereVertex(index, theta1, phi0, u0, v1);
                    index = addSphereVertex(index, theta1, phi1, u1, v1);
                }
            }
        }

        private int addSphereVertex(int index, double theta, double phi, double u, double v) {
            double sinTheta = Math.sin(theta);
            this.x[index] = sinTheta * Math.cos(phi);
            this.y[index] = Math.cos(theta);
            this.z[index] = sinTheta * Math.sin(phi);
            this.u[index] = u;
            this.v[index] = v;
            return index + 1;
        }
    }
}
