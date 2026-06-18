package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileDataStreamMatrix;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderDataStreamMatrix extends RenderCelestialEffectBase<TileDataStreamMatrix> {

    private static final int COLUMN_COUNT = 26;
    private static final int GLYPHS_PER_COLUMN = 9;
    private static final int GRID_RING_COUNT = 4;
    private static final double COLUMN_RADIUS = 2.84D;
    private static final double COLUMN_HEIGHT = 3.86D;
    private static final double GLYPH_SIZE = 0.115D;
    private static final double GLYPH_SPACING = 0.43D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float FALL_SPEED = 0.030F;
    private static final float MATRIX_ROTATION_SPEED = 0.022F;
    private static final int PRIMARY_COLOR = 0x45FF9D;
    private static final int SECONDARY_COLOR = 0x46D7FF;
    private static final int WHITE_COLOR = 0xEFFFFF;

    @Override
    protected void renderCelestialEffect(TileDataStreamMatrix te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCoreGrid(shader, ticks);
            drawColumns(shader, ticks);
            drawScanRings(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("data stream matrix shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCoreGrid(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * MATRIX_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        for (int i = 0; i < GRID_RING_COUNT; i++) {
            double radius = 0.72D + i * 0.54D;
            float alpha = 0.070F + 0.030F * wave(ticks * 0.041D + i);
            setTechUniforms(shader, ticks, 5.0F, 0.0F + i * 0.1F,
                    i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR, WHITE_COLOR, PRIMARY_COLOR,
                    alpha, 1.06F, (float) radius);
            drawShaderCircle(radius, 96, 0.018D + i * 0.004D);
        }
        GlStateManager.popMatrix();

        setTechUniforms(shader, ticks, 5.0F, 1.0F, 0x1AFFC0, SECONDARY_COLOR, WHITE_COLOR,
                0.045F, 0.95F, 2.74F);
        drawShaderWireSphere(2.74D, 7, 12, 0.017D);
        useAlphaBlend();
    }

    private void drawColumns(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * MATRIX_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < COLUMN_COUNT; i++) {
            double band = (i + 0.5D) / COLUMN_COUNT;
            double angle = i * GOLDEN_ANGLE;
            double radius = 0.55D + Math.pow(band, 0.52D) * COLUMN_RADIUS;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double drift = fract(ticks * FALL_SPEED + i * 0.137D);
            int color = i % 3 == 0 ? SECONDARY_COLOR : PRIMARY_COLOR;

            setTechUniforms(shader, ticks, 5.0F, 2.0F, color, SECONDARY_COLOR, WHITE_COLOR,
                    0.045F + 0.025F * wave(ticks * 0.030D + i), 0.92F, (float) COLUMN_HEIGHT);
            drawShaderLine(x, -COLUMN_HEIGHT * 0.5D, z, x, COLUMN_HEIGHT * 0.5D, z, 0.016D);

            for (int j = 0; j < GLYPHS_PER_COLUMN; j++) {
                double local = fract(drift + j / (double) GLYPHS_PER_COLUMN);
                double y = COLUMN_HEIGHT * 0.5D - local * COLUMN_HEIGHT;
                float fade = (float) Math.sin(Math.PI * local);
                float alpha = (0.10F + 0.36F * fade) * (j == 0 ? 1.15F : 1.0F);
                int glyphColor = j == 0 ? WHITE_COLOR : color;

                setTechUniforms(shader, ticks + i * 0.27F + j * 0.11F, 5.0F, 3.0F,
                        glyphColor, color, WHITE_COLOR, alpha, 1.30F, (float) GLYPH_SIZE);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate((float) Math.toDegrees(-angle) + 90.0F, 0.0F, 1.0F, 0.0F);
                drawGlyph((i + j * 5) & 7, GLYPH_SIZE * (0.86D + (j % 3) * 0.10D));
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawScanRings(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 3; i++) {
            double progress = fract(ticks * 0.010D + i * 0.333D);
            double y = 1.82D - progress * 3.64D;
            float fade = (float) Math.sin(Math.PI * progress);
            double radius = 2.62D - i * 0.24D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(ticks * (0.045F + i * 0.008F), 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 5.0F, 4.0F, i == 1 ? SECONDARY_COLOR : PRIMARY_COLOR,
                    WHITE_COLOR, SECONDARY_COLOR, 0.11F * fade, 1.18F, (float) radius);
            drawShaderCircle(radius, 88, 0.040D);

            setTechUniforms(shader, ticks, 5.0F, 5.0F, WHITE_COLOR, i == 1 ? SECONDARY_COLOR : PRIMARY_COLOR,
                    PRIMARY_COLOR, 0.10F * fade, 1.22F, (float) radius);
            drawRuneMarks(radius, 0.16D, 18, ticks * 0.011D + i, 0.018D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawGlyph(int glyph, double size) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double width = size * 0.22D;

        if ((glyph & 1) == 0) {
            addLine2D(buffer, -size, -size, size, -size, width);
            addLine2D(buffer, -size, size, size, size, width);
        } else {
            addLine2D(buffer, -size, -size, -size, size, width);
            addLine2D(buffer, size, -size, size, size, width);
        }

        if ((glyph & 2) == 0) {
            addLine2D(buffer, -size, 0.0D, size, 0.0D, width);
        } else {
            addLine2D(buffer, 0.0D, -size, 0.0D, size, width);
        }

        if ((glyph & 4) == 0) {
            addLine2D(buffer, -size, -size, size, size, width);
        } else {
            addLine2D(buffer, -size, size, size, -size, width);
        }

        tessellator.draw();
    }

    private static void drawRuneMarks(double radius, double length, int count, double phase, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < count; i++) {
            double angle = phase + TWO_PI * i / count;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double inner = radius - length * 0.5D;
            double outer = radius + length * 0.5D;
            addLine(buffer, cos * inner, 0.0D, sin * inner, cos * outer, 0.0D, sin * outer, width);

            if ((i & 3) == 0) {
                double tangentX = -sin * length * 0.36D;
                double tangentZ = cos * length * 0.36D;
                double markRadius = radius + length * 0.18D;
                addLine(buffer, cos * markRadius - tangentX, 0.0D, sin * markRadius - tangentZ,
                        cos * markRadius + tangentX, 0.0D, sin * markRadius + tangentZ, width);
            }
        }

        tessellator.draw();
    }

    private static void drawShaderWireSphere(double radius, int gridLat, int gridLon, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int lat = 1; lat < gridLat; lat++) {
            double theta = Math.PI * lat / gridLat;
            double y = radius * Math.cos(theta);
            double horizontalRadius = radius * Math.sin(theta);
            for (int lon = 0; lon < gridLon; lon++) {
                double u0 = lon / (double) gridLon;
                double u1 = (lon + 1.0D) / gridLon;
                addRingQuad(buffer, horizontalRadius, y, TWO_PI * u0, TWO_PI * u1, width, u0, u1);
            }
        }

        for (int lon = 0; lon < gridLon; lon++) {
            double phi = TWO_PI * lon / gridLon;
            double previousX = radius * Math.sin(0.0D) * Math.cos(phi);
            double previousY = radius;
            double previousZ = radius * Math.sin(0.0D) * Math.sin(phi);
            for (int lat = 1; lat <= gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                double x = radius * Math.sin(theta) * Math.cos(phi);
                double y = radius * Math.cos(theta);
                double z = radius * Math.sin(theta) * Math.sin(phi);
                addLine(buffer, previousX, previousY, previousZ, x, y, z, width);
                previousX = x;
                previousY = y;
                previousZ = z;
            }
        }

        tessellator.draw();
    }

    private static void drawShaderCircle(double radius, int segments, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            addRingQuad(buffer, radius, 0.0D, TWO_PI * u0, TWO_PI * u1, width, u0, u1);
        }
        tessellator.draw();
    }

    private static void drawShaderLine(double x0, double y0, double z0,
                                       double x1, double y1, double z1, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addLine(buffer, x0, y0, z0, x1, y1, z1, width);
        tessellator.draw();
    }

    private static void addLine2D(BufferBuilder buffer, double x0, double y0,
                                  double x1, double y1, double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 0.0001D) {
            return;
        }

        double px = -dy / length * width * 0.5D;
        double py = dx / length * width * 0.5D;
        addPosition(buffer, x0 + px, y0 + py, 0.0D, 0.0D, 1.0D);
        addPosition(buffer, x0 - px, y0 - py, 0.0D, 0.0D, 0.0D);
        addPosition(buffer, x1 - px, y1 - py, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x0 + px, y0 + py, 0.0D, 0.0D, 1.0D);
        addPosition(buffer, x1 - px, y1 - py, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, x1 + px, y1 + py, 0.0D, 1.0D, 1.0D);
    }

    private static void addLine(BufferBuilder buffer, double x0, double y0, double z0,
                                double x1, double y1, double z1, double width) {
        addLineRibbon(buffer, x0, y0, z0, x1, y1, z1, width);
    }

    private static void addRingQuad(BufferBuilder buffer, double radius, double y,
                                    double angle0, double angle1, double width, double u0, double u1) {
        double innerRadius = Math.max(0.001D, radius - width * 0.5D);
        double outerRadius = radius + width * 0.5D;
        double c0 = Math.cos(angle0);
        double s0 = Math.sin(angle0);
        double c1 = Math.cos(angle1);
        double s1 = Math.sin(angle1);
        addPosition(buffer, c0 * innerRadius, y, s0 * innerRadius, u0, 0.0D);
        addPosition(buffer, c0 * outerRadius, y, s0 * outerRadius, u0, 1.0D);
        addPosition(buffer, c1 * outerRadius, y, s1 * outerRadius, u1, 1.0D);
        addPosition(buffer, c0 * innerRadius, y, s0 * innerRadius, u0, 0.0D);
        addPosition(buffer, c1 * outerRadius, y, s1 * outerRadius, u1, 1.0D);
        addPosition(buffer, c1 * innerRadius, y, s1 * innerRadius, u1, 0.0D);
    }

    private static void addLineRibbon(BufferBuilder buffer, double x0, double y0, double z0,
                                      double x1, double y1, double z1, double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.0001D) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        double refX = Math.abs(dy) > 0.88D ? 1.0D : 0.0D;
        double refY = Math.abs(dy) > 0.88D ? 0.0D : 1.0D;
        double refZ = 0.0D;
        double px = dy * refZ - dz * refY;
        double py = dz * refX - dx * refZ;
        double pz = dx * refY - dy * refX;
        double pLength = Math.sqrt(px * px + py * py + pz * pz);
        if (pLength < 0.0001D) {
            px = 1.0D;
            py = 0.0D;
            pz = 0.0D;
            pLength = 1.0D;
        }

        double half = width * 0.5D / pLength;
        px *= half;
        py *= half;
        pz *= half;
        addRibbonVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addRibbonVertex(buffer, x0 - px, y0 - py, z0 - pz, 0.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addRibbonVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x1 + px, y1 + py, z1 + pz, 1.0D, 1.0D, px, py, pz);
    }

    private static void addRibbonVertex(BufferBuilder buffer, double x, double y, double z,
                                        double u, double v, double normalX, double normalY, double normalZ) {
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (normalLength < 0.0001D) {
            normalX = 0.0D;
            normalY = 1.0D;
            normalZ = 0.0D;
            normalLength = 1.0D;
        }
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) (normalX / normalLength), (float) (normalY / normalLength), (float) (normalZ / normalLength))
                .endVertex();
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
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
