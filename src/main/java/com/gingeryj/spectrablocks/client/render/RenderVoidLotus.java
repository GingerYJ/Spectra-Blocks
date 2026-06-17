package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileVoidLotus;
import net.minecraft.client.renderer.GlStateManager;

public class RenderVoidLotus extends RenderArcaneShaderTile<TileVoidLotus> {

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
    protected void renderShaderLayers(TileVoidLotus te, float ticks, ShaderProgram shader) {
        drawAura(shader, ticks);
        drawPetals(shader, ticks);
        drawCore(shader, ticks);
        drawVoidMotes(shader, ticks);
    }

    private void drawAura(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.052F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                HALO_RADIUS + pulse * 0.12D, 0x2D063F, 0xB06CFF,
                0.10F + pulse * 0.05F, 1.15F, 12.0F, 5.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 22, 22);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.08D, 0.0D);
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.22D, 1.34D + pulse * 0.06D, 0x5B1A83, 0xC58CFF,
                0.12F + pulse * 0.04F, 1.08F, 16.0F, 11.0F, pulse, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.5F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                1.10D + pulse * 0.05D, 0.0D, 0xB06CFF, 0xE7CBFF,
                0.19F + pulse * 0.10F, 1.25F, 18.0F, 13.0F, pulse, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPetals(ShaderProgram shader, float ticks) {
        float open = wave(ticks * PETAL_OPEN_SPEED);
        double outerLift = 0.16D + open * 0.18D;
        double innerLift = 0.28D + open * 0.20D;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * LOTUS_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < OUTER_PETAL_COUNT; i++) {
            double angle = TWO_PI * i / OUTER_PETAL_COUNT + Math.sin(ticks * 0.018D + i) * 0.025D;
            ArcaneShaderEffectRenderer.drawPetalLayer(shader, ticks, angle, OUTER_PETAL_LENGTH,
                    0.34D, outerLift, 0x1A0629, 0x6F2AA5,
                    0.49F, 1.15F, 18.0F, i * 9.0F, open);
        }

        GlStateManager.rotate(180.0F / INNER_PETAL_COUNT, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < INNER_PETAL_COUNT; i++) {
            double angle = TWO_PI * i / INNER_PETAL_COUNT - Math.sin(ticks * 0.021D + i * 1.7D) * 0.020D;
            ArcaneShaderEffectRenderer.drawPetalLayer(shader, ticks, angle, INNER_PETAL_LENGTH,
                    0.28D, innerLift, 0x25083A, 0xA85CFF,
                    0.60F, 1.25F, 20.0F, 37.0F + i * 11.0F, open);
        }
        GlStateManager.popMatrix();
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.070F);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS * 2.35D + pulse * 0.06D, 0x5F2A96, 0xC58CFF,
                0.16F + pulse * 0.07F, 1.25F, 16.0F, 71.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 18, 18);
        useAlphaBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                CORE_RADIUS, 0x030007, 0xC58CFF,
                0.90F, 1.0F, 18.0F, 73.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(1.7F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                CORE_RADIUS * 1.28D, 0.0D, 0xC58CFF, 0xE7CBFF,
                0.34F + pulse * 0.16F, 1.35F, 20.0F, 79.0F, pulse, 48);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawVoidMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = fract(ticks * 0.006D + i * 0.071D);
            double angle = i * GOLDEN_ANGLE + ticks * (MOTE_ORBIT_SPEED + (i % 4) * 0.001D);
            double radius = 0.46D + progress * 1.18D;
            double height = 0.02D + Math.sin(progress * Math.PI) * (0.18D + (i % 3) * 0.055D);
            double size = 0.015D + (1.0D - progress) * 0.018D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 5 == 0 ? 0xE7CBFF : 0x9B5BFF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    color, 0xFFFFFF, 0.18F + fade * 0.38F, 1.45F, 12.0F,
                    i * 13.0F, fade, ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
