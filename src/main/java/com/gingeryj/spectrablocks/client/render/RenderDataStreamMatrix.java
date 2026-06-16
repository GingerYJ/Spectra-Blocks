package com.gingeryj.spectrablocks.client.render;

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
        drawCoreGrid(ticks);
        drawColumns(ticks);
        drawScanRings(ticks);
    }

    private void drawCoreGrid(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * MATRIX_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        for (int i = 0; i < GRID_RING_COUNT; i++) {
            double radius = 0.72D + i * 0.54D;
            float alpha = 0.070F + 0.030F * wave(ticks * 0.041D + i);
            GlStateManager.glLineWidth(1.0F + i * 0.16F);
            RenderHelper.drawCircle(radius, i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR, alpha, 96);
        }
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        RenderHelper.drawWireframeSphere(2.74D, 0x1AFFC0, 0.045F, 7, 12);
        useAlphaBlend();
    }

    private void drawColumns(float ticks) {
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

            drawColumnGuide(x, z, color, 0.045F + 0.025F * wave(ticks * 0.030D + i));
            for (int j = 0; j < GLYPHS_PER_COLUMN; j++) {
                double local = fract(drift + (double) j / GLYPHS_PER_COLUMN);
                double y = COLUMN_HEIGHT * 0.5D - local * COLUMN_HEIGHT;
                float fade = (float) Math.sin(Math.PI * local);
                float alpha = (0.10F + 0.36F * fade) * (j == 0 ? 1.15F : 1.0F);
                int glyphColor = j == 0 ? WHITE_COLOR : color;

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate((float) Math.toDegrees(-angle) + 90.0F, 0.0F, 1.0F, 0.0F);
                drawGlyph((i + j * 5) & 7, GLYPH_SIZE * (0.86D + (j % 3) * 0.10D), glyphColor, alpha);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawScanRings(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 3; i++) {
            double progress = fract(ticks * 0.010D + i * 0.333D);
            double y = 1.82D - progress * 3.64D;
            float fade = (float) Math.sin(Math.PI * progress);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(ticks * (0.045F + i * 0.008F), 0.0F, 1.0F, 0.0F);
            GlStateManager.glLineWidth(2.0F);
            RenderHelper.drawCircle(2.62D - i * 0.24D, i == 1 ? SECONDARY_COLOR : PRIMARY_COLOR,
                    0.11F * fade, 88);
            GlStateManager.glLineWidth(1.0F);
            RenderEnergyEffectHelper.drawRuneMarks(2.62D - i * 0.24D, 0.16D, 18,
                    WHITE_COLOR, 0.10F * fade, ticks * 0.011D + i);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawColumnGuide(double x, double z, int color, float alpha) {
        GlStateManager.glLineWidth(1.0F);
        RenderHelper.drawLine(x, -COLUMN_HEIGHT * 0.5D, z, x, COLUMN_HEIGHT * 0.5D, z, color, alpha);
        RenderHelper.resetLineWidth();
    }

    private static void drawGlyph(int glyph, double size, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        if ((glyph & 1) == 0) {
            addLine(buffer, -size, -size, size, -size, rgb, alpha);
            addLine(buffer, -size, size, size, size, rgb, alpha * 0.80F);
        } else {
            addLine(buffer, -size, -size, -size, size, rgb, alpha);
            addLine(buffer, size, -size, size, size, rgb, alpha * 0.80F);
        }

        if ((glyph & 2) == 0) {
            addLine(buffer, -size, 0.0D, size, 0.0D, rgb, alpha * 0.92F);
        } else {
            addLine(buffer, 0.0D, -size, 0.0D, size, rgb, alpha * 0.92F);
        }

        if ((glyph & 4) == 0) {
            addLine(buffer, -size, -size, size, size, rgb, alpha * 0.72F);
        } else {
            addLine(buffer, -size, size, size, -size, rgb, alpha * 0.72F);
        }

        tessellator.draw();
    }

    private static void addLine(BufferBuilder buffer, double x0, double y0, double x1, double y1,
                                float[] rgb, float alpha) {
        buffer.pos(x0, y0, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x1, y1, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
