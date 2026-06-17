package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileGravityWell;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderGravityWell extends RenderCelestialEffectBase<TileGravityWell> {

    private static final float EFFECT_GRAVITY_WELL = 2.0F;
    private static final double CORE_RADIUS = 0.34D;
    private static final double FIELD_RADIUS = 1.74D;
    private static final int FIELD_SEGMENTS = 28;
    private static final int ORBIT_SEGMENTS = 144;
    private static final int PARTICLE_COUNT = 32;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;

    @Override
    protected void renderCelestialEffect(TileGravityWell te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("celestial_effect");
        if (shader == null) {
            return;
        }

        try {
            drawOuterField(shader, ticks);
            drawCompressedOrbits(shader, ticks);
            drawInfallParticles(shader, ticks);
            drawDarkCore(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("gravity well render failed: " + ex.getMessage());
        } finally {
            shader.end();
            useAlphaBlend();
            RenderHelper.resetLineWidth();
        }
    }

    private void drawDarkCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.046D);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.00D + pulse * 0.055D), ticks,
                EFFECT_GRAVITY_WELL, 0.0F, 0x020816, 0x08112A, 0.94F, 0.62F, 0.0F, 24);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.80D + pulse * 0.16D), ticks,
                EFFECT_GRAVITY_WELL, 1.0F, 0x153869, 0xBDF8FF, 0.135F, 1.00F, 0.21F, 22);
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (2.25D + pulse * 0.12D), ticks,
                EFFECT_GRAVITY_WELL, 2.0F, 0x7856C9, 0xD9FDFF, 0.085F, 0.92F, 0.46F, 18);
        useAlphaBlend();
    }

    private void drawCompressedOrbits(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.rotate(62.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(ticks * 0.035F, 0.0F, 1.0F, 0.0F);

            for (int i = 0; i < 5; i++) {
                double progress = i / 4.0D;
                double outer = lerp(0.72D, 2.42D, progress);
                double inner = outer - lerp(0.018D, 0.038D, progress);
                float alpha = (float) lerp(0.170D, 0.075D, progress);
                float seed = 0.13F + i * 0.173F;

                GlStateManager.pushMatrix();
                try {
                    GlStateManager.rotate(i * 17.0F - ticks * (0.110F + i * 0.018F), 0.0F, 1.0F, 0.0F);
                    GlStateManager.scale(1.0D, 1.0D, lerp(0.42D, 0.64D, progress));
                    RenderMiniatureGalaxy.drawShaderRing(shader, inner, outer, ticks,
                            EFFECT_GRAVITY_WELL, 3.0F + i, orbitColor(i), 0xD9FDFF,
                            alpha, 1.12F, seed, ORBIT_SEGMENTS);
                } finally {
                    GlStateManager.popMatrix();
                }
            }

            RenderMiniatureGalaxy.drawShaderSpiral(shader, 2.48D, 0.48D, ticks * 0.016D,
                    Math.PI * 2.55D, 0.055D, ticks, EFFECT_GRAVITY_WELL, 8.0F,
                    0x7CEBFF, 0xFFFFFF, 0.105F, 1.20F, 0.74F, 120);
            RenderMiniatureGalaxy.drawShaderSpiral(shader, 2.18D, 0.40D, Math.PI + ticks * 0.013D,
                    Math.PI * 2.20D, 0.040D, ticks, EFFECT_GRAVITY_WELL, 9.0F,
                    0x8F70FF, 0xD9FDFF, 0.075F, 1.05F, 0.39F, 104);
        } finally {
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawInfallParticles(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.rotate(62.0F, 1.0F, 0.0F, 0.0F);

            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double progress = fract(i * 0.097D + ticks * 0.0085D);
                double radius = lerp(2.28D, 0.36D, progress);
                double angle = i * GOLDEN_ANGLE - progress * 4.35D + ticks * 0.012D;
                double tailProgress = Math.max(0.0D, progress - 0.055D);
                double tailRadius = lerp(2.28D, 0.36D, tailProgress);
                double tailAngle = i * GOLDEN_ANGLE - tailProgress * 4.35D + ticks * 0.012D;
                double flatten = 0.52D + (i % 3) * 0.045D;
                double y = Math.sin(i * 1.41D + ticks * 0.025D) * 0.035D;
                double headX = Math.cos(angle) * radius;
                double headZ = Math.sin(angle) * radius * flatten;
                double tailX = Math.cos(tailAngle) * tailRadius;
                double tailZ = Math.sin(tailAngle) * tailRadius * flatten;
                float fade = (float) Math.sin(Math.PI * progress);
                int color = i % 6 == 0 ? 0xFFFFFF : (i % 4 == 0 ? 0xA889FF : 0x8EF7FF);

                GlStateManager.glLineWidth(1.2F);
                RenderMiniatureGalaxy.drawShaderLine(shader, headX, y, headZ, tailX, y * 0.45D, tailZ, ticks,
                        EFFECT_GRAVITY_WELL, 10.0F, color, 0xD9FDFF, 0.105F * fade, 1.14F, i * 0.041F);

                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(headX, y, headZ);
                    RenderMiniatureGalaxy.drawShaderSphere(shader, 0.018D + 0.027D * (1.0D - progress), ticks,
                            EFFECT_GRAVITY_WELL, 11.0F, color, 0xFFFFFF, 0.34F * fade, 1.28F, i * 0.067F, 8);
                } finally {
                    GlStateManager.popMatrix();
                }
            }
        } finally {
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
        GL11.glLineWidth(1.0F);
    }

    private void drawOuterField(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.030D);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, FIELD_RADIUS * (1.00D + pulse * 0.035D), ticks,
                EFFECT_GRAVITY_WELL, 12.0F, 0x123A68, 0x9AF8FF, 0.090F + pulse * 0.035F,
                0.82F, 0.18F, FIELD_SEGMENTS);
        RenderMiniatureGalaxy.drawShaderSphere(shader, FIELD_RADIUS * (0.76D + pulse * 0.028D), ticks,
                EFFECT_GRAVITY_WELL, 13.0F, 0x241D5C, 0xC2FAFF, 0.060F + pulse * 0.025F,
                0.74F, 0.58F, 22);
        useAlphaBlend();
    }

    private static int orbitColor(int index) {
        switch (index % 4) {
            case 0:
                return 0x38CFFF;
            case 1:
                return 0xBDF8FF;
            case 2:
                return 0x5E7DFF;
            default:
                return 0x8F70FF;
        }
    }
}
