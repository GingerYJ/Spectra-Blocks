package com.gingeryj.spectrablocks.client.render;

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

    @Override
    protected void renderCelestialEffect(TileEnergyNexus te, float ticks) {
        drawBaseFrame(ticks);
        drawContainmentRings(ticks);
        drawCentralReactor(ticks);
        drawPowerNodes(ticks);
        drawEnergyLinks(ticks);
        drawFluxPackets(ticks);
        drawRingBursts(ticks);
        drawControlledCharge(ticks);
    }

    private void drawBaseFrame(float ticks) {
        float pulse = wave(ticks * 0.035D);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.8F);
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double x = Math.cos(angle) * NODE_RADIUS;
            double z = Math.sin(angle) * NODE_RADIUS;
            double inwardX = Math.cos(angle) * (NODE_RADIUS - 0.44D);
            double inwardZ = Math.sin(angle) * (NODE_RADIUS - 0.44D);
            double tangentX = -Math.sin(angle) * 0.24D;
            double tangentZ = Math.cos(angle) * 0.24D;

            RenderHelper.drawLine(x, -0.58D, z, x, 0.58D, z, 0x6FEAFF, 0.16F + pulse * 0.07F);
            RenderHelper.drawLine(inwardX, -0.50D, inwardZ, x, -0.50D, z, 0x3A8FFF, 0.13F + pulse * 0.05F);
            RenderHelper.drawLine(inwardX, 0.50D, inwardZ, x, 0.50D, z, 0x3A8FFF, 0.13F + pulse * 0.05F);
            RenderHelper.drawLine(x - tangentX, -0.58D, z - tangentZ,
                    x + tangentX, -0.58D, z + tangentZ, 0x8FFFFF, 0.12F + pulse * 0.04F);
            RenderHelper.drawLine(x - tangentX, 0.58D, z - tangentZ,
                    x + tangentX, 0.58D, z + tangentZ, 0x8FFFFF, 0.12F + pulse * 0.04F);
        }
        RenderHelper.resetLineWidth();

        useAlphaBlend();
    }

    private void drawContainmentRings(float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.60D, 0.0D);
        GlStateManager.rotate(ticks * 0.10F, 0.0F, 1.0F, 0.0F);
        GlStateManager.glLineWidth(1.35F);
        drawDashedRing(1.05D + pulse * 0.012D, 0x55EFFF, 0.18F + pulse * 0.04F, 28, ticks * 0.012D);
        GlStateManager.glLineWidth(0.9F);
        drawDashedRing(0.92D, 0xD8FFFF, 0.10F + pulse * 0.03F, 18, -ticks * 0.008D);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.60D, 0.0D);
        GlStateManager.rotate(-ticks * 0.085F, 0.0F, 1.0F, 0.0F);
        GlStateManager.glLineWidth(1.25F);
        drawDashedRing(1.05D + pulse * 0.012D, 0x55EFFF, 0.16F + pulse * 0.04F, 28, -ticks * 0.010D);
        GlStateManager.glLineWidth(0.9F);
        drawDashedRing(0.92D, 0xD8FFFF, 0.09F + pulse * 0.03F, 18, ticks * 0.007D);
        GlStateManager.popMatrix();

        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawCentralReactor(float ticks) {
        float pulse = wave(ticks * 0.060D);
        float breath = smoothBreath(ticks * 0.050D);
        float glowBreath = smoothBreath(ticks * 0.050D - 0.28D);
        double outerBreath = 1.00D + breath * 0.30D;
        double innerBreath = 0.64D + breath * 0.14D;

        useAdditiveBlend();
        drawEnergyColumn(0.16D + breath * 0.030D, 1.68D + breath * 0.30D,
                0xAFFFFF, 0.14F + breath * 0.12F);
        drawEnergyColumn(0.28D + pulse * 0.010D, 1.34D + breath * 0.16D,
                0x247DFF, 0.040F + pulse * 0.026F);

        RenderHelper.drawSphere(CORE_RADIUS * 1.34D, 0x8FFFFF, 0.18F, 22, 22);
        RenderHelper.drawSphere(CORE_RADIUS * (1.46D + glowBreath * 0.34D),
                0xCFFFFF, 0.14F + glowBreath * 0.12F, 18, 18);
        RenderHelper.drawSphere(CORE_RADIUS * outerBreath, 0x47EFFF, 0.38F + breath * 0.18F, 22, 22);
        RenderHelper.drawSphere(CORE_RADIUS * innerBreath, 0xFFFFFF, 0.42F + breath * 0.08F, 18, 18);

        GlStateManager.glLineWidth(1.4F);
        drawCoreLatitudeBands(CORE_RADIUS * (1.36D + glowBreath * 0.08D), 0xEFFFFF, 0.18F + glowBreath * 0.08F);
        RenderHelper.resetLineWidth();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.34F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.glLineWidth(2.0F);
        drawDiamondFrame(0.58D + breath * 0.20D, 0.0D, 0xFFFFFF, 0.14F + breath * 0.10F);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPowerNodes(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double x = Math.cos(angle) * NODE_RADIUS;
            double z = Math.sin(angle) * NODE_RADIUS;
            float pulse = wave(ticks * 0.055D - i * 0.72D);
            int color = i % 2 == 0 ? 0x79F7FF : 0x4B89FF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, 0.0D, z);
            RenderHelper.drawSphere(0.16D + pulse * 0.030D, color, 0.34F + pulse * 0.18F, 10, 10);
            RenderHelper.drawSphere(0.085D, 0xFFFFFF, 0.38F + pulse * 0.16F, 8, 8);
            GlStateManager.glLineWidth(1.25F);
            RenderHelper.drawLine(0.0D, -0.58D, 0.0D, 0.0D, 0.58D, 0.0D, color, 0.13F + pulse * 0.08F);
            drawNodeBrace(0.26D, -0.32D, color, 0.12F + pulse * 0.06F, ticks * 0.010D + i * 0.28D);
            drawNodeBrace(0.26D, 0.32D, color, 0.12F + pulse * 0.06F, -ticks * 0.010D + i * 0.28D);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawEnergyLinks(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            float pulse = wave(ticks * 0.075D - i * 0.65D);
            int color = i % 2 == 0 ? 0x9CFFFF : 0x5EA5FF;

            GlStateManager.glLineWidth(3.0F);
            drawSegmentedBeam(angle, 0.46D, NODE_RADIUS - 0.18D, 0.0D, color, 0.10F + pulse * 0.20F);
            GlStateManager.glLineWidth(1.2F);
            drawSegmentedBeam(angle, 0.70D, NODE_RADIUS + 0.08D, 0.20D, 0xFFFFFF, 0.07F + pulse * 0.11F);
            drawSegmentedBeam(angle, 0.70D, NODE_RADIUS + 0.08D, -0.20D, 0xFFFFFF, 0.07F + pulse * 0.11F);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawFluxPackets(float ticks) {
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
                    int particleColor = particleColor(packet, lane, i, color);

                    drawSphereAt(cos * radius + tangentX, y, sin * radius + tangentZ, size,
                            particleColor, 0.12F + fade * 0.42F + (float) eased * 0.12F, 6, 6);
                }
            }
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawRingBursts(float ticks) {
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

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            GlStateManager.glLineWidth(1.5F + alpha * 1.2F);
            drawPulseArc(radius, 0xD8FFFF, alpha * 0.24F, 16);
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawControlledCharge(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ENERGY_DOT_COUNT; i++) {
            double band = (i + 0.5D) / ENERGY_DOT_COUNT;
            double angle = TWO_PI * (i % 12) / 12.0D + ticks * (0.006D + (i % 3) * 0.0007D);
            double radius = 0.54D + (i / 12) * 0.28D + Math.sin(ticks * 0.018D + i) * 0.025D;
            double y = -0.48D + band * 0.96D;
            int color = i % 3 == 0 ? 0xFFFFFF : (i % 3 == 1 ? 0x7DF7FF : 0x4A8CFF);
            float alpha = 0.10F + 0.15F * wave(ticks * 0.050D + i * 0.41D);

            drawSphereAt(Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                    0.014D + (i % 3) * 0.004D, color, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private static void drawDiamondFrame(double radius, double y, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        RenderHelper.drawLine(0.0D, y + radius, 0.0D, radius, y, 0.0D, color, alpha);
        RenderHelper.drawLine(radius, y, 0.0D, 0.0D, y - radius, 0.0D, color, alpha);
        RenderHelper.drawLine(0.0D, y - radius, 0.0D, -radius, y, 0.0D, color, alpha);
        RenderHelper.drawLine(-radius, y, 0.0D, 0.0D, y + radius, 0.0D, color, alpha);
        RenderHelper.drawLine(0.0D, y, radius, radius, y, 0.0D, color, alpha * 0.72F);
        RenderHelper.drawLine(radius, y, 0.0D, 0.0D, y, -radius, color, alpha * 0.72F);
        RenderHelper.drawLine(0.0D, y, -radius, -radius, y, 0.0D, color, alpha * 0.72F);
        RenderHelper.drawLine(-radius, y, 0.0D, 0.0D, y, radius, color, alpha * 0.72F);
    }

    private static void drawNodeBrace(double radius, double y, int color, float alpha, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        int braces = 3;
        int segmentsPerBrace = 4;
        double braceLength = Math.PI * 0.34D;
        for (int brace = 0; brace < braces; brace++) {
            double center = phase + TWO_PI * brace / braces;
            double start = center - braceLength * 0.5D;
            for (int segment = 0; segment < segmentsPerBrace; segment++) {
                double a0 = start + braceLength * segment / segmentsPerBrace;
                double a1 = start + braceLength * (segment + 1) / segmentsPerBrace;
                RenderHelper.drawLine(Math.cos(a0) * radius, y, Math.sin(a0) * radius,
                        Math.cos(a1) * radius, y, Math.sin(a1) * radius, color, alpha);
            }

            double spoke = center;
            RenderHelper.drawLine(Math.cos(spoke) * (radius - 0.08D), y, Math.sin(spoke) * (radius - 0.08D),
                    Math.cos(spoke) * (radius + 0.04D), y, Math.sin(spoke) * (radius + 0.04D),
                    0xFFFFFF, alpha * 0.45F);
        }
    }

    private static void drawSegmentedBeam(double angle, double innerRadius, double outerRadius,
                                          double y, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int segments = 5;
        for (int i = 0; i < segments; i++) {
            double start = innerRadius + (outerRadius - innerRadius) * (i / (double) segments);
            double end = innerRadius + (outerRadius - innerRadius) * ((i + 0.62D) / segments);
            float segmentAlpha = alpha * (0.65F + 0.35F * (i % 2));
            RenderHelper.drawLine(cos * start, y, sin * start, cos * end, y, sin * end,
                    color, segmentAlpha);
        }
    }

    private static void drawDashedRing(double radius, int color, float alpha, int dashes, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        double dashLength = TWO_PI / dashes * 0.54D;
        int segments = 4;
        for (int dash = 0; dash < dashes; dash++) {
            double start = phase + TWO_PI * dash / dashes - dashLength * 0.5D;
            double previousX = Math.cos(start) * radius;
            double previousZ = Math.sin(start) * radius;
            for (int segment = 1; segment <= segments; segment++) {
                double progress = (double) segment / segments;
                double angle = start + dashLength * progress;
                double localRadius = radius + Math.sin(phase * 3.0D + dash * 0.73D + progress * Math.PI) * 0.010D;
                double x = Math.cos(angle) * localRadius;
                double z = Math.sin(angle) * localRadius;
                RenderHelper.drawLine(previousX, 0.0D, previousZ, x, 0.0D, z,
                        color, alpha * (0.72F + 0.28F * (dash % 3) / 2.0F));
                previousX = x;
                previousZ = z;
            }
        }
    }

    private static int particleColor(int packet, int lane, int node, int fallback) {
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
        return fallback;
    }

    private static void drawCoreLatitudeBands(double radius, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        RenderHelper.drawCircle(radius, color, alpha * 0.80F, 72);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(62.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.drawCircle(radius * 0.96D, color, alpha * 0.55F, 72);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.drawCircle(radius * 0.96D, color, alpha * 0.55F, 72);
        GlStateManager.popMatrix();
    }

    private static float smoothBreath(double time) {
        double sine = 0.5D + 0.5D * Math.sin(time);
        return (float) (sine * sine * (3.0D - 2.0D * sine));
    }

    private static void drawPulseArc(double radius, int color, float alpha, int segments) {
        if (alpha <= 0.01F) {
            return;
        }

        double start = -0.58D;
        double sweep = 1.16D;
        double previousX = Math.cos(start) * radius;
        double previousZ = Math.sin(start) * radius;
        for (int i = 1; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = start + sweep * progress;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            float localAlpha = alpha * (float) Math.sin(progress * Math.PI);
            RenderHelper.drawLine(previousX, 0.0D, previousZ, x, 0.0D, z, color, localAlpha);
            previousX = x;
            previousZ = z;
        }
    }

    private static void drawEnergyColumn(double radius, double height, int color, float alpha) {
        if (alpha <= 0.01F || radius <= 0.0D || height <= 0.0D) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int segments = 28;
        double halfHeight = height * 0.5D;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = TWO_PI * i / segments;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            buffer.pos(x, -halfHeight, z).color(rgb[0], rgb[1], rgb[2], alpha * 0.20F).endVertex();
            buffer.pos(x, halfHeight, z).color(rgb[0], rgb[1], rgb[2], alpha * 0.20F).endVertex();
        }
        tessellator.draw();
    }
}
