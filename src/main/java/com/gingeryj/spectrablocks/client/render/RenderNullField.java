package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileNullField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderNullField extends RenderCelestialEffectBase<TileNullField> {

    private static final double OUTER_RADIUS = 2.05D;
    private static final double INNER_RADIUS = 1.46D;
    private static final double CORE_RADIUS = 0.34D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int SHELL_LAT_SEGMENTS = 22;
    private static final int SHELL_LON_SEGMENTS = 32;
    private static final int CAGE_LAT_LINES = 8;
    private static final int CAGE_SEGMENTS = 96;
    private static final int ARC_COUNT = 10;
    private static final int STATIC_POINT_COUNT = 42;
    private static final float BREATH_SPEED = 0.026F;
    private static final float CAGE_ROTATION_SPEED = 0.034F;
    private static final float ARC_DRIFT_SPEED = 0.011F;
    private static final float STATIC_BLINK_SPEED = 0.088F;
    private static final float OUTER_ALPHA = 0.215F;
    private static final float INNER_ALPHA = 0.090F;
    private static final float CAGE_ALPHA = 0.155F;
    private static final float CORE_ALPHA = 0.275F;
    private static final float STATIC_ALPHA = 0.52F;
    private static final int OUTER_COLOR = 0x08090D;
    private static final int INNER_COLOR = 0x262A33;
    private static final int CAGE_COLOR = 0x6E7482;
    private static final int CORE_COLOR = 0x000000;
    private static final int STATIC_COLOR = 0xB7C8FF;
    private static final int MUTED_COLOR = 0x4A4F5F;

    @Override
    protected void renderCelestialEffect(TileNullField te, float ticks) {
        drawSuppressionShell(ticks);
        drawMutedCage(ticks);
        drawCompressedCore(ticks);
        drawStaticPoints(ticks);
    }

    private void drawSuppressionShell(float ticks) {
        float breath = wave(ticks * BREATH_SPEED);

        useAlphaBlend();
        RenderHelper.drawSphere(OUTER_RADIUS + breath * 0.045D, OUTER_COLOR,
                OUTER_ALPHA + breath * 0.035F, SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);
        RenderHelper.drawSphere(INNER_RADIUS - breath * 0.035D, INNER_COLOR,
                INNER_ALPHA, SHELL_LAT_SEGMENTS - 4, SHELL_LON_SEGMENTS - 4);
    }

    private void drawMutedCage(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * CAGE_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) Math.sin(ticks * ARC_DRIFT_SPEED) * 7.5F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(1.1F);

        for (int i = 1; i < CAGE_LAT_LINES; i++) {
            double y = -OUTER_RADIUS + 2.0D * OUTER_RADIUS * i / CAGE_LAT_LINES;
            float alpha = CAGE_ALPHA * (0.35F + 0.65F * (float) Math.sin((double) i / CAGE_LAT_LINES * Math.PI));
            drawLatitudeCircle(OUTER_RADIUS * 1.006D, y, CAGE_COLOR, alpha, CAGE_SEGMENTS);
        }

        for (int i = 0; i < ARC_COUNT; i++) {
            double startYaw = i * TWO_PI / ARC_COUNT + ticks * ARC_DRIFT_SPEED;
            double sweep = TWO_PI * (0.28D + (i % 3) * 0.045D);
            double pitch = -0.55D + (i % 5) * 0.26D;
            drawSphericalArc(OUTER_RADIUS * 1.018D, startYaw, sweep, pitch, 0.055D,
                    ticks * ARC_DRIFT_SPEED + i, i % 2 == 0 ? CAGE_COLOR : MUTED_COLOR,
                    CAGE_ALPHA * (i % 2 == 0 ? 0.90F : 0.56F), 56);
        }

        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawCompressedCore(float ticks) {
        float breath = wave(ticks * BREATH_SPEED + 1.4D);

        useAlphaBlend();
        RenderHelper.drawSphere(CORE_RADIUS + breath * 0.025D, CORE_COLOR,
                CORE_ALPHA, 12, 12);
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(1.0F);
        drawFlatRing(0.42D, 0.72D + breath * 0.08D, MUTED_COLOR, 0.048F + breath * 0.020F, 72);
        drawFlatRing(0.86D, 1.06D + breath * 0.05D, CAGE_COLOR, 0.030F, 72);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawStaticPoints(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STATIC_POINT_COUNT; i++) {
            double blink = Math.max(0.0D, Math.sin(ticks * (STATIC_BLINK_SPEED + (i % 5) * 0.006D) + i * 1.37D));
            if (blink < 0.78D) {
                continue;
            }

            double yaw = i * 2.399963229728653D + Math.sin(i * 1.3D) * 0.18D;
            double pitch = Math.asin(-0.92D + 1.84D * fract(i * 0.61803398875D));
            double radius = OUTER_RADIUS * (0.72D + 0.22D * fract(i * 0.41421356237D));
            double horizontal = Math.cos(pitch) * radius;
            double x = Math.cos(yaw) * horizontal;
            double y = Math.sin(pitch) * radius;
            double z = Math.sin(yaw) * horizontal;
            double flash = (blink - 0.78D) / 0.22D;
            double size = 0.012D + flash * 0.026D;
            float alpha = STATIC_ALPHA * (float) flash;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, STATIC_COLOR, alpha, 5, 5);
            drawStaticCross(size * 2.8D, alpha * 0.62F);
            GlStateManager.popMatrix();
        }
    }

    private static void drawStaticCross(double size, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(STATIC_COLOR);
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
}
