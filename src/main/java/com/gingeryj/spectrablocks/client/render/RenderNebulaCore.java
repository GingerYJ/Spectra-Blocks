package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileNebulaCore;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderNebulaCore extends RenderCelestialEffectBase<TileNebulaCore> {

    private static final double OUTER_RADIUS = 4.60D;
    private static final int CLOUD_LAYER_COUNT = 5;
    private static final int DUST_COUNT = 118;
    private static final int STREAM_COUNT = 9;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int SPHERE_SEGMENTS = 28;
    private static final float EFFECT_NEBULA = 1.0F;

    private static final int[] CLOUD_COLORS = new int[]{
            0x2FD6CF, 0x8A77FF, 0xFF9FC7, 0xFFD67E, 0xCFFFE9
    };

    @Override
    protected void renderCelestialEffect(TileNebulaCore te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("celestial_effect");
        if (shader == null) {
            return;
        }

        try {
            drawCore(shader, ticks);
            drawCloudLayers(shader, ticks);
            drawDust(shader, ticks);
            drawStreamLines(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("nebula core render failed: " + ex.getMessage());
        } finally {
            shader.end();
            useAlphaBlend();
            GL11.glLineWidth(1.0F);
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.040D);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, 0.74D + pulse * 0.060D, ticks,
                EFFECT_NEBULA, 0.0F, 0xFFFFFF, 0x7EF2EA, 0.42F, 1.30F, 0.0F, 24);
        RenderMiniatureGalaxy.drawShaderSphere(shader, 1.15D + pulse * 0.110D, ticks,
                EFFECT_NEBULA, 1.0F, 0x7EF2EA, 0xFFFFFF, 0.24F, 1.18F, 0.22F, 24);
        RenderMiniatureGalaxy.drawShaderSphere(shader, 1.65D + pulse * 0.160D, ticks,
                EFFECT_NEBULA, 2.0F, 0xFFB6D6, 0xFFD67E, 0.125F, 0.96F, 0.45F, 24);
        useAlphaBlend();
    }

    private void drawCloudLayers(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < CLOUD_LAYER_COUNT; i++) {
            float direction = i % 2 == 0 ? 1.0F : -1.0F;
            float pulse = wave(ticks * (0.018D + i * 0.004D) + i * 0.7D);
            double radius = 2.15D + i * 0.54D + pulse * 0.12D;

            GlStateManager.pushMatrix();
            try {
                GlStateManager.rotate(direction * ticks * (0.030F + i * 0.012F), 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(18.0F + i * 11.0F, 1.0F, 0.0F, 0.35F);
                GlStateManager.scale(1.0D + i * 0.030D, 0.72D + i * 0.055D, 1.0D - i * 0.020D);
                RenderMiniatureGalaxy.drawShaderSphere(shader, radius, ticks,
                        EFFECT_NEBULA, 3.0F + i, CLOUD_COLORS[i],
                        CLOUD_COLORS[(i + 2) % CLOUD_COLORS.length], 0.080F + pulse * 0.040F,
                        0.92F + i * 0.055F, i * 0.29F, SPHERE_SEGMENTS);
                RenderMiniatureGalaxy.drawShaderSphere(shader, radius * 1.012D, ticks,
                        EFFECT_NEBULA, 4.0F + i, CLOUD_COLORS[(i + 2) % CLOUD_COLORS.length],
                        0xFFFFFF, 0.030F + pulse * 0.022F, 0.82F, 0.48F + i * 0.31F, 14);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawDust(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < DUST_COUNT; i++) {
            double band = (i + 0.5D) / DUST_COUNT;
            double yaw = i * GOLDEN_ANGLE + ticks * (0.0016D + (i % 6) * 0.00025D);
            double yNorm = -0.90D + (i % 37) * (1.80D / 36.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = 1.05D + Math.pow(band, 0.48D) * 3.55D;
            double flutter = Math.sin(ticks * 0.024D + i * 1.37D) * 0.16D;
            double x = Math.cos(yaw) * horizontal * (radius + flutter);
            double y = yNorm * (radius * 0.70D) + Math.cos(yaw * 2.0D + ticks * 0.016D) * 0.10D;
            double z = Math.sin(yaw) * horizontal * (radius - flutter * 0.5D);
            int color = i % 4 == 0 ? 0xFFD79E : (i % 3 == 0 ? 0xA5FFF2 : 0xFFE4F2);
            float alpha = 0.16F + 0.14F * wave(ticks * 0.031D + i);
            double size = 0.020D + (i % 5) * 0.004D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderMiniatureGalaxy.drawShaderSphere(shader, size, ticks, EFFECT_NEBULA, 8.0F,
                    color, 0xFFFFFF, alpha, 1.22F, i * 0.061F, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawStreamLines(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_COUNT; i++) {
            double radius = 2.55D + (i % 3) * 0.54D;
            double startYaw = i * Math.PI * 0.43D + ticks * (0.004D + (i % 2) * 0.002D);
            double sweep = Math.PI * (0.80D + (i % 4) * 0.17D);
            double basePitch = -0.48D + i * 0.12D;
            double pitchWave = 0.14D + (i % 3) * 0.045D;
            int color = i % 3 == 0 ? 0x75FFF0 : (i % 3 == 1 ? 0xFFB4D6 : 0xFFD486);
            float alpha = 0.17F + 0.06F * wave(ticks * 0.025D + i);

            GlStateManager.glLineWidth(3.0F);
            RenderMiniatureGalaxy.drawShaderArc(shader, radius, startYaw, sweep, basePitch, pitchWave,
                    ticks * 0.014D + i, ticks, EFFECT_NEBULA, 9.0F, color, 0xFFFFFF,
                    alpha * 0.40F, 0.92F, i * 0.13F, 54);
            GlStateManager.glLineWidth(1.3F);
            RenderMiniatureGalaxy.drawShaderArc(shader, radius, startYaw, sweep, basePitch, pitchWave,
                    ticks * 0.014D + i, ticks, EFFECT_NEBULA, 10.0F, 0xFFFFFF, color,
                    alpha, 1.18F, 0.38F + i * 0.11F, 54);
        }
        GL11.glLineWidth(1.0F);
        useAlphaBlend();
    }
}
