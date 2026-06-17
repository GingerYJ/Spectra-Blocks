package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileImaginaryCube;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderImaginaryCube extends TileEntitySpecialRenderer<TileImaginaryCube> {

    private static final double BASE_HALF_SIZE = 0.60D;
    private static final double OUTER_HALF_SIZE = 0.82D;
    private static final double INNER_HALF_SIZE = 0.42D;
    private static final double OFFSET_STRENGTH = 0.105D;
    private static final int CUBE_LAYER_COUNT = 4;
    private static final int CORNER_SPARK_COUNT = 16;
    private static final int CROSS_LINE_COUNT = 12;
    private static final float FRAME_ALPHA = 0.68F;
    private static final float GHOST_ALPHA = 0.24F;
    private static final float FACE_ALPHA = 0.060F;
    private static final float SPARK_ALPHA = 0.70F;
    private static final float ROTATION_SPEED = 0.36F;
    private static final int PRIMARY_COLOR = 0x7AFDFF;
    private static final int SECONDARY_COLOR = 0xFF75E6;
    private static final int GHOST_COLOR = 0xA58CFF;
    private static final int FACE_COLOR = 0x1A274B;
    private static final int HOT_COLOR = 0xFFFFFF;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileImaginaryCube te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }

        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        RenderQuality.update(centerX, centerY, centerZ);
        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(1.0D);
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        useNormalBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            drawTransparentFaces(ticks);
            drawShiftedFrames(ticks);
            drawCrossLines(ticks);
            drawCornerSparks(ticks);
        } finally {
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            if (!blendWasEnabled) {
                GlStateManager.disableBlend();
            }
            useNormalBlend();
            RenderHelper.resetLineWidth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void drawTransparentFaces(float ticks) {
        useNormalBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * ROTATION_SPEED * 0.45F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(18.0F + (float) Math.sin(ticks * 0.020F) * 4.0F, 1.0F, 0.0F, 0.0F);
        drawCubeFaces(INNER_HALF_SIZE, FACE_COLOR, FACE_ALPHA * (0.72F + 0.28F * (float) Math.sin(ticks * 0.065F)));
        GlStateManager.popMatrix();
    }

    private void drawShiftedFrames(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CUBE_LAYER_COUNT; i++) {
            double layer = (double) i / (CUBE_LAYER_COUNT - 1);
            double size = BASE_HALF_SIZE + (layer - 0.5D) * 0.22D;
            float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.115F + i * 1.8F);
            double offsetX = Math.sin(ticks * 0.034D + i * 2.1D) * OFFSET_STRENGTH;
            double offsetY = Math.cos(ticks * 0.030D + i * 1.7D) * OFFSET_STRENGTH * 0.72D;
            double offsetZ = Math.sin(ticks * 0.028D + i * 2.8D) * OFFSET_STRENGTH;
            int color = i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR;
            float alpha = (i == 1 ? FRAME_ALPHA : GHOST_ALPHA) * (0.62F + 0.38F * pulse);

            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
            GlStateManager.rotate(ticks * (ROTATION_SPEED + i * 0.11F) + i * 21.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.0F + i * 17.0F + (float) Math.sin(ticks * 0.018F + i) * 6.0F,
                    1.0F, 0.0F, 0.0F);
            GlStateManager.glLineWidth(i == 1 ? 2.8F : 1.3F);
            drawCubeEdges(size, color, alpha);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * ROTATION_SPEED * 0.72F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.glLineWidth(1.0F);
        drawCubeEdges(OUTER_HALF_SIZE, GHOST_COLOR, GHOST_ALPHA * 0.65F);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawCrossLines(float ticks) {
        useAdditiveBlend();
        float[] rgbA = RenderHelper.unpackRGB(PRIMARY_COLOR);
        float[] rgbB = RenderHelper.unpackRGB(SECONDARY_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(1.0F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < CROSS_LINE_COUNT; i++) {
            double phase = TWO_PI * i / CROSS_LINE_COUNT;
            double pulse = Math.max(0.0D, Math.sin(ticks * 0.075D + i * 0.83D));
            double a = OUTER_HALF_SIZE * (0.56D + 0.24D * Math.sin(ticks * 0.021D + i));
            double b = OUTER_HALF_SIZE * (0.56D + 0.24D * Math.cos(ticks * 0.026D + i));
            float alpha = (float) (GHOST_ALPHA * pulse);
            if (alpha <= 0.01F) {
                continue;
            }

            float[] rgb = i % 2 == 0 ? rgbA : rgbB;
            buffer.pos(Math.cos(phase) * a, Math.sin(phase * 1.7D) * b, -OUTER_HALF_SIZE)
                    .color(rgb[0], rgb[1], rgb[2], alpha * 0.42F).endVertex();
            buffer.pos(-Math.sin(phase) * b, Math.cos(phase * 1.3D) * a, OUTER_HALF_SIZE)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
        RenderHelper.resetLineWidth();
    }

    private void drawCornerSparks(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CORNER_SPARK_COUNT; i++) {
            double signX = (i & 1) == 0 ? -1.0D : 1.0D;
            double signY = (i & 2) == 0 ? -1.0D : 1.0D;
            double signZ = (i & 4) == 0 ? -1.0D : 1.0D;
            double ghost = (i & 8) == 0 ? 0.0D : 0.09D;
            double size = BASE_HALF_SIZE + ghost;
            double pulse = Math.max(0.0D, Math.sin(ticks * 0.125D + i * 1.17D));
            double blink = pulse * pulse;
            if (blink <= 0.03D) {
                continue;
            }

            double x = signX * size + Math.sin(ticks * 0.040D + i) * 0.035D;
            double y = signY * size + Math.cos(ticks * 0.034D + i) * 0.035D;
            double z = signZ * size + Math.sin(ticks * 0.030D + i * 2.0D) * 0.035D;
            double sparkSize = 0.018D + blink * 0.035D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(sparkSize, i % 3 == 0 ? HOT_COLOR : PRIMARY_COLOR,
                    SPARK_ALPHA * (float) blink, 5, 5);
            drawSpark(sparkSize * 2.4D, i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR,
                    SPARK_ALPHA * 0.50F * (float) blink);
            GlStateManager.popMatrix();
        }
    }

    private void drawCubeFaces(double halfSize, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFace(buffer, -halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, rgb, alpha);
        addFace(buffer, halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, rgb, alpha);
        addFace(buffer, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, rgb, alpha);
        addFace(buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, rgb, alpha);
        addFace(buffer, halfSize, -halfSize, -halfSize, halfSize, halfSize, halfSize, rgb, alpha);
        addFace(buffer, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, rgb, alpha);
        tessellator.draw();
    }

    private void addFace(BufferBuilder buffer, double x0, double y0, double z0,
                         double x1, double y1, double z1, float[] rgb, float alpha) {
        buffer.pos(x0, y0, z0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x1, y0, z0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha * 0.78F).endVertex();
        buffer.pos(x0, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha * 0.78F).endVertex();
    }

    private void drawCubeEdges(double halfSize, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                addLine(buffer, x * halfSize, y * halfSize, -halfSize,
                        x * halfSize, y * halfSize, halfSize, rgb, alpha);
            }
        }
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                addLine(buffer, x * halfSize, -halfSize, z * halfSize,
                        x * halfSize, halfSize, z * halfSize, rgb, alpha);
            }
        }
        for (int y = -1; y <= 1; y += 2) {
            for (int z = -1; z <= 1; z += 2) {
                addLine(buffer, -halfSize, y * halfSize, z * halfSize,
                        halfSize, y * halfSize, z * halfSize, rgb, alpha);
            }
        }
        tessellator.draw();
    }

    private void drawSpark(double size, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        addLine(buffer, -size, 0.0D, 0.0D, size, 0.0D, 0.0D, rgb, alpha);
        addLine(buffer, 0.0D, -size, 0.0D, 0.0D, size, 0.0D, rgb, alpha);
        addLine(buffer, 0.0D, 0.0D, -size, 0.0D, 0.0D, size, rgb, alpha);
        tessellator.draw();
    }

    private void addLine(BufferBuilder buffer, double x0, double y0, double z0,
                         double x1, double y1, double z1, float[] rgb, float alpha) {
        buffer.pos(x0, y0, z0).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }

    private static void useAdditiveBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private static void useNormalBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
