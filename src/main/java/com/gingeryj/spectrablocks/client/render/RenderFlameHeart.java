package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileFlameHeart;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderFlameHeart extends RenderCelestialEffectBase<TileFlameHeart> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double HEART_RADIUS = 0.58D;
    private static final double GLOW_RADIUS = 1.36D;
    private static final double INNER_RING_RADIUS = 1.28D;
    private static final double OUTER_RING_RADIUS = 2.95D;
    private static final int FLAME_TONGUE_COUNT = 18;
    private static final int SPARK_COUNT = 58;
    private static final int RING_SEGMENTS = 132;
    private static final float HEART_PULSE_SPEED = 0.070F;
    private static final float TONGUE_ROTATION_SPEED = 0.047F;
    private static final float SPARK_DRIFT_SPEED = 0.008F;

    @Override
    protected void renderCelestialEffect(TileFlameHeart te, float ticks) {
        drawHeart(ticks);
        drawFlameRings(ticks);
        drawFlameTongues(ticks);
        drawSparks(ticks);
    }

    private void drawHeart(float ticks) {
        float pulse = wave(ticks * HEART_PULSE_SPEED);

        useAdditiveBlend();
        RenderHelper.drawSphere(GLOW_RADIUS + pulse * 0.12D, 0xFF3A14, 0.18F + pulse * 0.06F, 24, 24);
        RenderHelper.drawSphere(HEART_RADIUS * 1.42D + pulse * 0.05D, 0xFF9B1F, 0.40F + pulse * 0.12F, 22, 22);
        useAlphaBlend();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(8.0F + pulse * 5.0F, 0.0F, 0.0F, 1.0F);
        drawHeartShape(HEART_RADIUS + pulse * 0.035D, 0xFF2D1D, 0xFFD25C, 0.66F + pulse * 0.12F);
        GlStateManager.popMatrix();

        useAdditiveBlend();
        RenderHelper.drawSphere(HEART_RADIUS * 0.48D, 0xFFF3A6, 0.52F + pulse * 0.18F, 18, 18);
        useAlphaBlend();
    }

    private void drawFlameRings(float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(72.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.42F, 0.0F, 1.0F, 0.0F);
        drawFlatRing(INNER_RING_RADIUS - 0.09D, INNER_RING_RADIUS + 0.08D + pulse * 0.025D,
                0xFF761E, 0.12F + pulse * 0.05F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.7F);
        RenderHelper.drawCircle(INNER_RING_RADIUS + pulse * 0.045D, 0xFFD06A, 0.24F + pulse * 0.08F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(103.0F, 1.0F, 0.0F, 0.26F);
        GlStateManager.rotate(ticks * -0.28F, 0.0F, 1.0F, 0.0F);
        drawFlatRing(OUTER_RING_RADIUS * 0.70D, OUTER_RING_RADIUS * 0.74D + pulse * 0.025D,
                0xD83B16, 0.080F + pulse * 0.030F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.2F);
        RenderHelper.drawCircle(OUTER_RING_RADIUS * 0.76D, 0xFFB347, 0.14F + pulse * 0.06F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFlameTongues(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < FLAME_TONGUE_COUNT; i++) {
            double progress = (double) i / FLAME_TONGUE_COUNT;
            double angle = TWO_PI * progress + ticks * (TONGUE_ROTATION_SPEED + (i % 4) * 0.002D);
            double baseRadius = 0.78D + (i % 3) * 0.12D;
            double length = 1.18D + (i % 5) * 0.18D;
            double width = 0.18D + (i % 4) * 0.030D;
            double sway = Math.sin(ticks * 0.055D + i * 0.73D) * 0.16D;
            double lift = 0.52D + (i % 5) * 0.055D;
            int rootColor = i % 3 == 0 ? 0xFF4A14 : 0xFF781C;
            int tipColor = i % 4 == 0 ? 0xFFE07A : 0xFFB326;
            float alpha = 0.20F + 0.11F * wave(ticks * 0.050D + i);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(14.0F + (i % 6) * 7.0F, 1.0F, 0.0F, 0.12F);
            drawFlameTongue(angle, baseRadius, length, width, lift, sway, rootColor, tipColor, alpha);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSparks(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(i * 0.119D + ticks * SPARK_DRIFT_SPEED);
            double yaw = i * 2.399963229728653D + ticks * (0.024D + (i % 6) * 0.001D);
            double radius = 0.55D + progress * (OUTER_RING_RADIUS - 0.38D);
            double height = -0.64D + progress * 1.82D + Math.sin(ticks * 0.026D + i) * 0.16D;
            double size = 0.014D + (i % 4) * 0.006D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 5 == 0 ? 0xFFF2A2 : (i % 2 == 0 ? 0xFF8B1D : 0xFFCF4E);

            drawSphereAt(Math.cos(yaw) * radius, height, Math.sin(yaw) * radius,
                    size, color, 0.16F + fade * 0.34F, 6, 6);
            if ((i & 11) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(Math.cos(yaw) * radius, height, Math.sin(yaw) * radius);
                RenderEnergyEffectHelper.drawSpark(size * 3.6D, 0xFFE57C, fade * 0.26F);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }

    private static void drawHeartShape(double radius, int lowerColor, int upperColor, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0D, 1.10D, 0.82D);
        drawSphereAt(-radius * 0.34D, radius * 0.14D, 0.0D, radius * 0.62D,
                upperColor, alpha * 0.78F, 18, 18);
        drawSphereAt(radius * 0.34D, radius * 0.14D, 0.0D, radius * 0.62D,
                upperColor, alpha * 0.78F, 18, 18);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -radius * 0.36D, 0.0D);
        GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.80D, 0.80D, 0.62D);
        RenderHelper.drawSphere(radius * 0.88D, lowerColor, alpha, 18, 18);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private static void drawFlameTongue(double angle, double baseRadius, double length, double halfWidth,
                                        double lift, double sway, int rootColor, int tipColor, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] root = RenderHelper.unpackRGB(rootColor);
        float[] tip = RenderHelper.unpackRGB(tipColor);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double tangentX = -sin;
        double tangentZ = cos;
        double rootX = cos * baseRadius;
        double rootZ = sin * baseRadius;
        double midRadius = baseRadius + length * 0.44D;
        double tipRadius = baseRadius + length;
        double midX = cos * midRadius + tangentX * sway;
        double midZ = sin * midRadius + tangentZ * sway;
        double tipX = cos * tipRadius + tangentX * sway * 0.55D;
        double tipZ = sin * tipRadius + tangentZ * sway * 0.55D;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        addVertex(buffer, rootX - tangentX * halfWidth, -0.24D, rootZ - tangentZ * halfWidth, root, alpha * 0.46F);
        addVertex(buffer, midX, lift * 0.34D, midZ, root, alpha);
        addVertex(buffer, tipX, lift, tipZ, tip, alpha * 0.62F);
        addVertex(buffer, rootX + tangentX * halfWidth, -0.24D, rootZ + tangentZ * halfWidth, root, alpha * 0.46F);
        addVertex(buffer, tipX, lift, tipZ, tip, alpha * 0.62F);
        addVertex(buffer, midX, lift * 0.34D, midZ, root, alpha);
        tessellator.draw();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
