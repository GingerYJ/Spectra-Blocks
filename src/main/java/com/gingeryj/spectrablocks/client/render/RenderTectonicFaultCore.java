package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileTectonicFaultCore;
import net.minecraft.client.renderer.GlStateManager;

public class RenderTectonicFaultCore extends RenderCelestialEffectBase<TileTectonicFaultCore> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int SLAB_COUNT = 9;
    private static final int SPARK_COUNT = 24;

    @Override
    protected void renderCelestialEffect(TileTectonicFaultCore te, float ticks) {
        ShaderProgram natural = ShaderManager.getProgram("natural_effect");
        ShaderProgram arcane = ShaderManager.getProgram("arcane_effect");
        if (natural == null || arcane == null) {
            return;
        }

        drawPressureColumn(natural, ticks);
        drawFaultLines(arcane, ticks);
        drawStoneSlabs(arcane, ticks);
        drawSeismicRings(natural, ticks);
        drawMoltenSparks(natural, ticks);
    }

    private void drawPressureColumn(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045D);

        useAlphaBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(shader, 1.05D + pulse * 0.08D,
                RenderNaturalShaderHelper.MODE_ENTROPY, 2.4F, 0x241F21, 0x5A3B2A, 0xFFB45E,
                0.22F + pulse * 0.05F, pulse, 1.10F, ticks * 0.035F, 17.0F, 20, 20);
        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(shader, 0.46D + pulse * 0.04D,
                RenderNaturalShaderHelper.MODE_SOLAR, 4.6F, 0xE6722F, 0xFFCE66, 0xFFFFFF,
                0.34F + pulse * 0.12F, pulse, 1.22F, ticks * 0.042F, 23.0F, 18, 18);
        useAlphaBlend();
    }

    private void drawFaultLines(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 5; i++) {
            double start = i * TWO_PI / 5.0D + ticks * 0.004D;
            double phase = fract(ticks * 0.010D + i * 0.17D);
            float alpha = 0.20F + (float) Math.sin(phase * Math.PI) * 0.42F;
            ArcaneShaderEffectRenderer.drawJaggedArcLayer(shader, ticks,
                    0.60D + i * 0.20D, start, 0.66D + i * 0.07D,
                    -0.46D + i * 0.09D, 0.10D + i * 0.035D, 0.060D,
                    7 + i, 0xFF7A2C, 0xFFE1A3, alpha, 1.55F,
                    18.0F, i * 13.0F, (float) phase);
        }
        useAlphaBlend();
    }

    private void drawStoneSlabs(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < SLAB_COUNT; i++) {
            double angle = i * TWO_PI / SLAB_COUNT + Math.sin(ticks * 0.011D + i) * 0.09D;
            double radius = 0.74D + (i % 3) * 0.28D + Math.sin(ticks * 0.013D + i * 0.8D) * 0.045D;
            double height = -0.28D + (i % 4) * 0.18D + Math.sin(ticks * 0.019D + i) * 0.050D;
            float pulse = wave(ticks * 0.033D + i * 0.61D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            GlStateManager.rotate((float) Math.toDegrees(-angle), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (10.0D + Math.sin(ticks * 0.017D + i) * 12.0D), 1.0F, 0.0F, 0.0F);
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks,
                    0.26D + (i % 3) * 0.055D, 0.54D + (i % 2) * 0.11D,
                    i % 2 == 0 ? 0x2B2728 : 0x403532, 0xFF8B3E,
                    0.32F + pulse * 0.12F, 0.95F, 9.0F, 30.0F + i * 5.0F, pulse);
            useAdditiveBlend();
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks,
                    0.16D + (i % 3) * 0.035D, 0.36D + (i % 2) * 0.08D,
                    0xFF8B3E, 0xFFE1A3, 0.10F + pulse * 0.10F,
                    1.30F, 14.0F, 70.0F + i * 7.0F, pulse);
            useAlphaBlend();
            GlStateManager.popMatrix();
        }
    }

    private void drawSeismicRings(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.54D, 0.0D);
        for (int i = 0; i < 3; i++) {
            double phase = fract(ticks * 0.0075D + i * 0.33D);
            double radius = 0.72D + phase * 1.72D;
            float alpha = (float) Math.sin(phase * Math.PI) * (0.26F - i * 0.035F);
            int color = i % 2 == 0 ? 0xFF8B3E : 0xF0C06A;
            RenderNaturalShaderHelper.drawShaderRing(shader, radius - 0.030D, radius + 0.030D,
                    RenderNaturalShaderHelper.MODE_SOLAR, 4.9F + i * 0.18F,
                    color, 0xFFE1A3, 0xFFFFFF, alpha, (float) phase, 1.08F,
                    ticks * 0.042F, 193.0F + i * 17.0F, 96);
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawMoltenSparks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double phase = fract(ticks * (0.012D + (i % 4) * 0.001D) + i * 0.047D);
            double angle = i * 2.399963229728653D + ticks * 0.009D;
            double radius = 1.70D - phase * 1.32D;
            double y = -0.36D + Math.sin(phase * Math.PI) * (0.62D + (i % 3) * 0.10D);
            float alpha = (float) Math.sin(phase * Math.PI) * 0.62F;
            double size = 0.020D + (i % 4) * 0.006D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(shader, size,
                    RenderNaturalShaderHelper.MODE_SOLAR, 5.0F, 0xFF8B3E, 0xFFE1A3, 0xFFFFFF,
                    alpha, alpha, 1.45F, ticks * 0.045F, i * 3.0F, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
