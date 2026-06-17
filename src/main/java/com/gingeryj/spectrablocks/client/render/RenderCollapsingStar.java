package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileCollapsingStar;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderCollapsingStar extends RenderCelestialEffectBase<TileCollapsingStar> {

    private static final double CORE_RADIUS = 0.88D;
    private static final double RING_INNER_RADIUS = 1.10D;
    private static final double RING_OUTER_RADIUS = 3.35D;
    private static final int RING_SEGMENTS = 192;
    private static final int INFALL_PARTICLE_COUNT = 68;
    private static final int FLASH_PERIOD = 230;
    private static final int FLASH_LENGTH = 18;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float EFFECT_COLLAPSING_STAR = 2.0F;

    @Override
    protected void renderCelestialEffect(TileCollapsingStar te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("celestial_effect");
        if (shader == null) {
            return;
        }

        try {
            drawAccretionRing(shader, ticks);
            drawCore(shader, ticks);
            drawInfallParticles(shader, ticks);
            drawFlash(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("collapsing star render failed: " + ex.getMessage());
        } finally {
            shader.end();
            useAlphaBlend();
            GL11.glLineWidth(1.0F);
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.055D);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.00D + pulse * 0.035D), ticks,
                EFFECT_COLLAPSING_STAR, 0.0F, 0x000000, 0x120602, 0.94F, 0.70F, 0.0F, 28);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.45D + pulse * 0.10D), ticks,
                EFFECT_COLLAPSING_STAR, 1.0F, 0xFFB24A, 0xFFE0A0, 0.105F, 1.05F, 0.21F, 28);
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.62D + pulse * 0.08D), ticks,
                EFFECT_COLLAPSING_STAR, 2.0F, 0xFFE0A0, 0xFFFFFF, 0.145F, 0.92F, 0.42F, 18);
        useAlphaBlend();
    }

    private void drawAccretionRing(ShaderProgram shader, float ticks) {
        GlStateManager.pushMatrix();
        try {
            GlStateManager.rotate(67.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(ticks * 0.185F, 0.0F, 1.0F, 0.0F);

            useAdditiveBlend();
            RenderMiniatureGalaxy.drawShaderRing(shader, RING_INNER_RADIUS, RING_OUTER_RADIUS, ticks,
                    EFFECT_COLLAPSING_STAR, 3.0F, 0xFF7A2E, 0xFFE5A3, 0.135F, 1.05F, 0.0F, RING_SEGMENTS);
            RenderMiniatureGalaxy.drawShaderRing(shader, 1.48D, 2.70D, ticks,
                    EFFECT_COLLAPSING_STAR, 4.0F, 0xFFE5A3, 0xFFFFFF, 0.165F, 1.20F, 0.27F, RING_SEGMENTS);
            RenderMiniatureGalaxy.drawShaderSpiral(shader, 1.04D, 3.18D, ticks * 0.010D,
                    Math.PI * 1.65D, 0.18D, ticks, EFFECT_COLLAPSING_STAR, 5.0F,
                    0xFFB34F, 0xFFECC8, 0.185F, 1.14F, 0.53F, 96);
            RenderMiniatureGalaxy.drawShaderSpiral(shader, 1.20D, 3.32D, Math.PI + ticks * 0.012D,
                    Math.PI * 1.48D, 0.13D, ticks, EFFECT_COLLAPSING_STAR, 6.0F,
                    0xFFFFFF, 0xFF9B42, 0.110F, 1.30F, 0.79F, 96);

            GlStateManager.glLineWidth(3.0F);
            RenderMiniatureGalaxy.drawShaderRing(shader, RING_OUTER_RADIUS * 1.000D, RING_OUTER_RADIUS * 1.024D, ticks,
                    EFFECT_COLLAPSING_STAR, 7.0F, 0xFF9B42, 0xFFECC8, 0.090F, 1.00F, 0.15F, RING_SEGMENTS);
            GlStateManager.glLineWidth(1.4F);
            RenderMiniatureGalaxy.drawShaderRing(shader, RING_INNER_RADIUS * 0.970D, RING_INNER_RADIUS * 0.990D, ticks,
                    EFFECT_COLLAPSING_STAR, 8.0F, 0xFFECC8, 0xFFFFFF, 0.190F, 1.18F, 0.38F, RING_SEGMENTS);
        } finally {
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawInfallParticles(ShaderProgram shader, float ticks) {
        GlStateManager.pushMatrix();
        try {
            GlStateManager.rotate(67.0F, 1.0F, 0.0F, 0.0F);
            useAdditiveBlend();

            for (int i = 0; i < INFALL_PARTICLE_COUNT; i++) {
                double progress = fract(i * 0.071D + ticks * 0.0105D);
                double radius = lerp(RING_OUTER_RADIUS * 0.98D, CORE_RADIUS * 0.72D, progress);
                double angle = i * GOLDEN_ANGLE - progress * 4.70D + ticks * 0.010D;
                double tailProgress = Math.max(0.0D, progress - 0.045D);
                double tailRadius = lerp(RING_OUTER_RADIUS * 0.98D, CORE_RADIUS * 0.72D, tailProgress);
                double tailAngle = i * GOLDEN_ANGLE - tailProgress * 4.70D + ticks * 0.010D;
                double y = Math.sin(i * 1.13D + ticks * 0.038D) * 0.060D;
                double headX = Math.cos(angle) * radius;
                double headZ = Math.sin(angle) * radius;
                double tailX = Math.cos(tailAngle) * tailRadius;
                double tailZ = Math.sin(tailAngle) * tailRadius;
                float fade = (float) Math.sin(Math.PI * progress);
                int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFD27D : 0xFF6F38);

                GlStateManager.glLineWidth(1.6F);
                RenderMiniatureGalaxy.drawShaderLine(shader, headX, y, headZ, tailX, y * 0.65D, tailZ, ticks,
                        EFFECT_COLLAPSING_STAR, 9.0F, color, 0xFFFFFF, 0.14F * fade, 1.18F, i * 0.031F);

                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(headX, y, headZ);
                    RenderMiniatureGalaxy.drawShaderSphere(shader, 0.028D + 0.050D * (1.0D - progress), ticks,
                            EFFECT_COLLAPSING_STAR, 10.0F, color, 0xFFFFFF, 0.42F * fade, 1.35F, i * 0.047F, 8);
                } finally {
                    GlStateManager.popMatrix();
                }
            }
        } finally {
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawFlash(ShaderProgram shader, float ticks) {
        int cycle = Math.floorMod((int) ticks, FLASH_PERIOD);
        if (cycle >= FLASH_LENGTH) {
            return;
        }

        float progress = cycle / (float) FLASH_LENGTH;
        float fade = (float) Math.sin(Math.PI * progress);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, 1.25D + progress * 2.35D, ticks,
                EFFECT_COLLAPSING_STAR, 11.0F, 0xFFFFFF, 0xFFC15D, 0.24F * fade, 1.45F, progress, 28);
        RenderMiniatureGalaxy.drawShaderSphere(shader, 1.80D + progress * 1.70D, ticks,
                EFFECT_COLLAPSING_STAR, 12.0F, 0xFFC15D, 0xFFF1C4, 0.18F * fade, 1.22F, 0.35F + progress, 24);
        GlStateManager.glLineWidth(4.0F);
        RenderMiniatureGalaxy.drawShaderSphere(shader, 1.55D + progress * 2.20D, ticks,
                EFFECT_COLLAPSING_STAR, 13.0F, 0xFFF1C4, 0xFFFFFF, 0.16F * fade, 1.10F, 0.70F + progress, 16);
        useAlphaBlend();
    }
}
