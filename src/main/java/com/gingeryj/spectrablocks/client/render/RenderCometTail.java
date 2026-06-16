package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileCometTail;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderCometTail extends RenderCelestialEffectBase<TileCometTail> {

    private static final double ORBIT_RADIUS = 1.34D;
    private static final double ORBIT_Z_SCALE = 0.72D;
    private static final double ORBIT_Y_SCALE = 0.34D;
    private static final double TAIL_SWEEP = 2.85D;
    private static final double TAIL_WIDTH = 0.24D;
    private static final int TAIL_SEGMENTS = 48;
    private static final int TAIL_STREAMS = 5;
    private static final int DUST_COUNT = 54;
    private static final float ORBIT_SPEED = 0.018F;

    @Override
    protected void renderCelestialEffect(TileCometTail te, float ticks) {
        double angle = ticks * ORBIT_SPEED;

        drawOrbitGuide(ticks);
        drawTailRibbons(ticks, angle);
        drawTailDust(ticks, angle);
        drawCometCore(ticks, angle);
    }

    private void drawOrbitGuide(float ticks) {
        float pulse = wave(ticks * 0.032D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0D, 1.0D, ORBIT_Z_SCALE);
        GlStateManager.glLineWidth(1.2F);
        RenderHelper.drawCircle(ORBIT_RADIUS, 0x4CB2FF, 0.055F + pulse * 0.025F, 112);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawTailRibbons(float ticks, double angle) {
        useAdditiveBlend();
        for (int stream = 0; stream < TAIL_STREAMS; stream++) {
            int color = stream == 0 ? 0xFFFFFF : (stream % 2 == 0 ? 0x89E8FF : 0xFFE1A3);
            float alpha = stream == 0 ? 0.30F : 0.18F;
            drawTailRibbon(ticks, angle, stream, color, alpha);
        }
        useAlphaBlend();
    }

    private void drawTailRibbon(float ticks, double angle, int stream, int color, float alpha) {
        float[] rgb = RenderHelper.unpackRGB(color);
        double streamOffset = (stream - (TAIL_STREAMS - 1) * 0.5D) * 0.040D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= TAIL_SEGMENTS; i++) {
            double progress = (double) i / TAIL_SEGMENTS;
            double sampleAngle = angle - progress * TAIL_SWEEP - streamOffset * 0.45D;
            double radius = ORBIT_RADIUS - progress * 0.34D + Math.sin(ticks * 0.025D + stream) * 0.025D;
            double x = Math.cos(sampleAngle) * radius;
            double y = Math.sin(sampleAngle * 0.72D) * ORBIT_Y_SCALE
                    + Math.sin(progress * Math.PI * 4.0D + stream + ticks * 0.036D) * 0.050D * progress;
            double z = Math.sin(sampleAngle) * radius * ORBIT_Z_SCALE;
            double halfWidth = TAIL_WIDTH * (1.0D - progress * 0.82D) * (1.0D + stream * 0.035D);
            double normalX = -Math.sin(sampleAngle);
            double normalZ = Math.cos(sampleAngle) * ORBIT_Z_SCALE;
            double normalLength = Math.sqrt(normalX * normalX + normalZ * normalZ);
            if (normalLength > 0.0001D) {
                normalX /= normalLength;
                normalZ /= normalLength;
            }
            float fade = (float) ((1.0D - progress) * (1.0D - progress));
            float vertexAlpha = alpha * fade * (0.72F + 0.28F * wave(ticks * 0.050D + i + stream));

            buffer.pos(x + normalX * halfWidth, y + streamOffset * (1.0D - progress), z + normalZ * halfWidth)
                    .color(rgb[0], rgb[1], rgb[2], vertexAlpha)
                    .endVertex();
            buffer.pos(x - normalX * halfWidth, y - streamOffset * (1.0D - progress), z - normalZ * halfWidth)
                    .color(rgb[0], rgb[1], rgb[2], vertexAlpha * 0.70F)
                    .endVertex();
        }

        tessellator.draw();
    }

    private void drawTailDust(float ticks, double angle) {
        useAdditiveBlend();
        for (int i = 0; i < DUST_COUNT; i++) {
            double progress = fract(i * 0.037D + ticks * (0.0042D + (i % 5) * 0.0006D));
            double sampleAngle = angle - progress * TAIL_SWEEP - (i % 4) * 0.045D;
            double radius = ORBIT_RADIUS - progress * 0.42D;
            double spread = Math.sin(progress * Math.PI) * (0.14D + (i % 6) * 0.010D);
            double side = Math.sin(i * 12.9898D) * spread;
            double x = Math.cos(sampleAngle) * radius - Math.sin(sampleAngle) * side;
            double y = Math.sin(sampleAngle * 0.72D) * ORBIT_Y_SCALE
                    + Math.sin(ticks * 0.031D + i) * 0.11D * progress;
            double z = Math.sin(sampleAngle) * radius * ORBIT_Z_SCALE + Math.cos(sampleAngle) * side;
            float alpha = (float) ((1.0D - progress) * (0.12D + 0.28D * wave(ticks * 0.047D + i)));
            int color = i % 6 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x75E7FF : 0xFFD98F);

            drawSphereAt(x, y, z, 0.018D + (i % 4) * 0.004D, color, alpha, 6, 6);
        }
        useAlphaBlend();
    }

    private void drawCometCore(float ticks, double angle) {
        double x = Math.cos(angle) * ORBIT_RADIUS;
        double y = Math.sin(angle * 0.72D) * ORBIT_Y_SCALE;
        double z = Math.sin(angle) * ORBIT_RADIUS * ORBIT_Z_SCALE;
        float pulse = wave(ticks * 0.088D);

        useAdditiveBlend();
        drawSphereAt(x, y, z, 0.42D + pulse * 0.050D, 0x73DFFF, 0.16F + pulse * 0.050F, 18, 18);
        drawSphereAt(x, y, z, 0.21D + pulse * 0.024D, 0xFFF2C2, 0.55F + pulse * 0.16F, 16, 16);
        drawSphereAt(x, y, z, 0.10D, 0xFFFFFF, 0.72F, 12, 12);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glLineWidth(2.0F);
        RenderEnergyEffectHelper.drawSpark(0.34D + pulse * 0.06D, 0xFFFFFF, 0.22F + pulse * 0.12F);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }
}
