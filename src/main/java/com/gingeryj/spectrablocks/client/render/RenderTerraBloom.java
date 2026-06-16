package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileTerraBloom;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTerraBloom extends RenderCelestialEffectBase<TileTerraBloom> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.42D;
    private static final double AURA_RADIUS = 1.28D;
    private static final double INNER_LEAF_LENGTH = 1.18D;
    private static final double OUTER_LEAF_LENGTH = 1.92D;
    private static final double VINE_RADIUS = 2.36D;
    private static final int INNER_LEAF_COUNT = 8;
    private static final int OUTER_LEAF_COUNT = 12;
    private static final int VINE_COUNT = 7;
    private static final int POLLEN_COUNT = 54;
    private static final int RING_SEGMENTS = 112;
    private static final float BLOOM_SPEED = 0.038F;
    private static final float LEAF_ROTATION_SPEED = 0.19F;
    private static final float POLLEN_SPEED = 0.007F;

    @Override
    protected void renderCelestialEffect(TileTerraBloom te, float ticks) {
        drawLifeCore(ticks);
        drawLeafBloom(ticks);
        drawVines(ticks);
        drawPollen(ticks);
    }

    private void drawLifeCore(float ticks) {
        float pulse = wave(ticks * 0.058D);

        useAdditiveBlend();
        RenderHelper.drawSphere(AURA_RADIUS + pulse * 0.10D, 0x80E343, 0.13F + pulse * 0.05F, 22, 22);
        RenderHelper.drawSphere(CORE_RADIUS * 1.86D + pulse * 0.055D, 0xF2D05B, 0.20F + pulse * 0.07F, 20, 20);
        useAlphaBlend();
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.028D, 0x58B83A, 0.56F + pulse * 0.14F, 18, 18);
        RenderHelper.drawSphere(CORE_RADIUS * 0.56D, 0xF9E27A, 0.48F + pulse * 0.16F, 16, 16);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.25F, 0.0F, 1.0F, 0.0F);
        drawFlatRing(0.52D, 0.82D + pulse * 0.035D, 0xDAB84B, 0.10F + pulse * 0.04F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.4F);
        RenderHelper.drawCircle(0.88D + pulse * 0.040D, 0xFFE78C, 0.18F + pulse * 0.08F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawLeafBloom(float ticks) {
        float bloom = wave(ticks * BLOOM_SPEED);
        double outerLift = 0.18D + bloom * 0.26D;
        double innerLift = 0.30D + bloom * 0.20D;

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * LEAF_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < OUTER_LEAF_COUNT; i++) {
            double angle = TWO_PI * i / OUTER_LEAF_COUNT + Math.sin(ticks * 0.018D + i) * 0.035D;
            drawLeaf(angle, OUTER_LEAF_LENGTH, 0.30D, outerLift,
                    0x1B6E32, i % 3 == 0 ? 0xC9B64A : 0x58CA52, 0.45F + bloom * 0.06F);
        }

        GlStateManager.rotate(180.0F / INNER_LEAF_COUNT, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < INNER_LEAF_COUNT; i++) {
            double angle = TWO_PI * i / INNER_LEAF_COUNT - Math.sin(ticks * 0.022D + i) * 0.026D;
            drawLeaf(angle, INNER_LEAF_LENGTH, 0.24D, innerLift,
                    0x2A8C3E, 0xEAD36B, 0.56F + bloom * 0.07F);
        }
        GlStateManager.popMatrix();
    }

    private void drawVines(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < VINE_COUNT; i++) {
            double start = TWO_PI * i / VINE_COUNT + ticks * (0.018D + (i % 3) * 0.002D);
            double sweep = Math.PI * (1.10D + (i % 4) * 0.12D);
            double radius = 0.92D + (i % 3) * 0.16D;
            double endRadius = VINE_RADIUS - (i % 2) * 0.22D;
            float pulse = wave(ticks * 0.034D + i);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(15.0F + i * 8.0F, 1.0F, 0.0F, 0.18F);
            drawSpiralRibbon(radius, endRadius, start, sweep,
                    0.055D + pulse * 0.012D, i % 2 == 0 ? 0x75D752 : 0xD7BC4E,
                    0.09F + pulse * 0.045F, 56);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.12D, 0.0D);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * -0.18F, 0.0F, 1.0F, 0.0F);
        GlStateManager.glLineWidth(1.5F);
        RenderHelper.drawCircle(1.68D, 0x8EDE5F, 0.14F, RING_SEGMENTS);
        RenderHelper.drawCircle(2.22D, 0xD6BE54, 0.10F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPollen(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < POLLEN_COUNT; i++) {
            double progress = fract(i * 0.097D + ticks * POLLEN_SPEED);
            double yaw = i * 2.399963229728653D + ticks * (0.019D + (i % 5) * 0.001D);
            double radius = 0.44D + progress * 2.48D;
            double y = -0.46D + Math.sin(progress * Math.PI) * 1.18D
                    + Math.sin(ticks * 0.018D + i) * 0.08D;
            double size = 0.014D + (i % 5) * 0.004D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 4 == 0 ? 0xFFE37B : (i % 3 == 0 ? 0xA8F56B : 0x5DDC58);

            drawSphereAt(Math.cos(yaw) * radius, y, Math.sin(yaw) * radius,
                    size, color, 0.16F + fade * 0.30F, 6, 6);
            if ((i & 15) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(Math.cos(yaw) * radius, y, Math.sin(yaw) * radius);
                RenderEnergyEffectHelper.drawSpark(size * 3.1D, 0xFFF0A0, fade * 0.20F);
                GlStateManager.popMatrix();
            }
        }
        useAlphaBlend();
    }

    private static void drawLeaf(double angle, double length, double halfWidth, double lift,
                                 int rootColor, int tipColor, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] root = RenderHelper.unpackRGB(rootColor);
        float[] tip = RenderHelper.unpackRGB(tipColor);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double tangentX = -sin;
        double tangentZ = cos;
        double rootRadius = 0.18D;
        double midRadius = length * 0.55D;
        double tipRadius = length;
        double rootY = -0.06D;
        double midY = lift * 0.42D;
        double tipY = lift;

        double rootX = cos * rootRadius;
        double rootZ = sin * rootRadius;
        double leftX = cos * midRadius + tangentX * halfWidth;
        double leftZ = sin * midRadius + tangentZ * halfWidth;
        double rightX = cos * midRadius - tangentX * halfWidth;
        double rightZ = sin * midRadius - tangentZ * halfWidth;
        double tipX = cos * tipRadius;
        double tipZ = sin * tipRadius;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        addVertex(buffer, rootX, rootY, rootZ, root, alpha * 0.70F);
        addVertex(buffer, leftX, midY, leftZ, tip, alpha);
        addVertex(buffer, tipX, tipY, tipZ, tip, alpha * 0.54F);
        addVertex(buffer, rootX, rootY, rootZ, root, alpha * 0.70F);
        addVertex(buffer, tipX, tipY, tipZ, tip, alpha * 0.54F);
        addVertex(buffer, rightX, midY, rightZ, tip, alpha);
        tessellator.draw();

        GlStateManager.glLineWidth(1.0F);
        RenderHelper.drawLine(rootX, rootY + 0.012D, rootZ, tipX, tipY + 0.012D, tipZ,
                0xF4DD7A, alpha * 0.38F);
        RenderHelper.resetLineWidth();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
