package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import com.gingeryj.spectrablocks.tile.TileVoidCrystal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderVoidCrystal extends RenderArcaneShaderTile<TileVoidCrystal> {

    private static final double CRYSTAL_RADIUS = 0.48D;
    private static final double CRYSTAL_HEIGHT = 1.82D;
    private static final double INNER_HALO_RADIUS = 1.18D;
    private static final double OUTER_HALO_RADIUS = 2.15D;
    private static final double RUNE_RING_RADIUS = 1.38D;
    private static final int CRYSTAL_FACETS = 7;
    private static final int RUNE_SEGMENTS = 112;
    private static final int RUNE_MARKS = 18;
    private static final int INWARD_PARTICLE_COUNT = 42;
    private static final int ARC_COUNT = 5;
    private static final float CRYSTAL_ROTATION_SPEED = 0.34F;
    private static final float PARTICLE_PULL_SPEED = 0.018F;
    private static final float RUNE_ROTATION_SPEED = 0.90F;
    private static final float ARC_ROTATION_SPEED = 0.042F;

    @Override
    protected void renderShaderLayers(TileVoidCrystal te, float ticks, ShaderProgram shader) {
        drawHalo(shader, ticks);
        drawCrystal(shader, ticks);
        drawRuneRing(shader, ticks);
        drawInwardParticles(shader, ticks);
        drawShortArcs(shader, ticks);
    }

    private void drawHalo(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                INNER_HALO_RADIUS + pulse * 0.08D, 0x4B168B, 0xB67CFF,
                0.17F + pulse * 0.07F, 1.15F, 8.0F, 0.8F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 24, 24);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                OUTER_HALO_RADIUS + pulse * 0.16D, 0x160022, 0x884CFF,
                0.12F + pulse * 0.05F, 1.35F, 12.0F, 2.1F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 28, 28);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.08F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                OUTER_HALO_RADIUS * 0.86D, -0.42D, 0x884CFF, 0xE8CCFF,
                0.18F + pulse * 0.08F, 1.0F, 10.0F, 3.0F, pulse, RUNE_SEGMENTS);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                OUTER_HALO_RADIUS * 0.86D, 0.46D, 0x5F2A96, 0xD7B7FF,
                0.15F + pulse * 0.07F, 1.0F, 11.0F, 6.0F, pulse, RUNE_SEGMENTS);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCrystal(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.060F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * CRYSTAL_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(7.5F, 0.35F, 0.0F, 1.0F);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks, CRYSTAL_RADIUS, CRYSTAL_HEIGHT,
                CRYSTAL_FACETS, 0x150020, 0xB67CFF, 0.72F + pulse * 0.09F,
                1.22F, 16.0F, 11.0F, pulse);
        useAdditiveBlend();
        GlStateManager.scale(1.025D, 1.020D, 1.025D);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks, CRYSTAL_RADIUS, CRYSTAL_HEIGHT,
                CRYSTAL_FACETS, 0x6F2AA5, 0xF0D7FF, 0.26F + pulse * 0.12F,
                1.8F, 22.0F, 17.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawRuneRing(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.035F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.20F);
        GlStateManager.rotate(ticks * RUNE_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, RUNE_RING_RADIUS,
                0.070D, 0x3E155F, 0xB287FF, 0.28F + pulse * 0.10F,
                1.15F, 18.0F, 23.0F, pulse, RUNE_SEGMENTS);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                RUNE_RING_RADIUS, 0.0D, 0xB287FF, 0xF0D7FF,
                0.34F + pulse * 0.13F, 1.25F, 20.0F, 31.0F, pulse, RUNE_SEGMENTS);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, RUNE_RING_RADIUS,
                0.22D, 0.028D, RUNE_MARKS, 0xD7B7FF, 0x884CFF,
                0.42F + pulse * 0.17F, 1.35F, 26.0F, 37.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawInwardParticles(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < INWARD_PARTICLE_COUNT; i++) {
            double progress = fract(ticks * PARTICLE_PULL_SPEED + i * 0.061D);
            double radius = 2.35D * (1.0D - progress) + 0.20D;
            double angle = i * GOLDEN_ANGLE + ticks * (0.012D + (i % 5) * 0.002D);
            double height = Math.sin(i * 1.618D + ticks * 0.027D) * (0.70D * (1.0D - progress));
            double moteX = Math.cos(angle) * radius;
            double moteZ = Math.sin(angle) * radius;
            double size = 0.024D + (1.0D - progress) * 0.020D;
            float fade = (float) Math.sin(Math.PI * progress);

            GlStateManager.pushMatrix();
            GlStateManager.translate(moteX, height, moteZ);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    i % 3 == 0 ? 0xE8CCFF : 0x8D4CFF, 0xFFFFFF,
                    0.26F + fade * 0.42F, 1.45F, 9.0F, i * 13.0F, fade,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 7, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawShortArcs(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ARC_COUNT; i++) {
            double phase = ticks * ARC_ROTATION_SPEED + i * 1.2566370614359172D;
            double arcPhase = fract(ticks * 0.013D + i * 0.21D);
            float arcAlpha = (float) Math.sin(Math.PI * arcPhase) * 0.46F;
            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks,
                    0.92D + (i % 3) * 0.20D, phase,
                    0.18D + (i % 2) * 0.08D, -0.42D + (i % 4) * 0.26D,
                    0.46D + (i % 2) * 0.18D, 0.055D, 7,
                    i % 2 == 0 ? 0xF0D7FF : 0x9B5BFF, 0xFFFFFF,
                    arcAlpha, 1.5F, 14.0F, i * 23.0F + 5.0F, (float) arcPhase);
        }
        useAlphaBlend();
    }
}

