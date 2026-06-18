package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderNeonCircuitCore extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.36D;
    private static final double FRAME_RADIUS = 1.28D;
    private static final double NODE_RADIUS = 1.42D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int CYAN = 0x43F6FF;
    private static final int BLUE = 0x238CFF;
    private static final int MAGENTA = 0xFF4DFF;
    private static final int WHITE = 0xF5FFFF;
    private static final int NODE_COUNT = 12;
    private static final double RIBBON_THIN = 0.014D;
    private static final double RIBBON_NORMAL = 0.020D;
    private static final double RIBBON_MEDIUM = 0.026D;
    private static final double RIBBON_BOLD = 0.038D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCore(shader, ticks);
            drawCircuitFrames(shader, ticks);
            drawFlowingTraces(shader, ticks);
            drawSignalNodes(shader, ticks);
            drawVerticalField(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("neon circuit core shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.060D);
        float spark = wave(ticks * 0.110D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 9.0F, 0.0F, CYAN, BLUE, WHITE,
                0.16F + pulse * 0.08F, 1.18F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.58D + pulse * 0.20D), 16, 18);
        setTechUniforms(shader, ticks, 9.0F, 0.1F, MAGENTA, CYAN, WHITE,
                0.18F + spark * 0.10F, 1.32F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (1.12D + pulse * 0.10D), 16, 18);
        setTechUniforms(shader, ticks, 9.0F, 0.2F, WHITE, CYAN, MAGENTA,
                0.40F + pulse * 0.18F, 1.58F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * (0.56D + pulse * 0.08D), 12, 14);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.24F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(62.0F, 1.0F, 0.0F, 0.0F);
        setTechUniforms(shader, ticks, 9.0F, 0.4F, WHITE, CYAN, MAGENTA,
                0.16F + pulse * 0.08F, 1.26F, 0.56F);
        drawShaderCircle(0.56D + pulse * 0.04D, 64, RIBBON_NORMAL);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawCircuitFrames(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 4; i++) {
            double radius = FRAME_RADIUS + i * 0.18D;
            double y = (i - 1.5D) * 0.18D;
            float pulse = wave(ticks * 0.043D + i * 1.21D);
            int color = i % 2 == 0 ? CYAN : MAGENTA;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(45.0F + i * 22.5F + ticks * (0.018F - i * 0.002F), 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 9.0F, 1.0F + i * 0.12F, color, BLUE, WHITE,
                    0.13F + pulse * 0.05F, 1.10F, (float) radius);
            drawSquareFrame(radius, RIBBON_NORMAL + i * 0.003D);

            setTechUniforms(shader, ticks, 9.0F, 1.6F + i * 0.12F, WHITE, color, MAGENTA,
                    0.08F + pulse * 0.05F, 1.20F, (float) radius);
            drawCornerTicks(radius, 0.17D + i * 0.015D, RIBBON_THIN);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFlowingTraces(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int ring = 0; ring < 3; ring++) {
            double radius = FRAME_RADIUS + ring * 0.24D;
            double y = -0.20D + ring * 0.20D;
            int color = ring == 1 ? MAGENTA : CYAN;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(45.0F + ring * 30.0F, 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 9.0F, 2.0F + ring * 0.20F, color, WHITE, BLUE,
                    0.15F, 1.34F, (float) radius);
            drawFlowPacketsOnSquare(radius, ticks * (0.020D + ring * 0.004D) + ring * 0.19D, 6,
                    RIBBON_BOLD);

            setTechUniforms(shader, ticks, 9.0F, 2.7F + ring * 0.20F, WHITE, color, MAGENTA,
                    0.10F, 1.24F, (float) radius);
            drawFlowPacketsOnSquare(radius * 0.82D, -ticks * (0.017D + ring * 0.003D), 4,
                    RIBBON_NORMAL);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSignalNodes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NODE_COUNT; i++) {
            double angle = TWO_PI * i / NODE_COUNT + Math.PI * 0.25D;
            double y = Math.sin(i * 1.7D) * 0.22D;
            double radius = NODE_RADIUS + ((i & 1) == 0 ? 0.13D : -0.07D);
            float pulse = wave(ticks * 0.085D + i * 0.91D);
            double size = 0.034D + (i % 3) * 0.009D + pulse * 0.018D;
            int color = i % 4 == 0 ? WHITE : (i % 3 == 0 ? MAGENTA : CYAN);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            setTechUniforms(shader, ticks + i * 0.15F, 9.0F, 3.0F, color, CYAN, MAGENTA,
                    0.22F + pulse * 0.42F, 1.44F, (float) size);
            drawShaderSphere(size, 7, 8);
            setTechUniforms(shader, ticks, 9.0F, 3.2F, color, WHITE, BLUE,
                    0.08F + pulse * 0.08F, 1.18F, 0.18F);
            drawNodePin(0.18D, RIBBON_THIN);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawVerticalField(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.050D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(45.0F - ticks * 0.020F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 9.0F, 4.0F, CYAN, MAGENTA, WHITE,
                0.055F + pulse * 0.035F, 1.08F, 1.18F);
        drawVerticalCircuitCage(1.18D, 1.10D, RIBBON_NORMAL);

        setTechUniforms(shader, ticks, 9.0F, 4.4F, WHITE, CYAN, MAGENTA,
                0.11F + pulse * 0.06F, 1.30F, 0.90F);
        drawRisingScanDiamond(0.90D + pulse * 0.06D, ticks * 0.012D, RIBBON_MEDIUM);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private static void drawSquareFrame(double radius, double width) {
        drawShaderLine(-radius, 0.0D, -radius, radius, 0.0D, -radius, width);
        drawShaderLine(radius, 0.0D, -radius, radius, 0.0D, radius, width);
        drawShaderLine(radius, 0.0D, radius, -radius, 0.0D, radius, width);
        drawShaderLine(-radius, 0.0D, radius, -radius, 0.0D, -radius, width);
    }

    private static void drawCornerTicks(double radius, double length, double width) {
        for (int corner = 0; corner < 4; corner++) {
            double sx = corner < 2 ? 1.0D : -1.0D;
            double sz = corner == 0 || corner == 3 ? 1.0D : -1.0D;
            drawShaderLine(sx * radius, 0.0D, sz * (radius - length), sx * radius, 0.0D, sz * radius,
                    width);
            drawShaderLine(sx * (radius - length), 0.0D, sz * radius, sx * radius, 0.0D, sz * radius,
                    width);
        }
    }

    private static void drawFlowPacketsOnSquare(double radius, double phase, int count, double width) {
        for (int i = 0; i < count; i++) {
            double progress = fract(phase + i / (double) count);
            double end = fract(progress + 0.075D);
            drawSquarePathSegment(radius, progress, end, width);
        }
    }

    private static void drawSquarePathSegment(double radius, double start, double end, double width) {
        int steps = end < start ? 2 : 1;
        double segmentStart = start;
        for (int i = 0; i < steps; i++) {
            double segmentEnd = i == 0 && end < start ? 1.0D : end;
            drawSquarePathStrip(radius, segmentStart, segmentEnd, 4, width);
            segmentStart = 0.0D;
        }
    }

    private static void drawSquarePathStrip(double radius, double start, double end, int segments, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = lerp(start, end, i / (double) segments);
            double[] point = squarePoint(radius, progress);
            double[] tangent = squareTangent(progress);
            addFlatRibbonVertexPair(buffer, point[0], 0.0D, point[1], tangent[0], tangent[1],
                    width, i / (double) segments);
        }
        tessellator.draw();
    }

    private static double[] squarePoint(double radius, double progress) {
        double side = fract(progress) * 4.0D;
        int edge = (int) Math.floor(side);
        double local = side - edge;
        if (edge == 0) {
            return new double[]{lerp(-radius, radius, local), -radius};
        }
        if (edge == 1) {
            return new double[]{radius, lerp(-radius, radius, local)};
        }
        if (edge == 2) {
            return new double[]{lerp(radius, -radius, local), radius};
        }
        return new double[]{-radius, lerp(radius, -radius, local)};
    }

    private static double[] squareTangent(double progress) {
        double side = fract(progress) * 4.0D;
        int edge = (int) Math.floor(side);
        if (edge == 0) {
            return new double[]{1.0D, 0.0D};
        }
        if (edge == 1) {
            return new double[]{0.0D, 1.0D};
        }
        if (edge == 2) {
            return new double[]{-1.0D, 0.0D};
        }
        return new double[]{0.0D, -1.0D};
    }

    private static void drawNodePin(double radius, double width) {
        drawShaderLine(-radius, 0.0D, 0.0D, radius, 0.0D, 0.0D, width);
        drawShaderLine(0.0D, 0.0D, -radius, 0.0D, 0.0D, radius, width);
    }

    private static void drawVerticalCircuitCage(double radius, double halfHeight, double width) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.25D + TWO_PI * i / 4.0D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            drawShaderLine(x, -halfHeight, z, x, halfHeight, z, width);
            drawShaderLine(x * 0.72D, -halfHeight * 0.42D, z * 0.72D, x, -halfHeight * 0.12D, z,
                    width * 0.82D);
            drawShaderLine(x * 0.72D, halfHeight * 0.42D, z * 0.72D, x, halfHeight * 0.12D, z,
                    width * 0.82D);
        }
    }

    private static void drawRisingScanDiamond(double radius, double phase, double width) {
        double y = lerp(-0.78D, 0.78D, fract(phase));
        drawShaderLine(0.0D, y, -radius, radius, y, 0.0D, width);
        drawShaderLine(radius, y, 0.0D, 0.0D, y, radius, width);
        drawShaderLine(0.0D, y, radius, -radius, y, 0.0D, width);
        drawShaderLine(-radius, y, 0.0D, 0.0D, y, -radius, width);
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

    private static void addFlatRibbonVertexPair(BufferBuilder buffer, double x, double y, double z,
                                                double tangentX, double tangentZ, double width,
                                                double progress) {
        double sideX = -tangentZ * width * 0.5D;
        double sideZ = tangentX * width * 0.5D;
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
