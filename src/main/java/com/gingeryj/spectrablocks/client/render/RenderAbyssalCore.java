package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileAbyssalCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderAbyssalCore extends RenderCelestialEffectBase<TileAbyssalCore> {

    private static final double CORE_RADIUS = 0.62D;
    private static final double INNER_GLOW_RADIUS = 1.18D;
    private static final double WATER_SHELL_RADIUS = 2.72D;
    private static final double OUTER_PLANKTON_RADIUS = 3.45D;
    private static final int RING_SEGMENTS = 144;
    private static final int PLANKTON_COUNT = 80;
    private static final int BUBBLE_COUNT = 18;
    private static final int WAVE_RING_COUNT = 6;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileAbyssalCore te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawCore(ticks, naturalShader);
        drawWaterShell(ticks, naturalShader, colorShader);
        drawWaveRings(ticks, colorShader);
        drawPlankton(ticks, naturalShader);
        drawRisingBubbles(ticks, naturalShader);
    }

    private void drawCore(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.050D);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, INNER_GLOW_RADIUS + pulse * 0.12D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 0.2F, 0x0A6F78, 0x35EAD8, 0xE6FFF8,
                0.22F + pulse * 0.06F, pulse, 0.92F, ticks * 0.030F, 13.0F, 30);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS + pulse * 0.045D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 0.0F, 0x64FFE4, 0xE6FFF8, 0x2CC8FF,
                0.58F + pulse * 0.12F, pulse, 1.35F, ticks * 0.052F, 29.0F, 28);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, CORE_RADIUS * 0.64D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 0.4F, 0xE6FFF8, 0x64FFE4, 0xFFFFFF,
                0.58F, pulse, 1.50F, ticks * 0.070F, 43.0F, 20);
        useAlphaBlend();
    }

    private void drawWaterShell(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        float pulse = wave(ticks * 0.022D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.030F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.25F);
        GlStateManager.scale(1.0D, 0.78D + pulse * 0.035D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, WATER_SHELL_RADIUS + pulse * 0.07D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 1.0F, 0x053C48, 0x0A6F78, 0x57FFF0,
                0.145F + pulse * 0.030F, pulse, 0.70F, ticks * 0.018F, 71.0F, 28);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, WATER_SHELL_RADIUS * 0.76D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 1.4F, 0x1DCDC1, 0x2AA9FF, 0xE9FFF8,
                0.070F + pulse * 0.025F, pulse, 0.86F, ticks * 0.026F, 89.0F, 26);
        GlStateManager.glLineWidth(1.3F);
        RenderNaturalShaderHelper.drawBasicWireSphere(colorShader, WATER_SHELL_RADIUS * 0.98D,
                0x57FFF0, 0.075F + pulse * 0.030F, 9, 16);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawWaveRings(float ticks, ShaderProgram colorShader) {
        useAdditiveBlend();
        for (int i = 0; i < WAVE_RING_COUNT; i++) {
            double progress = (i + 1.0D) / WAVE_RING_COUNT;
            double y = -1.16D + i * 0.46D + Math.sin(ticks * 0.026D + i) * 0.055D;
            double baseRadius = 0.68D + progress * 2.10D;
            float pulse = wave(ticks * (0.028D + i * 0.003D) + i * 0.81D);
            int color = i % 2 == 0 ? 0x69FFF0 : 0x2AA9FF;
            float alpha = 0.10F + pulse * 0.070F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(7.0F + i * 9.0F, 1.0F, 0.0F, 0.25F);
            GlStateManager.rotate(ticks * (0.035F + i * 0.006F), 0.0F, 1.0F, 0.0F);
            RenderNaturalShaderHelper.drawBasicFlatRing(colorShader,
                    baseRadius - 0.035D, baseRadius + 0.035D + pulse * 0.025D,
                    color, alpha * 0.44F, RING_SEGMENTS);
            GlStateManager.glLineWidth(1.6F);
            RenderNaturalShaderHelper.drawBasicCircle(colorShader, baseRadius + pulse * 0.045D,
                    color, alpha, RING_SEGMENTS);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawPlankton(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < PLANKTON_COUNT; i++) {
            double band = (i + 0.5D) / PLANKTON_COUNT;
            double yaw = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 5) * 0.0007D);
            double yNorm = -0.88D + (i % 41) * (1.76D / 40.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double current = Math.sin(ticks * 0.018D + i * 0.53D) * 0.16D;
            double radius = 1.05D + Math.pow(band, 0.58D) * (OUTER_PLANKTON_RADIUS - 1.05D);
            double x = Math.cos(yaw) * horizontal * (radius + current);
            double y = yNorm * radius * 0.72D + Math.sin(yaw * 1.7D + ticks * 0.010D) * 0.12D;
            double z = Math.sin(yaw) * horizontal * (radius - current * 0.45D);
            double size = 0.018D + (i % 5) * 0.005D;
            int color = i % 7 == 0 ? 0xE9FFF8 : (i % 3 == 0 ? 0x48FFE2 : 0x2CC8FF);
            float alpha = 0.13F + 0.28F * wave(ticks * 0.036D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_ABYSSAL, 2.0F + (i % 5) * 0.1F,
                    color, 0x2AA9FF, 0xE9FFF8, alpha, alpha, 1.08F,
                    ticks * 0.040F, i * 17.0F, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawRisingBubbles(float ticks, ShaderProgram naturalShader) {
        useAlphaBlend();
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            double progress = fract(i * 0.137D + ticks * 0.0036D);
            double yaw = i * GOLDEN_ANGLE + Math.sin(ticks * 0.010D + i) * 0.20D;
            double radius = 0.26D + (i % 6) * 0.19D;
            double bubble = 0.025D + (i % 4) * 0.008D;
            float alpha = (float) Math.sin(Math.PI * progress) * 0.16F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(yaw) * radius, -1.45D + progress * 2.96D, Math.sin(yaw) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, bubble,
                    RenderNaturalShaderHelper.MODE_ABYSSAL, 2.8F, 0xB9FFF5, 0x8BFFF0, 0xFFFFFF,
                    alpha, (float) progress, 0.70F, ticks * 0.022F, 211.0F + i, 6);
            GlStateManager.popMatrix();
        }
    }
}
