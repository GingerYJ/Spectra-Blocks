package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileEntropyCloud;
import net.minecraft.client.renderer.GlStateManager;

public class RenderEntropyCloud extends RenderCelestialEffectBase<TileEntropyCloud> {

    private static final double OUTER_RADIUS = 3.35D;
    private static final int CLOUD_LAYER_COUNT = 6;
    private static final int CLOUD_NODE_COUNT = 96;
    private static final int CRACK_COUNT = 8;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int SPHERE_SEGMENTS = 22;

    @Override
    protected void renderCelestialEffect(TileEntropyCloud te, float ticks) {
        drawCloudBody(ticks);
        drawNoiseNodes(ticks);
        drawBlackCracks(ticks);
    }

    private void drawCloudBody(float ticks) {
        useAlphaBlend();
        int layers = RenderQuality.low() ? Math.max(3, CLOUD_LAYER_COUNT - 2) : CLOUD_LAYER_COUNT;
        for (int i = 0; i < layers; i++) {
            float pulse = wave(ticks * (0.021D + i * 0.004D) + i * 0.91D);
            double radius = 1.44D + i * 0.33D + pulse * 0.16D;
            int color = i % 3 == 0 ? 0xF4F5F0 : (i % 3 == 1 ? 0xB9BCBB : 0x777B80);
            float alpha = 0.060F + pulse * 0.038F;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(ticks * (0.042F + i * 0.013F) * (i % 2 == 0 ? 1.0F : -1.0F),
                    0.15F + i * 0.10F, 1.0F, 0.35F);
            GlStateManager.rotate(17.0F + i * 29.0F, 1.0F, 0.0F, 0.5F);
            GlStateManager.scale(1.0D + i * 0.055D, 0.62D + i * 0.047D, 0.92D - i * 0.020D);
            RenderHelper.drawSphere(radius, color, alpha, SPHERE_SEGMENTS, SPHERE_SEGMENTS);
            GlStateManager.popMatrix();
        }

        useAdditiveBlend();
        RenderHelper.drawSphere(0.60D + 0.05D * wave(ticks * 0.057D), 0xFFFFFF, 0.10F, 18, 18);
        useAlphaBlend();
    }

    private void drawNoiseNodes(float ticks) {
        useAlphaBlend();
        int stride = RenderQuality.detailStride();
        for (int i = 0; i < CLOUD_NODE_COUNT; i += stride) {
            double band = (i + 0.5D) / CLOUD_NODE_COUNT;
            double yaw = i * GOLDEN_ANGLE + ticks * (0.0035D + (i % 5) * 0.0007D);
            double yNorm = -0.94D + (i % 41) * (1.88D / 40.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double rebuild = Math.sin(ticks * 0.038D + i * 0.73D);
            double localRadius = 0.70D + Math.pow(band, 0.62D) * OUTER_RADIUS
                    + rebuild * (0.13D + (i % 4) * 0.035D);
            double shear = Math.sin(ticks * 0.017D + i * 1.37D) * 0.24D;
            double x = Math.cos(yaw) * horizontal * localRadius + shear * yNorm;
            double y = yNorm * localRadius * (0.56D + (i % 3) * 0.045D);
            double z = Math.sin(yaw) * horizontal * (localRadius - shear * 0.35D);
            double size = 0.040D + (i % 6) * 0.006D + Math.max(0.0D, rebuild) * 0.030D;
            float alpha = 0.055F + 0.075F * wave(ticks * 0.049D + i * 0.51D);
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 4 == 0 ? 0xD1D4D2 : 0x8F9494);

            drawSphereAt(x, y, z, size, color, alpha, 6, 6);
        }
    }

    private void drawBlackCracks(float ticks) {
        useAlphaBlend();
        int crackCount = RenderQuality.detailCount(CRACK_COUNT, 3);
        for (int i = 0; i < crackCount; i++) {
            double cycle = fract(ticks * (0.010D + i * 0.0012D) + i * 0.173D);
            float flash = (float) Math.max(0.0D, Math.sin(Math.PI * cycle));
            flash = flash * flash * flash;
            if (flash <= 0.015F) {
                continue;
            }

            GlStateManager.glLineWidth(3.8F);
            RenderEnergyEffectHelper.drawJaggedArc(1.12D + (i % 4) * 0.31D,
                    i * 0.83D + ticks * (0.004D + (i % 3) * 0.001D),
                    0.42D + (i % 3) * 0.25D,
                    -0.62D + (i % 5) * 0.31D,
                    0.22D + (i % 2) * 0.16D,
                    0.105D, 7, 0x020204, flash * 0.42F, ticks, 311 + i * 47);
            GlStateManager.glLineWidth(1.3F);
            RenderEnergyEffectHelper.drawJaggedArc(1.12D + (i % 4) * 0.31D,
                    i * 0.83D + ticks * (0.004D + (i % 3) * 0.001D),
                    0.42D + (i % 3) * 0.25D,
                    -0.62D + (i % 5) * 0.31D,
                    0.22D + (i % 2) * 0.16D,
                    0.075D, 7, 0x121217, flash * 0.72F, ticks, 617 + i * 47);
        }
        RenderHelper.resetLineWidth();
    }
}
