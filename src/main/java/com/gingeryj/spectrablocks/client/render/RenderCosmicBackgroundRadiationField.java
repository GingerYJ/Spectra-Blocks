package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileCosmicBackgroundRadiationField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderCosmicBackgroundRadiationField
        extends RenderCelestialEffectBase<TileCosmicBackgroundRadiationField> {

    private static final double FIELD_RADIUS = 5.90D;
    private static final int SHELL_SEGMENTS = 34;
    private static final int NOISE_POINT_COUNT = 158;
    private static final int CONTOUR_COUNT = 13;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float DRIFT_SPEED = 0.006F;
    private static final float EFFECT_BACKGROUND_RADIATION = 3.0F;

    @Override
    protected void renderCelestialEffect(TileCosmicBackgroundRadiationField te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("celestial_effect");
        if (shader == null) {
            return;
        }

        try {
            GlStateManager.pushMatrix();
            try {
                GlStateManager.rotate(ticks * DRIFT_SPEED, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(11.0F + ticks * 0.002F, 1.0F, 0.0F, 0.25F);
                drawFieldShell(shader, ticks);
                drawContours(shader, ticks);
                drawNoise(shader, ticks);
            } finally {
                GlStateManager.popMatrix();
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("cosmic background radiation field render failed: " + ex.getMessage());
        } finally {
            shader.end();
            useAlphaBlend();
            GL11.glLineWidth(1.0F);
        }
    }

    private void drawFieldShell(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.008D);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);

        useAlphaBlend();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        RenderMiniatureGalaxy.drawShaderSphere(shader, FIELD_RADIUS, ticks,
                EFFECT_BACKGROUND_RADIATION, 0.0F, 0xC9DEFF, 0xFFD6AC,
                0.050F + pulse * 0.018F, 0.86F, 0.0F, SHELL_SEGMENTS);
        restoreCullState(cullWasEnabled, previousCullFace);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, FIELD_RADIUS * 1.004D, ticks,
                EFFECT_BACKGROUND_RADIATION, 1.0F, 0xEAF4FF, 0xBFD8FF,
                0.025F + pulse * 0.014F, 0.76F, 0.31F, 18);
        useAlphaBlend();
    }

    private void drawContours(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CONTOUR_COUNT; i++) {
            double band = -0.82D + i * (1.64D / (CONTOUR_COUNT - 1));
            double y = band * FIELD_RADIUS;
            int color = i % 3 == 0 ? 0xFFD6AC : (i % 3 == 1 ? 0xD9FFE9 : 0xBFD8FF);
            float alpha = 0.045F + 0.026F * wave(ticks * 0.010D + i * 0.61D);

            GlStateManager.glLineWidth(i % 4 == 0 ? 2.0F : 1.0F);
            RenderMiniatureGalaxy.drawShaderLatitudeCircle(shader, FIELD_RADIUS * (0.990D + (i % 2) * 0.004D), y,
                    ticks, EFFECT_BACKGROUND_RADIATION, 3.0F, color, 0xFFFFFF, alpha,
                    0.92F, i * 0.07F, 160);
        }

        for (int i = 0; i < 8; i++) {
            double startYaw = i * Math.PI * 0.31D + ticks * 0.0015D;
            double basePitch = -0.58D + i * 0.165D;
            int color = i % 2 == 0 ? 0xF3E9FF : 0xBEE9FF;

            GlStateManager.glLineWidth(1.2F);
            RenderMiniatureGalaxy.drawShaderArc(shader, FIELD_RADIUS * 1.006D, startYaw, Math.PI * 1.18D,
                    basePitch, 0.050D, ticks * 0.006D + i, ticks, EFFECT_BACKGROUND_RADIATION,
                    4.0F, color, 0xFFD6AC, 0.060F, 1.06F, 0.35F + i * 0.09F, 64);
        }
        GL11.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawNoise(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < NOISE_POINT_COUNT; i++) {
            double yaw = i * GOLDEN_ANGLE + ticks * (0.0009D + (i % 7) * 0.00008D);
            double yNorm = -0.98D + (i % 53) * (1.96D / 52.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double mottling = Math.sin(i * 12.9898D + ticks * 0.006D) * 0.5D
                    + Math.cos(i * 4.141D - ticks * 0.004D) * 0.5D;
            double radius = FIELD_RADIUS * (0.955D + mottling * 0.010D);
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius;
            double z = Math.sin(yaw) * horizontal * radius;
            int color;
            if (mottling > 0.38D) {
                color = 0xFFD7B5;
            } else if (mottling < -0.32D) {
                color = 0xB8D5FF;
            } else {
                color = 0xE6FFF0;
            }

            float alpha = 0.055F + 0.035F * wave(ticks * 0.011D + i * 0.43D);
            double size = 0.010D + (i % 4) * 0.003D;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderMiniatureGalaxy.drawShaderSphere(shader, size, ticks, EFFECT_BACKGROUND_RADIATION, 6.0F,
                    color, 0xFFFFFF, alpha, 1.18F, i * 0.053F, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void restoreCullState(boolean cullWasEnabled, int previousCullFace) {
        GlStateManager.cullFace(previousCullFace == GL11.GL_FRONT
                ? GlStateManager.CullFace.FRONT
                : GlStateManager.CullFace.BACK);
        if (cullWasEnabled) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }
    }
}
