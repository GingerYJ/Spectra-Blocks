package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderEventHorizonMirror extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final float EFFECT_GRAVITATIONAL_LENS = 2.0F;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double MIRROR_RADIUS = 1.12D;
    private static final double FIELD_RADIUS = 1.32D;
    private static final int DISC_SEGMENTS = 112;
    private static final int SPHERE_LAT_SEGMENTS = 28;
    private static final int SPHERE_LON_SEGMENTS = 36;
    private static final int REFLECTION_BANDS = 7;
    private static final int BAND_SEGMENTS = 44;
    private static final int CAUSTIC_ARCS = 5;
    private static final int ARC_SEGMENTS = 36;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    private static final int[] REFLECTION_COLORS = new int[]{
            0xEAFBFF, 0x9CCFE3, 0xC9EFFF, 0xF7FFFF
    };

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram spaceShader = ShaderManager.getProgram("space_effect");
        ShaderProgram basicShader = ShaderManager.getProgram("basic");
        if (spaceShader == null && basicShader == null) {
            return;
        }

        if (basicShader != null) {
            drawDarkMirrorDisc(basicShader, ticks);
        }
        if (spaceShader != null) {
            drawMirrorField(spaceShader, ticks);
        }
        if (basicShader != null) {
            drawSilverBlueEdge(basicShader, ticks);
            drawReflectionBands(basicShader, ticks);
            drawCausticArcs(basicShader, ticks);
        }
    }

    private void drawDarkMirrorDisc(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.018D);

        useAlphaBlend();
        drawRadialDisc(shader, MIRROR_RADIUS * 1.01D, 0.0D,
                0x000102, 0.76F, 0x030A12, 0.58F + pulse * 0.035F, DISC_SEGMENTS);
        drawRadialDisc(shader, MIRROR_RADIUS * 0.82D, 0.004D,
                0x010305, 0.42F, 0x0D1720, 0.10F + pulse * 0.025F, DISC_SEGMENTS);
    }

    private void drawMirrorField(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.020D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.010F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(4.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.0D, 0.105D + pulse * 0.010D, 1.0D);
        drawSpaceSphere(shader, ticks, MIRROR_RADIUS + pulse * 0.012D,
                0.0F, 0.42F, 0.17F,
                0x020407, 0x14202A, 0x83C7DF, 0xF4FFFF,
                SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * 0.006F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(11.0F, 0.0F, 0.0F, 1.0F);
        drawSpaceSphere(shader, ticks, FIELD_RADIUS,
                0.0F, 0.18F, 0.59F,
                0x020407, 0x5F91A8, 0xBFEFFF, 0xFFFFFF,
                24, 32);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSilverBlueEdge(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.026D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.012D, 0.0D);
        RenderNaturalShaderHelper.drawBasicCircle(shader, MIRROR_RADIUS * 1.012D,
                0xEAFBFF, 0.34F + pulse * 0.09F, DISC_SEGMENTS);
        RenderNaturalShaderHelper.drawBasicCircle(shader, MIRROR_RADIUS * 1.035D,
                0x78BBD6, 0.14F + pulse * 0.045F, DISC_SEGMENTS);
        RenderNaturalShaderHelper.drawBasicFlatRing(shader, MIRROR_RADIUS * 0.987D, MIRROR_RADIUS * 1.030D,
                0xBDEEFF, 0.035F + pulse * 0.018F, DISC_SEGMENTS);
        GlStateManager.popMatrix();

        for (int i = 0; i < 3; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(54.0F + i * 23.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) (ticks * (0.003D + i * 0.001D) + i * 61.0D),
                    0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicCircle(shader, FIELD_RADIUS * (0.96D + i * 0.025D),
                    i == 1 ? 0xF7FFFF : 0x9FD7EA, 0.060F + pulse * 0.030F, 104);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawReflectionBands(ShaderProgram shader, float ticks) {
        if (!shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            useAdditiveBlend();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < REFLECTION_BANDS; i++) {
                double baseAngle = i * TWO_PI / REFLECTION_BANDS
                        + Math.sin(ticks * 0.005D + i * 0.77D) * 0.055D
                        + ticks * (0.0011D + (i % 3) * 0.00025D);
                double offset = -0.48D + i * 0.16D
                        + Math.sin(ticks * 0.009D + i * 1.73D) * 0.034D;
                double safeOffset = Math.min(MIRROR_RADIUS - 0.10D, Math.abs(offset) + 0.09D);
                double halfLength = Math.sqrt(Math.max(0.0D, MIRROR_RADIUS * MIRROR_RADIUS
                        - safeOffset * safeOffset)) * (0.82D + (i % 3) * 0.045D);
                float baseAlpha = 0.030F + wave(ticks * 0.024D + i * 0.61D) * 0.055F;
                int color = REFLECTION_COLORS[i % REFLECTION_COLORS.length];
                double phase = ticks * (0.010D + i * 0.0009D) + i * 0.84D;

                for (int segment = 0; segment < BAND_SEGMENTS; segment++) {
                    ReflectionPoint p0 = reflectionPoint(segment / (double) BAND_SEGMENTS,
                            baseAngle, halfLength, offset, phase, baseAlpha);
                    ReflectionPoint p1 = reflectionPoint((segment + 1.0D) / BAND_SEGMENTS,
                            baseAngle, halfLength, offset, phase, baseAlpha);
                    RenderHelper.addColorLine(buffer, p0.x, p0.y, p0.z, p1.x, p1.y, p1.z,
                            0.014D, color, p0.alpha, p1.alpha);
                }
            }
            tessellator.draw();
        } finally {
            shader.end();
            useAlphaBlend();
        }
    }

    private void drawCausticArcs(ShaderProgram shader, float ticks) {
        if (!shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            useAdditiveBlend();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < CAUSTIC_ARCS; i++) {
                double start = i * GOLDEN_ANGLE + ticks * (0.0024D + i * 0.00045D);
                double sweep = 0.34D + (i % 3) * 0.15D;
                double radius = 0.36D + (i % 4) * 0.16D
                        + Math.sin(ticks * 0.011D + i) * 0.018D;
                double centerAngle = i * 1.37D + ticks * 0.001D;
                double centerX = Math.cos(centerAngle) * 0.13D;
                double centerZ = Math.sin(centerAngle) * 0.13D;
                float alpha = 0.055F + wave(ticks * 0.030D + i * 0.91D) * 0.070F;
                int color = REFLECTION_COLORS[(i + 2) % REFLECTION_COLORS.length];

                for (int segment = 0; segment < ARC_SEGMENTS; segment++) {
                    ReflectionPoint p0 = causticPoint(segment / (double) ARC_SEGMENTS,
                            start, sweep, radius, centerX, centerZ, ticks, i, alpha);
                    ReflectionPoint p1 = causticPoint((segment + 1.0D) / ARC_SEGMENTS,
                            start, sweep, radius, centerX, centerZ, ticks, i, alpha);
                    RenderHelper.addColorLine(buffer, p0.x, p0.y, p0.z, p1.x, p1.y, p1.z,
                            0.016D, color, p0.alpha, p1.alpha);
                }
            }
            tessellator.draw();
        } finally {
            shader.end();
            useAlphaBlend();
        }
    }

    private static ReflectionPoint reflectionPoint(double progress,
                                                   double angle, double halfLength, double offset,
                                                   double phase, float baseAlpha) {
        double centered = progress * 2.0D - 1.0D;
        double localX = centered * halfLength;
        double bend = Math.sin(progress * Math.PI + phase) * 0.046D
                + Math.sin(progress * TWO_PI * 2.0D + phase * 0.37D) * 0.015D;
        double localZ = offset + bend;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = localX * cos - localZ * sin;
        double z = localX * sin + localZ * cos;
        double fade = Math.sin(Math.PI * progress);
        float shimmer = 0.75F + 0.25F * (float) Math.sin(phase + progress * TWO_PI);
        float alpha = (float) (baseAlpha * Math.pow(Math.max(0.0D, fade), 1.45D)) * shimmer;
        return new ReflectionPoint(x, 0.020D + bend * 0.030D, z, alpha);
    }

    private static ReflectionPoint causticPoint(double progress,
                                                double start, double sweep, double radius,
                                                double centerX, double centerZ, float ticks, int seed,
                                                float baseAlpha) {
        double fade = Math.sin(Math.PI * progress);
        double angle = start + sweep * progress
                + Math.sin(progress * Math.PI * 2.0D + ticks * 0.006D + seed) * 0.026D;
        double localRadius = radius + fade * (0.045D + seed * 0.003D);
        double x = centerX + Math.cos(angle) * localRadius;
        double z = centerZ + Math.sin(angle) * localRadius;
        double length = Math.sqrt(x * x + z * z);
        if (length > MIRROR_RADIUS * 0.96D) {
            double clamp = MIRROR_RADIUS * 0.96D / length;
            x *= clamp;
            z *= clamp;
        }
        float alpha = (float) (baseAlpha * Math.pow(Math.max(0.0D, fade), 1.25D));
        return new ReflectionPoint(x, 0.032D + fade * 0.010D, z, alpha);
    }

    private static void drawRadialDisc(ShaderProgram shader, double radius, double y,
                                       int centerColor, float centerAlpha,
                                       int edgeColor, float edgeAlpha, int segments) {
        if (shader == null || radius <= 0.0D || segments < 3 || !shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] center = unpackRGB(centerColor);
            float[] edge = unpackRGB(edgeColor);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(0.0D, y, 0.0D)
                    .color(center[0], center[1], center[2], centerAlpha)
                    .endVertex();
            for (int i = 0; i <= segments; i++) {
                double angle = TWO_PI * i / segments;
                buffer.pos(Math.cos(angle) * radius, y, Math.sin(angle) * radius)
                        .color(edge[0], edge[1], edge[2], edgeAlpha)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private static void drawSpaceSphere(ShaderProgram shader, float ticks, double radius,
                                        float layer, float alpha, float seed,
                                        int primaryColor, int secondaryColor,
                                        int accentColor, int highlightColor,
                                        int latSegs, int lonSegs) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || latSegs < 3 || lonSegs < 3
                || !shader.begin()) {
            return;
        }

        try {
            setSpaceUniforms(shader, ticks, EFFECT_GRAVITATIONAL_LENS, layer, alpha, seed,
                    primaryColor, secondaryColor, accentColor, highlightColor);
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
                    addSphereVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                    addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                    addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                    addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                    addSphereVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
                }
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
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

    private static void setSpaceUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                         float alpha, float seed, int primaryColor, int secondaryColor,
                                         int accentColor, int highlightColor) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.052F + seed * 5.0F);
        shader.setUniform1f("uTime", ticks * 0.030F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform1f("uPulse", pulse);
        setUniformColor(shader, "uPrimaryColor", primaryColor);
        setUniformColor(shader, "uSecondaryColor", secondaryColor);
        setUniformColor(shader, "uAccentColor", accentColor);
        setUniformColor(shader, "uHighlightColor", highlightColor);
    }

    private static void setUniformColor(ShaderProgram shader, String name, int color) {
        shader.setUniform3f(name,
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F);
    }

    private static void setBasicUniforms(ShaderProgram shader) {
        shader.setUniform1f("alpha", 1.0F);
        shader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void putColorVertex(BufferBuilder buffer, double x, double y, double z,
                                       int color, float alpha) {
        float[] rgb = unpackRGB(color);
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }

    private static final class ReflectionPoint {
        private final double x;
        private final double y;
        private final double z;
        private final float alpha;

        private ReflectionPoint(double x, double y, double z, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.alpha = alpha;
        }
    }
}
