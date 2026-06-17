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
            GlStateManager.glLineWidth(1.0F + i * 0.16F);
            drawShaderCircle(radius, 96);
        }
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();

        setTechUniforms(shader, ticks, 5.0F, 1.0F, 0x1AFFC0, SECONDARY_COLOR, WHITE_COLOR,
                0.045F, 0.95F, 2.74F);
        drawShaderWireSphere(2.74D, 7, 12);
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
            GlStateManager.glLineWidth(1.0F);
            drawShaderLine(x, -COLUMN_HEIGHT * 0.5D, z, x, COLUMN_HEIGHT * 0.5D, z);

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
        GlStateManager.glLineWidth(1.0F);
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
            GlStateManager.glLineWidth(2.0F);
            drawShaderCircle(radius, 88);

            setTechUniforms(shader, ticks, 5.0F, 5.0F, WHITE_COLOR, i == 1 ? SECONDARY_COLOR : PRIMARY_COLOR,
                    PRIMARY_COLOR, 0.10F * fade, 1.22F, (float) radius);
            GlStateManager.glLineWidth(1.0F);
            drawRuneMarks(radius, 0.16D, 18, ticks * 0.011D + i);
            GlStateManager.popMatrix();
        }
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private static void drawGlyph(int glyph, double size) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        if ((glyph & 1) == 0) {
            addLine2D(buffer, -size, -size, size, -size);
            addLine2D(buffer, -size, size, size, size);
        } else {
            addLine2D(buffer, -size, -size, -size, size);
            addLine2D(buffer, size, -size, size, size);
        }

        if ((glyph & 2) == 0) {
            addLine2D(buffer, -size, 0.0D, size, 0.0D);
        } else {
            addLine2D(buffer, 0.0D, -size, 0.0D, size);
        }

        if ((glyph & 4) == 0) {
            addLine2D(buffer, -size, -size, size, size);
        } else {
            addLine2D(buffer, -size, size, size, -size);
        }

        tessellator.draw();
    }

    private static void drawRuneMarks(double radius, double length, int count, double phase) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < count; i++) {
            double angle = phase + TWO_PI * i / count;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double inner = radius - length * 0.5D;
            double outer = radius + length * 0.5D;
            addLine(buffer, cos * inner, 0.0D, sin * inner, cos * outer, 0.0D, sin * outer);

            if ((i & 3) == 0) {
                double tangentX = -sin * length * 0.36D;
                double tangentZ = cos * length * 0.36D;
                double markRadius = radius + length * 0.18D;
                addLine(buffer, cos * markRadius - tangentX, 0.0D, sin * markRadius - tangentZ,
                        cos * markRadius + tangentX, 0.0D, sin * markRadius + tangentZ);
            }
        }

        tessellator.draw();
    }

    private static void drawShaderWireSphere(double radius, int gridLat, int gridLon) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int lat = 1; lat < gridLat; lat++) {
            double theta = Math.PI * lat / gridLat;
            double y = radius * Math.cos(theta);
            double horizontalRadius = radius * Math.sin(theta);
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lon = 0; lon < gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
                addPosition(buffer, horizontalRadius * Math.cos(phi), y, horizontalRadius * Math.sin(phi),
                        lon / (double) gridLon, lat / (double) gridLat);
            }
            tessellator.draw();
        }

        for (int lon = 0; lon < gridLon; lon++) {
            double phi = TWO_PI * lon / gridLon;
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lat = 0; lat <= gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                addPosition(buffer, radius * Math.sin(theta) * Math.cos(phi),
                        radius * Math.cos(theta),
                        radius * Math.sin(theta) * Math.sin(phi),
                        lon / (double) gridLon, lat / (double) gridLat);
            }
            tessellator.draw();
        }
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

    private static void addLine2D(BufferBuilder buffer, double x0, double y0, double x1, double y1) {
        addLine(buffer, x0, y0, 0.0D, x1, y1, 0.0D);
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
