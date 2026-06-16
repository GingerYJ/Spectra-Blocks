package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

final class RenderEnergyEffectHelper {

    private static final double TWO_PI = Math.PI * 2.0D;

    private RenderEnergyEffectHelper() {
    }

    static void drawFacetedCrystal(double radius, double height, int primaryColor, int secondaryColor,
                                   float alpha, int facets) {
        if (alpha <= 0.01F) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        double topY = height * 0.5D;
        double bottomY = -height * 0.5D;
        for (int i = 0; i < facets; i++) {
            double angle0 = TWO_PI * i / facets;
            double angle1 = TWO_PI * (i + 1) / facets;
            double x0 = Math.cos(angle0) * radius;
            double z0 = Math.sin(angle0) * radius;
            double x1 = Math.cos(angle1) * radius;
            double z1 = Math.sin(angle1) * radius;
            float[] upperColor = RenderHelper.unpackRGB((i & 1) == 0 ? primaryColor : secondaryColor);
            float[] lowerColor = RenderHelper.unpackRGB((i & 1) == 0 ? secondaryColor : primaryColor);

            addVertex(buffer, 0.0D, topY, 0.0D, upperColor, alpha);
            addVertex(buffer, x0, 0.0D, z0, upperColor, alpha);
            addVertex(buffer, x1, 0.0D, z1, upperColor, alpha);

            addVertex(buffer, 0.0D, bottomY, 0.0D, lowerColor, alpha * 0.82F);
            addVertex(buffer, x1, 0.0D, z1, lowerColor, alpha * 0.82F);
            addVertex(buffer, x0, 0.0D, z0, lowerColor, alpha * 0.82F);
        }

        tessellator.draw();
    }

    static void drawCrystalEdges(double radius, double height, int color, float alpha, int facets) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        double topY = height * 0.5D;
        double bottomY = -height * 0.5D;
        for (int i = 0; i < facets; i++) {
            double angle0 = TWO_PI * i / facets;
            double angle1 = TWO_PI * (i + 1) / facets;
            double x0 = Math.cos(angle0) * radius;
            double z0 = Math.sin(angle0) * radius;
            double x1 = Math.cos(angle1) * radius;
            double z1 = Math.sin(angle1) * radius;

            addVertex(buffer, 0.0D, topY, 0.0D, rgb, alpha);
            addVertex(buffer, x0, 0.0D, z0, rgb, alpha);
            addVertex(buffer, 0.0D, bottomY, 0.0D, rgb, alpha);
            addVertex(buffer, x0, 0.0D, z0, rgb, alpha);
            addVertex(buffer, x0, 0.0D, z0, rgb, alpha * 0.7F);
            addVertex(buffer, x1, 0.0D, z1, rgb, alpha * 0.7F);
        }

        tessellator.draw();
    }

    static void drawFlatBand(double radius, double halfWidth, int color, float alpha, int segments) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = TWO_PI * i / segments;
            double inner = radius - halfWidth;
            double outer = radius + halfWidth;
            addVertex(buffer, Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner, rgb, alpha);
            addVertex(buffer, Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer, rgb, alpha);
        }
        tessellator.draw();
    }

    static void drawRuneMarks(double radius, double length, int count, int color, float alpha, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < count; i++) {
            double angle = phase + TWO_PI * i / count;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double inner = radius - length * 0.5D;
            double outer = radius + length * 0.5D;
            addVertex(buffer, cos * inner, 0.0D, sin * inner, rgb, alpha);
            addVertex(buffer, cos * outer, 0.0D, sin * outer, rgb, alpha);

            if ((i & 3) == 0) {
                double tangentX = -sin * length * 0.36D;
                double tangentZ = cos * length * 0.36D;
                double markRadius = radius + length * 0.18D;
                addVertex(buffer, cos * markRadius - tangentX, 0.0D, sin * markRadius - tangentZ, rgb, alpha * 0.75F);
                addVertex(buffer, cos * markRadius + tangentX, 0.0D, sin * markRadius + tangentZ, rgb, alpha * 0.75F);
            }
        }

        tessellator.draw();
    }

    static void drawStarRays(double innerRadius, double outerRadius, int rayCount,
                             int color, float alpha, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < rayCount; i++) {
            double angle = phase + TWO_PI * i / rayCount;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            float rayAlpha = alpha * (0.72F + 0.28F * (i % 2));
            addVertex(buffer, cos * innerRadius, 0.0D, sin * innerRadius, rgb, rayAlpha);
            addVertex(buffer, cos * outerRadius, 0.0D, sin * outerRadius, rgb, rayAlpha * 0.45F);
        }

        tessellator.draw();
    }

    static void drawJaggedArc(double radius, double angleStart, double angleLength, double yOffset,
                              double verticalLift, double jitter, int segments, int color,
                              float alpha, float ticks, int seed) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = angleStart + angleLength * progress;
            double lift = Math.sin(Math.PI * progress) * verticalLift;
            double rough = noise(seed, i, ticks) * jitter;
            double localRadius = radius + rough;
            double y = yOffset + lift + noise(seed + 17, i, ticks * 0.73F) * jitter * 0.42D;
            float pointAlpha = alpha * (float) Math.sin(Math.PI * progress);
            addVertex(buffer, Math.cos(angle) * localRadius, y, Math.sin(angle) * localRadius, rgb, pointAlpha);
        }

        tessellator.draw();
    }

    static void drawSpark(double size, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        addVertex(buffer, -size, 0.0D, 0.0D, rgb, alpha * 0.55F);
        addVertex(buffer, size, 0.0D, 0.0D, rgb, alpha * 0.55F);
        addVertex(buffer, 0.0D, -size, 0.0D, rgb, alpha * 0.55F);
        addVertex(buffer, 0.0D, size, 0.0D, rgb, alpha * 0.55F);
        addVertex(buffer, 0.0D, 0.0D, -size, rgb, alpha * 0.55F);
        addVertex(buffer, 0.0D, 0.0D, size, rgb, alpha * 0.55F);
        tessellator.draw();
    }

    private static double noise(int seed, int step, float ticks) {
        return Math.sin(seed * 12.9898D + step * 78.233D + ticks * 0.318D);
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
