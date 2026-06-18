package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSingularityLattice extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.30D;
    private static final double LATTICE_RADIUS = 1.23D;
    private static final int RING_COUNT = 5;
    private static final int NODES_PER_RING = 10;
    private static final int COLLAPSE_DOT_COUNT = 22;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram techShader = ShaderManager.getProgram("tech_effect");
        ShaderProgram basicShader = ShaderManager.getProgram("basic");
        if (techShader == null) {
            return;
        }

        try {
            drawDimSingularity(techShader, basicShader, ticks);
            drawLatticeShell(techShader, ticks);
            drawPulseLinks(techShader, ticks);
            drawCollapsingDots(techShader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("singularity lattice shader render failed: " + ex.getMessage());
        }
    }

    private void drawDimSingularity(ShaderProgram techShader, ShaderProgram basicShader, float ticks) {
        float breath = smoothPulse(ticks * 0.050D);
        float flicker = wave(ticks * 0.113D);

        useAlphaBlend();
        if (basicShader != null && basicShader.begin()) {
            try {
                setBasicUniforms(basicShader, 1.0F, 0x010104);
                drawColorSphere(CORE_RADIUS * (1.00D + breath * 0.035D), 20, 22,
                        0.0F, 0.0F, 0.0F, 0.93F);
            } finally {
                basicShader.end();
            }
        }

        if (!techShader.begin()) {
            return;
        }

        try {
            useAdditiveBlend();
            setTechUniforms(techShader, ticks, 6.0F, 0.0F, 0x1B1033, 0x3D93FF, 0xDDFBFF,
                    0.090F + flicker * 0.035F, 0.82F, (float) CORE_RADIUS);
            drawShaderSphere(CORE_RADIUS * (1.72D + breath * 0.22D), 18, 20);

            setTechUniforms(techShader, ticks, 6.0F, 0.2F, 0x7DDFFF, 0x7A56FF, 0xFFFFFF,
                    0.065F + breath * 0.045F, 1.08F, 0.58F);
            drawTiltedRing(0.58D + breath * 0.030D, ticks * 0.026F, 71.0F);
            drawTiltedRing(0.50D + flicker * 0.022D, -ticks * 0.032F, -53.0F);
            useAlphaBlend();
        } finally {
            techShader.end();
        }
    }

    private void drawLatticeShell(ShaderProgram shader, float ticks) {
        if (!shader.begin()) {
            return;
        }

        try {
            useAdditiveBlend();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(ticks * 0.055F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(11.0F + (float) Math.sin(ticks * 0.021D) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) Math.sin(ticks * 0.017D) * 4.0F, 0.0F, 0.0F, 1.0F);

            for (int ring = 0; ring < RING_COUNT; ring++) {
                for (int node = 0; node < NODES_PER_RING; node++) {
                    if ((node + ring) % 3 != 1) {
                        drawNeighborLink(shader, ticks, ring, node, ring, node + 1, 0.055F, 0.18F);
                    }
                    if (ring < RING_COUNT - 1 && (node + ring) % 2 == 0) {
                        drawNeighborLink(shader, ticks, ring, node, ring + 1, node + (ring % 2), 0.045F, 0.14F);
                    }
                }
            }

            for (int ring = 0; ring < RING_COUNT; ring++) {
                for (int node = 0; node < NODES_PER_RING; node++) {
                    drawLatticeNode(shader, ticks, ring, node);
                }
            }

            GlStateManager.popMatrix();
            useAlphaBlend();
        } finally {
            shader.end();
        }
    }

    private void drawPulseLinks(ShaderProgram shader, float ticks) {
        if (!shader.begin()) {
            return;
        }

        try {
            useAdditiveBlend();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(ticks * 0.055F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(11.0F + (float) Math.sin(ticks * 0.021D) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) Math.sin(ticks * 0.017D) * 4.0F, 0.0F, 0.0F, 1.0F);

            for (int i = 0; i < 18; i++) {
                int ring = i % RING_COUNT;
                int node = (i * 4 + ring) % NODES_PER_RING;
                boolean vertical = i % 3 == 0 && ring < RING_COUNT - 1;
                LatticePoint a = latticePoint(ring, node, ticks);
                LatticePoint b = vertical
                        ? latticePoint(ring + 1, node + 1, ticks)
                        : latticePoint(ring, node + 1, ticks);
                double progress = fract(ticks * (0.018D + (i % 4) * 0.002D) + i * 0.137D);
                float alpha = (float) Math.sin(progress * Math.PI);
                double span = 0.25D + (i % 3) * 0.055D;
                double start = clamp01(progress - span * 0.5D);
                double end = clamp01(progress + span * 0.5D);
                LatticePoint p0 = interpolate(a, b, start);
                LatticePoint p1 = interpolate(a, b, end);
                int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x9FF6FF : 0xA178FF);

                setTechUniforms(shader, ticks + i * 0.19F, 6.0F, 3.0F + i * 0.04F,
                        color, 0x79DFFF, 0xFFFFFF, 0.13F + alpha * 0.32F, 1.45F, 0.42F);
                drawShaderLine(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z);
            }

            GlStateManager.popMatrix();
            useAlphaBlend();
        } finally {
            shader.end();
        }
    }

    private void drawCollapsingDots(ShaderProgram shader, float ticks) {
        if (!shader.begin()) {
            return;
        }

        try {
            useAdditiveBlend();
            for (int i = 0; i < COLLAPSE_DOT_COUNT; i++) {
                double progress = fract(ticks * (0.010D + (i % 5) * 0.0016D) + i * 0.071D);
                double eased = progress * progress;
                double radius = lerp(1.34D, CORE_RADIUS * 0.78D, eased);
                double theta = Math.acos(1.0D - 2.0D * fract(i * 0.38196601125D));
                double phi = TWO_PI * fract(i * 0.61803398875D) + ticks * (0.010D + (i % 4) * 0.002D);
                double spiral = (1.0D - eased) * Math.sin(progress * Math.PI * 2.0D + i) * 0.12D;
                double x = Math.sin(theta) * Math.cos(phi + spiral) * radius;
                double y = Math.cos(theta) * radius * 0.86D;
                double z = Math.sin(theta) * Math.sin(phi + spiral) * radius;
                float fade = (float) Math.sin(progress * Math.PI);
                double size = 0.014D + (i % 4) * 0.004D + fade * 0.010D;
                int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x8EEAFF : 0xA56BFF);

                setTechUniforms(shader, ticks + i * 0.07F, 6.0F, 4.0F,
                        color, 0x69D9FF, 0xFFFFFF, 0.08F + fade * 0.34F, 1.32F, (float) size);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                drawShaderSphere(size, 6, 7);
                GlStateManager.popMatrix();
            }
            useAlphaBlend();
        } finally {
            shader.end();
        }
    }

    private static void drawNeighborLink(ShaderProgram shader, float ticks,
                                         int ringA, int nodeA, int ringB, int nodeB,
                                         float baseAlpha, float pulseAlpha) {
        LatticePoint a = latticePoint(ringA, nodeA, ticks);
        LatticePoint b = latticePoint(ringB, nodeB, ticks);
        double pulse = smoothPulse(ticks * 0.034D + ringA * 0.71D + nodeA * 0.43D);
        float alpha = baseAlpha + (float) pulse * pulseAlpha;
        int color = (nodeA + ringA) % 5 == 0 ? 0xF4FFFF : ((nodeA + ringA) % 2 == 0 ? 0x8BDFFF : 0x9B77FF);

        setTechUniforms(shader, ticks, 6.0F, 2.0F + ringA * 0.10F, color, 0x68CFFF, 0xFFFFFF,
                alpha, 1.12F, (float) LATTICE_RADIUS);
        drawBrokenLink(a, b, ticks * 0.012D + ringA * 0.19D + nodeA * 0.07D);
    }

    private static void drawLatticeNode(ShaderProgram shader, float ticks, int ring, int node) {
        LatticePoint point = latticePoint(ring, node, ticks);
        float pulse = wave(ticks * 0.060D + ring * 0.68D + node * 0.49D);
        double tier = Math.abs(ring - (RING_COUNT - 1) * 0.5D);
        double size = 0.026D + (2.0D - Math.min(2.0D, tier)) * 0.006D
                + (node % 3) * 0.003D + pulse * 0.012D;
        int color = node % 5 == 0 ? 0xFFFFFF : (node % 2 == 0 ? 0xA9F5FF : 0xAA77FF);

        GlStateManager.pushMatrix();
        GlStateManager.translate(point.x, point.y, point.z);
        setTechUniforms(shader, ticks, 6.0F, 1.0F + ring * 0.11F, color, 0x77DAFF, 0xFFFFFF,
                0.24F + pulse * 0.26F, 1.38F, (float) size);
        drawShaderSphere(size, 7, 8);

        if ((ring + node) % 4 == 0) {
            setTechUniforms(shader, ticks, 6.0F, 1.6F + ring * 0.08F, color, 0xA778FF, 0xFFFFFF,
                    0.055F + pulse * 0.070F, 1.12F, (float) (size * 2.6D));
            drawShaderSphere(size * 2.6D, 6, 7);
        }
        GlStateManager.popMatrix();
    }

    private static LatticePoint latticePoint(int ring, int node, float ticks) {
        int wrappedNode = Math.floorMod(node, NODES_PER_RING);
        double latProgress = (ring + 1.0D) / (RING_COUNT + 1.0D);
        double theta = Math.PI * latProgress;
        double ringOffset = ring % 2 == 0 ? 0.0D : Math.PI / NODES_PER_RING;
        double phi = TWO_PI * wrappedNode / NODES_PER_RING + ringOffset;
        double wrinkle = Math.sin(ticks * 0.026D + ring * 1.17D + wrappedNode * 0.63D) * 0.045D;
        double radius = LATTICE_RADIUS + wrinkle
                + Math.sin(ticks * 0.013D + ring * 0.52D - wrappedNode * 0.37D) * 0.025D;
        double twist = Math.sin(ticks * 0.018D + ring * 0.84D) * 0.035D
                + Math.cos(ticks * 0.016D + wrappedNode * 0.58D) * 0.025D;
        double sinTheta = Math.sin(theta);
        double y = Math.cos(theta) * radius * 0.88D
                + Math.sin(ticks * 0.022D + wrappedNode * 0.81D) * 0.025D;
        double x = Math.cos(phi + twist) * sinTheta * radius;
        double z = Math.sin(phi + twist) * sinTheta * radius;

        return new LatticePoint(x, y, z);
    }

    private static void drawBrokenLink(LatticePoint a, LatticePoint b, double phase) {
        double gap = 0.16D;
        for (int segment = 0; segment < 3; segment++) {
            double start = segment / 3.0D + 0.025D + Math.sin(phase + segment) * 0.018D;
            double end = start + (1.0D / 3.0D - gap);
            LatticePoint p0 = interpolate(a, b, clamp01(start));
            LatticePoint p1 = interpolate(a, b, clamp01(end));
            drawShaderLine(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z);
        }
    }

    private static void drawTiltedRing(double radius, float rotation, float tilt) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(tilt, 1.0F, 0.0F, 0.0F);
        drawShaderCircle(radius, 72);
        GlStateManager.popMatrix();
    }

    private static void drawShaderCircle(double radius, int segments) {
        RenderHelper.drawTexturedCircle(radius, segments, 0.014D);
    }

    private static void drawShaderLine(double x0, double y0, double z0, double x1, double y1, double z1) {
        RenderHelper.drawTexturedLine(x0, y0, z0, x1, y1, z1, 0.014D);
    }

    private static void drawShaderSphere(double radius, int latSegs, int lonSegs) {
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

    private static void drawColorSphere(double radius, int latSegs, int lonSegs,
                                        float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = TWO_PI * lon / lonSegs;
                double phi1 = TWO_PI * (lon + 1) / lonSegs;
                addColorSphereVertex(buffer, radius, theta0, phi0, red, green, blue, alpha);
                addColorSphereVertex(buffer, radius, theta1, phi0, red, green, blue, alpha);
                addColorSphereVertex(buffer, radius, theta1, phi1, red, green, blue, alpha);
                addColorSphereVertex(buffer, radius, theta0, phi0, red, green, blue, alpha);
                addColorSphereVertex(buffer, radius, theta1, phi1, red, green, blue, alpha);
                addColorSphereVertex(buffer, radius, theta0, phi1, red, green, blue, alpha);
            }
        }
        tessellator.draw();
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi, double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void addColorSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                             float red, float green, float blue, float alpha) {
        buffer.pos(Math.sin(theta) * Math.cos(phi) * radius,
                        Math.cos(theta) * radius,
                        Math.sin(theta) * Math.sin(phi) * radius)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static LatticePoint interpolate(LatticePoint a, LatticePoint b, double progress) {
        return new LatticePoint(
                lerp(a.x, b.x, progress),
                lerp(a.y, b.y, progress),
                lerp(a.z, b.z, progress));
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static float smoothPulse(double time) {
        double sine = 0.5D + 0.5D * Math.sin(time);
        return (float) (sine * sine * (3.0D - 2.0D * sine));
    }

    private static void setTechUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                        int primary, int secondary, int tertiary,
                                        float alpha, float intensity, float scale) {
        shader.setUniform1f("uTime", ticks * 0.040F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uScale", scale);
        setUniformColor(shader, "uPrimaryColor", primary);
        setUniformColor(shader, "uSecondaryColor", secondary);
        setUniformColor(shader, "uTertiaryColor", tertiary);
    }

    private static void setBasicUniforms(ShaderProgram shader, float alpha, int tint) {
        float[] rgb = RenderHelper.unpackRGB(tint);
        shader.setUniform1f("alpha", alpha);
        shader.setUniform4f("tint", rgb[0], rgb[1], rgb[2], 1.0F);
    }

    private static void setUniformColor(ShaderProgram shader, String name, int color) {
        float[] rgb = RenderHelper.unpackRGB(color);
        shader.setUniform3f(name, rgb[0], rgb[1], rgb[2]);
    }

    private static final class LatticePoint {
        private final double x;
        private final double y;
        private final double z;

        private LatticePoint(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
