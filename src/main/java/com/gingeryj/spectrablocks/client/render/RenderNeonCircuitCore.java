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
        GlStateManager.glLineWidth(1.35F);
        drawShaderCircle(0.56D + pulse * 0.04D, 64);
        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(1.0F);
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
            GlStateManager.glLineWidth(1.2F + i * 0.12F);
            drawSquareFrame(radius);

            setTechUniforms(shader, ticks, 9.0F, 1.6F + i * 0.12F, WHITE, color, MAGENTA,
                    0.08F + pulse * 0.05F, 1.20F, (float) radius);
            GlStateManager.glLineWidth(0.9F);
            drawCornerTicks(radius, 0.17D + i * 0.015D);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
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
            GlStateManager.glLineWidth(2.0F);
            drawFlowPacketsOnSquare(radius, ticks * (0.020D + ring * 0.004D) + ring * 0.19D, 6);

            setTechUniforms(shader, ticks, 9.0F, 2.7F + ring * 0.20F, WHITE, color, MAGENTA,
                    0.10F, 1.24F, (float) radius);
            GlStateManager.glLineWidth(1.0F);
            drawFlowPacketsOnSquare(radius * 0.82D, -ticks * (0.017D + ring * 0.003D), 4);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
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
            GlStateManager.glLineWidth(0.9F);
            drawNodePin(0.18D);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawVerticalField(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.050D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(45.0F - ticks * 0.020F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 9.0F, 4.0F, CYAN, MAGENTA, WHITE,
                0.055F + pulse * 0.035F, 1.08F, 1.18F);
        GlStateManager.glLineWidth(1.05F);
        drawVerticalCircuitCage(1.18D, 1.10D);

        setTechUniforms(shader, ticks, 9.0F, 4.4F, WHITE, CYAN, MAGENTA,
                0.11F + pulse * 0.06F, 1.30F, 0.90F);
        GlStateManager.glLineWidth(1.4F);
        drawRisingScanDiamond(0.90D + pulse * 0.06D, ticks * 0.012D);
        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private static void drawSquareFrame(double radius) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, -radius, 0.0D, -radius, 0.0D, 0.0D);
        addPosition(buffer, radius, 0.0D, -radius, 1.0D, 0.0D);
        addPosition(buffer, radius, 0.0D, radius, 1.0D, 1.0D);
        addPosition(buffer, -radius, 0.0D, radius, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void drawCornerTicks(double radius, double length) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int corner = 0; corner < 4; corner++) {
            double sx = corner < 2 ? 1.0D : -1.0D;
            double sz = corner == 0 || corner == 3 ? 1.0D : -1.0D;
            addLine(buffer, sx * radius, 0.0D, sz * (radius - length), sx * radius, 0.0D, sz * radius);
            addLine(buffer, sx * (radius - length), 0.0D, sz * radius, sx * radius, 0.0D, sz * radius);
        }
        tessellator.draw();
    }

    private static void drawFlowPacketsOnSquare(double radius, double phase, int count) {
        for (int i = 0; i < count; i++) {
            double progress = fract(phase + i / (double) count);
            double end = fract(progress + 0.075D);
            drawSquarePathSegment(radius, progress, end);
        }
    }

    private static void drawSquarePathSegment(double radius, double start, double end) {
        int steps = end < start ? 2 : 1;
        double segmentStart = start;
        for (int i = 0; i < steps; i++) {
            double segmentEnd = i == 0 && end < start ? 1.0D : end;
            drawSquarePathStrip(radius, segmentStart, segmentEnd, 4);
            segmentStart = 0.0D;
        }
    }

    private static void drawSquarePathStrip(double radius, double start, double end, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = lerp(start, end, i / (double) segments);
            double[] point = squarePoint(radius, progress);
            addPosition(buffer, point[0], 0.0D, point[1], i / (double) segments, 0.5D);
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

    private static void drawNodePin(double radius) {
        drawShaderLine(-radius, 0.0D, 0.0D, radius, 0.0D, 0.0D);
        drawShaderLine(0.0D, 0.0D, -radius, 0.0D, 0.0D, radius);
    }

    private static void drawVerticalCircuitCage(double radius, double halfHeight) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.25D + TWO_PI * i / 4.0D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            drawShaderLine(x, -halfHeight, z, x, halfHeight, z);
            drawShaderLine(x * 0.72D, -halfHeight * 0.42D, z * 0.72D, x, -halfHeight * 0.12D, z);
            drawShaderLine(x * 0.72D, halfHeight * 0.42D, z * 0.72D, x, halfHeight * 0.12D, z);
        }
    }

    private static void drawRisingScanDiamond(double radius, double phase) {
        double y = lerp(-0.78D, 0.78D, fract(phase));
        drawShaderLine(0.0D, y, -radius, radius, y, 0.0D);
        drawShaderLine(radius, y, 0.0D, 0.0D, y, radius);
        drawShaderLine(0.0D, y, radius, -radius, y, 0.0D);
        drawShaderLine(-radius, y, 0.0D, 0.0D, y, -radius);
    }

    private static void drawShaderCircle(double radius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            addPosition(buffer, Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius, progress, 0.5D);
        }
        tessellator.draw();
    }

    private static void drawShaderLine(double x0, double y0, double z0, double x1, double y1, double z1) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addLine(buffer, x0, y0, z0, x1, y1, z1);
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

    private static void addLine(BufferBuilder buffer, double x0, double y0, double z0,
                                double x1, double y1, double z1) {
        addPosition(buffer, x0, y0, z0, 0.0D, 0.0D);
        addPosition(buffer, x1, y1, z1, 1.0D, 1.0D);
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
