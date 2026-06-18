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
    private static final double BASIC_LINE_HALF_WIDTH = 0.010D;
    private static final double BASIC_CIRCLE_HALF_WIDTH = 0.012D;
    private static final double BASIC_ARC_HALF_WIDTH = 0.014D;
    private static final double BASIC_WIRE_HALF_WIDTH = 0.010D;
    private static final double BASIC_RAY_HALF_WIDTH = 0.014D;
    private static final double EPSILON = 1.0E-5D;

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
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            addColoredSegmentRibbon(buffer, x1, y1, z1, x2, y2, z2,
                    BASIC_LINE_HALF_WIDTH, rgb, alpha, alpha);
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderLine(ShaderProgram shader, float mode, float layer,
                               double x1, double y1, double z1, double x2, double y2, double z2,
                               int primaryColor, int secondaryColor, int accentColor,
                               float alpha, float pulse, float intensity, float time, float seed) {
        drawShaderLine(shader, mode, layer, x1, y1, z1, x2, y2, z2,
                primaryColor, secondaryColor, accentColor, alpha, pulse, intensity, time, seed,
                BASIC_LINE_HALF_WIDTH);
    }

    static void drawShaderLine(ShaderProgram shader, float mode, float layer,
                               double x1, double y1, double z1, double x2, double y2, double z2,
                               int primaryColor, int secondaryColor, int accentColor,
                               float alpha, float pulse, float intensity, float time, float seed,
                               double halfWidth) {
        if (shader == null || alpha <= 0.005F || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            RenderHelper.addTexturedLine(buffer, x1, y1, z1, x2, y2, z2, halfWidth);
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
            double innerRadius = Math.max(0.0D, radius - BASIC_CIRCLE_HALF_WIDTH);
            double outerRadius = radius + BASIC_CIRCLE_HALF_WIDTH;
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = TWO_PI * i / segments;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                addColoredVertex(buffer, cos * outerRadius, 0.0D, sin * outerRadius, rgb, alpha);
                addColoredVertex(buffer, cos * innerRadius, 0.0D, sin * innerRadius, rgb, alpha);
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderCircle(ShaderProgram shader, double radius, float mode, float layer,
                                 int primaryColor, int secondaryColor, int accentColor,
                                 float alpha, float pulse, float intensity,
                                 float time, float seed, int segments) {
        drawShaderRing(shader, Math.max(0.0D, radius - BASIC_CIRCLE_HALF_WIDTH),
                radius + BASIC_CIRCLE_HALF_WIDTH, mode, layer, primaryColor, secondaryColor,
                accentColor, alpha, pulse, intensity, time, seed, segments);
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

    static void drawShaderRing(ShaderProgram shader, double innerRadius, double outerRadius,
                               float mode, float layer, int primaryColor, int secondaryColor,
                               int accentColor, float alpha, float pulse, float intensity,
                               float time, float seed, int segments) {
        if (shader == null || alpha <= 0.005F || innerRadius <= 0.0D
                || outerRadius <= innerRadius || segments < 3 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i <= segments; i++) {
                double progress = i / (double) segments;
                double angle = TWO_PI * progress;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                buffer.pos(cos * outerRadius, 0.0D, sin * outerRadius)
                        .tex(progress, 1.0D)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                buffer.pos(cos * innerRadius, 0.0D, sin * innerRadius)
                        .tex(progress, 0.0D)
                        .normal(0.0F, 1.0F, 0.0F)
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

    static void drawShaderSpiralRibbon(ShaderProgram shader, double startRadius, double endRadius,
                                       double startAngle, double sweep, double width,
                                       float mode, float layer, int primaryColor, int secondaryColor,
                                       int accentColor, float alpha, float pulse, float intensity,
                                       float time, float seed, int segments) {
        if (shader == null || alpha <= 0.005F || width <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
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
                        .tex(progress, 1.0D)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                buffer.pos(cos * inner, -y, sin * inner)
                        .tex(progress, 0.0D)
                        .normal(0.0F, 1.0F, 0.0F)
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
            int pointCount = segments + 1;
            double[] xs = new double[pointCount];
            double[] ys = new double[pointCount];
            double[] zs = new double[pointCount];
            float[] alphas = new float[pointCount];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double yaw = startYaw + sweepYaw * progress;
                double pitch = basePitch + Math.sin(phase + progress * TWO_PI) * pitchWave;
                double horizontal = Math.cos(pitch) * radius;
                float fade = (float) Math.sin(Math.PI * progress);
                xs[i] = Math.cos(yaw) * horizontal;
                ys[i] = Math.sin(pitch) * radius;
                zs[i] = Math.sin(yaw) * horizontal;
                alphas[i] = alpha * (0.20F + 0.80F * fade);
            }
            drawColoredPolylineRibbon(xs, ys, zs, alphas, pointCount, false, BASIC_ARC_HALF_WIDTH, rgb);
        } finally {
            shader.end();
        }
    }

    static void drawShaderSphericalArc(ShaderProgram shader, double radius, double startYaw,
                                       double sweepYaw, double basePitch, double pitchWave,
                                       double phase, float mode, float layer,
                                       int primaryColor, int secondaryColor, int accentColor,
                                       float alpha, float pulse, float intensity,
                                       float time, float seed, int segments) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            int pointCount = segments + 1;
            double[] xs = new double[pointCount];
            double[] ys = new double[pointCount];
            double[] zs = new double[pointCount];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double yaw = startYaw + sweepYaw * progress;
                double pitch = basePitch + Math.sin(phase + progress * TWO_PI) * pitchWave;
                double horizontal = Math.cos(pitch) * radius;
                xs[i] = Math.cos(yaw) * horizontal;
                ys[i] = Math.sin(pitch) * radius;
                zs[i] = Math.sin(yaw) * horizontal;
            }
            drawTexturedPolylineRibbon(xs, ys, zs, pointCount, false, BASIC_ARC_HALF_WIDTH);
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
            int pointCount = segments + 1;
            double[] xs = new double[pointCount];
            double[] ys = new double[pointCount];
            double[] zs = new double[pointCount];
            float[] alphas = new float[pointCount];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double fade = Math.sin(Math.PI * progress);
                double angle = startYaw + sweepYaw * progress
                        + deterministicJitter(seed, i, ticks) * jitter * 0.80D;
                double localRadius = radius + deterministicJitter(seed + 71, i, ticks) * jitter;
                double localY = y + lift * fade
                        + deterministicJitter(seed + 137, i, ticks) * jitter * 0.85D;
                xs[i] = Math.cos(angle) * localRadius;
                ys[i] = localY;
                zs[i] = Math.sin(angle) * localRadius;
                alphas[i] = alpha * (0.18F + 0.82F * (float) fade);
            }
            drawColoredPolylineRibbon(xs, ys, zs, alphas, pointCount, false, BASIC_ARC_HALF_WIDTH, rgb);
        } finally {
            shader.end();
        }
    }

    static void drawShaderJaggedArc(ShaderProgram shader, double radius, double startYaw,
                                    double sweepYaw, double y, double lift, double jitter,
                                    int segments, float mode, float layer,
                                    int primaryColor, int secondaryColor, int accentColor,
                                    float alpha, float pulse, float intensity,
                                    float time, float seed, float ticks, int jitterSeed) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 2 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            int pointCount = segments + 1;
            double[] xs = new double[pointCount];
            double[] ys = new double[pointCount];
            double[] zs = new double[pointCount];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double fade = Math.sin(Math.PI * progress);
                double angle = startYaw + sweepYaw * progress
                        + deterministicJitter(jitterSeed, i, ticks) * jitter * 0.80D;
                double localRadius = radius + deterministicJitter(jitterSeed + 71, i, ticks) * jitter;
                double localY = y + lift * fade
                        + deterministicJitter(jitterSeed + 137, i, ticks) * jitter * 0.85D;
                xs[i] = Math.cos(angle) * localRadius;
                ys[i] = localY;
                zs[i] = Math.sin(angle) * localRadius;
            }
            drawTexturedPolylineRibbon(xs, ys, zs, pointCount, false, BASIC_ARC_HALF_WIDTH);
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
                double innerRadius = Math.max(0.0D, horizontalRadius - BASIC_WIRE_HALF_WIDTH);
                double outerRadius = horizontalRadius + BASIC_WIRE_HALF_WIDTH;
                buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                for (int lon = 0; lon <= gridLon; lon++) {
                    double phi = TWO_PI * lon / gridLon;
                    double cos = Math.cos(phi);
                    double sin = Math.sin(phi);
                    addColoredVertex(buffer, outerRadius * cos, y, outerRadius * sin, rgb, alpha);
                    addColoredVertex(buffer, innerRadius * cos, y, innerRadius * sin, rgb, alpha);
                }
                tessellator.draw();
            }

            for (int lon = 0; lon < gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
                int pointCount = gridLat + 1;
                double[] xs = new double[pointCount];
                double[] ys = new double[pointCount];
                double[] zs = new double[pointCount];
                float[] alphas = new float[pointCount];
                for (int lat = 0; lat <= gridLat; lat++) {
                    double theta = Math.PI * lat / gridLat;
                    xs[lat] = radius * Math.sin(theta) * Math.cos(phi);
                    ys[lat] = radius * Math.cos(theta);
                    zs[lat] = radius * Math.sin(theta) * Math.sin(phi);
                    alphas[lat] = alpha;
                }
                drawColoredPolylineRibbon(xs, ys, zs, alphas, pointCount, false, BASIC_WIRE_HALF_WIDTH, rgb);
            }
        } finally {
            shader.end();
        }
    }

    static void drawShaderWireSphere(ShaderProgram shader, double radius, float mode, float layer,
                                     int primaryColor, int secondaryColor, int accentColor,
                                     float alpha, float pulse, float intensity,
                                     float time, float seed, int gridLat, int gridLon) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D
                || gridLat < 2 || gridLon < 3 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            for (int lat = 1; lat < gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                double y = radius * Math.cos(theta);
                double horizontalRadius = radius * Math.sin(theta);
                double innerRadius = Math.max(0.0D, horizontalRadius - BASIC_WIRE_HALF_WIDTH);
                double outerRadius = horizontalRadius + BASIC_WIRE_HALF_WIDTH;
                buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
                for (int lon = 0; lon <= gridLon; lon++) {
                    double progress = lon / (double) gridLon;
                    double phi = TWO_PI * progress;
                    double cos = Math.cos(phi);
                    double sin = Math.sin(phi);
                    buffer.pos(outerRadius * cos, y, outerRadius * sin)
                            .tex(progress, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
                    buffer.pos(innerRadius * cos, y, innerRadius * sin)
                            .tex(progress, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
                }
                tessellator.draw();
            }

            for (int lon = 0; lon < gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
                int pointCount = gridLat + 1;
                double[] xs = new double[pointCount];
                double[] ys = new double[pointCount];
                double[] zs = new double[pointCount];
                for (int lat = 0; lat <= gridLat; lat++) {
                    double theta = Math.PI * lat / gridLat;
                    xs[lat] = radius * Math.sin(theta) * Math.cos(phi);
                    ys[lat] = radius * Math.cos(theta);
                    zs[lat] = radius * Math.sin(theta) * Math.sin(phi);
                }
                drawTexturedPolylineRibbon(xs, ys, zs, pointCount, false, BASIC_WIRE_HALF_WIDTH);
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
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < rayCount; i++) {
                double angle = TWO_PI * i / rayCount + phase;
                double rayPulse = 0.74D + 0.26D * Math.sin(phase * 7.0D + i * 1.618D);
                double outer = innerRadius + (outerRadius - innerRadius) * rayPulse;
                addColoredSegmentRibbon(buffer,
                        Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                        Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                        BASIC_RAY_HALF_WIDTH, rgb, alpha * 0.34F, alpha);
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    static void drawShaderStarRays(ShaderProgram shader, double innerRadius, double outerRadius,
                                   int rayCount, float mode, float layer,
                                   int primaryColor, int secondaryColor, int accentColor,
                                   float alpha, float pulse, float intensity,
                                   float time, float seed, double phase) {
        if (shader == null || alpha <= 0.005F || innerRadius <= 0.0D
                || outerRadius <= innerRadius || rayCount < 2 || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, mode, layer, primaryColor, secondaryColor, accentColor,
                    alpha, pulse, intensity, time, seed);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i < rayCount; i++) {
                double angle = TWO_PI * i / rayCount + phase;
                double rayPulse = 0.74D + 0.26D * Math.sin(phase * 7.0D + i * 1.618D);
                double outer = innerRadius + (outerRadius - innerRadius) * rayPulse;
                RenderHelper.addTexturedLine(buffer,
                        Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                        Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                        BASIC_RAY_HALF_WIDTH);
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

    private static void drawColoredPolylineRibbon(double[] xs, double[] ys, double[] zs,
                                                  float[] alphas, int pointCount, boolean closed,
                                                  double halfWidth, float[] rgb) {
        if (pointCount < 2) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int vertexCount = closed ? pointCount + 1 : pointCount;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < vertexCount; i++) {
            int index = closed ? i % pointCount : i;
            int previous = closed ? (index + pointCount - 1) % pointCount : Math.max(0, index - 1);
            int next = closed ? (index + 1) % pointCount : Math.min(pointCount - 1, index + 1);
            double[] side = sideVector(xs[next] - xs[previous], ys[next] - ys[previous], zs[next] - zs[previous]);
            double u = vertexCount <= 1 ? 0.0D : i / (double) (vertexCount - 1);

            addColoredVertex(buffer, xs[index] + side[0] * halfWidth, ys[index] + side[1] * halfWidth,
                    zs[index] + side[2] * halfWidth, rgb, alphas[index]);
            addColoredVertex(buffer, xs[index] - side[0] * halfWidth, ys[index] - side[1] * halfWidth,
                    zs[index] - side[2] * halfWidth, rgb, alphas[index]);
        }
        tessellator.draw();
    }

    private static void drawTexturedPolylineRibbon(double[] xs, double[] ys, double[] zs,
                                                   int pointCount, boolean closed, double halfWidth) {
        if (pointCount < 2) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int vertexCount = closed ? pointCount + 1 : pointCount;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < vertexCount; i++) {
            int index = closed ? i % pointCount : i;
            int previous = closed ? (index + pointCount - 1) % pointCount : Math.max(0, index - 1);
            int next = closed ? (index + 1) % pointCount : Math.min(pointCount - 1, index + 1);
            double[] side = sideVector(xs[next] - xs[previous], ys[next] - ys[previous], zs[next] - zs[previous]);
            double u = vertexCount <= 1 ? 0.0D : i / (double) (vertexCount - 1);

            addTexturedVertex(buffer, xs[index] + side[0] * halfWidth, ys[index] + side[1] * halfWidth,
                    zs[index] + side[2] * halfWidth, u, 1.0D, side[0], side[1], side[2]);
            addTexturedVertex(buffer, xs[index] - side[0] * halfWidth, ys[index] - side[1] * halfWidth,
                    zs[index] - side[2] * halfWidth, u, 0.0D, -side[0], -side[1], -side[2]);
        }
        tessellator.draw();
    }

    private static void addTexturedVertex(BufferBuilder buffer, double x, double y, double z,
                                          double u, double v, double normalX, double normalY, double normalZ) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) normalX, (float) normalY, (float) normalZ)
                .endVertex();
    }

    private static void addColoredSegmentRibbon(BufferBuilder buffer,
                                                double x1, double y1, double z1,
                                                double x2, double y2, double z2,
                                                double halfWidth, float[] rgb,
                                                float alpha1, float alpha2) {
        double[] side = sideVector(x2 - x1, y2 - y1, z2 - z1);
        double sx = side[0] * halfWidth;
        double sy = side[1] * halfWidth;
        double sz = side[2] * halfWidth;

        addColoredVertex(buffer, x1 + sx, y1 + sy, z1 + sz, rgb, alpha1);
        addColoredVertex(buffer, x2 + sx, y2 + sy, z2 + sz, rgb, alpha2);
        addColoredVertex(buffer, x2 - sx, y2 - sy, z2 - sz, rgb, alpha2);
        addColoredVertex(buffer, x1 + sx, y1 + sy, z1 + sz, rgb, alpha1);
        addColoredVertex(buffer, x2 - sx, y2 - sy, z2 - sz, rgb, alpha2);
        addColoredVertex(buffer, x1 - sx, y1 - sy, z1 - sz, rgb, alpha1);
    }

    private static void addColoredVertex(BufferBuilder buffer, double x, double y, double z,
                                         float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }

    private static double[] sideVector(double dx, double dy, double dz) {
        double axisX = Math.abs(dy) > 0.82D ? 1.0D : 0.0D;
        double axisY = Math.abs(dy) > 0.82D ? 0.0D : 1.0D;
        double sideX = dy * 0.0D - dz * axisY;
        double sideY = dz * axisX - dx * 0.0D;
        double sideZ = dx * axisY - dy * axisX;
        double length = Math.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);
        if (length < EPSILON) {
            return new double[]{1.0D, 0.0D, 0.0D};
        }
        return new double[]{sideX / length, sideY / length, sideZ / length};
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
