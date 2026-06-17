package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

final class RenderNaturalShaderHelper {

    static final float MODE_STARDUST = 0.0F;
    static final float MODE_AURORA = 1.0F;
    static final float MODE_ABYSSAL = 2.0F;
    static final float MODE_STORM = 3.0F;
    static final float MODE_ENTROPY = 4.0F;
    static final float MODE_SOLAR = 5.0F;
    static final float MODE_HOURGLASS = 6.0F;

    private static final double TWO_PI = Math.PI * 2.0D;

    private RenderNaturalShaderHelper() {
    }

    static void drawNaturalSphere(ShaderProgram shader, double radius, float mode, float layer,
                                  int primaryColor, int secondaryColor, int accentColor,
                                  float alpha, float pulse, float intensity,
                                  float time, float seed, int segments) {
        drawNaturalSphere(shader, radius, mode, layer, primaryColor, secondaryColor, accentColor,
                alpha, pulse, intensity, time, seed, segments, segments);
    }

    static void drawNaturalSphere(ShaderProgram shader, double radius, float mode, float layer,
                                  int primaryColor, int secondaryColor, int accentColor,
                                  float alpha, float pulse, float intensity,
                                  float time, float seed, int latSegs, int lonSegs) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || latSegs < 3 || lonSegs < 3 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            drawSphereGeometry(radius, latSegs, lonSegs);
        } finally {
            shader.end();
        }
    }

    static void drawAuroraSheet(ShaderProgram shader, double width, double height, double baseY,
                                int segments, float layer, int primaryColor, int secondaryColor,
                                int accentColor, float alpha, float pulse, float time, float seed) {
        if (shader == null || alpha <= 0.005F || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, MODE_AURORA, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, 1.0F, time, seed);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double centered = progress - 0.5D;
                double edgeFade = Math.sin(Math.PI * progress);
                double x = centered * width;
                double phase = time * 0.80D + progress * Math.PI * 4.2D + layer * 0.84D;
                double sway = Math.sin(phase) * (0.22D + layer * 0.020D);
                double smallWave = Math.sin(phase * 1.9D + layer) * 0.075D;
                double z = sway + smallWave;
                double bottomY = baseY + Math.sin(phase + 1.3D) * 0.055D;
                double topY = baseY + height * (0.88D + 0.12D * edgeFade)
                        + Math.sin(phase * 0.78D) * 0.18D;

                buffer.pos(x, bottomY, z * 0.45D)
                        .tex(progress, 0.0D)
                        .normal(0.0F, 0.0F, 1.0F)
                        .endVertex();
                buffer.pos(x, topY, z)
                        .tex(progress, 1.0D)
                        .normal(0.0F, 0.0F, 1.0F)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicLine(ShaderProgram shader, double x1, double y1, double z1,
                              double x2, double y2, double z2, int color, float alpha) {
        if (shader == null || alpha <= 0.005F || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            buffer.pos(x2, y2, z2).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicCircle(ShaderProgram shader, double radius, int color, float alpha, int segments) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 3 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < segments; i++) {
                double angle = TWO_PI * i / segments;
                buffer.pos(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius)
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicFlatRing(ShaderProgram shader, double innerRadius, double outerRadius,
                                  int color, float alpha, int segments) {
        if (shader == null || alpha <= 0.005F || innerRadius <= 0.0D
                || outerRadius <= innerRadius || segments < 3 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = TWO_PI * i / segments;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                buffer.pos(cos * outerRadius, 0.0D, sin * outerRadius)
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
                buffer.pos(cos * innerRadius, 0.0D, sin * innerRadius)
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicSpiralRibbon(ShaderProgram shader, double startRadius, double endRadius,
                                      double startAngle, double sweep, double width,
                                      int color, float alpha, int segments) {
        if (shader == null || alpha <= 0.005F || width <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double angle = startAngle + sweep * progress;
                double radius = lerp(startRadius, endRadius, progress);
                double halfWidth = width * (1.0D - progress * 0.55D);
                double y = Math.sin(startAngle * 1.7D + progress * Math.PI * 3.0D) * 0.025D;
                float edgeFade = (float) Math.sin(Math.PI * progress);
                float vertexAlpha = alpha * (0.18F + 0.82F * edgeFade);
                double inner = Math.max(0.0D, radius - halfWidth);
                double outer = radius + halfWidth;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                buffer.pos(cos * outer, y, sin * outer)
                        .color(rgb[0], rgb[1], rgb[2], vertexAlpha)
                        .endVertex();
                buffer.pos(cos * inner, -y, sin * inner)
                        .color(rgb[0], rgb[1], rgb[2], vertexAlpha)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicSphericalArc(ShaderProgram shader, double radius, double startYaw,
                                      double sweepYaw, double basePitch, double pitchWave,
                                      double phase, int color, float alpha, int segments) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double yaw = startYaw + sweepYaw * progress;
                double pitch = basePitch + Math.sin(phase + progress * TWO_PI) * pitchWave;
                double horizontal = Math.cos(pitch) * radius;
                float fade = (float) Math.sin(Math.PI * progress);
                buffer.pos(Math.cos(yaw) * horizontal,
                                Math.sin(pitch) * radius,
                                Math.sin(yaw) * horizontal)
                        .color(rgb[0], rgb[1], rgb[2], alpha * (0.20F + 0.80F * fade))
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicJaggedArc(ShaderProgram shader, double radius, double startYaw,
                                   double sweepYaw, double y, double lift, double jitter,
                                   int segments, int color, float alpha, float ticks, int seed) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double fade = Math.sin(Math.PI * progress);
                double angle = startYaw + sweepYaw * progress
                        + deterministicJitter(seed, i, ticks) * jitter * 0.80D;
                double localRadius = radius + deterministicJitter(seed + 71, i, ticks) * jitter;
                double localY = y + lift * fade
                        + deterministicJitter(seed + 137, i, ticks) * jitter * 0.85D;
                buffer.pos(Math.cos(angle) * localRadius, localY, Math.sin(angle) * localRadius)
                        .color(rgb[0], rgb[1], rgb[2], alpha * (0.18F + 0.82F * (float) fade))
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawBasicWireSphere(ShaderProgram shader, double radius, int color, float alpha,
                                    int gridLat, int gridLon) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D
                || gridLat < 2 || gridLon < 3 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            for (int lat = 1; lat < gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                double y = radius * Math.cos(theta);
                double horizontalRadius = radius * Math.sin(theta);
                buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                for (int lon = 0; lon < gridLon; lon++) {
                    double phi = TWO_PI * lon / gridLon;
                    buffer.pos(horizontalRadius * Math.cos(phi), y, horizontalRadius * Math.sin(phi))
                            .color(rgb[0], rgb[1], rgb[2], alpha)
                            .endVertex();
                }
                tessellator.draw();
            }

            for (int lon = 0; lon < gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
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
        } finally {
            shader.end();
        }
    }

    static void drawBasicStarRays(ShaderProgram shader, double innerRadius, double outerRadius,
                                  int rayCount, int color, float alpha, double phase) {
        if (shader == null || alpha <= 0.005F || innerRadius <= 0.0D
                || outerRadius <= innerRadius || rayCount < 2 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < rayCount; i++) {
                double angle = TWO_PI * i / rayCount + phase;
                double rayPulse = 0.74D + 0.26D * Math.sin(phase * 7.0D + i * 1.618D);
                double outer = innerRadius + (outerRadius - innerRadius) * rayPulse;
                buffer.pos(Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius)
                        .color(rgb[0], rgb[1], rgb[2], alpha * 0.34F)
                        .endVertex();
                buffer.pos(Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer)
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .endVertex();
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
                double phi0 = TWO_PI * lon / lonSegs;
                double phi1 = TWO_PI * (lon + 1) / lonSegs;
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

    private static void setNaturalUniforms(ShaderProgram shader, float mode, float layer,
                                           int primaryColor, int secondaryColor, int accentColor,
                                           float alpha, float pulse, float intensity,
                                           float time, float seed) {
        float[] primary = unpackRGB(primaryColor);
        float[] secondary = unpackRGB(secondaryColor);
        float[] accent = unpackRGB(accentColor);
        shader.setUniform1f("uTime", time);
        shader.setUniform1f("uMode", mode);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uPulse", pulse);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uSecondaryColor", secondary[0], secondary[1], secondary[2]);
        shader.setUniform3f("uAccentColor", accent[0], accent[1], accent[2]);
    }

    private static void setBasicUniforms(ShaderProgram shader) {
        shader.setUniform1f("alpha", 1.0F);
        shader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static double deterministicJitter(int seed, int step, float ticks) {
        double frame = Math.floor(ticks * 0.35D);
        double value = Math.sin(seed * 12.9898D + step * 78.233D + frame * 0.37D) * 43758.5453D;
        return value - Math.floor(value) - 0.5D;
    }
}
