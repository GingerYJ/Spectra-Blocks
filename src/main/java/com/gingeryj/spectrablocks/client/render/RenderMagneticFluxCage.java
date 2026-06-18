package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderMagneticFluxCage extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.34D;
    private static final double CAGE_RADIUS = 1.18D;
    private static final double CAGE_HALF_HEIGHT = 0.92D;
    private static final double RING_RADIUS = 1.28D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int FIELD_LINE_COUNT = 8;
    private static final int NODE_COUNT = 10;
    private static final int CYAN = 0x46F6FF;
    private static final int BLUE = 0x238CFF;
    private static final int WHITE = 0xF4FFFF;
    private static final int VIOLET = 0xB764FF;
    private static final double RIBBON_THIN = 0.014D;
    private static final double RIBBON_NORMAL = 0.020D;
    private static final double RIBBON_MEDIUM = 0.026D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawEnergyCore(shader, ticks);
            drawFluxCage(shader, ticks);
            drawCounterRotatingRings(shader, ticks);
            drawMovingNodes(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("magnetic flux cage shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawEnergyCore(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.062D);
        float flicker = wave(ticks * 0.130D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 10.0F, 0.0F, CYAN, BLUE, WHITE,
                0.14F + pulse * 0.09F, 1.18F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.70D + pulse * 0.24D), 16, 18);
        setTechUniforms(shader, ticks, 10.0F, 0.1F, VIOLET, CYAN, WHITE,
                0.10F + flicker * 0.08F, 1.26F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.18D + pulse * 0.10D), 14, 16);
        setTechUniforms(shader, ticks, 10.0F, 0.2F, WHITE, CYAN, BLUE,
                0.42F + pulse * 0.18F, 1.56F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (0.54D + pulse * 0.07D), 12, 14);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.22F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.0F);
        setTechUniforms(shader, ticks, 10.0F, 0.4F, WHITE, CYAN, VIOLET,
                0.13F + pulse * 0.08F, 1.22F, 0.54F);
        drawShaderCircle(0.54D + pulse * 0.035D, 56, RIBBON_NORMAL);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawFluxCage(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.044D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(22.5F + ticks * 0.012F, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < FIELD_LINE_COUNT; i++) {
            double angle = TWO_PI * i / FIELD_LINE_COUNT;
            double lean = Math.sin(ticks * 0.025D + i * 0.83D) * 0.045D;
            int color = i % 3 == 0 ? WHITE : (i % 2 == 0 ? CYAN : BLUE);

            setTechUniforms(shader, ticks, 10.0F, 1.0F + i * 0.03F, color, CYAN, VIOLET,
                    0.12F + pulse * 0.055F, 1.18F, (float) CAGE_RADIUS);
            drawCurvedFieldLine(angle, CAGE_RADIUS + lean, CAGE_HALF_HEIGHT, 9,
                    i % 2 == 0 ? RIBBON_MEDIUM : RIBBON_NORMAL);

            setTechUniforms(shader, ticks, 10.0F, 1.4F + i * 0.03F, VIOLET, CYAN, WHITE,
                    0.045F + pulse * 0.028F, 1.05F, (float) CAGE_RADIUS);
            drawInnerReturnLine(angle + Math.PI / FIELD_LINE_COUNT, CAGE_RADIUS * 0.66D,
                    CAGE_HALF_HEIGHT * 0.78D, RIBBON_THIN);
        }
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawCounterRotatingRings(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.048D);

        useAdditiveBlend();
        drawPolarRing(shader, ticks, CAGE_HALF_HEIGHT, ticks * 0.100F, CYAN, WHITE, 2.0F, 0.16F + pulse * 0.06F);
        drawPolarRing(shader, ticks, -CAGE_HALF_HEIGHT, -ticks * 0.115F, BLUE, VIOLET, 2.2F, 0.14F + pulse * 0.05F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * 0.036F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 10.0F, 2.6F, WHITE, CYAN, VIOLET,
                0.060F + pulse * 0.032F, 1.08F, (float) CAGE_RADIUS);
        drawShaderCircle(CAGE_RADIUS * 0.86D, 64, RIBBON_THIN);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawMovingNodes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * (i % FIELD_LINE_COUNT) / FIELD_LINE_COUNT + 22.5D * Math.PI / 180.0D;
            double progress = fract(ticks * (0.010D + (i % 3) * 0.002D) + i * 0.173D);
            double y = lerp(-CAGE_HALF_HEIGHT * 0.88D, CAGE_HALF_HEIGHT * 0.88D, progress);
            double bow = Math.sin(progress * Math.PI) * 0.13D;
            double radius = CAGE_RADIUS + bow;
            double drift = Math.sin(ticks * 0.022D + i) * 0.030D;
            float fade = (float) Math.sin(progress * Math.PI);
            float spark = wave(ticks * 0.090D + i * 0.67D);
            double size = 0.026D + (i % 3) * 0.007D + spark * 0.009D;
            int color = i % 5 == 0 ? VIOLET : (i % 2 == 0 ? WHITE : CYAN);

            setTechUniforms(shader, ticks + i * 0.11F, 10.0F, 3.0F, color, CYAN, WHITE,
                    0.16F + fade * 0.38F, 1.42F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle + drift) * radius, y, Math.sin(angle + drift) * radius);
            drawShaderSphere(size, 7, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawPolarRing(ShaderProgram shader, float ticks, double y, float rotation,
                                      int primary, int secondary, float layer, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 10.0F, layer, primary, secondary, VIOLET,
                alpha, 1.24F, (float) RING_RADIUS);
        drawDashedRing(RING_RADIUS, 28, rotation * 0.010D, RIBBON_MEDIUM);

        setTechUniforms(shader, ticks, 10.0F, layer + 0.1F, WHITE, primary, VIOLET,
                alpha * 0.62F, 1.14F, (float) (RING_RADIUS * 0.82D));
        drawDashedRing(RING_RADIUS * 0.82D, 18, -rotation * 0.008D, RIBBON_THIN);
        GlStateManager.popMatrix();
    }

    private static void drawCurvedFieldLine(double angle, double radius, double halfHeight, int segments,
                                            double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double y = lerp(-halfHeight, halfHeight, progress);
            double bow = Math.sin(progress * Math.PI) * 0.12D;
            double twist = (progress - 0.5D) * 0.18D;
            double localRadius = radius + bow;
            addVerticalRibbonVertexPair(buffer, Math.cos(angle + twist) * localRadius, y,
                    Math.sin(angle + twist) * localRadius, angle + twist, width, progress);
        }
        tessellator.draw();
    }

    private static void drawInnerReturnLine(double angle, double radius, double halfHeight, double width) {
        drawShaderLine(Math.cos(angle) * radius, -halfHeight, Math.sin(angle) * radius,
                Math.cos(angle) * radius, halfHeight, Math.sin(angle) * radius, width);
    }

    private static void drawDashedRing(double radius, int dashes, double phase, double width) {
        double dashLength = TWO_PI / dashes * 0.56D;
        for (int dash = 0; dash < dashes; dash++) {
            double start = phase + TWO_PI * dash / dashes - dashLength * 0.5D;
            double end = start + dashLength;
            drawShaderLine(Math.cos(start) * radius, 0.0D, Math.sin(start) * radius,
                    Math.cos(end) * radius, 0.0D, Math.sin(end) * radius, width);
        }
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

    private static void addVerticalRibbonVertexPair(BufferBuilder buffer, double x, double y, double z,
                                                    double radialAngle, double width, double progress) {
        double sideX = -Math.sin(radialAngle) * width * 0.5D;
        double sideZ = Math.cos(radialAngle) * width * 0.5D;
        addPosition(buffer, x - sideX, y, z - sideZ, progress, 0.0D);
        addPosition(buffer, x + sideX, y, z + sideZ, progress, 1.0D);
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
