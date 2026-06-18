package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileStardustFountain;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderStardustFountain extends RenderCelestialEffectBase<TileStardustFountain> {

    private static final double BASIN_RADIUS = 1.18D;
    private static final double PLUME_HEIGHT = 2.72D;
    private static final double TOP_SPREAD_RADIUS = 1.62D;
    private static final int RING_SEGMENTS = 128;
    private static final int STREAM_COUNT = 16;
    private static final int DUST_COUNT = 30;
    private static final float BASIN_ROTATION_SPEED = 0.58F;
    private static final float CORE_PULSE_SPEED = 0.075F;

    @Override
    protected void renderCelestialEffect(TileStardustFountain te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        drawBasin(ticks, naturalShader);
        drawRisingStream(ticks, naturalShader);
        drawFallingStardust(ticks, naturalShader);
        drawFountainCore(ticks, naturalShader);
    }

    private void drawBasin(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * 0.048F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.04D, 0.0D);
        GlStateManager.rotate(ticks * BASIN_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, BASIN_RADIUS * 0.82D + pulse * 0.04D,
                RenderNaturalShaderHelper.MODE_STARDUST, 0.2F, 0x285A91, 0x9AE8FF, 0xFFF4C6,
                0.115F + pulse * 0.040F, pulse, 0.72F, ticks * 0.030F, 3.0F, 28);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0D, 0.06D, 1.0D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, BASIN_RADIUS + pulse * 0.05D,
                RenderNaturalShaderHelper.MODE_STARDUST, 0.0F, 0x285A91, 0x9AE8FF, 0xFFF4C6,
                0.18F + pulse * 0.05F, pulse, 0.95F, ticks * 0.024F, 7.0F, 30);
        GlStateManager.popMatrix();
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, BASIN_RADIUS,
                RenderNaturalShaderHelper.MODE_STARDUST, 4.6F,
                0x9AE8FF, 0x285A91, 0xFFF4C6, 0.27F + pulse * 0.12F,
                pulse, 1.04F, ticks * 0.030F, 193.0F, RING_SEGMENTS);
        RenderNaturalShaderHelper.drawShaderStarRays(naturalShader, 0.32D, BASIN_RADIUS * 0.92D, 10,
                RenderNaturalShaderHelper.MODE_STARDUST, 4.9F,
                0xFFF4C6, 0x9AE8FF, 0xFFFFFF, 0.16F + pulse * 0.08F,
                pulse, 1.12F, ticks * 0.032F, 211.0F, ticks * 0.010D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRisingStream(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_COUNT; i++) {
            double progress = fract(ticks * 0.020D + i * 0.061D);
            double angle = i * 2.399963229728653D + ticks * 0.032D;
            double radius = 0.10D + Math.sin(progress * Math.PI) * 0.22D + (i % 4) * 0.010D;
            double height = progress * PLUME_HEIGHT;
            double size = 0.040D + Math.sin(progress * Math.PI) * 0.066D;
            float fade = 0.18F + (float) Math.sin(Math.PI * progress) * 0.36F;
            int color = i % 3 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFEFC2 : 0x95E7FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_STARDUST, 1.0F + (i % 4) * 0.2F,
                    color, 0x95E7FF, 0xFFF4C6, fade, (float) progress, 1.18F,
                    ticks * 0.045F, i * 13.0F, 10);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFallingStardust(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < DUST_COUNT; i++) {
            double progress = fract(ticks * 0.010D + i * 0.037D);
            double angle = i * 2.399963229728653D + ticks * (0.011D + (i % 5) * 0.001D);
            double arc = Math.sin(progress * Math.PI);
            double radius = TOP_SPREAD_RADIUS * arc * (0.74D + (i % 6) * 0.045D);
            double height = PLUME_HEIGHT - progress * 1.72D + arc * 0.28D;
            double size = 0.032D + (1.0D - progress) * 0.052D;
            float fade = 0.12F + (float) arc * 0.32F;
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD68A : 0x78DFFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_STARDUST, 2.0F + (i % 5) * 0.1F,
                    color, 0x78DFFF, 0xFFF2B8, fade, (float) arc, 1.0F,
                    ticks * 0.038F, 41.0F + i * 5.0F, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFountainCore(float ticks, ShaderProgram naturalShader) {
        float pulse = wave(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.30D + pulse * 0.04D,
                RenderNaturalShaderHelper.MODE_STARDUST, 3.0F, 0xFFFFFF, 0x8AE6FF, 0xFFF2B8,
                0.72F + pulse * 0.16F, pulse, 1.42F, ticks * 0.060F, 97.0F, 22);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.76D + pulse * 0.10D,
                RenderNaturalShaderHelper.MODE_STARDUST, 3.4F, 0x8AE6FF, 0x285A91, 0xFFFFFF,
                0.14F + pulse * 0.055F, pulse, 0.95F, ticks * 0.035F, 131.0F, 24);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, PLUME_HEIGHT, 0.0D);
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.24D + pulse * 0.045D,
                RenderNaturalShaderHelper.MODE_STARDUST, 4.0F, 0xFFF2B8, 0xFFFFFF, 0x95E7FF,
                0.30F + pulse * 0.18F, pulse, 1.22F, ticks * 0.052F, 173.0F, 18);
        GlStateManager.disableCull();
        GlStateManager.popMatrix();

        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        useAlphaBlend();
    }
}
