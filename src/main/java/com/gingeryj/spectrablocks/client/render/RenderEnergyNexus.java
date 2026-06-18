package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileEnergyNexus;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderEnergyNexus extends RenderCelestialEffectBase<TileEnergyNexus> {

    private static final double CORE_RADIUS = 0.46D;
    private static final double NODE_RADIUS = 1.58D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int NODE_COUNT = 4;
    private static final int ENERGY_DOT_COUNT = 36;
    private static final double RIBBON_THIN = 0.014D;
    private static final double RIBBON_NORMAL = 0.020D;
    private static final double RIBBON_MEDIUM = 0.026D;
    private static final double RIBBON_BOLD = 0.036D;
    private static final double RIBBON_BEAM = 0.052D;

    @Override
    protected void renderCelestialEffect(TileEnergyNexus te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawBaseFrame(shader, ticks);
            drawContainmentRings(shader, ticks);
            drawCentralReactor(shader, ticks);
            drawPowerNodes(shader, ticks);
            drawEnergyLinks(shader, ticks);
            drawFluxPackets(shader, ticks);
            drawRingBursts(shader, ticks);
            drawControlledCharge(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("energy nexus shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawBaseFrame(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.035D);

        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double x = Math.cos(angle) * NODE_RADIUS;
            double z = Math.sin(angle) * NODE_RADIUS;
            double inwardX = Math.cos(angle) * (NODE_RADIUS - 0.44D);
            double inwardZ = Math.sin(angle) * (NODE_RADIUS - 0.44D);
            double tangentX = -Math.sin(angle) * 0.24D;
            double tangentZ = Math.cos(angle) * 0.24D;

            setTechUniforms(shader, ticks, 6.0F, 1.0F, 0x6FEAFF, 0x3A8FFF, 0xD8FFFF,
                    0.16F + pulse * 0.07F, 1.05F, (float) NODE_RADIUS);
            drawShaderLine(x, -0.58D, z, x, 0.58D, z, RIBBON_MEDIUM);
            setTechUniforms(shader, ticks, 6.0F, 1.1F, 0x3A8FFF, 0x6FEAFF, 0xD8FFFF,
                    0.13F + pulse * 0.05F, 0.98F, (float) NODE_RADIUS);
            drawShaderLine(inwardX, -0.50D, inwardZ, x, -0.50D, z, RIBBON_NORMAL);
            drawShaderLine(inwardX, 0.50D, inwardZ, x, 0.50D, z, RIBBON_NORMAL);
            setTechUniforms(shader, ticks, 6.0F, 1.2F, 0x8FFFFF, 0x3A8FFF, 0xFFFFFF,
                    0.12F + pulse * 0.04F, 1.05F, (float) NODE_RADIUS);
            drawShaderLine(x - tangentX, -0.58D, z - tangentZ, x + tangentX, -0.58D, z + tangentZ,
                    RIBBON_NORMAL);
            drawShaderLine(x - tangentX, 0.58D, z - tangentZ, x + tangentX, 0.58D, z + tangentZ,
                    RIBBON_NORMAL);
        }
        useAlphaBlend();
    }

    private void drawContainmentRings(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.60D, 0.0D);
        GlStateManager.rotate(ticks * 0.10F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 2.0F, 0x55EFFF, 0xD8FFFF, 0x247DFF,
                0.18F + pulse * 0.04F, 1.10F, 1.05F);
        drawDashedRing(1.05D + pulse * 0.012D, 28, ticks * 0.012D, RIBBON_NORMAL);
        setTechUniforms(shader, ticks, 6.0F, 2.1F, 0xD8FFFF, 0x55EFFF, 0x247DFF,
                0.10F + pulse * 0.03F, 1.00F, 0.92F);
        drawDashedRing(0.92D, 18, -ticks * 0.008D, RIBBON_THIN);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.60D, 0.0D);
        GlStateManager.rotate(-ticks * 0.085F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 2.2F, 0x55EFFF, 0xD8FFFF, 0x247DFF,
                0.16F + pulse * 0.04F, 1.10F, 1.05F);
        drawDashedRing(1.05D + pulse * 0.012D, 28, -ticks * 0.010D, RIBBON_NORMAL);
        setTechUniforms(shader, ticks, 6.0F, 2.3F, 0xD8FFFF, 0x55EFFF, 0x247DFF,
                0.09F + pulse * 0.03F, 1.00F, 0.92F);
        drawDashedRing(0.92D, 18, ticks * 0.007D, RIBBON_THIN);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawCentralReactor(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.060D);
        float breath = smoothBreath(ticks * 0.050D);
        float glowBreath = smoothBreath(ticks * 0.050D - 0.28D);
        double outerBreath = 1.00D + breath * 0.30D;
        double innerBreath = 0.64D + breath * 0.14D;

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 0.0F, 0xAFFFFF, 0x247DFF, 0xFFFFFF,
                0.14F + breath * 0.12F, 1.22F, 1.68F);
        drawEnergyColumn(0.16D + breath * 0.030D, 1.68D + breath * 0.30D);
        setTechUniforms(shader, ticks, 6.0F, 0.1F, 0x247DFF, 0xAFFFFF, 0xFFFFFF,
                0.040F + pulse * 0.026F, 1.02F, 1.34F);
        drawEnergyColumn(0.28D + pulse * 0.010D, 1.34D + breath * 0.16D);

        setTechUniforms(shader, ticks, 6.0F, 0.2F, 0x8FFFFF, 0x47EFFF, 0xFFFFFF,
                0.18F, 1.10F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * 1.34D, 24, 24);
        setTechUniforms(shader, ticks, 6.0F, 0.3F, 0xCFFFFF, 0x47EFFF, 0xFFFFFF,
                0.14F + glowBreath * 0.12F, 1.22F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.46D + glowBreath * 0.34D), 20, 20);
        setTechUniforms(shader, ticks, 6.0F, 0.4F, 0x47EFFF, 0x8FFFFF, 0xFFFFFF,
                0.38F + breath * 0.18F, 1.45F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * outerBreath, 24, 24);
        setTechUniforms(shader, ticks, 6.0F, 0.5F, 0xFFFFFF, 0x47EFFF, 0xAFFFFF,
                0.42F + breath * 0.08F, 1.55F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * innerBreath, 20, 20);

        setTechUniforms(shader, ticks, 6.0F, 2.8F, 0xEFFFFF, 0x47EFFF, 0xFFFFFF,
                0.18F + glowBreath * 0.08F, 1.25F, (float) CORE_RADIUS);
        drawCoreLatitudeBands(CORE_RADIUS * (1.36D + glowBreath * 0.08D), RIBBON_NORMAL);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.34F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 3.0F, 0xFFFFFF, 0x47EFFF, 0xAFFFFF,
                0.14F + breath * 0.10F, 1.26F, 0.58F);
        drawDiamondFrame(0.58D + breath * 0.20D, 0.0D, RIBBON_BOLD);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPowerNodes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double x = Math.cos(angle) * NODE_RADIUS;
            double z = Math.sin(angle) * NODE_RADIUS;
            float pulse = wave(ticks * 0.055D - i * 0.72D);
            int color = i % 2 == 0 ? 0x79F7FF : 0x4B89FF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, 0.0D, z);
            setTechUniforms(shader, ticks, 6.0F, 4.0F, color, 0xFFFFFF, 0x74A8FF,
                    0.34F + pulse * 0.18F, 1.36F, 0.16F);
            drawShaderSphere(0.16D + pulse * 0.030D, 12, 12);
            setTechUniforms(shader, ticks, 6.0F, 4.1F, 0xFFFFFF, color, 0xAFFFFF,
                    0.38F + pulse * 0.16F, 1.55F, 0.085F);
            drawShaderSphere(0.085D, 9, 9);
            setTechUniforms(shader, ticks, 6.0F, 4.2F, color, 0xFFFFFF, 0x74A8FF,
                    0.13F + pulse * 0.08F, 1.20F, 0.58F);
            drawShaderLine(0.0D, -0.58D, 0.0D, 0.0D, 0.58D, 0.0D, RIBBON_NORMAL);
            drawNodeBrace(0.26D, -0.32D, ticks * 0.010D + i * 0.28D, RIBBON_THIN);
            drawNodeBrace(0.26D, 0.32D, -ticks * 0.010D + i * 0.28D, RIBBON_THIN);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawEnergyLinks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            float pulse = wave(ticks * 0.075D - i * 0.65D);
            int color = i % 2 == 0 ? 0x9CFFFF : 0x5EA5FF;

            setTechUniforms(shader, ticks, 6.0F, 5.0F, color, 0xFFFFFF, 0x74A8FF,
                    0.10F + pulse * 0.20F, 1.45F, (float) NODE_RADIUS);
            drawSegmentedBeam(angle, 0.46D, NODE_RADIUS - 0.18D, 0.0D, RIBBON_BEAM);
            setTechUniforms(shader, ticks, 6.0F, 5.1F, 0xFFFFFF, color, 0xAFFFFF,
                    0.07F + pulse * 0.11F, 1.30F, (float) NODE_RADIUS);
            drawSegmentedBeam(angle, 0.70D, NODE_RADIUS + 0.08D, 0.20D, RIBBON_NORMAL);
            drawSegmentedBeam(angle, 0.70D, NODE_RADIUS + 0.08D, -0.20D, RIBBON_NORMAL);
        }
        useAlphaBlend();
    }

    private void drawFluxPackets(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            int color = i % 2 == 0 ? 0xBFFFFF : 0x74A8FF;

            for (int lane = 0; lane < 3; lane++) {
                double laneOffset = (lane - 1) * 0.16D;
                double lanePhase = lane * 0.113D + i * 0.037D;
                for (int packet = 0; packet < 3; packet++) {
                    double progress = fract(ticks * (0.018D + lane * 0.002D)
                            + packet * 0.34D + lanePhase);
                    double eased = progress * progress;
                    double radius = lerp(NODE_RADIUS - 0.08D, 0.42D, eased);
                    double swirl = Math.sin(progress * Math.PI * 1.65D + lane * 2.1D + i) * 0.075D;
                    double offset = (laneOffset + swirl) * (1.0D - eased * 0.70D);
                    double tangentX = -sin * offset;
                    double tangentZ = cos * offset;
                    double y = (lane - 1) * 0.16D * (1.0D - eased * 0.75D)
                            + Math.sin(progress * Math.PI * 2.0D + ticks * 0.030D + i + lane) * 0.065D;
                    float fade = (float) Math.sin(progress * Math.PI);
                    double seedSize = 0.012D + ((packet * 5 + lane * 7 + i * 3) % 6) * 0.006D;
                    double size = seedSize + fade * (0.010D + lane * 0.004D);
                    int packetColor = packetColor(packet, lane, i, color);

                    setTechUniforms(shader, ticks, 6.0F, 6.0F, packetColor, color, 0xFFFFFF,
                            0.12F + fade * 0.42F + (float) eased * 0.12F, 1.38F, (float) size);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(cos * radius + tangentX, y, sin * radius + tangentZ);
                    drawShaderSphere(size, 7, 7);
                    GlStateManager.popMatrix();
                }
            }
        }
        useAlphaBlend();
    }

    private void drawRingBursts(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double phase = fract(ticks * 0.010D + i * 0.25D);
            float alpha = (float) Math.sin(phase * Math.PI);
            if (alpha <= 0.02F) {
                continue;
            }

            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double radius = lerp(0.72D, 1.28D, phase);
            double y = i % 2 == 0 ? 0.60D : -0.60D;

            setTechUniforms(shader, ticks, 6.0F, 7.0F, 0xD8FFFF, 0x74A8FF, 0xFFFFFF,
                    alpha * 0.24F, 1.32F, (float) radius);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            drawPulseArc(radius, 16, RIBBON_MEDIUM + alpha * 0.020D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawControlledCharge(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ENERGY_DOT_COUNT; i++) {
            double band = (i + 0.5D) / ENERGY_DOT_COUNT;
            double angle = TWO_PI * (i % 12) / 12.0D + ticks * (0.006D + (i % 3) * 0.0007D);
            double radius = 0.54D + (i / 12) * 0.28D + Math.sin(ticks * 0.018D + i) * 0.025D;
            double y = -0.48D + band * 0.96D;
            int color = i % 3 == 0 ? 0xFFFFFF : (i % 3 == 1 ? 0x7DF7FF : 0x4A8CFF);
            float alpha = 0.10F + 0.15F * wave(ticks * 0.050D + i * 0.41D);
            double size = 0.014D + (i % 3) * 0.004D;

            setTechUniforms(shader, ticks, 6.0F, 8.0F, color, 0x7DF7FF, 0xFFFFFF,
                    alpha, 1.22F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            drawShaderSphere(size, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawDiamondFrame(double radius, double y, double width) {
        drawShaderLine(0.0D, y + radius, 0.0D, radius, y, 0.0D, width);
        drawShaderLine(radius, y, 0.0D, 0.0D, y - radius, 0.0D, width);
        drawShaderLine(0.0D, y - radius, 0.0D, -radius, y, 0.0D, width);
        drawShaderLine(-radius, y, 0.0D, 0.0D, y + radius, 0.0D, width);
        drawShaderLine(0.0D, y, radius, radius, y, 0.0D, width * 0.72D);
        drawShaderLine(radius, y, 0.0D, 0.0D, y, -radius, width * 0.72D);
        drawShaderLine(0.0D, y, -radius, -radius, y, 0.0D, width * 0.72D);
        drawShaderLine(-radius, y, 0.0D, 0.0D, y, radius, width * 0.72D);
    }

    private static void drawNodeBrace(double radius, double y, double phase, double width) {
        int braces = 3;
        int segmentsPerBrace = 4;
        double braceLength = Math.PI * 0.34D;
        for (int brace = 0; brace < braces; brace++) {
            double center = phase + TWO_PI * brace / braces;
            double start = center - braceLength * 0.5D;
            for (int segment = 0; segment < segmentsPerBrace; segment++) {
                double a0 = start + braceLength * segment / segmentsPerBrace;
                double a1 = start + braceLength * (segment + 1) / segmentsPerBrace;
                drawShaderLine(Math.cos(a0) * radius, y, Math.sin(a0) * radius,
                        Math.cos(a1) * radius, y, Math.sin(a1) * radius, width);
            }

            double spoke = center;
            drawShaderLine(Math.cos(spoke) * (radius - 0.08D), y, Math.sin(spoke) * (radius - 0.08D),
                    Math.cos(spoke) * (radius + 0.04D), y, Math.sin(spoke) * (radius + 0.04D),
                    width * 0.86D);
        }
    }

    private static void drawSegmentedBeam(double angle, double innerRadius, double outerRadius, double y,
                                          double width) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int segments = 5;
        for (int i = 0; i < segments; i++) {
            double start = innerRadius + (outerRadius - innerRadius) * (i / (double) segments);
            double end = innerRadius + (outerRadius - innerRadius) * ((i + 0.62D) / segments);
            drawShaderLine(cos * start, y, sin * start, cos * end, y, sin * end, width);
        }
    }

    private static void drawDashedRing(double radius, int dashes, double phase, double width) {
        double dashLength = TWO_PI / dashes * 0.54D;
        int segments = 4;
        for (int dash = 0; dash < dashes; dash++) {
            double start = phase + TWO_PI * dash / dashes - dashLength * 0.5D;
            double previousX = Math.cos(start) * radius;
            double previousZ = Math.sin(start) * radius;
            for (int segment = 1; segment <= segments; segment++) {
                double progress = segment / (double) segments;
                double angle = start + dashLength * progress;
                double localRadius = radius + Math.sin(phase * 3.0D + dash * 0.73D + progress * Math.PI) * 0.010D;
                double x = Math.cos(angle) * localRadius;
                double z = Math.sin(angle) * localRadius;
                drawShaderLine(previousX, 0.0D, previousZ, x, 0.0D, z, width);
                previousX = x;
                previousZ = z;
            }
        }
    }

    private static void drawCoreLatitudeBands(double radius, double width) {
        drawShaderCircle(radius, 72, width);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(62.0F, 1.0F, 0.0F, 0.0F);
        drawShaderCircle(radius * 0.96D, 72, width * 0.82D);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        drawShaderCircle(radius * 0.96D, 72, width * 0.82D);
        GlStateManager.popMatrix();
    }

    private static void drawPulseArc(double radius, int segments, double width) {
        double start = -0.58D;
        double sweep = 1.16D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = start + sweep * progress;
            addRingRibbonVertexPair(buffer, radius, angle, width, progress);
        }
        tessellator.draw();
    }

    private static void drawEnergyColumn(double radius, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int segments = 28;
        double halfHeight = height * 0.5D;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            addPosition(buffer, x, -halfHeight, z, progress, 0.0D);
            addPosition(buffer, x, halfHeight, z, progress, 1.0D);
        }
        tessellator.draw();
    }

    private static void drawShaderCircle(double radius, int segments, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            addRingRibbonVertexPair(buffer, radius, angle, width, progress);
        }
        tessellator.draw();
    }

    private static void drawShaderLine(double x0, double y0, double z0, double x1, double y1, double z1,
                                       double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addLineRibbon(buffer, x0, y0, z0, x1, y1, z1, width);
        tessellator.draw();
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

    private static void addRingRibbonVertexPair(BufferBuilder buffer, double radius, double angle,
                                                double width, double progress) {
        double halfWidth = width * 0.5D;
        double inner = Math.max(0.0D, radius - halfWidth);
        double outer = radius + halfWidth;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        addPosition(buffer, cos * outer, 0.0D, sin * outer, progress, 1.0D);
        addPosition(buffer, cos * inner, 0.0D, sin * inner, progress, 0.0D);
    }

    private static void addLineRibbon(BufferBuilder buffer, double x0, double y0, double z0,
                                      double x1, double y1, double z1, double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 1.0E-5D || width <= 0.0D) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        double upX = Math.abs(dy) > 0.92D ? 1.0D : 0.0D;
        double upY = Math.abs(dy) > 0.92D ? 0.0D : 1.0D;
        double sideX = dy * 0.0D - dz * upY;
        double sideY = dz * upX - dx * 0.0D;
        double sideZ = dx * upY - dy * upX;
        double sideLength = Math.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);
        if (sideLength <= 1.0E-5D) {
            sideX = 1.0D;
            sideY = 0.0D;
            sideZ = 0.0D;
            sideLength = 1.0D;
        }

        double halfWidth = width * 0.5D / sideLength;
        sideX *= halfWidth;
        sideY *= halfWidth;
        sideZ *= halfWidth;
        addPosition(buffer, x0 - sideX, y0 - sideY, z0 - sideZ, 0.0D, 0.0D);
        addPosition(buffer, x0 + sideX, y0 + sideY, z0 + sideZ, 0.0D, 1.0D);
        addPosition(buffer, x1 - sideX, y1 - sideY, z1 - sideZ, 1.0D, 0.0D);
        addPosition(buffer, x1 + sideX, y1 + sideY, z1 + sideZ, 1.0D, 1.0D);
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static int packetColor(int packet, int lane, int node, int defaultColor) {
        int selector = Math.floorMod(packet + lane * 2 + node, 5);
        if (selector == 0) {
            return 0xFFFFFF;
        }
        if (selector == 1) {
            return 0xBFFFFF;
        }
        if (selector == 2) {
            return 0x7DF7FF;
        }
        if (selector == 3) {
            return 0x74A8FF;
        }
        return defaultColor;
    }

    private static float smoothBreath(double time) {
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
