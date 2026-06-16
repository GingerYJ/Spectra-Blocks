package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileFrostCore;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderFrostCore extends RenderCelestialEffectBase<TileFrostCore> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.54D;
    private static final double AURA_RADIUS = 1.20D;
    private static final double FROST_RING_RADIUS = 2.10D;
    private static final int CRYSTAL_FACETS = 8;
    private static final int FROST_RING_COUNT = 5;
    private static final int SNOWFLAKE_COUNT = 14;
    private static final int ICE_MOTE_COUNT = 46;
    private static final int RING_SEGMENTS = 132;
    private static final float CORE_ROTATION_SPEED = 0.34F;
    private static final float RING_ROTATION_SPEED = 0.026F;
    private static final float MOTE_ORBIT_SPEED = 0.010F;

    @Override
    protected void renderCelestialEffect(TileFrostCore te, float ticks) {
        drawCrystalCore(ticks);
        drawFrostRings(ticks);
        drawSnowCrystalLines(ticks);
        drawIceMotes(ticks);
    }

    private void drawCrystalCore(float ticks) {
        float pulse = wave(ticks * 0.050D);

        useAdditiveBlend();
        RenderHelper.drawSphere(AURA_RADIUS + pulse * 0.08D, 0x8BE8FF, 0.15F + pulse * 0.05F, 22, 22);
        useAlphaBlend();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * CORE_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(12.0F, 0.0F, 0.0F, 1.0F);
        RenderEnergyEffectHelper.drawFacetedCrystal(CORE_RADIUS, 1.54D + pulse * 0.08D,
                0xDDFBFF, 0x6FD8FF, 0.58F + pulse * 0.10F, CRYSTAL_FACETS);
        GlStateManager.glLineWidth(1.6F);
        RenderEnergyEffectHelper.drawCrystalEdges(CORE_RADIUS * 1.03D, 1.58D + pulse * 0.08D,
                0xFFFFFF, 0.30F + pulse * 0.14F, CRYSTAL_FACETS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();

        useAdditiveBlend();
        RenderHelper.drawSphere(CORE_RADIUS * 0.54D, 0xFFFFFF, 0.30F + pulse * 0.16F, 16, 16);
        useAlphaBlend();
    }

    private void drawFrostRings(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < FROST_RING_COUNT; i++) {
            double progress = (double) i / (FROST_RING_COUNT - 1);
            double radius = 1.04D + progress * (FROST_RING_RADIUS - 1.04D);
            double tilt = 68.0D + i * 11.0D;
            float pulse = wave(ticks * 0.032D + i * 0.78D);
            int color = i % 2 == 0 ? 0xBFF6FF : 0x62CFFF;
            float alpha = 0.09F + pulse * 0.045F;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) tilt, 1.0F, 0.0F, 0.18F);
            GlStateManager.rotate(ticks * (RING_ROTATION_SPEED + i * 0.007F) * (i % 2 == 0 ? 1.0F : -1.0F),
                    0.0F, 1.0F, 0.0F);
            drawFlatRing(radius - 0.035D, radius + 0.035D + pulse * 0.014D,
                    color, alpha * 0.45F, RING_SEGMENTS);
            GlStateManager.glLineWidth(1.2F + pulse * 0.6F);
            RenderHelper.drawCircle(radius + pulse * 0.035D, color, alpha + 0.045F, RING_SEGMENTS);
            RenderEnergyEffectHelper.drawRuneMarks(radius, 0.15D, 16 + i * 2,
                    0xE9FFFF, alpha * 0.72F, ticks * 0.010D + i);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSnowCrystalLines(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SNOWFLAKE_COUNT; i++) {
            double yaw = TWO_PI * i / SNOWFLAKE_COUNT + ticks * (0.012D + (i % 4) * 0.001D);
            double radius = 1.34D + (i % 5) * 0.20D;
            double y = -0.72D + (i % 7) * 0.24D + Math.sin(ticks * 0.020D + i) * 0.06D;
            float alpha = 0.11F + 0.10F * wave(ticks * 0.045D + i);

            GlStateManager.glLineWidth(1.3F);
            drawSnowflake(yaw, radius, y, 0.28D + (i % 3) * 0.045D,
                    i % 2 == 0 ? 0xFFFFFF : 0xA7EDFF, alpha);
            RenderHelper.resetLineWidth();
        }
        useAlphaBlend();
    }

    private void drawIceMotes(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ICE_MOTE_COUNT; i++) {
            double progress = (i + 0.5D) / ICE_MOTE_COUNT;
            double yaw = i * 2.399963229728653D + ticks * (MOTE_ORBIT_SPEED + (i % 5) * 0.0008D);
            double yNorm = -0.86D + (i % 29) * (1.72D / 28.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = 0.72D + Math.pow(progress, 0.60D) * 2.12D;
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius * 0.54D + Math.sin(ticks * 0.016D + i) * 0.08D;
            double z = Math.sin(yaw) * horizontal * radius;
            double size = 0.012D + (i % 5) * 0.004D;
            int color = i % 4 == 0 ? 0xFFFFFF : 0x9DEFFF;
            float alpha = 0.13F + 0.24F * wave(ticks * 0.040D + i);

            drawSphereAt(x, y, z, size, color, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private static void drawSnowflake(double yaw, double radius, double y, double armLength,
                                      int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        double centerX = Math.cos(yaw) * radius;
        double centerZ = Math.sin(yaw) * radius;
        for (int arm = 0; arm < 6; arm++) {
            double angle = yaw + TWO_PI * arm / 6.0D;
            double endX = centerX + Math.cos(angle) * armLength;
            double endZ = centerZ + Math.sin(angle) * armLength;
            RenderHelper.drawLine(centerX, y, centerZ, endX, y + Math.sin(angle * 2.0D) * 0.035D,
                    endZ, color, alpha);

            double branchAngleA = angle + 0.70D;
            double branchAngleB = angle - 0.70D;
            double branchBaseX = centerX + Math.cos(angle) * armLength * 0.58D;
            double branchBaseZ = centerZ + Math.sin(angle) * armLength * 0.58D;
            RenderHelper.drawLine(branchBaseX, y, branchBaseZ,
                    branchBaseX + Math.cos(branchAngleA) * armLength * 0.25D, y,
                    branchBaseZ + Math.sin(branchAngleA) * armLength * 0.25D,
                    color, alpha * 0.62F);
            RenderHelper.drawLine(branchBaseX, y, branchBaseZ,
                    branchBaseX + Math.cos(branchAngleB) * armLength * 0.25D, y,
                    branchBaseZ + Math.sin(branchAngleB) * armLength * 0.25D,
                    color, alpha * 0.62F);
        }
    }
}