abstract class RenderArcaneShaderTile<T extends TileScalableEffect> extends TileEntitySpecialRenderer<T> {

    protected static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final String ARCANE_SHADER = "arcane_effect";

    @Override
    public final void render(T te, double x, double y, double z,
                             float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }

        ShaderProgram shader = ShaderManager.getProgram(ARCANE_SHADER);
        if (shader == null) {
            return;
        }

        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(1.0D);
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        int previousShadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);

        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        useAlphaBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();

        try {
            renderShaderLayers(te, ticks, shader);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("arcane shader render failed: " + ex.getMessage());
        } finally {
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.shadeModel(previousShadeModel);
            if (textureWasEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
            if (lightingWasEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            GlStateManager.depthMask(true);
            if (blendWasEnabled) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
            useAlphaBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    protected abstract void renderShaderLayers(T te, float ticks, ShaderProgram shader);

    protected static void useAlphaBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    protected static void useAdditiveBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    protected static float wave(double time) {
        return 0.5F + 0.5F * (float) Math.sin(time);
    }

    protected static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    protected static double fract(double value) {
        return value - Math.floor(value);
    }
}

final class ArcaneShaderEffectRenderer {

    static final float LAYER_AURA = 0.0F;
    static final float LAYER_CORE = 1.0F;
    static final float LAYER_RING = 2.0F;
    static final float LAYER_MOTE = 3.0F;
    static final float LAYER_FILAMENT = 4.0F;
    static final float LAYER_PETAL = 5.0F;
    static final float LAYER_SHARD = 6.0F;
    static final float LAYER_CRYSTAL = 7.0F;

    private static final double TWO_PI = Math.PI * 2.0D;

    private ArcaneShaderEffectRenderer() {
    }

    static boolean beginLayer(ShaderProgram shader, float ticks, float layerMode,
                              int primaryColor, int secondaryColor, float alpha,
                              float intensity, float noiseScale, float seed, float pulse) {
        if (alpha <= 0.01F || shader == null || !shader.begin()) {
            return false;
        }

        float[] primary = unpackRGB(primaryColor);
        float[] secondary = unpackRGB(secondaryColor);
        shader.setUniform1f("uTime", ticks * 0.035F);
        shader.setUniform1f("uLayerMode", layerMode);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uNoiseScale", noiseScale);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform1f("uPulse", pulse);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uSecondaryColor", secondary[0], secondary[1], secondary[2]);
        return true;
    }

    static void drawSphereLayer(ShaderProgram shader, float ticks, double radius,
                                int primaryColor, int secondaryColor, float alpha,
                                float intensity, float noiseScale, float seed, float pulse,
                                float layerMode, int latSegs, int lonSegs) {
        if (radius <= 0.0D || !beginLayer(shader, ticks, layerMode, primaryColor, secondaryColor,
                alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawSphereGeometry(radius, latSegs, lonSegs);
        } finally {
            shader.end();
        }
    }

    static void drawCrystalLayer(ShaderProgram shader, float ticks, double radius, double height,
                                 int facets, int primaryColor, int secondaryColor, float alpha,
                                 float intensity, float noiseScale, float seed, float pulse) {
        if (radius <= 0.0D || height <= 0.0D || facets < 3 || !beginLayer(shader, ticks, LAYER_CRYSTAL,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawCrystalGeometry(radius, height, facets);
        } finally {
            shader.end();
        }
    }

    static void drawCircleRibbonLayer(ShaderProgram shader, float ticks, double radius, double width,
                                      int primaryColor, int secondaryColor, float alpha,
                                      float intensity, float noiseScale, float seed, float pulse,
                                      int segments) {
        drawFlatRingLayer(shader, ticks, Math.max(0.0D, radius - width * 0.5D),
                radius + width * 0.5D, primaryColor, secondaryColor, alpha,
                intensity, noiseScale, seed, pulse, segments);
    }

    static void drawFlatRingLayer(ShaderProgram shader, float ticks, double innerRadius, double outerRadius,
                                  int primaryColor, int secondaryColor, float alpha,
                                  float intensity, float noiseScale, float seed, float pulse,
                                  int segments) {
        if (innerRadius < 0.0D || outerRadius <= innerRadius || !beginLayer(shader, ticks, LAYER_RING,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawFlatRingGeometry(innerRadius, outerRadius, segments);
        } finally {
            shader.end();
        }
    }

    static void drawRadialMarksLayer(ShaderProgram shader, float ticks, double radius, double length,
                                     double width, int marks, int primaryColor, int secondaryColor,
                                     float alpha, float intensity, float noiseScale, float seed, float pulse) {
        if (marks <= 0 || length <= 0.0D || width <= 0.0D || !beginLayer(shader, ticks, LAYER_RING,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawRadialMarksGeometry(radius, length, width, marks);
        } finally {
            shader.end();
        }
    }

    static void drawPetalLayer(ShaderProgram shader, float ticks, double angle, double length,
                               double halfWidth, double lift, int rootColor, int tipColor,
                               float alpha, float intensity, float noiseScale, float seed, float pulse) {
        if (length <= 0.0D || halfWidth <= 0.0D || !beginLayer(shader, ticks, LAYER_PETAL,
                rootColor, tipColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawPetalGeometry(angle, length, halfWidth, lift);
        } finally {
            shader.end();
        }
    }

    static void drawShardLayer(ShaderProgram shader, float ticks, double width, double height,
                               int primaryColor, int secondaryColor, float alpha,
                               float intensity, float noiseScale, float seed, float pulse) {
        if (width <= 0.0D || height <= 0.0D || !beginLayer(shader, ticks, LAYER_SHARD,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawShardGeometry(width, height);
        } finally {
            shader.end();
        }
    }

    static void drawLineLayer(ShaderProgram shader, float ticks,
                              double x1, double y1, double z1, double x2, double y2, double z2,
                              int primaryColor, int secondaryColor, float alpha,
                              float intensity, float noiseScale, float seed, float pulse) {
        double width = filamentWidth(alpha, intensity, 0.018D);
        if (!beginLayer(shader, ticks, LAYER_FILAMENT, primaryColor, secondaryColor,
                alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawTubeLineGeometry(x1, y1, z1, x2, y2, z2, width);
        } finally {
            shader.end();
        }
    }

    static void drawJaggedArcLayer(ShaderProgram shader, float ticks, double radius,
                                   double startAngle, double sweep, double yOffset,
                                   double heightWave, double jitter, int segments,
                                   int primaryColor, int secondaryColor, float alpha,
                                   float intensity, float noiseScale, float seed, float pulse) {
        double width = filamentWidth(alpha, intensity, Math.max(0.018D, jitter * 0.62D));
        if (radius <= 0.0D || segments <= 0 || !beginLayer(shader, ticks, LAYER_FILAMENT,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            double[] xs = new double[segments + 1];
            double[] ys = new double[segments + 1];
            double[] zs = new double[segments + 1];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double noise = Math.sin(seed * 12.9898D + i * 78.233D + ticks * 0.030D) * jitter;
                double angle = startAngle + sweep * progress + noise;
                double pointRadius = radius + Math.sin(progress * Math.PI) * jitter * 2.2D;
                double y = yOffset + Math.sin(progress * Math.PI) * heightWave + noise * 0.6D;
                xs[i] = Math.cos(angle) * pointRadius;
                ys[i] = y;
                zs[i] = Math.sin(angle) * pointRadius;
            }
            drawTubePathGeometry(xs, ys, zs, segments + 1, width, true);
        } finally {
            shader.end();
        }
    }

    static void drawHelixLayer(ShaderProgram shader, float ticks, double phase,
                               double baseRadius, double topRadius, double height,
                               double turns, int segments, int primaryColor, int secondaryColor,
                               float alpha, float intensity, float noiseScale, float seed, float pulse) {
        double width = filamentWidth(alpha, intensity, 0.016D);
        if (segments <= 0 || !beginLayer(shader, ticks, LAYER_FILAMENT,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            double[] xs = new double[segments + 1];
            double[] ys = new double[segments + 1];
            double[] zs = new double[segments + 1];
            for (int i = 0; i <= segments; i++) {
                double progress = (double) i / segments;
                double angle = phase + progress * TWO_PI * turns;
                double radius = baseRadius + (topRadius - baseRadius) * progress;
                xs[i] = Math.cos(angle) * radius;
                ys[i] = progress * height - 0.08D;
                zs[i] = Math.sin(angle) * radius;
            }
            drawTubePathGeometry(xs, ys, zs, segments + 1, width, false);
        } finally {
            shader.end();
        }
    }

    static void drawLatitudeCircleLayer(ShaderProgram shader, float ticks, double sphereRadius, double y,
                                        int primaryColor, int secondaryColor, float alpha,
                                        float intensity, float noiseScale, float seed, float pulse,
                                        int segments) {
        double width = filamentWidth(alpha, intensity, 0.020D);
        if (Math.abs(y) >= sphereRadius || !beginLayer(shader, ticks, LAYER_FILAMENT,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            double radius = Math.sqrt(Math.max(0.0D, sphereRadius * sphereRadius - y * y));
            drawLatitudeRibbonGeometry(radius, y, width, segments);
        } finally {
            shader.end();
        }
    }

    static void drawSphereGeometry(double radius, int latSegs, int lonSegs) {
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
    }

    private static void drawCrystalGeometry(double radius, double height, int facets) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double topY = height * 0.5D;
        double bottomY = -height * 0.5D;
        for (int i = 0; i < facets; i++) {
            double progress0 = (double) i / facets;
            double progress1 = (double) (i + 1) / facets;
            double angle0 = TWO_PI * progress0;
            double angle1 = TWO_PI * progress1;
            double x0 = Math.cos(angle0) * radius;
            double z0 = Math.sin(angle0) * radius;
            double x1 = Math.cos(angle1) * radius;
            double z1 = Math.sin(angle1) * radius;
            addTriangle(buffer, 0.0D, topY, 0.0D, 0.5D, 1.0D,
                    x0, 0.0D, z0, progress0, 0.5D,
                    x1, 0.0D, z1, progress1, 0.5D);
            addTriangle(buffer, 0.0D, bottomY, 0.0D, 0.5D, 0.0D,
                    x1, 0.0D, z1, progress1, 0.5D,
                    x0, 0.0D, z0, progress0, 0.5D);
        }
        tessellator.draw();
    }

    private static void drawFlatRingGeometry(double innerRadius, double outerRadius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = TWO_PI * progress;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addVertex(buffer, cos * outerRadius, 0.0D, sin * outerRadius,
                    progress, 1.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, cos * innerRadius, 0.0D, sin * innerRadius,
                    progress, 0.0D, 0.0F, 1.0F, 0.0F);
        }
        tessellator.draw();
    }

    private static void drawRadialMarksGeometry(double radius, double length, double width, int marks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < marks; i++) {
            double progress = (double) i / marks;
            double angle = TWO_PI * progress;
            double markLength = length * (i % 4 == 0 ? 1.32D : 1.0D);
            double markWidth = width * (i % 3 == 0 ? 1.45D : 1.0D);
            double inner = radius - markLength * 0.5D;
            double outer = radius + markLength * 0.5D;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double tx = -sin * markWidth;
            double tz = cos * markWidth;
            addFlatQuad(buffer,
                    cos * inner + tx, sin * inner + tz, progress, 0.0D,
                    cos * outer + tx, sin * outer + tz, progress, 1.0D,
                    cos * outer - tx, sin * outer - tz, progress + 0.015D, 1.0D,
                    cos * inner - tx, sin * inner - tz, progress + 0.015D, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawTubeLineGeometry(double x1, double y1, double z1,
                                             double x2, double y2, double z2,
                                             double width) {
        double[] xs = new double[]{x1, x2};
        double[] ys = new double[]{y1, y2};
        double[] zs = new double[]{z1, z2};
        drawTubePathGeometry(xs, ys, zs, 2, width, false);
    }

    private static void drawTubePathGeometry(double[] xs, double[] ys, double[] zs,
                                             int count, double width, boolean addCaps) {
        if (count < 2 || width <= 0.0D) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        double[] ax = new double[count];
        double[] ay = new double[count];
        double[] az = new double[count];
        double[] bx = new double[count];
        double[] by = new double[count];
        double[] bz = new double[count];

        for (int i = 0; i < count; i++) {
            int previous = Math.max(0, i - 1);
            int next = Math.min(count - 1, i + 1);
            double tx = xs[next] - xs[previous];
            double ty = ys[next] - ys[previous];
            double tz = zs[next] - zs[previous];
            double length = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (length <= 0.0001D && i + 1 < count) {
                tx = xs[i + 1] - xs[i];
                ty = ys[i + 1] - ys[i];
                tz = zs[i + 1] - zs[i];
                length = Math.sqrt(tx * tx + ty * ty + tz * tz);
            }
            if (length <= 0.0001D && i > 0) {
                tx = xs[i] - xs[i - 1];
                ty = ys[i] - ys[i - 1];
                tz = zs[i] - zs[i - 1];
                length = Math.sqrt(tx * tx + ty * ty + tz * tz);
            }
            if (length <= 0.0001D) {
                tx = 0.0D;
                ty = 1.0D;
                tz = 0.0D;
                length = 1.0D;
            }

            tx /= length;
            ty /= length;
            tz /= length;
            double[] basisA = perpendicularUnit(tx, ty, tz);
            ax[i] = basisA[0];
            ay[i] = basisA[1];
            az[i] = basisA[2];
            bx[i] = ty * az[i] - tz * ay[i];
            by[i] = tz * ax[i] - tx * az[i];
            bz[i] = tx * ay[i] - ty * ax[i];
            double bLength = Math.sqrt(bx[i] * bx[i] + by[i] * by[i] + bz[i] * bz[i]);
            if (bLength <= 0.0001D) {
                bx[i] = 0.0D;
                by[i] = 1.0D;
                bz[i] = 0.0D;
            } else {
                bx[i] /= bLength;
                by[i] /= bLength;
                bz[i] /= bLength;
            }
        }

        int sides = 6;
        double radius = width * 0.5D;
        for (int i = 0; i < count - 1; i++) {
            double u0 = i / (double) (count - 1);
            double u1 = (i + 1.0D) / (count - 1);
            for (int side = 0; side < sides; side++) {
                double a0 = TWO_PI * side / sides;
                double a1 = TWO_PI * (side + 1) / sides;
                double n00x = ax[i] * Math.cos(a0) + bx[i] * Math.sin(a0);
                double n00y = ay[i] * Math.cos(a0) + by[i] * Math.sin(a0);
                double n00z = az[i] * Math.cos(a0) + bz[i] * Math.sin(a0);
                double n01x = ax[i] * Math.cos(a1) + bx[i] * Math.sin(a1);
                double n01y = ay[i] * Math.cos(a1) + by[i] * Math.sin(a1);
                double n01z = az[i] * Math.cos(a1) + bz[i] * Math.sin(a1);
                double n10x = ax[i + 1] * Math.cos(a0) + bx[i + 1] * Math.sin(a0);
                double n10y = ay[i + 1] * Math.cos(a0) + by[i + 1] * Math.sin(a0);
                double n10z = az[i + 1] * Math.cos(a0) + bz[i + 1] * Math.sin(a0);
                double n11x = ax[i + 1] * Math.cos(a1) + bx[i + 1] * Math.sin(a1);
                double n11y = ay[i + 1] * Math.cos(a1) + by[i + 1] * Math.sin(a1);
                double n11z = az[i + 1] * Math.cos(a1) + bz[i + 1] * Math.sin(a1);
                double v0 = side / (double) sides;
                double v1 = (side + 1.0D) / sides;

                addVertex(buffer, xs[i] + n00x * radius, ys[i] + n00y * radius, zs[i] + n00z * radius,
                        u0, v0, (float) n00x, (float) n00y, (float) n00z);
                addVertex(buffer, xs[i + 1] + n10x * radius, ys[i + 1] + n10y * radius, zs[i + 1] + n10z * radius,
                        u1, v0, (float) n10x, (float) n10y, (float) n10z);
                addVertex(buffer, xs[i + 1] + n11x * radius, ys[i + 1] + n11y * radius, zs[i + 1] + n11z * radius,
                        u1, v1, (float) n11x, (float) n11y, (float) n11z);
                addVertex(buffer, xs[i] + n00x * radius, ys[i] + n00y * radius, zs[i] + n00z * radius,
                        u0, v0, (float) n00x, (float) n00y, (float) n00z);
                addVertex(buffer, xs[i + 1] + n11x * radius, ys[i + 1] + n11y * radius, zs[i + 1] + n11z * radius,
                        u1, v1, (float) n11x, (float) n11y, (float) n11z);
                addVertex(buffer, xs[i] + n01x * radius, ys[i] + n01y * radius, zs[i] + n01z * radius,
                        u0, v1, (float) n01x, (float) n01y, (float) n01z);
            }
        }

        if (addCaps) {
            addPathCap(buffer, xs[0], ys[0], zs[0], xs[1] - xs[0], ys[1] - ys[0], zs[1] - zs[0],
                    width * 0.5D, 0.0D);
            addPathCap(buffer, xs[count - 1], ys[count - 1], zs[count - 1],
                    xs[count - 2] - xs[count - 1], ys[count - 2] - ys[count - 1], zs[count - 2] - zs[count - 1],
                    width * 0.5D, 1.0D);
        }

        tessellator.draw();
    }

    private static void addPathCap(BufferBuilder buffer, double x, double y, double z,
                                   double dx, double dy, double dz, double radius, double u) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0001D) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        double[] basisA = perpendicularUnit(dx, dy, dz);
        double ax = basisA[0];
        double ay = basisA[1];
        double az = basisA[2];
        double bx = dy * az - dz * ay;
        double by = dz * ax - dx * az;
        double bz = dx * ay - dy * ax;
        int sides = 6;
        for (int side = 0; side < sides; side++) {
            double a0 = TWO_PI * side / sides;
            double a1 = TWO_PI * (side + 1) / sides;
            double n0x = ax * Math.cos(a0) + bx * Math.sin(a0);
            double n0y = ay * Math.cos(a0) + by * Math.sin(a0);
            double n0z = az * Math.cos(a0) + bz * Math.sin(a0);
            double n1x = ax * Math.cos(a1) + bx * Math.sin(a1);
            double n1y = ay * Math.cos(a1) + by * Math.sin(a1);
            double n1z = az * Math.cos(a1) + bz * Math.sin(a1);
            addVertex(buffer, x, y, z, u, 0.5D, (float) dx, (float) dy, (float) dz);
            addVertex(buffer, x + n1x * radius, y + n1y * radius, z + n1z * radius,
                    u, (side + 1.0D) / sides, (float) dx, (float) dy, (float) dz);
            addVertex(buffer, x + n0x * radius, y + n0y * radius, z + n0z * radius,
                    u, side / (double) sides, (float) dx, (float) dy, (float) dz);
        }
    }

    private static void drawLatitudeRibbonGeometry(double radius, double y, double width, int segments) {
        if (radius <= 0.0D || width <= 0.0D || segments <= 2) {
            return;
        }

        double innerRadius = Math.max(0.001D, radius - width * 0.5D);
        double outerRadius = radius + width * 0.5D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            double angle0 = TWO_PI * u0;
            double angle1 = TWO_PI * u1;
            double c0 = Math.cos(angle0);
            double s0 = Math.sin(angle0);
            double c1 = Math.cos(angle1);
            double s1 = Math.sin(angle1);

            addVertex(buffer, c0 * innerRadius, y, s0 * innerRadius,
                    u0, 0.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, c0 * outerRadius, y, s0 * outerRadius,
                    u0, 1.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, c1 * outerRadius, y, s1 * outerRadius,
                    u1, 1.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, c0 * innerRadius, y, s0 * innerRadius,
                    u0, 0.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, c1 * outerRadius, y, s1 * outerRadius,
                    u1, 1.0D, 0.0F, 1.0F, 0.0F);
            addVertex(buffer, c1 * innerRadius, y, s1 * innerRadius,
                    u1, 0.0D, 0.0F, 1.0F, 0.0F);
        }
        tessellator.draw();
    }

    private static double[] perpendicularUnit(double x, double y, double z) {
        double refX = Math.abs(y) > 0.84D ? 1.0D : 0.0D;
        double refY = Math.abs(y) > 0.84D ? 0.0D : 1.0D;
        double refZ = 0.0D;
        double px = y * refZ - z * refY;
        double py = z * refX - x * refZ;
        double pz = x * refY - y * refX;
        double length = Math.sqrt(px * px + py * py + pz * pz);
        if (length <= 0.0001D) {
            return new double[]{1.0D, 0.0D, 0.0D};
        }
        return new double[]{px / length, py / length, pz / length};
    }

    private static double filamentWidth(float alpha, float intensity, double baseWidth) {
        return baseWidth * (0.82D + Math.min(1.8D, Math.max(0.0D, intensity)) * 0.16D)
                + Math.min(0.060D, Math.max(0.0D, alpha) * 0.030D);
    }

    private static void drawPetalGeometry(double angle, double length, double halfWidth, double lift) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double tangentX = -sin;
        double tangentZ = cos;
        double rootRadius = 0.16D;
        double midRadius = length * 0.58D;
        double tipRadius = length;
        double rootY = -0.03D;
        double midY = lift * 0.45D;
        double tipY = lift;

        double rootX = cos * rootRadius;
        double rootZ = sin * rootRadius;
        double leftX = cos * midRadius + tangentX * halfWidth;
        double leftZ = sin * midRadius + tangentZ * halfWidth;
        double rightX = cos * midRadius - tangentX * halfWidth;
        double rightZ = sin * midRadius - tangentZ * halfWidth;
        double tipX = cos * tipRadius;
        double tipZ = sin * tipRadius;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addTriangle(buffer, rootX, rootY, rootZ, 0.0D, 0.0D,
                leftX, midY, leftZ, 0.0D, 0.65D,
                tipX, tipY, tipZ, 0.5D, 1.0D);
        addTriangle(buffer, rootX, rootY, rootZ, 1.0D, 0.0D,
                tipX, tipY, tipZ, 0.5D, 1.0D,
                rightX, midY, rightZ, 1.0D, 0.65D);
        tessellator.draw();
    }

    private static void drawShardGeometry(double width, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double topY = height * 0.58D;
        double baseY = -height * 0.38D;
        double x0 = -width * 0.42D;
        double y0 = baseY + height * 0.04D;
        double z0 = width * 0.10D;
        double x1 = width * 0.36D;
        double y1 = baseY + height * 0.20D;
        double z1 = width * 0.16D;
        double x2 = width * 0.12D;
        double y2 = baseY - height * 0.08D;
        double z2 = -width * 0.38D;
        addTriangle(buffer, 0.0D, topY, 0.0D, 0.5D, 1.0D, x0, y0, z0, 0.0D, 0.0D, x1, y1, z1, 1.0D, 0.0D);
        addTriangle(buffer, 0.0D, topY, 0.0D, 0.5D, 1.0D, x1, y1, z1, 1.0D, 0.0D, x2, y2, z2, 0.5D, 0.0D);
        addTriangle(buffer, 0.0D, topY, 0.0D, 0.5D, 1.0D, x2, y2, z2, 0.5D, 0.0D, x0, y0, z0, 0.0D, 0.0D);
        addTriangle(buffer, x0, y0, z0, 0.0D, 0.0D, x2, y2, z2, 0.5D, 0.0D, x1, y1, z1, 1.0D, 0.0D);
        tessellator.draw();
    }

    private static void addFlatQuad(BufferBuilder buffer,
                                    double x1, double z1, double u1, double v1,
                                    double x2, double z2, double u2, double v2,
                                    double x3, double z3, double u3, double v3,
                                    double x4, double z4, double u4, double v4) {
        addVertex(buffer, x1, 0.0D, z1, u1, v1, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, x2, 0.0D, z2, u2, v2, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, x3, 0.0D, z3, u3, v3, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, x1, 0.0D, z1, u1, v1, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, x3, 0.0D, z3, u3, v3, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, x4, 0.0D, z4, u4, v4, 0.0F, 1.0F, 0.0F);
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                        double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        addVertex(buffer, normalX * radius, normalY * radius, normalZ * radius,
                u, v, normalX, normalY, normalZ);
    }

    private static void addTriangle(BufferBuilder buffer,
                                    double x1, double y1, double z1, double u1, double v1,
                                    double x2, double y2, double z2, double u2, double v2,
                                    double x3, double y3, double z3, double u3, double v3) {
        double ax = x2 - x1;
        double ay = y2 - y1;
        double az = z2 - z1;
        double bx = x3 - x1;
        double by = y3 - y1;
        double bz = z3 - z1;
        double nx = ay * bz - az * by;
        double ny = az * bx - ax * bz;
        double nz = ax * by - ay * bx;
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len <= 0.0001D) {
            nx = 0.0D;
            ny = 1.0D;
            nz = 0.0D;
            len = 1.0D;
        }

        float normalX = (float) (nx / len);
        float normalY = (float) (ny / len);
        float normalZ = (float) (nz / len);
        addVertex(buffer, x1, y1, z1, u1, v1, normalX, normalY, normalZ);
        addVertex(buffer, x2, y2, z2, u2, v2, normalX, normalY, normalZ);
        addVertex(buffer, x3, y3, z3, u3, v3, normalX, normalY, normalZ);
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  double u, double v, float normalX, float normalY, float normalZ) {
        buffer.pos(x, y, z).tex(u, v).normal(normalX, normalY, normalZ).endVertex();
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }
}
