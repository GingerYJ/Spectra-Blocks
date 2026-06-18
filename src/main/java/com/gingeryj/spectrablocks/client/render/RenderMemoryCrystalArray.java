package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileMemoryCrystalArray;
import net.minecraft.client.renderer.GlStateManager;

public class RenderMemoryCrystalArray extends RenderArcaneShaderTile<TileMemoryCrystalArray> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int CRYSTAL_COUNT = 10;
    private static final int MEMORY_MOTES = 20;
    private static final int RING_SEGMENTS = 88;

    @Override
    protected void renderShaderLayers(TileMemoryCrystalArray te, float ticks, ShaderProgram shader) {
        drawCoreCrystal(shader, ticks);
        drawCacheRings(shader, ticks);
        drawMemoryCrystals(shader, ticks);
        drawMemoryLinks(shader, ticks);
        drawIndexMotes(shader, ticks);
    }

    private void drawCoreCrystal(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045D);
        double bob = Math.sin(ticks * 0.045D) * 0.045D;

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, bob, 0.0D);
        GlStateManager.rotate(ticks * 0.22F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.36D, 1.26D, 7, 0x182038, 0xA8F6FF,
                0.62F + pulse * 0.12F, 1.25F, 14.0F, 12.0F, pulse);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawCrystalLayer(shader, ticks,
                0.20D, 0.82D, 7, 0xFFFFFF, 0xBFAFFF,
                0.22F + pulse * 0.18F, 1.55F, 20.0F, 18.0F, pulse);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCacheRings(ShaderProgram shader, float ticks) {
        drawCacheRing(shader, ticks, 0.96D, -0.34D, 0.18F, 0xA8F6FF, 0xFFFFFF, 11.0F);
        drawCacheRing(shader, ticks, 1.42D, 0.02D, -0.27F, 0xBFAFFF, 0xE8E3FF, 23.0F);
        drawCacheRing(shader, ticks, 1.84D, 0.38D, 0.36F, 0x80E9FF, 0xFFFFFF, 37.0F);
    }

    private void drawCacheRing(ShaderProgram shader, float ticks, double radius, double y,
                               float speed, int color, int highlight, float seed) {
        float pulse = wave(ticks * 0.030D + seed);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate((float) (62.0D - radius * 9.0D), 1.0F, 0.0F, 0.18F);
        GlStateManager.rotate(ticks * speed, 0.0F, 1.0F, 0.0F);
        useAlphaBlend();
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, radius,
                0.045D, color, highlight, 0.20F + pulse * 0.07F,
                1.08F, 12.0F, seed, pulse, RING_SEGMENTS);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks, radius,
                0.14D, 0.018D, 18, highlight, color,
                0.22F + pulse * 0.12F, 1.24F, 18.0F, seed + 5.0F, pulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawMemoryCrystals(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < CRYSTAL_COUNT; i++) {
            double angle = i * TWO_PI / CRYSTAL_COUNT - ticks * 0.0021D;
            double radius = 1.54D + Math.sin(ticks * 0.018D + i) * 0.055D;
            double y = Math.sin(i * 1.7D + ticks * 0.020D) * 0.34D;
            float pulse = wave(ticks * 0.034D + i * 0.48D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            GlStateManager.rotate((float) Math.toDegrees(-angle) + ticks * -0.12F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.0F + i * 3.0F, 1.0F, 0.0F, 0.0F);
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks,
                    0.18D + (i % 3) * 0.025D, 0.58D + (i % 2) * 0.10D,
                    i % 2 == 0 ? 0x1B2542 : 0x251F46, 0xA8F6FF,
                    0.42F + pulse * 0.14F, 1.08F, 12.0F, 50.0F + i * 4.0F, pulse);
            useAdditiveBlend();
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks,
                    0.10D, 0.36D, 0xFFFFFF, i % 2 == 0 ? 0xA8F6FF : 0xBFAFFF,
                    0.16F + pulse * 0.10F, 1.35F, 18.0F, 80.0F + i * 6.0F, pulse);
            useAlphaBlend();
            GlStateManager.popMatrix();
        }
    }

    private void drawMemoryLinks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CRYSTAL_COUNT; i++) {
            double angle = i * TWO_PI / CRYSTAL_COUNT - ticks * 0.0021D;
            double radius = 1.44D;
            double y = Math.sin(i * 1.7D + ticks * 0.020D) * 0.30D;
            float pulse = wave(ticks * 0.060D + i * 0.62D);

            ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                    Math.cos(angle) * 0.34D, 0.02D, Math.sin(angle) * 0.34D,
                    Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                    i % 2 == 0 ? 0xA8F6FF : 0xBFAFFF, 0xFFFFFF,
                    0.12F + pulse * 0.20F, 1.28F, 16.0F, i * 7.0F, pulse);
        }
        useAlphaBlend();
    }

    private void drawIndexMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MEMORY_MOTES; i++) {
            int lane = i % CRYSTAL_COUNT;
            double laneAngle = lane * TWO_PI / CRYSTAL_COUNT - ticks * 0.0021D;
            double progress = fract(ticks * 0.010D + i * 0.071D);
            double radius = lerp(0.40D, 1.42D, progress);
            double y = Math.sin(progress * Math.PI) * 0.34D
                    + Math.sin(lane * 1.7D + ticks * 0.020D) * 0.18D;
            float fade = (float) Math.sin(progress * Math.PI);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(laneAngle) * radius, y, Math.sin(laneAngle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.018D + (i % 4) * 0.004D, i % 2 == 0 ? 0xFFFFFF : 0xA8F6FF,
                    0xBFAFFF, 0.14F + fade * 0.34F, 1.45F, 10.0F,
                    i * 9.0F, fade, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
