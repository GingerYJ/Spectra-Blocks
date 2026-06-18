package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileInkMirageBasin;
import net.minecraft.client.renderer.GlStateManager;

public class RenderInkMirageBasin extends RenderCelestialEffectBase<TileInkMirageBasin> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int INK_DROPS = 32;

    @Override
    protected void renderCelestialEffect(TileInkMirageBasin te, float ticks) {
        ShaderProgram natural = ShaderManager.getProgram("natural_effect");
        ShaderProgram arcane = ShaderManager.getProgram("arcane_effect");
        ShaderProgram basic = ShaderManager.getProgram("basic");
        if (natural == null || arcane == null || basic == null) {
            return;
        }

        drawInkSurface(natural, basic, ticks);
        drawFoldedMirages(natural, ticks);
        drawBrushStrokes(basic, ticks);
        drawInkDrops(arcane, ticks);
    }

    private void drawInkSurface(ShaderProgram natural, ShaderProgram basic, float ticks) {
        float pulse = wave(ticks * 0.024D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.42D, 0.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(natural, 1.18D + pulse * 0.035D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 2.0F, 0x05070C, 0x0D2631, 0x8BE7D7,
                0.26F + pulse * 0.05F, pulse, 0.82F, ticks * 0.022F, 44.0F, 20, 20);
        GlStateManager.scale(1.0D, 0.018D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(natural, 1.42D,
                RenderNaturalShaderHelper.MODE_ABYSSAL, 2.8F, 0x020306, 0x103943, 0xC9FFF0,
                0.46F + pulse * 0.08F, pulse, 1.05F, ticks * 0.028F, 58.0F, 18, 18);
        GlStateManager.popMatrix();

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.40D, 0.0D);
        GlStateManager.rotate((float) (ticks * 0.055D), 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawBasicSpiralRibbon(basic, 0.28D, 1.46D, ticks * 0.010D,
                TWO_PI * 1.82D, 0.060D, 0x9AF8E8, 0.22F + pulse * 0.10F, 72);
        RenderNaturalShaderHelper.drawBasicFlatRing(basic, 1.18D, 1.50D, 0x214B55, 0.14F, 96);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFoldedMirages(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < 4; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(i * 90.0F + ticks * 0.018F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, -0.18D + i * 0.045D, 0.20D + i * 0.030D);
            GlStateManager.rotate(16.0F + i * 8.0F + (float) Math.sin(ticks * 0.018D + i) * 4.0F,
                    1.0F, 0.0F, 0.0F);
            RenderNaturalShaderHelper.drawAuroraSheet(shader,
                    1.65D - i * 0.14D, 1.15D + i * 0.08D, -0.18D,
                    18, 1.2F + i * 0.55F, 0x061016, 0x145A66, 0xD8FFF4,
                    0.15F + i * 0.025F, wave(ticks * 0.030D + i), ticks * 0.026F, 70.0F + i * 9.0F);
            GlStateManager.popMatrix();
        }
    }

    private void drawBrushStrokes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 6; i++) {
            double phase = fract(ticks * 0.0065D + i * 0.17D);
            double start = i * 1.18D + ticks * 0.006D;
            float alpha = (float) Math.sin(phase * Math.PI) * 0.36F;
            double inner = 0.42D + (i % 2) * 0.12D;
            double outer = 1.28D + (i % 3) * 0.16D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, -0.22D + i * 0.035D, 0.0D);
            GlStateManager.rotate(12.0F + i * 17.0F, 1.0F, 0.0F, 0.36F);
            RenderNaturalShaderHelper.drawBasicSpiralRibbon(shader, inner, outer, start,
                    1.45D + i * 0.10D, 0.050D + (i % 3) * 0.008D,
                    i % 2 == 0 ? 0xD8FFF4 : 0x75DACC, alpha, 26);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawInkDrops(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < INK_DROPS; i++) {
            double phase = fract(ticks * (0.006D + (i % 5) * 0.0007D) + i * 0.073D);
            double angle = i * 2.399963229728653D + Math.sin(ticks * 0.010D + i) * 0.22D;
            double radius = 1.55D - phase * 1.05D + Math.sin(i * 0.71D) * 0.06D;
            double y = -0.20D + Math.sin(phase * Math.PI) * (0.48D + (i % 4) * 0.06D);
            float alpha = 0.16F + (float) Math.sin(phase * Math.PI) * 0.32F;
            double size = 0.022D + (i % 5) * 0.005D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    i % 3 == 0 ? 0xD8FFF4 : 0x0B2028, 0x89F7E5,
                    alpha, 1.20F, 8.0F, i * 11.0F, (float) phase,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }
}
