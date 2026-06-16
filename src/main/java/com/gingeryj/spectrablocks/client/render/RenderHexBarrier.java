package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileHexBarrier;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderHexBarrier extends RenderCelestialEffectBase<TileHexBarrier> {

    private static final double PANEL_WIDTH = 3.84D;
    private static final double PANEL_HEIGHT = 2.96D;
    private static final double PANEL_CENTER_Y = 0.34D;
    private static final double PANEL_HALF_WIDTH = PANEL_WIDTH * 0.5D;
    private static final double PANEL_HALF_HEIGHT = PANEL_HEIGHT * 0.5D;
    private static final double CELL_RADIUS = 0.315D;
    private static final double HEX_ANGLE_OFFSET = Math.PI / 6.0D;
    private static final int GRID_ROWS = 7;
    private static final int GRID_COLUMNS = 9;
    private static final int LAYER_COUNT = 3;
    private static final float PANEL_DRIFT_SPEED = 0.022F;
    private static final float CELL_PULSE_SPEED = 0.045F;
    private static final float SCAN_SPEED = 0.008F;
    private static final float BORDER_PULSE_SPEED = 0.034F;
    private static final float PANEL_ALPHA = 0.078F;
    private static final float HEX_ALPHA = 0.48F;
    private static final float FILL_ALPHA = 0.070F;
    private static final float SCAN_ALPHA = 0.34F;
    private static final float BORDER_ALPHA = 0.54F;
    private static final int PANEL_COLOR = 0x2CEBFF;
    private static final int GRID_COLOR = 0x9DFFF4;
    private static final int HOT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x5B8CFF;

    @Override
    protected void renderCelestialEffect(TileHexBarrier te, float ticks) {
        drawPanelLayers(ticks);
        drawHoneycomb(ticks);
        drawScanBand(ticks);
        drawBorderRails(ticks);
    }

    private void drawPanelLayers(float ticks) {
        useAlphaBlend();
        for (int i = 0; i < LAYER_COUNT; i++) {
            double zOffset = (i - 1) * 0.055D;
            float yaw = (i - 1) * 2.1F + (float) Math.sin(ticks * PANEL_DRIFT_SPEED + i) * 0.45F;
            float alpha = PANEL_ALPHA * (1.0F - i * 0.18F);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            drawPanelQuad(zOffset, i == 1 ? PANEL_COLOR : SECONDARY_COLOR, alpha);
            GlStateManager.popMatrix();
        }
    }

    private void drawHoneycomb(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.35F);

        double xStep = Math.sqrt(3.0D) * CELL_RADIUS;
        double yStep = 1.5D * CELL_RADIUS;
        int halfRows = GRID_ROWS / 2;
        int halfColumns = GRID_COLUMNS / 2;

        for (int row = -halfRows; row <= halfRows; row++) {
            double y = PANEL_CENTER_Y + row * yStep;
            double xOffset = (row & 1) == 0 ? 0.0D : xStep * 0.5D;
            for (int column = -halfColumns; column <= halfColumns; column++) {
                double x = column * xStep + xOffset;
                if (Math.abs(x) > PANEL_HALF_WIDTH - CELL_RADIUS * 0.42D
                        || Math.abs(y - PANEL_CENTER_Y) > PANEL_HALF_HEIGHT - CELL_RADIUS * 0.28D) {
                    continue;
                }

                double scanPosition = -PANEL_HALF_WIDTH + fract(ticks * SCAN_SPEED) * PANEL_WIDTH;
                double scanBoost = Math.max(0.0D, 1.0D - Math.abs(x - scanPosition) / 0.42D);
                float pulse = wave(ticks * CELL_PULSE_SPEED + row * 0.73D + column * 0.41D);
                float alpha = HEX_ALPHA * (0.45F + pulse * 0.36F + (float) scanBoost * 0.42F);
                int color = scanBoost > 0.35D ? HOT_COLOR : (row + column) % 4 == 0 ? SECONDARY_COLOR : GRID_COLOR;

                if (((row * 37 + column * 19) & 3) == 0) {
                    drawHexFill(x, y, 0.012D, CELL_RADIUS * 0.88D, color, FILL_ALPHA * (0.55F + pulse * 0.45F));
                }
                drawHexOutline(x, y, 0.018D, CELL_RADIUS, color, alpha);
            }
        }

        RenderHelper.resetLineWidth();
    }

    private void drawScanBand(float ticks) {
        useAdditiveBlend();
        double scanPosition = -PANEL_HALF_WIDTH + fract(ticks * SCAN_SPEED) * PANEL_WIDTH;
        double bandWidth = 0.18D + wave(ticks * BORDER_PULSE_SPEED) * 0.05D;

        float[] rgb = RenderHelper.unpackRGB(HOT_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(scanPosition - bandWidth, PANEL_CENTER_Y - PANEL_HALF_HEIGHT, 0.030D)
                .color(rgb[0], rgb[1], rgb[2], 0.0F)
                .endVertex();
        buffer.pos(scanPosition, PANEL_CENTER_Y - PANEL_HALF_HEIGHT, 0.032D)
                .color(rgb[0], rgb[1], rgb[2], SCAN_ALPHA * 0.55F)
                .endVertex();
        buffer.pos(scanPosition - bandWidth, PANEL_CENTER_Y + PANEL_HALF_HEIGHT, 0.030D)
                .color(rgb[0], rgb[1], rgb[2], 0.0F)
                .endVertex();
        buffer.pos(scanPosition, PANEL_CENTER_Y + PANEL_HALF_HEIGHT, 0.032D)
                .color(rgb[0], rgb[1], rgb[2], SCAN_ALPHA)
                .endVertex();
        buffer.pos(scanPosition + bandWidth, PANEL_CENTER_Y + PANEL_HALF_HEIGHT, 0.030D)
                .color(rgb[0], rgb[1], rgb[2], 0.0F)
                .endVertex();
        buffer.pos(scanPosition + bandWidth, PANEL_CENTER_Y - PANEL_HALF_HEIGHT, 0.030D)
                .color(rgb[0], rgb[1], rgb[2], 0.0F)
                .endVertex();
        tessellator.draw();
    }

    private void drawBorderRails(float ticks) {
        float pulse = wave(ticks * BORDER_PULSE_SPEED);
        float alpha = BORDER_ALPHA * (0.70F + pulse * 0.30F);
        double left = -PANEL_HALF_WIDTH;
        double right = PANEL_HALF_WIDTH;
        double bottom = PANEL_CENTER_Y - PANEL_HALF_HEIGHT;
        double top = PANEL_CENTER_Y + PANEL_HALF_HEIGHT;

        useAdditiveBlend();
        GlStateManager.glLineWidth(2.8F);
        RenderHelper.drawLine(left, bottom, 0.046D, right, bottom, 0.046D, HOT_COLOR, alpha * 0.68F);
        RenderHelper.drawLine(right, bottom, 0.046D, right, top, 0.046D, HOT_COLOR, alpha);
        RenderHelper.drawLine(right, top, 0.046D, left, top, 0.046D, HOT_COLOR, alpha * 0.68F);
        RenderHelper.drawLine(left, top, 0.046D, left, bottom, 0.046D, HOT_COLOR, alpha);
        GlStateManager.glLineWidth(1.1F);
        RenderHelper.drawLine(left * 0.94D, PANEL_CENTER_Y, 0.060D,
                right * 0.94D, PANEL_CENTER_Y, 0.060D, GRID_COLOR, alpha * 0.32F);
        RenderHelper.resetLineWidth();
    }

    private static void drawPanelQuad(double z, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        double left = -PANEL_HALF_WIDTH;
        double right = PANEL_HALF_WIDTH;
        double bottom = PANEL_CENTER_Y - PANEL_HALF_HEIGHT;
        double top = PANEL_CENTER_Y + PANEL_HALF_HEIGHT;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(left, bottom, z).color(rgb[0], rgb[1], rgb[2], alpha * 0.20F).endVertex();
        buffer.pos(right, bottom, z).color(rgb[0], rgb[1], rgb[2], alpha * 0.42F).endVertex();
        buffer.pos(left, top, z).color(rgb[0], rgb[1], rgb[2], alpha * 0.58F).endVertex();
        buffer.pos(right, top, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        tessellator.draw();
    }

    private static void drawHexFill(double centerX, double centerY, double z, double radius,
                                    int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(centerX, centerY, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        for (int i = 0; i <= 6; i++) {
            double angle = HEX_ANGLE_OFFSET + Math.PI * 2.0D * i / 6.0D;
            buffer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha * 0.18F)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void drawHexOutline(double centerX, double centerY, double z, double radius,
                                       int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 6; i++) {
            double angle = HEX_ANGLE_OFFSET + Math.PI * 2.0D * i / 6.0D;
            double sparkle = 0.76D + 0.24D * (i % 2);
            buffer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, z)
                    .color(rgb[0], rgb[1], rgb[2], alpha * (float) sparkle)
                    .endVertex();
        }
        tessellator.draw();
    }
}
