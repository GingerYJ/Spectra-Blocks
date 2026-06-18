package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderHologramField extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double FIELD_RADIUS = 1.78D;
    private static final double FIELD_HEIGHT_SCALE = 0.72D;
    private static final double PANEL_RADIUS = 1.22D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int PRIMARY_COLOR = 0x4DEBFF;
    private static final int SECONDARY_COLOR = 0x18A8FF;
    private static final int ACCENT_COLOR = 0xD9FFFF;
    private static final int POINT_COUNT = 14;
    private static final double RIBBON_THIN = 0.014D;
    private static final double RIBBON_NORMAL = 0.020D;
    private static final double RIBBON_BOLD = 0.030D;
    private static final double PANEL_RIBBON = 0.012D;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawFieldShell(shader, ticks);
            drawScanLines(shader, ticks);
            drawProjectionPanels(shader, ticks);
            drawSignalPoints(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("hologram field shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawFieldShell(ShaderProgram shader, float ticks) {
        float breath = smoothPulse(ticks * 0.045D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0D, FIELD_HEIGHT_SCALE, 1.0D);
        setTechUniforms(shader, ticks, 8.0F, 0.0F, PRIMARY_COLOR, SECONDARY_COLOR, ACCENT_COLOR,
                0.075F + breath * 0.025F, 1.06F, (float) FIELD_RADIUS);
        drawShaderSphere(FIELD_RADIUS * (0.985D + breath * 0.018D), 16, 24);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 8.0F, 0.2F, ACCENT_COLOR, PRIMARY_COLOR, SECONDARY_COLOR,
                0.105F + breath * 0.040F, 1.18F, (float) FIELD_RADIUS);
        drawWireEllipsoid(FIELD_RADIUS * 1.012D, 5, 12, RIBBON_THIN);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.026F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 8.0F, 0.4F, SECONDARY_COLOR, PRIMARY_COLOR, ACCENT_COLOR,
                0.070F + breath * 0.030F, 1.10F, (float) FIELD_RADIUS);
        drawDashedCircle(FIELD_RADIUS * 0.96D, 36, ticks * 0.010D, RIBBON_NORMAL);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawScanLines(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 7; i++) {
            double progress = fract(ticks * 0.0065D + i * 0.143D);
            double y = lerp(-FIELD_RADIUS * FIELD_HEIGHT_SCALE, FIELD_RADIUS * FIELD_HEIGHT_SCALE, progress);
            double normalized = y / (FIELD_RADIUS * FIELD_HEIGHT_SCALE);
            double radius = FIELD_RADIUS * Math.sqrt(Math.max(0.0D, 1.0D - normalized * normalized));
            float fade = (float) Math.sin(progress * Math.PI);
            int color = i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(ticks * (0.032F + i * 0.004F), 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 8.0F, 1.0F + i * 0.05F, color, ACCENT_COLOR, SECONDARY_COLOR,
                    0.040F + fade * 0.125F, 1.16F, (float) radius);
            drawShaderCircle(radius, 72, i == 0 ? RIBBON_BOLD : RIBBON_NORMAL);
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < 3; i++) {
            double progress = fract(ticks * 0.010D + i * 0.333D);
            float alpha = (float) Math.sin(progress * Math.PI) * 0.11F;
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (i * 60.0D + ticks * 0.014D), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(72.0F, 1.0F, 0.0F, 0.0F);
            setTechUniforms(shader, ticks, 8.0F, 1.8F, ACCENT_COLOR, PRIMARY_COLOR, SECONDARY_COLOR,
                    alpha, 1.20F, (float) FIELD_RADIUS);
            drawPulseArc(FIELD_RADIUS * (0.86D + progress * 0.16D), 22, RIBBON_NORMAL + alpha * 0.050D);
            GlStateManager.popMatrix();
        }

        useAlphaBlend();
    }

    private void drawProjectionPanels(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < 4; i++) {
            double angle = TWO_PI * i / 4.0D + ticks * (0.004D + i * 0.0006D);
            double bob = Math.sin(ticks * 0.035D + i * 1.7D) * 0.055D;
            double width = 0.62D + (i & 1) * 0.12D;
            double height = 0.42D + (i % 3) * 0.055D;
            double y = -0.38D + i * 0.25D + bob;
            int color = i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * PANEL_RADIUS, y, Math.sin(angle) * PANEL_RADIUS);
            GlStateManager.rotate((float) Math.toDegrees(-angle) + 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) Math.sin(ticks * 0.018D + i) * 5.0F, 0.0F, 0.0F, 1.0F);

            setTechUniforms(shader, ticks, 8.0F, 2.0F + i * 0.12F, color, SECONDARY_COLOR, ACCENT_COLOR,
                    0.030F + wave(ticks * 0.041D + i) * 0.018F, 0.92F, (float) width);
            drawPanelQuad(width, height);

            useAdditiveBlend();
            setTechUniforms(shader, ticks, 8.0F, 2.6F + i * 0.12F, ACCENT_COLOR, color, SECONDARY_COLOR,
                    0.105F + wave(ticks * 0.052D + i) * 0.045F, 1.18F, (float) width);
            drawPanelFrame(width, height, PANEL_RIBBON);
            drawPanelGrid(width, height, 4, 3, PANEL_RIBBON * 0.74D);
            useAlphaBlend();
            GlStateManager.popMatrix();
        }
    }

    private void drawSignalPoints(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < POINT_COUNT; i++) {
            double phase = i * 2.137D;
            double orbit = ticks * (0.010D + (i % 4) * 0.0015D) + phase;
            double vertical = Math.sin(orbit * 0.78D + i) * 0.72D;
            double cap = Math.sqrt(Math.max(0.0D, 1.0D - (vertical / 1.16D) * (vertical / 1.16D)));
            double radius = FIELD_RADIUS * (0.22D + 0.66D * cap);
            double x = Math.cos(orbit) * radius;
            double z = Math.sin(orbit) * radius;
            double y = vertical * FIELD_HEIGHT_SCALE;
            float pulse = wave(ticks * 0.090D + i * 0.73D);
            double size = 0.018D + (i % 3) * 0.006D + pulse * 0.010D;
            int color = i % 5 == 0 ? ACCENT_COLOR : (i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR);

            setTechUniforms(shader, ticks + i * 0.13F, 8.0F, 3.0F, color, PRIMARY_COLOR, ACCENT_COLOR,
                    0.16F + pulse * 0.34F, 1.35F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            drawShaderSphere(size, 6, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawPanelQuad(double width, double height) {
        double halfWidth = width * 0.5D;
        double halfHeight = height * 0.5D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, -halfWidth, -halfHeight, 0.0D, 0.0D, 1.0D);
        addPosition(buffer, -halfWidth, halfHeight, 0.0D, 0.0D, 0.0D);
        addPosition(buffer, halfWidth, -halfHeight, 0.0D, 1.0D, 1.0D);
        addPosition(buffer, halfWidth, halfHeight, 0.0D, 1.0D, 0.0D);
        tessellator.draw();
    }

    private static void drawPanelFrame(double width, double height, double lineWidth) {
        double halfWidth = width * 0.5D;
        double halfHeight = height * 0.5D;
        drawPanelLine(-halfWidth, -halfHeight, halfWidth, -halfHeight, 0.002D, lineWidth);
        drawPanelLine(halfWidth, -halfHeight, halfWidth, halfHeight, 0.002D, lineWidth);
        drawPanelLine(halfWidth, halfHeight, -halfWidth, halfHeight, 0.002D, lineWidth);
        drawPanelLine(-halfWidth, halfHeight, -halfWidth, -halfHeight, 0.002D, lineWidth);
    }

    private static void drawPanelGrid(double width, double height, int columns, int rows, double lineWidth) {
        double halfWidth = width * 0.5D;
        double halfHeight = height * 0.5D;
        for (int column = 1; column < columns; column++) {
            double x = -halfWidth + width * column / columns;
            drawPanelLine(x, -halfHeight, x, halfHeight, 0.004D, lineWidth);
        }
        for (int row = 1; row < rows; row++) {
            double y = -halfHeight + height * row / rows;
            drawPanelLine(-halfWidth, y, halfWidth, y, 0.004D, lineWidth);
        }
    }

    private static void drawWireEllipsoid(double radius, int gridLat, int gridLon, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int lat = 1; lat < gridLat; lat++) {
            double theta = Math.PI * lat / gridLat;
            double y = radius * Math.cos(theta);
            double horizontalRadius = radius * Math.sin(theta);
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lon = 0; lon <= gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
                addRingRibbonVertexPair(buffer, horizontalRadius, phi, y, width, lon / (double) gridLon);
            }
            tessellator.draw();
        }

        for (int lon = 0; lon < gridLon; lon++) {
            double phi = TWO_PI * lon / gridLon;
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lat = 0; lat <= gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                double localRadius = radius * Math.sin(theta);
                double y = radius * Math.cos(theta);
                addLineRibbonVertexPair(buffer,
                        localRadius * Math.cos(phi - 0.010D), y, localRadius * Math.sin(phi - 0.010D),
                        localRadius * Math.cos(phi + 0.010D), y, localRadius * Math.sin(phi + 0.010D),
                        width, lat / (double) gridLat);
            }
            tessellator.draw();
        }
    }

    private static void drawDashedCircle(double radius, int dashes, double phase, double width) {
        double dashLength = TWO_PI / dashes * 0.52D;
        for (int dash = 0; dash < dashes; dash++) {
            double start = phase + TWO_PI * dash / dashes - dashLength * 0.5D;
            double end = start + dashLength;
            drawShaderLine(Math.cos(start) * radius, 0.0D, Math.sin(start) * radius,
                    Math.cos(end) * radius, 0.0D, Math.sin(end) * radius, width);
        }
    }

    private static void drawPulseArc(double radius, int segments, double width) {
        double start = -0.72D;
        double sweep = 1.44D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = start + sweep * progress;
            addRingRibbonVertexPair(buffer, radius, angle, 0.0D, width, progress);
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
            addRingRibbonVertexPair(buffer, radius, angle, 0.0D, width, progress);
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

    private static void drawPanelLine(double x0, double y0, double x1, double y1, double z, double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length <= 1.0E-5D || width <= 0.0D) {
            return;
        }

        double halfWidth = width * 0.5D;
        double sideX = -dy / length * halfWidth;
        double sideY = dx / length * halfWidth;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, x0 - sideX, y0 - sideY, z, 0.0D, 0.0D);
        addPosition(buffer, x0 + sideX, y0 + sideY, z, 0.0D, 1.0D);
        addPosition(buffer, x1 - sideX, y1 - sideY, z, 1.0D, 0.0D);
        addPosition(buffer, x1 + sideX, y1 + sideY, z, 1.0D, 1.0D);
        tessellator.draw();
    }

    private static void addRingRibbonVertexPair(BufferBuilder buffer, double radius, double angle, double y,
                                                double width, double progress) {
        double halfWidth = width * 0.5D;
        double inner = Math.max(0.0D, radius - halfWidth);
        double outer = radius + halfWidth;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        addPosition(buffer, cos * outer, y, sin * outer, progress, 1.0D);
        addPosition(buffer, cos * inner, y, sin * inner, progress, 0.0D);
    }

    private static void addLineRibbonVertexPair(BufferBuilder buffer, double x0, double y0, double z0,
                                                double x1, double y1, double z1, double width,
                                                double progress) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 1.0E-5D || width <= 0.0D) {
            return;
        }

        double halfWidth = width * 0.5D / length;
        addPosition(buffer, x0, y0 - halfWidth, z0, progress, 0.0D);
        addPosition(buffer, x1, y1 + halfWidth, z1, progress, 1.0D);
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
                .normal(0.0F, 0.0F, 1.0F)
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
