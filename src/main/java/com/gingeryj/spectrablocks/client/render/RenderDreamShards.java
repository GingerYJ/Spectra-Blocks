package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileDreamShards;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderDreamShards extends RenderCelestialEffectBase<TileDreamShards> {

    private static final int SHARD_COUNT = 34;
    private static final int GLIMMER_COUNT = 64;
    private static final double FLOAT_RADIUS = 2.48D;
    private static final double RING_RADIUS = 2.16D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double RING_CYCLE_SPEED = 0.0085D;
    private static final int[] SHARD_COLORS = new int[]{
            0x86F7FF, 0xB889FF, 0xFF91D8, 0xFFE99D, 0xA6FFCB
    };

    @Override
    protected void renderCelestialEffect(TileDreamShards te, float ticks) {
        float ringPulse = ringPulse(ticks);

        drawSoftCenter(ticks, ringPulse);
        drawAssemblingRing(ticks, ringPulse);
        drawShards(ticks, ringPulse);
        drawGlimmers(ticks);
    }

    private void drawSoftCenter(float ticks, float ringPulse) {
        float pulse = wave(ticks * 0.045D);

        useAdditiveBlend();
        RenderHelper.drawSphere(0.38D + pulse * 0.06D, 0xFFFFFF, 0.28F + ringPulse * 0.12F, 16, 16);
        RenderHelper.drawSphere(0.90D + pulse * 0.18D, 0xBEEBFF, 0.095F + ringPulse * 0.075F, 20, 20);
        useAlphaBlend();
    }

    private void drawAssemblingRing(float ticks, float ringPulse) {
        if (ringPulse <= 0.01F) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(68.0F, 1.0F, 0.0F, 0.20F);
        GlStateManager.rotate(ticks * 0.19F, 0.0F, 1.0F, 0.0F);
        drawFlatRing(RING_RADIUS - 0.045D, RING_RADIUS + 0.045D,
                0xF6FBFF, 0.22F * ringPulse, 128);
        RenderHelper.drawCircle(RING_RADIUS, 0xB889FF, 0.30F * ringPulse, 128);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawShards(float ticks, float ringPulse) {
        useAlphaBlend();
        int stride = RenderQuality.low() ? 2 : 1;
        for (int i = 0; i < SHARD_COUNT; i += stride) {
            double driftYaw = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 6) * 0.0011D);
            double yNorm = -0.78D + (i % 17) * (1.56D / 16.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double wobble = Math.sin(ticks * 0.041D + i * 0.79D) * 0.24D;
            double driftRadius = FLOAT_RADIUS + wobble + (i % 4) * 0.035D;
            double driftX = Math.cos(driftYaw) * horizontal * driftRadius;
            double driftY = yNorm * driftRadius * 0.62D + Math.sin(ticks * 0.028D + i) * 0.16D;
            double driftZ = Math.sin(driftYaw) * horizontal * driftRadius;

            double ringAngle = i * Math.PI * 2.0D / SHARD_COUNT + ticks * 0.012D;
            double ringX = Math.cos(ringAngle) * RING_RADIUS;
            double ringY = Math.sin(i * 0.70D + ticks * 0.055D) * 0.055D;
            double ringZ = Math.sin(ringAngle) * RING_RADIUS;
            double x = lerp(driftX, ringX, ringPulse);
            double y = lerp(driftY, ringY, ringPulse);
            double z = lerp(driftZ, ringZ, ringPulse);
            int color = SHARD_COLORS[i % SHARD_COLORS.length];
            float alpha = 0.28F + 0.26F * wave(ticks * 0.052D + i * 0.42D) + ringPulse * 0.16F;
            double size = 0.20D + (i % 5) * 0.028D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate((float) (ticks * (1.55D + (i % 4) * 0.24D) + i * 31.0D),
                    0.34F + (i % 3) * 0.18F, 1.0F, 0.25F);
            GlStateManager.rotate((float) (i * 23.0D + ringPulse * 180.0D), 0.0F, 0.0F, 1.0F);
            drawShard(size, 0.38D + (i % 4) * 0.035D, color, alpha);
            GlStateManager.popMatrix();
        }
    }

    private void drawGlimmers(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        for (int i = 0; i < GLIMMER_COUNT; i += stride) {
            double yaw = i * GOLDEN_ANGLE - ticks * (0.004D + (i % 4) * 0.0007D);
            double yNorm = -0.86D + (i % 29) * (1.72D / 28.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = 1.15D + (i % 11) * 0.14D + wave(ticks * 0.035D + i) * 0.32D;
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius * 0.78D;
            double z = Math.sin(yaw) * horizontal * radius;
            int color = SHARD_COLORS[(i + 2) % SHARD_COLORS.length];
            float alpha = 0.12F + 0.18F * wave(ticks * 0.065D + i * 0.71D);

            drawSphereAt(x, y, z, 0.018D + (i % 3) * 0.006D, color, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private static float ringPulse(float ticks) {
        double cycle = fract(ticks * RING_CYCLE_SPEED);
        double center = 0.68D;
        double width = 0.105D;
        double distance = Math.abs(cycle - center);
        if (distance > width) {
            return 0.0F;
        }

        double normalized = 1.0D - distance / width;
        return (float) (normalized * normalized * (3.0D - 2.0D * normalized));
    }

    private static void drawShard(double width, double height, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        addVertex(buffer, 0.0D, height * 0.58D, 0.0D, rgb, alpha);
        addVertex(buffer, -width * 0.42D, -height * 0.34D, width * 0.10D, rgb, alpha * 0.76F);
        addVertex(buffer, width * 0.36D, -height * 0.18D, width * 0.16D, rgb, alpha * 0.88F);

        addVertex(buffer, 0.0D, height * 0.58D, 0.0D, rgb, alpha * 0.92F);
        addVertex(buffer, width * 0.36D, -height * 0.18D, width * 0.16D, rgb, alpha * 0.82F);
        addVertex(buffer, width * 0.12D, -height * 0.46D, -width * 0.38D, rgb, alpha * 0.58F);

        addVertex(buffer, 0.0D, height * 0.58D, 0.0D, rgb, alpha * 0.78F);
        addVertex(buffer, width * 0.12D, -height * 0.46D, -width * 0.38D, rgb, alpha * 0.56F);
        addVertex(buffer, -width * 0.42D, -height * 0.34D, width * 0.10D, rgb, alpha * 0.66F);

        addVertex(buffer, -width * 0.42D, -height * 0.34D, width * 0.10D, rgb, alpha * 0.52F);
        addVertex(buffer, width * 0.12D, -height * 0.46D, -width * 0.38D, rgb, alpha * 0.50F);
        addVertex(buffer, width * 0.36D, -height * 0.18D, width * 0.16D, rgb, alpha * 0.58F);

        tessellator.draw();

        if (!RenderQuality.low()) {
            GlStateManager.glLineWidth(1.2F);
            RenderHelper.drawLine(0.0D, height * 0.58D, 0.0D,
                    -width * 0.42D, -height * 0.34D, width * 0.10D, 0xFFFFFF, alpha * 0.24F);
            RenderHelper.drawLine(0.0D, height * 0.58D, 0.0D,
                    width * 0.36D, -height * 0.18D, width * 0.16D, 0xFFFFFF, alpha * 0.22F);
            RenderHelper.drawLine(0.0D, height * 0.58D, 0.0D,
                    width * 0.12D, -height * 0.46D, -width * 0.38D, 0xFFFFFF, alpha * 0.20F);
            RenderHelper.resetLineWidth();
        }
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  float[] rgb, float alpha) {
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
    }
}
