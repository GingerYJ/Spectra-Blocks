package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileMiniatureGalaxy;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderMiniatureGalaxy extends RenderCelestialEffectBase<TileMiniatureGalaxy> {

    private static final double GALAXY_RADIUS = 4.05D;
    private static final double CORE_RADIUS = 0.45D;
    private static final int ARM_COUNT = 4;
    private static final int DISC_SEGMENTS = 192;
    private static final int SPHERE_SEGMENTS = 28;
    private static final int STAR_SEGMENTS = 10;
    private static final int STAR_COUNT = 132;
    private static final int TRAIL_COUNT = 12;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double DEFAULT_LINE_HALF_WIDTH = 0.012D;
    private static final double DEFAULT_ARC_HALF_WIDTH = 0.014D;
    private static final double DEFAULT_LATITUDE_HALF_WIDTH = 0.012D;
    private static final double EPSILON = 1.0E-5D;
    private static final float ROTATION_SPEED = 0.055F;

    @Override
    protected void renderCelestialEffect(TileMiniatureGalaxy te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("celestial_effect");
        if (shader == null) {
            return;
        }

        try {
            drawCore(shader, ticks);

            GlStateManager.pushMatrix();
            try {
                GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(ticks * ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
                drawDisc(shader, ticks);
                drawStarField(shader, ticks);
                drawTrailingClusters(shader, ticks);
            } finally {
                GlStateManager.popMatrix();
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("miniature galaxy render failed: " + ex.getMessage());
        } finally {
            shader.end();
            useAlphaBlend();
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.031D);

        useAdditiveBlend();
        drawShaderSphere(shader, CORE_RADIUS * (2.2D + pulse * 0.18D), ticks,
                0.0F, 0.0F, 0xFFE6A3, 0xA9D7FF, 0.16F, 0.90F, 0.0F, SPHERE_SEGMENTS);
        drawShaderSphere(shader, CORE_RADIUS * (1.45D + pulse * 0.10D), ticks,
                0.0F, 1.0F, 0xA9D7FF, 0xFFFFFF, 0.20F, 1.10F, 0.2F, SPHERE_SEGMENTS);
        drawShaderSphere(shader, CORE_RADIUS * (0.92D + pulse * 0.07D), ticks,
                0.0F, 2.0F, 0xFFFFFF, 0xFFE8B2, 0.70F, 1.35F, 0.4F, SPHERE_SEGMENTS);
        useAlphaBlend();
    }

    private void drawDisc(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        drawShaderRing(shader, 0.42D, GALAXY_RADIUS, ticks,
                0.0F, 3.0F, 0x21344A, 0x6CA8D9, 0.070F, 0.74F, 0.0F, DISC_SEGMENTS);
        drawShaderRing(shader, 0.58D, GALAXY_RADIUS * 0.78D, ticks,
                0.0F, 4.0F, 0x6CA8D9, 0xF7F3FF, 0.065F, 0.88F, 0.35F, DISC_SEGMENTS);

        useAdditiveBlend();
        for (int i = 0; i < ARM_COUNT; i++) {
            double start = i * Math.PI * 2.0D / ARM_COUNT + ticks * 0.002D;
            int color = i % 2 == 0 ? 0x8CCBFF : 0xFFE0A1;
            drawShaderSpiral(shader, 0.48D, GALAXY_RADIUS * 0.92D, start,
                    Math.PI * 2.25D, 0.22D, ticks, 0.0F, 5.0F, color,
                    0xF7F3FF, 0.145F, 1.05F, i * 0.21F, 116);
            drawShaderSpiral(shader, 0.66D, GALAXY_RADIUS * 0.98D, start + 0.18D,
                    Math.PI * 2.05D, 0.095D, ticks, 0.0F, 6.0F, 0xF7F3FF,
                    color, 0.110F, 1.16F, 0.45F + i * 0.17F, 116);
        }
        useAlphaBlend();
    }

    private void drawStarField(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STAR_COUNT; i++) {
            double band = (i + 0.5D) / STAR_COUNT;
            double radius = 0.58D + Math.pow(band, 0.64D) * (GALAXY_RADIUS - 0.68D);
            double angle = i * GOLDEN_ANGLE + ticks * (0.0038D / (0.45D + radius));
            double armBias = Math.sin(angle * ARM_COUNT - radius * 2.4D);
            double y = Math.sin(i * 1.713D + ticks * 0.010D) * (0.018D + radius * 0.010D);
            radius += armBias * 0.065D;

            int color = i % 7 == 0 ? 0xFFE3A8 : (i % 5 == 0 ? 0x9DD6FF : 0xEEF6FF);
            float flicker = 0.65F + 0.35F * wave(ticks * (0.035D + (i % 5) * 0.006D) + i);
            double size = 0.018D + (i % 4) * 0.004D + (i % 13 == 0 ? 0.020D : 0.0D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            drawShaderSphere(shader, size, ticks, 0.0F, 7.0F, color,
                    0xFFFFFF, 0.42F * flicker, 1.45F, i * 0.037F, STAR_SEGMENTS);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawTrailingClusters(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < TRAIL_COUNT; i++) {
            double radius = 1.25D + i * 0.205D;
            double angle = i * GOLDEN_ANGLE + ticks * (0.0048D / radius);
            double trailAngle = angle - 0.34D;
            double y = Math.sin(i * 0.77D + ticks * 0.013D) * 0.045D;
            double headX = Math.cos(angle) * radius;
            double headZ = Math.sin(angle) * radius;
            double tailX = Math.cos(trailAngle) * (radius * 1.035D);
            double tailZ = Math.sin(trailAngle) * (radius * 1.035D);
            float pulse = wave(ticks * 0.045D + i * 0.9D);

            drawShaderLine(shader, headX, y, headZ, tailX, y * 0.35D, tailZ, ticks,
                    0.0F, 8.0F, 0x83C8FF, 0xFFFFFF, 0.075F + 0.045F * pulse, 0.85F, i * 0.11F,
                    0.030D);
            drawShaderLine(shader, headX, y, headZ, tailX, y * 0.35D, tailZ, ticks,
                    0.0F, 9.0F, 0xFFFFFF, 0x83C8FF, 0.155F + 0.070F * pulse, 1.15F, 0.5F + i * 0.09F,
                    0.014D);
            GlStateManager.pushMatrix();
            GlStateManager.translate(headX, y, headZ);
            drawShaderSphere(shader, 0.045D + pulse * 0.025D, ticks, 0.0F, 10.0F,
                    i % 3 == 0 ? 0xFFE5A8 : 0xDDEEFF, 0xFFFFFF, 0.55F, 1.40F, i * 0.07F, STAR_SEGMENTS);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
    }

    static void drawShaderSphere(ShaderProgram shader, double radius, float ticks, float effect, float layer,
                                 int primaryColor, int accentColor, float alpha, float intensity,
                                 float seed, int segments) {
        if (radius <= 0.0D || alpha <= 0.01F || !beginLayer(shader, ticks, effect, layer,
                primaryColor, accentColor, alpha, intensity, seed)) {
            return;
        }

        try {
            drawSphereGeometry(radius, segments, segments);
        } finally {
            shader.end();
        }
    }

    static void drawShaderRing(ShaderProgram shader, double innerRadius, double outerRadius, float ticks,
                               float effect, float layer, int primaryColor, int accentColor, float alpha,
                               float intensity, float seed, int segments) {
        if (innerRadius <= 0.0D || outerRadius <= innerRadius || alpha <= 0.01F || !beginLayer(shader, ticks,
                effect, layer, primaryColor, accentColor, alpha, intensity, seed)) {
            return;
        }

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.PI * 2.0D * i / segments;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double u = i / (double) segments;
                buffer.pos(cos * outerRadius, 0.0D, sin * outerRadius)
                        .tex(u, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
                buffer.pos(cos * innerRadius, 0.0D, sin * innerRadius)
                        .tex(u, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderSpiral(ShaderProgram shader, double startRadius, double endRadius,
                                 double startAngle, double sweep, double width, float ticks,
                                 float effect, float layer, int primaryColor, int accentColor, float alpha,
                                 float intensity, float seed, int segments) {
        if (width <= 0.0D || alpha <= 0.01F || !beginLayer(shader, ticks, effect, layer,
                primaryColor, accentColor, alpha, intensity, seed)) {
            return;
        }

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double angle = startAngle + sweep * progress;
                double radius = lerp(startRadius, endRadius, progress);
                double halfWidth = width * (1.0D - progress * 0.55D);
                double y = Math.sin(startAngle * 1.7D + progress * Math.PI * 3.0D) * 0.025D;
                double inner = Math.max(0.0D, radius - halfWidth);
                double outer = radius + halfWidth;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                buffer.pos(cos * outer, y, sin * outer)
                        .tex(progress, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
                buffer.pos(cos * inner, -y, sin * inner)
                        .tex(progress, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderLine(ShaderProgram shader, double x1, double y1, double z1,
                               double x2, double y2, double z2, float ticks, float effect, float layer,
                               int primaryColor, int accentColor, float alpha, float intensity, float seed) {
        drawShaderLine(shader, x1, y1, z1, x2, y2, z2, ticks, effect, layer,
                primaryColor, accentColor, alpha, intensity, seed, DEFAULT_LINE_HALF_WIDTH);
    }

    static void drawShaderLine(ShaderProgram shader, double x1, double y1, double z1,
                               double x2, double y2, double z2, float ticks, float effect, float layer,
                               int primaryColor, int accentColor, float alpha, float intensity, float seed,
                               double halfWidth) {
        if (alpha <= 0.01F || !beginLayer(shader, ticks, effect, layer, primaryColor, accentColor,
                alpha, intensity, seed)) {
            return;
        }

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            addRibbonSegment(buffer, x1, y1, z1, x2, y2, z2, Math.max(EPSILON, halfWidth));
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderArc(ShaderProgram shader, double radius, double startYaw, double sweepYaw,
                              double basePitch, double pitchWave, double phase, float ticks,
                              float effect, float layer, int primaryColor, int accentColor, float alpha,
                              float intensity, float seed, int segments) {
        if (radius <= 0.0D || alpha <= 0.01F || !beginLayer(shader, ticks, effect, layer,
                primaryColor, accentColor, alpha, intensity, seed)) {
            return;
        }

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            int pointCount = segments + 1;
            double[] xs = new double[pointCount];
            double[] ys = new double[pointCount];
            double[] zs = new double[pointCount];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double yaw = startYaw + sweepYaw * progress;
                double pitch = basePitch + Math.sin(phase + progress * Math.PI * 2.0D) * pitchWave;
                double horizontal = Math.cos(pitch) * radius;
                xs[i] = Math.cos(yaw) * horizontal;
                ys[i] = Math.sin(pitch) * radius;
                zs[i] = Math.sin(yaw) * horizontal;
            }
            drawPolylineRibbon(buffer, tessellator, xs, ys, zs, pointCount, false, DEFAULT_ARC_HALF_WIDTH);
        } finally {
            shader.end();
        }
    }

    static void drawShaderLatitudeCircle(ShaderProgram shader, double sphereRadius, double y, float ticks,
                                         float effect, float layer, int primaryColor, int accentColor,
                                         float alpha, float intensity, float seed, int segments) {
        if (alpha <= 0.01F || Math.abs(y) >= sphereRadius || !beginLayer(shader, ticks, effect, layer,
                primaryColor, accentColor, alpha, intensity, seed)) {
            return;
        }

        try {
            double radius = Math.sqrt(Math.max(0.0D, sphereRadius * sphereRadius - y * y));
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            double innerRadius = Math.max(0.0D, radius - DEFAULT_LATITUDE_HALF_WIDTH);
            double outerRadius = radius + DEFAULT_LATITUDE_HALF_WIDTH;
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.PI * 2.0D * i / segments;
                double progress = i / (double) segments;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                addPosition(buffer, cos * outerRadius, y, sin * outerRadius, progress, 1.0D, 0.0D, 1.0D, 0.0D);
                addPosition(buffer, cos * innerRadius, y, sin * innerRadius, progress, 0.0D, 0.0D, 1.0D, 0.0D);
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private static void drawSphereGeometry(double radius, int latSegs, int lonSegs) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = 2.0D * Math.PI * lon / lonSegs;
                double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
            }
        }
        tessellator.draw();
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                        double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z,
                                    double u, double v, double normalX, double normalY, double normalZ) {
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (normalLength < EPSILON) {
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

    private static void drawPolylineRibbon(BufferBuilder buffer, Tessellator tessellator,
                                           double[] xs, double[] ys, double[] zs, int pointCount,
                                           boolean closed, double halfWidth) {
        if (pointCount < 2) {
            return;
        }

        int vertexCount = closed ? pointCount + 1 : pointCount;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < vertexCount; i++) {
            int index = closed ? i % pointCount : i;
            int previous = closed ? (index + pointCount - 1) % pointCount : Math.max(0, index - 1);
            int next = closed ? (index + 1) % pointCount : Math.min(pointCount - 1, index + 1);
            double[] side = sideVector(xs[next] - xs[previous], ys[next] - ys[previous], zs[next] - zs[previous]);
            double progress = vertexCount <= 1 ? 0.0D : i / (double) (vertexCount - 1);
            addPosition(buffer, xs[index] + side[0] * halfWidth, ys[index] + side[1] * halfWidth,
                    zs[index] + side[2] * halfWidth, progress, 1.0D, 0.0D, 1.0D, 0.0D);
            addPosition(buffer, xs[index] - side[0] * halfWidth, ys[index] - side[1] * halfWidth,
                    zs[index] - side[2] * halfWidth, progress, 0.0D, 0.0D, 1.0D, 0.0D);
        }
        tessellator.draw();
    }

    private static void addRibbonSegment(BufferBuilder buffer, double x1, double y1, double z1,
                                         double x2, double y2, double z2, double halfWidth) {
        double[] side = sideVector(x2 - x1, y2 - y1, z2 - z1);
        double sx = side[0] * halfWidth;
        double sy = side[1] * halfWidth;
        double sz = side[2] * halfWidth;
        addPosition(buffer, x1 + sx, y1 + sy, z1 + sz, 0.0D, 1.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x2 + sx, y2 + sy, z2 + sz, 1.0D, 1.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x2 - sx, y2 - sy, z2 - sz, 1.0D, 0.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x1 + sx, y1 + sy, z1 + sz, 0.0D, 1.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x2 - sx, y2 - sy, z2 - sz, 1.0D, 0.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x1 - sx, y1 - sy, z1 - sz, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D);
    }

    private static double[] sideVector(double dx, double dy, double dz) {
        double axisX = Math.abs(dy) > 0.82D ? 1.0D : 0.0D;
        double axisY = Math.abs(dy) > 0.82D ? 0.0D : 1.0D;
        double sideX = -dz * axisY;
        double sideY = dz * axisX;
        double sideZ = dx * axisY - dy * axisX;
        double length = Math.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);
        if (length < EPSILON) {
            return new double[]{1.0D, 0.0D, 0.0D};
        }
        return new double[]{sideX / length, sideY / length, sideZ / length};
    }

    private static boolean beginLayer(ShaderProgram shader, float ticks, float effect, float layer,
                                      int primaryColor, int accentColor, float alpha, float intensity, float seed) {
        if (!shader.begin()) {
            return false;
        }

        float[] primary = unpackRGB(primaryColor);
        float[] accent = unpackRGB(accentColor);
        shader.setUniform1f("uTime", ticks * 0.025F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uAccentColor", accent[0], accent[1], accent[2]);
        return true;
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }
}
