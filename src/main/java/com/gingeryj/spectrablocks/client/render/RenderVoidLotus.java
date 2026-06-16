package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileVoidLotus;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderVoidLotus extends RenderCelestialEffectBase<TileVoidLotus> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double INNER_PETAL_LENGTH = 1.02D;
    private static final double OUTER_PETAL_LENGTH = 1.58D;
    private static final double CORE_RADIUS = 0.22D;
    private static final double HALO_RADIUS = 1.24D;
    private static final int OUTER_PETAL_COUNT = 10;
    private static final int INNER_PETAL_COUNT = 8;
    private static final int MOTE_COUNT = 34;
    private static final int RING_SEGMENTS = 96;
    private static final float PETAL_OPEN_SPEED = 0.040F;
    private static final float LOTUS_ROTATION_SPEED = 0.22F;
    private static final float MOTE_ORBIT_SPEED = 0.018F;

    @Override
    protected void renderCelestialEffect(TileVoidLotus te, float ticks) {
        drawAura(ticks);
        drawPetals(ticks);
        drawCore(ticks);
        drawVoidMotes(ticks);
    }

    private void drawAura(float ticks) {
        float pulse = wave(ticks * 0.052F);

        useAdditiveBlend();
        RenderHelper.drawSphere(HALO_RADIUS + pulse * 0.12D, 0x2D063F, 0.075F + pulse * 0.040F, 22, 22);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.08D, 0.0D);
        RenderCelestialEffectBase.drawFlatRing(0.22D, 1.34D + pulse * 0.06D, 0x5B1A83, 0.090F + pulse * 0.035F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.5F);
        RenderHelper.drawCircle(1.10D + pulse * 0.05D, 0xB06CFF, 0.18F + pulse * 0.10F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPetals(float ticks) {
        float open = 0.5F + 0.5F * (float) Math.sin(ticks * PETAL_OPEN_SPEED);
        double outerLift = 0.16D + open * 0.18D;
        double innerLift = 0.28D + open * 0.20D;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * LOTUS_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < OUTER_PETAL_COUNT; i++) {
            double angle = TWO_PI * i / OUTER_PETAL_COUNT + Math.sin(ticks * 0.018D + i) * 0.025D;
            drawPetal(angle, OUTER_PETAL_LENGTH, 0.34D, outerLift, 0x1A0629, 0x6F2AA5, 0.46F);
        }

        GlStateManager.rotate(180.0F / INNER_PETAL_COUNT, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < INNER_PETAL_COUNT; i++) {
            double angle = TWO_PI * i / INNER_PETAL_COUNT - Math.sin(ticks * 0.021D + i * 1.7D) * 0.020D;
            drawPetal(angle, INNER_PETAL_LENGTH, 0.28D, innerLift, 0x25083A, 0xA85CFF, 0.58F);
        }
        GlStateManager.popMatrix();
    }

    private void drawCore(float ticks) {
        float pulse = wave(ticks * 0.070F);

        useAdditiveBlend();
        RenderHelper.drawSphere(CORE_RADIUS * 2.35D + pulse * 0.06D, 0x5F2A96, 0.13F + pulse * 0.06F, 18, 18);
        useAlphaBlend();
        RenderHelper.drawSphere(CORE_RADIUS, 0x030007, 0.88F, 18, 18);
        GlStateManager.glLineWidth(1.7F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.drawCircle(CORE_RADIUS * 1.28D, 0xC58CFF, 0.34F + pulse * 0.16F, 48);
        GlStateManager.popMatrix();
        RenderHelper.resetLineWidth();
    }

    private void drawVoidMotes(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = fract(ticks * 0.006D + i * 0.071D);
            double angle = i * 2.399963229728653D + ticks * (MOTE_ORBIT_SPEED + (i % 4) * 0.001D);
            double radius = 0.46D + progress * 1.18D;
            double height = 0.02D + Math.sin(progress * Math.PI) * (0.18D + (i % 3) * 0.055D);
            double size = 0.015D + (1.0D - progress) * 0.018D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 5 == 0 ? 0xE7CBFF : 0x9B5BFF;

            drawSphereAt(Math.cos(angle) * radius, height, Math.sin(angle) * radius,
                    size, color, 0.18F + fade * 0.38F, 6, 6);
        }
        useAlphaBlend();
    }

    private void drawPetal(double angle, double length, double halfWidth, double lift,
                           int rootColor, int tipColor, float alpha) {
        float[] root = RenderHelper.unpackRGB(rootColor);
        float[] tip = RenderHelper.unpackRGB(tipColor);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double tangentX = -sin;
        double tangentZ = cos;
        double rootRadius = 0.16D;
        double midRadius = length * 0.58D;
        double tipRadius = length;
        double rootY = -0.03D;
        double midY = lift * 0.45D;
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
        addVertex(buffer, rootX, rootY, rootZ, root, alpha * 0.72F);
        addVertex(buffer, leftX, midY, leftZ, tip, alpha);
        addVertex(buffer, tipX, tipY, tipZ, tip, alpha * 0.62F);
        addVertex(buffer, rootX, rootY, rootZ, root, alpha * 0.72F);
        addVertex(buffer, tipX, tipY, tipZ, tip, alpha * 0.62F);
        addVertex(buffer, rightX, midY, rightZ, tip, alpha);
        tessellator.draw();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
