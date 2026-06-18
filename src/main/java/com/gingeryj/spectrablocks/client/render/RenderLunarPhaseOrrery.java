package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;

public class RenderLunarPhaseOrrery extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.31D;
    private static final double MOON_ORBIT_RADIUS = 1.38D;
    private static final double OUTER_ORBIT_RADIUS = 1.92D;
    private static final int MOON_COUNT = 8;
    private static final int RING_SEGMENTS = 104;
    private static final int SPHERE_SEGMENTS = 18;
    private static final int SMALL_SPHERE_SEGMENTS = 10;
    private static final float ORBIT_SPEED = 0.034F;
    private static final int MOON_WHITE = 0xF4F8FF;
    private static final int MOON_BLUE = 0xAFCFFF;
    private static final int MOON_SHADOW = 0x172033;
    private static final int SILVER = 0xC9D7EA;
    private static final int GOLD = 0xE9C779;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram celestialShader = ShaderManager.getProgram("celestial_effect");
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (celestialShader == null || naturalShader == null) {
            return;
        }

        try {
            drawMoonCore(celestialShader, naturalShader, ticks);
            drawInstrumentRings(celestialShader, naturalShader, ticks);
            drawPhaseMoons(celestialShader, naturalShader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("lunar phase orrery render failed: " + ex.getMessage());
        } finally {
            celestialShader.end();
            naturalShader.end();
            useAlphaBlend();
        }
    }

    private void drawMoonCore(ShaderProgram shader, ShaderProgram naturalShader, float ticks) {
        float pulse = wave(ticks * 0.026D);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (2.05D + pulse * 0.14D), ticks,
                0.0F, 0.0F, MOON_BLUE, MOON_WHITE, 0.095F + pulse * 0.035F, 0.82F, 0.13F, SPHERE_SEGMENTS);
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.26D + pulse * 0.08D), ticks,
                0.0F, 1.0F, MOON_WHITE, 0xFFFFFF, 0.58F + pulse * 0.10F, 1.22F, 0.31F, SPHERE_SEGMENTS);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderNaturalShaderHelper.drawShaderStarRays(naturalShader, 0.26D, 0.72D, 12,
                RenderNaturalShaderHelper.MODE_STARDUST, 0.6F,
                0xEAF4FF, MOON_BLUE, MOON_WHITE, 0.060F + pulse * 0.035F,
                pulse, 1.10F, ticks * 0.026F, 23.0F, ticks * 0.005D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawInstrumentRings(ShaderProgram shader, ShaderProgram naturalShader, float ticks) {
        float pulse = wave(ticks * 0.021D);

        drawTiltedRing(shader, naturalShader, ticks, MOON_ORBIT_RADIUS, 23.0F, 1.0F, 0.0F,
                SILVER, MOON_WHITE, 0.075F + pulse * 0.030F, 0.0F, 0.28F);
        drawTiltedRing(shader, naturalShader, ticks, OUTER_ORBIT_RADIUS, -41.0F, 0.18F, 1.0F,
                GOLD, 0xFFF0C0, 0.060F + pulse * 0.020F, 0.37F, -0.15F);
        drawTiltedRing(shader, naturalShader, ticks, 1.64D, 68.0F, 0.82F, 0.28F,
                0xDDE7F5, 0xFFFFFF, 0.048F + pulse * 0.018F, 0.74F, 0.19F);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.016F, 0.0F, 0.0F, 1.0F);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, 0.82D,
                RenderNaturalShaderHelper.MODE_STARDUST, 1.2F,
                0xEEF6FF, MOON_BLUE, MOON_WHITE, 0.070F + pulse * 0.030F,
                pulse, 1.05F, ticks * 0.024F, 59.0F, 72);
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, 2.18D,
                RenderNaturalShaderHelper.MODE_AURORA, 1.4F,
                GOLD, 0xFFF0C0, MOON_WHITE, 0.050F + pulse * 0.020F,
                pulse, 0.92F, ticks * 0.020F, 67.0F, 96);
        drawCrossHairs(naturalShader, 2.18D, GOLD, 0.040F + pulse * 0.018F, pulse, ticks);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawTiltedRing(ShaderProgram shader, ShaderProgram naturalShader, float ticks, double radius,
                                float tilt, float axisX, float axisZ, int primaryColor, int accentColor,
                                float alpha, float seed, float spinScale) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * spinScale, 0.0F, 1.0F, 0.0F);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderRing(shader, radius - 0.018D, radius + 0.018D, ticks,
                0.0F, 2.0F + seed, primaryColor, accentColor, alpha, 0.82F, seed, RING_SEGMENTS);
        useAdditiveBlend();
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, radius,
                RenderNaturalShaderHelper.MODE_STARDUST, 2.3F + seed,
                accentColor, primaryColor, 0xFFFFFF, alpha * 1.35F,
                wave(ticks * 0.020D + seed), 1.08F, ticks * 0.022F, 101.0F + seed * 37.0F, RING_SEGMENTS);

        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPhaseMoons(ShaderProgram shader, ShaderProgram naturalShader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(23.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * ORBIT_SPEED, 0.0F, 1.0F, 0.0F);

        for (int i = 0; i < MOON_COUNT; i++) {
            double angle = Math.PI * 2.0D * i / MOON_COUNT;
            double verticalBob = Math.sin(ticks * 0.018D + i * 0.71D) * 0.045D;
            double x = Math.cos(angle) * MOON_ORBIT_RADIUS;
            double z = Math.sin(angle) * MOON_ORBIT_RADIUS;
            float localPulse = wave(ticks * 0.037D + i * 0.83D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, verticalBob, z);
            GlStateManager.rotate((float) Math.toDegrees(-angle), 0.0F, 1.0F, 0.0F);
            drawPhaseMoon(shader, naturalShader, ticks, i, localPulse);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawPhaseMoon(ShaderProgram shader, ShaderProgram naturalShader, float ticks,
                               int phaseIndex, float pulse) {
        double moonRadius = 0.105D + (phaseIndex % 2) * 0.010D;
        double shadeOffset = (phaseIndex - 3.5D) / 3.5D * moonRadius * 0.74D;
        float brightAlpha = 0.46F + pulse * 0.16F;
        float shadowAlpha = 0.30F + pulse * 0.06F;

        RenderMiniatureGalaxy.drawShaderSphere(shader, moonRadius * (1.42D + pulse * 0.10D), ticks,
                0.0F, 3.0F, MOON_BLUE, MOON_WHITE, 0.055F + pulse * 0.025F, 0.82F,
                phaseIndex * 0.19F, SMALL_SPHERE_SEGMENTS);
        RenderMiniatureGalaxy.drawShaderSphere(shader, moonRadius, ticks,
                0.0F, 4.0F, MOON_WHITE, 0xFFFFFF, brightAlpha, 1.20F,
                phaseIndex * 0.23F, SMALL_SPHERE_SEGMENTS);

        GlStateManager.pushMatrix();
        GlStateManager.translate(shadeOffset, 0.0D, -0.010D);
        RenderMiniatureGalaxy.drawShaderSphere(shader, moonRadius * 0.96D, ticks,
                0.0F, 5.0F, MOON_SHADOW, MOON_BLUE, shadowAlpha, 0.56F,
                0.45F + phaseIndex * 0.17F, SMALL_SPHERE_SEGMENTS);
        GlStateManager.popMatrix();

        int color = phaseIndex % 2 == 0 ? SILVER : GOLD;
        RenderNaturalShaderHelper.drawShaderCircle(naturalShader, moonRadius * 1.32D,
                RenderNaturalShaderHelper.MODE_STARDUST, 3.0F + phaseIndex * 0.08F,
                color, MOON_WHITE, MOON_BLUE, 0.060F + pulse * 0.035F,
                pulse, 1.08F, ticks * 0.030F, 131.0F + phaseIndex * 11.0F, 20);
    }

    private void drawCrossHairs(ShaderProgram naturalShader, double radius, int color,
                                float alpha, float pulse, float ticks) {
        RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_STARDUST,
                1.6F, -radius, 0.0D, 0.0D, radius, 0.0D, 0.0D,
                color, MOON_WHITE, 0xFFFFFF, alpha, pulse, 1.0F, ticks * 0.026F, 149.0F);
        RenderNaturalShaderHelper.drawShaderLine(naturalShader, RenderNaturalShaderHelper.MODE_STARDUST,
                1.7F, 0.0D, 0.0D, -radius, 0.0D, 0.0D, radius,
                color, MOON_WHITE, 0xFFFFFF, alpha, pulse, 1.0F, ticks * 0.026F, 157.0F);
    }
}
