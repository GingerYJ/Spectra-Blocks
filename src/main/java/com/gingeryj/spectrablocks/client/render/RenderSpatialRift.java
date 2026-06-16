package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileSpatialRift;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSpatialRift extends TileEntitySpecialRenderer<TileSpatialRift> {

    private static final double HALF_HEIGHT = 1.24D;
    private static final double BASE_HALF_WIDTH = 0.055D;
    private static final double WIDTH_VARIANCE = 0.105D;
    private static final double JITTER_STRENGTH = 0.060D;
    private static final int CRACK_SEGMENTS = 18;
    private static final int RIPPLE_SEGMENTS = 96;
    private static final int RIPPLE_COUNT = 5;
    private static final int LIGHT_SHARD_COUNT = 42;
    private static final float PRIMARY_PLANE_ALPHA = 0.74F;
    private static final float SECONDARY_PLANE_ALPHA = 0.38F;
    private static final float EDGE_ALPHA = 0.58F;
    private static final float FLASH_ALPHA = 0.42F;
    private static final float RIPPLE_ALPHA = 0.145F;
    private static final float SHARD_ALPHA = 0.62F;
    private static final float PLANE_ROTATION_SPEED = 0.16F;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int VOID_COLOR = 0x03020A;
    private static final int EDGE_COLOR = 0x74F5FF;
    private static final int OUTER_EDGE_COLOR = 0xC65DFF;
    private static final int FLASH_COLOR = 0xFFFFFF;
    private static final int RIPPLE_COLOR = 0x3CCBFF;

    @Override
    public void render(TileSpatialRift te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
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
            drawRiftPlane(ticks, 0.0F, 0.0D, PRIMARY_PLANE_ALPHA);
            drawRiftPlane(ticks, 86.0F, 9.0D, SECONDARY_PLANE_ALPHA);
            drawRiftPlane(ticks, -48.0F, 18.0D, SECONDARY_PLANE_ALPHA * 0.68F);
            drawLightShards(ticks);
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
            GlStateManager.popMatrix();
        }
    }

    private void drawRiftPlane(float ticks, float baseRotation, double seed, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(baseRotation + (float) Math.sin(ticks * 0.018F + seed) * PLANE_ROTATION_SPEED,
                0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) Math.sin(ticks * 0.022F + seed * 0.71D) * 3.0F,
                0.0F, 0.0F, 1.0F);

        useNormalBlend();
        drawCrackBody(ticks, seed, alpha);

        useAdditiveBlend();
        drawRippleNoise(ticks, seed, alpha);
        drawCrackEdges(ticks, seed, alpha);
        GlStateManager.popMatrix();
    }

    private void drawCrackBody(float ticks, double seed, float alpha) {
        float[] rgb = RenderHelper.unpackRGB(VOID_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= CRACK_SEGMENTS; i++) {
            double progress = (double) i / CRACK_SEGMENTS;
            double y = -HALF_HEIGHT + progress * HALF_HEIGHT * 2.0D;
            double center = crackCenter(progress, ticks, seed);
            double halfWidth = crackHalfWidth(progress, ticks, seed);
            buffer.pos(center - halfWidth, y, -0.006D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            buffer.pos(center + halfWidth, y, 0.006D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawCrackEdges(float ticks, double seed, float planeAlpha) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.090F + seed);
        float flash = sharpPulse(ticks * 0.41F + (float) seed);

        GlStateManager.glLineWidth(5.0F);
        drawCrackEdge(ticks, seed, -1.0D, OUTER_EDGE_COLOR, EDGE_ALPHA * planeAlpha * (0.55F + 0.45F * pulse));
        drawCrackEdge(ticks, seed, 1.0D, OUTER_EDGE_COLOR, EDGE_ALPHA * planeAlpha * (0.55F + 0.45F * pulse));

        GlStateManager.glLineWidth(2.0F);
        drawCrackEdge(ticks, seed, -1.0D, EDGE_COLOR, planeAlpha * (0.42F + 0.32F * pulse));
        drawCrackEdge(ticks, seed, 1.0D, EDGE_COLOR, planeAlpha * (0.42F + 0.32F * pulse));

        if (flash > 0.08F) {
            GlStateManager.glLineWidth(1.0F);
            drawCrackEdge(ticks + 3.0F, seed, -1.0D, FLASH_COLOR, FLASH_ALPHA * flash * planeAlpha);
            drawCrackEdge(ticks + 3.0F, seed, 1.0D, FLASH_COLOR, FLASH_ALPHA * flash * planeAlpha);
        }
        RenderHelper.resetLineWidth();
    }

    private void drawCrackEdge(float ticks, double seed, double side, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= CRACK_SEGMENTS; i++) {
            double progress = (double) i / CRACK_SEGMENTS;
            double y = -HALF_HEIGHT + progress * HALF_HEIGHT * 2.0D;
            double center = crackCenter(progress, ticks, seed);
            double halfWidth = crackHalfWidth(progress, ticks, seed);
            double saw = Math.sin(progress * Math.PI * 37.0D + seed) * 0.020D;
            buffer.pos(center + side * (halfWidth + saw), y, 0.013D * side)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawRippleNoise(float ticks, double seed, float planeAlpha) {
        for (int ring = 0; ring < RIPPLE_COUNT; ring++) {
            double age = fract(ticks * 0.006D + ring * 0.217D + seed * 0.013D);
            double width = 0.28D + age * 0.88D;
            double height = 0.48D + age * 1.10D;
            float fade = (float) Math.sin(age * Math.PI);
            drawWavyEllipse(width, height, ticks, seed + ring * 2.7D,
                    RIPPLE_COLOR, RIPPLE_ALPHA * fade * planeAlpha);
        }
    }

    private void drawWavyEllipse(double width, double height, float ticks, double seed, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < RIPPLE_SEGMENTS; i++) {
            double angle = 2.0D * Math.PI * i / RIPPLE_SEGMENTS;
            double noise = 1.0D
                    + Math.sin(angle * 5.0D + ticks * 0.050D + seed) * 0.055D
                    + Math.cos(angle * 9.0D - ticks * 0.032D + seed * 0.7D) * 0.030D;
            buffer.pos(Math.cos(angle) * width * noise,
                            Math.sin(angle) * height * noise,
                            Math.sin(angle * 3.0D + ticks * 0.040D + seed) * 0.018D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawLightShards(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < LIGHT_SHARD_COUNT; i++) {
            double phase = fract(ticks * (0.010D + (i % 5) * 0.0015D) + i * 0.137D);
            double angle = i * GOLDEN_ANGLE + ticks * 0.026D;
            double radius = 0.24D + phase * (1.35D + (i % 4) * 0.08D);
            double y = -1.04D + (i % 19) * (2.08D / 18.0D)
                    + Math.sin(ticks * 0.040D + i) * 0.050D;
            double x = Math.cos(angle) * radius * 0.46D;
            double z = Math.sin(angle) * radius * 0.58D;
            double sparkle = Math.max(0.0D, Math.sin(ticks * 0.23D + i * 1.91D));
            float fade = (float) Math.sin(phase * Math.PI);
            float alpha = SHARD_ALPHA * fade * (0.42F + 0.58F * (float) sparkle);
            double size = 0.018D + sparkle * 0.026D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, i % 3 == 0 ? FLASH_COLOR : EDGE_COLOR, alpha, 7, 7);
            GlStateManager.popMatrix();

            double tail = 0.055D + sparkle * 0.075D;
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.drawLine(x, y, z, x - Math.cos(angle) * tail, y + 0.025D, z - Math.sin(angle) * tail,
                    i % 2 == 0 ? EDGE_COLOR : OUTER_EDGE_COLOR, alpha * 0.58F);
        }
        RenderHelper.resetLineWidth();
        useNormalBlend();
    }

    private static double crackCenter(double progress, float ticks, double seed) {
        return Math.sin(progress * Math.PI * 5.0D + ticks * 0.036D + seed) * JITTER_STRENGTH
                + Math.sin(progress * Math.PI * 13.0D + seed * 1.37D) * 0.034D;
    }

    private static double crackHalfWidth(double progress, float ticks, double seed) {
        double taper = Math.sin(progress * Math.PI);
        double noise = 0.5D + 0.5D * Math.sin(progress * Math.PI * 8.0D - ticks * 0.058D + seed);
        double jagged = 0.5D + 0.5D * Math.sin(progress * Math.PI * 23.0D + seed * 2.11D);
        return (BASE_HALF_WIDTH + WIDTH_VARIANCE * (0.65D * noise + 0.35D * jagged))
                * (0.18D + 0.82D * taper);
    }

    private static float sharpPulse(float value) {
        float pulse = Math.max(0.0F, (float) Math.sin(value));
        return pulse * pulse * pulse * pulse;
    }

    private static double fract(double value) {
        return value - Math.floor(value);
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
