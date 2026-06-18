package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderEtherReactorCore extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.34D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int CYAN = 0x64F7FF;
    private static final int PALE_CYAN = 0xBFFFFF;
    private static final int BLUE = 0x2C8DFF;
    private static final int WHITE = 0xF6FFFF;
    private static final int RING_COUNT = 3;
    private static final int PACKET_COUNT = 12;

    private static final double[] RING_RADII = {0.82D, 1.08D, 1.34D};
    private static final double[] RING_WIDTHS = {0.052D, 0.044D, 0.038D};
    private static final float[] RING_TILT_X = {64.0F, -38.0F, 18.0F};
    private static final float[] RING_TILT_Z = {-18.0F, 56.0F, -67.0F};
    private static final float[] RING_SPIN = {0.070F, -0.054F, 0.043F};
    private static final int[] RING_COLORS = {CYAN, PALE_CYAN, BLUE};

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCentralCore(shader, ticks);
            drawReactorRings(shader, ticks);
            drawConvergingPackets(shader, ticks);
            drawInnerCharge(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("ether reactor core shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCentralCore(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.060D);
        float spark = wave(ticks * 0.145D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 0.0F, CYAN, BLUE, WHITE,
                0.12F + pulse * 0.08F, 1.20F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.86D + pulse * 0.22D), 18, 20);

        setTechUniforms(shader, ticks, 6.0F, 0.2F, PALE_CYAN, CYAN, WHITE,
                0.18F + pulse * 0.10F, 1.34F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.26D + spark * 0.10D), 16, 18);

        setTechUniforms(shader, ticks, 6.0F, 0.4F, WHITE, PALE_CYAN, CYAN,
                0.44F + pulse * 0.16F, 1.62F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (0.62D + pulse * 0.08D), 12, 14);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.18F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 0.7F, WHITE, CYAN, PALE_CYAN,
                0.12F + pulse * 0.06F, 1.22F, 0.48F);
        drawOrbitArcSegments(0.48D + pulse * 0.035D, 6, ticks * 0.010D, 0.62D, 5);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawReactorRings(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int ring = 0; ring < RING_COUNT; ring++) {
            double radius = RING_RADII[ring];
            double width = RING_WIDTHS[ring];
            float pulse = smoothPulse(ticks * (0.044D + ring * 0.008D) + ring * 0.81D);
            float spin = ticks * RING_SPIN[ring] + ring * 19.0F;
            int color = RING_COLORS[ring];

            GlStateManager.pushMatrix();
            GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(RING_TILT_X[ring], 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(RING_TILT_Z[ring], 0.0F, 0.0F, 1.0F);

            setTechUniforms(shader, ticks, 6.0F, 1.0F + ring * 0.24F, color, BLUE, WHITE,
                    0.060F + pulse * 0.028F, 1.08F, (float) radius);
            drawRingRibbon(radius - width, radius + width, 72);

            setTechUniforms(shader, ticks, 6.0F, 1.5F + ring * 0.24F, WHITE, color, PALE_CYAN,
                    0.14F + pulse * 0.065F, 1.36F, (float) radius);
            drawOrbitArcSegments(radius + width * 0.42D, 5 + ring, ticks * (0.010D + ring * 0.002D),
                    0.50D - ring * 0.035D, 6);

            setTechUniforms(shader, ticks, 6.0F, 1.9F + ring * 0.22F, color, PALE_CYAN, BLUE,
                    0.085F + pulse * 0.040F, 1.18F, (float) radius);
            drawOrbitArcSegments(radius - width * 1.7D, 7, -ticks * (0.007D + ring * 0.001D),
                    0.28D, 4);

            drawOrbitNodes(shader, ticks, ring, radius);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawOrbitNodes(ShaderProgram shader, float ticks, int ring, double radius) {
        int nodeCount = 4 + ring;
        int color = RING_COLORS[ring];

        for (int i = 0; i < nodeCount; i++) {
            double progress = fract(ticks * (0.012D + ring * 0.0025D) + i / (double) nodeCount + ring * 0.17D);
            double angle = TWO_PI * progress;
            float pulse = wave(ticks * 0.095D + i * 0.84D + ring);
            double nodeRadius = radius + Math.sin(ticks * 0.031D + i) * 0.012D;
            double size = 0.040D + ring * 0.004D + pulse * 0.014D;

            setTechUniforms(shader, ticks + i * 0.13F, 6.0F, 2.8F + ring * 0.12F,
                    i % 3 == 0 ? WHITE : color, PALE_CYAN, BLUE,
                    0.24F + pulse * 0.34F, 1.44F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * nodeRadius, 0.0D, Math.sin(angle) * nodeRadius);
            drawShaderSphere(size, 7, 8);
            GlStateManager.popMatrix();

            setTechUniforms(shader, ticks, 6.0F, 3.2F + ring * 0.12F, WHITE, color, PALE_CYAN,
                    0.080F + pulse * 0.055F, 1.18F, (float) radius);
            drawShortOrbitTail(nodeRadius, angle - 0.16D, angle - 0.045D, 4);
        }
    }

    private void drawConvergingPackets(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < PACKET_COUNT; i++) {
            double progress = fract(ticks * (0.010D + (i % 3) * 0.0018D) + i * 0.137D);
            double eased = progress * progress * (3.0D - 2.0D * progress);
            double radius = lerp(1.48D, 0.20D, eased);
            double angle = TWO_PI * i / PACKET_COUNT + ticks * (0.004D + (i % 4) * 0.0008D);
            double drift = Math.sin(progress * Math.PI + i * 1.37D) * 0.18D * (1.0D - eased);
            double y = Math.sin(progress * Math.PI * 1.35D + i * 0.53D) * 0.36D * (1.0D - eased * 0.75D);
            double x = Math.cos(angle) * radius + Math.cos(angle + Math.PI * 0.5D) * drift;
            double z = Math.sin(angle) * radius + Math.sin(angle + Math.PI * 0.5D) * drift;
            float fade = (float) Math.sin(progress * Math.PI);
            float spark = wave(ticks * 0.110D + i * 0.47D);
            double size = 0.020D + (i % 3) * 0.006D + fade * 0.012D;
            int color = i % 4 == 0 ? WHITE : (i % 2 == 0 ? PALE_CYAN : CYAN);

            setTechUniforms(shader, ticks + i * 0.07F, 6.0F, 4.0F, color, CYAN, WHITE,
                    0.12F + fade * 0.34F + spark * 0.06F, 1.38F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            drawShaderSphere(size, 6, 7);
            GlStateManager.popMatrix();

            double tailRadius = Math.min(1.52D, radius + 0.18D * (1.0D - eased));
            setTechUniforms(shader, ticks, 6.0F, 4.3F, color, BLUE, PALE_CYAN,
                    0.035F + fade * 0.075F, 1.12F, (float) radius);
            drawShaderLine(Math.cos(angle) * tailRadius, y * 1.06D, Math.sin(angle) * tailRadius,
                    x, y, z);
        }
        useAlphaBlend();
    }

    private void drawInnerCharge(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 14; i++) {
            double band = (i + 0.5D) / 14.0D;
            double angle = TWO_PI * band + ticks * (0.014D + (i % 2) * 0.004D);
            double radius = 0.26D + (i % 4) * 0.038D + Math.sin(ticks * 0.052D + i) * 0.012D;
            double y = Math.cos(band * Math.PI * 2.0D + ticks * 0.030D) * 0.18D;
            float pulse = wave(ticks * 0.090D + i * 0.71D);
            double size = 0.010D + (i % 3) * 0.004D;

            setTechUniforms(shader, ticks + i * 0.05F, 6.0F, 5.0F,
                    i % 5 == 0 ? WHITE : PALE_CYAN, CYAN, BLUE,
                    0.16F + pulse * 0.20F, 1.34F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            drawShaderSphere(size, 5, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawRingRibbon(double innerRadius, double outerRadius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addPosition(buffer, cos * outerRadius, 0.0D, sin * outerRadius, progress, 1.0D);
            addPosition(buffer, cos * innerRadius, 0.0D, sin * innerRadius, progress, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawOrbitArcSegments(double radius, int arcs, double phase, double sweep, int segmentsPerArc) {
        for (int arc = 0; arc < arcs; arc++) {
            double start = phase + TWO_PI * arc / arcs;
            drawArc(radius, start, sweep, segmentsPerArc);
        }
    }

    private static void drawShortOrbitTail(double radius, double start, double end, int segments) {
        drawArc(radius, start, end - start, segments);
    }

    private static void drawArc(double radius, double start, double sweep, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double previousAngle = start;
        double previousX = Math.cos(previousAngle) * radius;
        double previousZ = Math.sin(previousAngle) * radius;
        for (int i = 1; i <= segments; i++) {
            double angle = start + sweep * i / (double) segments;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            RenderHelper.addTexturedLine(buffer, previousX, 0.0D, previousZ, x, 0.0D, z, 0.022D);
            previousX = x;
            previousZ = z;
        }
        tessellator.draw();
    }

    private static void drawShaderLine(double x0, double y0, double z0, double x1, double y1, double z1) {
        RenderHelper.drawTexturedLine(x0, y0, z0, x1, y1, z1, 0.018D);
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

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi, double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
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
        setColor(shader, "uPrimaryColor", primary);
        setColor(shader, "uSecondaryColor", secondary);
        setColor(shader, "uTertiaryColor", tertiary);
    }

    private static void setColor(ShaderProgram shader, String uniform, int color) {
        shader.setUniform3f(uniform,
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F);
    }
}
