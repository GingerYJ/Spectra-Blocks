package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileQuantumBubble;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderQuantumBubble extends TileEntitySpecialRenderer<TileQuantumBubble> {

    private static final double BUBBLE_RADIUS = 0.92D;
    private static final double INNER_RADIUS = 0.72D;
    private static final int SHELL_LAT_SEGMENTS = 20;
    private static final int SHELL_LON_SEGMENTS = 28;
    private static final int GRID_LAT_LINES = 7;
    private static final int GRID_LON_LINES = 12;
    private static final int GRID_SEGMENTS = 96;
    private static final int FLASH_POINT_COUNT = 48;
    private static final int ORBIT_ARC_COUNT = 5;
    private static final int ORBIT_SEGMENTS = 84;
    private static final float SHELL_ALPHA = 0.115F;
    private static final float GRID_ALPHA = 0.50F;
    private static final float POINT_ALPHA = 0.72F;
    private static final float ORBIT_ALPHA = 0.35F;
    private static final float GRID_ROTATION_SPEED = 0.38F;
    private static final int SHELL_COLOR = 0x65F7FF;
    private static final int GRID_COLOR = 0xB8FFF5;
    private static final int POINT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x73A7FF;
    private static final int WARNING_COLOR = 0xFFD86E;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileQuantumBubble te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }

        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        if (!RenderQuality.shouldRender(centerX, centerY, centerZ)) {
            return;
        }
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
            drawShell(ticks);
            drawJumpingGrid(ticks);
            if (!RenderQuality.low()) {
                drawOrbitArcs(ticks);
            }
            drawFlashPoints(ticks);
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

    private void drawShell(float ticks) {
        useNormalBlend();
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.050F);
        RenderHelper.drawSphere(BUBBLE_RADIUS + 0.025D * pulse, SHELL_COLOR,
                SHELL_ALPHA * (0.75F + 0.25F * pulse), SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);

        useAdditiveBlend();
        RenderHelper.drawSphere(INNER_RADIUS + 0.020D * (1.0D - pulse), SECONDARY_COLOR,
                SHELL_ALPHA * 0.42F, SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);
    }

    private void drawJumpingGrid(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * GRID_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) Math.sin(ticks * 0.017F) * 8.0F, 0.0F, 0.0F, 1.0F);

        GlStateManager.glLineWidth(2.6F);
        drawGrid(ticks, GRID_COLOR, GRID_ALPHA * 0.28F, 0.055D);
        GlStateManager.glLineWidth(1.2F);
        drawGrid(ticks + 11.0F, GRID_COLOR, GRID_ALPHA, 0.035D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawGrid(float ticks, int color, float alpha, double jitter) {
        int latLines = RenderQuality.low() ? 4 : GRID_LAT_LINES;
        int lonLines = RenderQuality.low() ? 7 : GRID_LON_LINES;
        for (int lat = 1; lat < latLines; lat++) {
            double theta = Math.PI * lat / latLines;
            double y = BUBBLE_RADIUS * Math.cos(theta);
            double radius = BUBBLE_RADIUS * Math.sin(theta);
            drawLatitude(radius, y, color, alpha, ticks, lat * 1.3D, jitter);
        }

        for (int lon = 0; lon < lonLines; lon++) {
            double phi = TWO_PI * lon / lonLines;
            drawLongitude(phi, color, alpha * (lon % 3 == 0 ? 1.0F : 0.62F), ticks, lon * 0.91D, jitter);
        }
    }

    private void drawLatitude(double radius, double y, int color, float alpha,
                              float ticks, double seed, double jitter) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        int segments = RenderQuality.scaleSegments(GRID_SEGMENTS, 16, 96);
        for (int i = 0; i < segments; i++) {
            double angle = TWO_PI * i / segments;
            double hop = Math.sin(angle * 8.0D + ticks * 0.080D + seed) * jitter;
            double localRadius = radius + hop;
            double localY = y + Math.cos(angle * 5.0D - ticks * 0.060D + seed) * jitter * 0.45D;
            float pointAlpha = alpha * (0.68F + 0.32F * (float) wave(ticks * 0.09D + i * 0.17D + seed));
            buffer.pos(Math.cos(angle) * localRadius, localY, Math.sin(angle) * localRadius)
                    .color(rgb[0], rgb[1], rgb[2], pointAlpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawLongitude(double phi, int color, float alpha, float ticks, double seed, double jitter) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int segments = RenderQuality.scaleSegments(GRID_SEGMENTS, 16, 96);
        for (int i = 0; i <= segments; i++) {
            double theta = Math.PI * i / segments;
            double pulse = Math.sin(theta * 9.0D - ticks * 0.090D + seed) * jitter;
            double radius = BUBBLE_RADIUS + pulse;
            double horizontal = Math.sin(theta) * radius;
            double y = Math.cos(theta) * radius;
            double wobblePhi = phi + Math.sin(theta * 4.0D + ticks * 0.045D + seed) * 0.018D;
            float pointAlpha = alpha * (0.60F + 0.40F * (float) Math.sin(theta));
            buffer.pos(Math.cos(wobblePhi) * horizontal, y, Math.sin(wobblePhi) * horizontal)
                    .color(rgb[0], rgb[1], rgb[2], pointAlpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawOrbitArcs(float ticks) {
        useAdditiveBlend();
        int arcCount = RenderQuality.detailCount(ORBIT_ARC_COUNT, 2);
        for (int i = 0; i < arcCount; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(36.0F + i * 28.0F + ticks * (0.22F + i * 0.04F), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(i * 61.0F - ticks * 0.31F, 0.0F, 1.0F, 0.0F);
            GlStateManager.glLineWidth(i == 0 ? 2.2F : 1.2F);
            drawOrbitArc(BUBBLE_RADIUS * (0.78D + i * 0.055D),
                    TWO_PI * (0.35D + (i % 3) * 0.08D),
                    ticks, i * 2.4D,
                    i % 2 == 0 ? SECONDARY_COLOR : WARNING_COLOR,
                    ORBIT_ALPHA * (i == 0 ? 1.0F : 0.72F));
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
    }

    private void drawOrbitArc(double radius, double sweep, float ticks, double seed, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double start = ticks * 0.018D + seed;
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int segments = RenderQuality.scaleSegments(ORBIT_SEGMENTS, 12, 84);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = start + progress * sweep;
            float fade = (float) Math.sin(progress * Math.PI);
            buffer.pos(Math.cos(angle) * radius,
                            Math.sin(angle * 3.0D + seed) * 0.026D,
                            Math.sin(angle) * radius)
                    .color(rgb[0], rgb[1], rgb[2], alpha * fade).endVertex();
        }
        tessellator.draw();
    }

    private void drawFlashPoints(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        for (int i = 0; i < FLASH_POINT_COUNT; i += stride) {
            double yaw = i * 2.399963229728653D + Math.sin(i * 1.7D) * 0.24D;
            double pitch = Math.asin(-0.92D + 1.84D * fract(i * 0.61803398875D));
            double pulse = Math.max(0.0D, Math.sin(ticks * (0.10D + (i % 5) * 0.018D) + i * 1.33D));
            double blink = pulse * pulse * pulse;
            if (blink <= 0.035D) {
                continue;
            }

            double radius = BUBBLE_RADIUS + 0.018D * Math.sin(ticks * 0.07D + i);
            double horizontal = Math.cos(pitch) * radius;
            double x = Math.cos(yaw) * horizontal;
            double y = Math.sin(pitch) * radius;
            double z = Math.sin(yaw) * horizontal;
            double size = 0.012D + blink * 0.035D;
            int color = i % 9 == 0 ? WARNING_COLOR : (i % 4 == 0 ? SECONDARY_COLOR : POINT_COLOR);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, color, POINT_ALPHA * (float) blink, 5, 5);
            drawEnergySpark(size * 2.2D, color, POINT_ALPHA * 0.48F * (float) blink);
            GlStateManager.popMatrix();
        }
    }

    private void drawEnergySpark(double size, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-size, 0.0D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(size, 0.0D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(0.0D, -size, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(0.0D, size, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(0.0D, 0.0D, -size).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(0.0D, 0.0D, size).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        tessellator.draw();
    }

    private static double wave(double value) {
        return 0.5D + 0.5D * Math.sin(value);
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
