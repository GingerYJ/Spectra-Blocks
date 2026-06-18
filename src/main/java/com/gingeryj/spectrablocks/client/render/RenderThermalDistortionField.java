package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileThermalDistortionField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderThermalDistortionField extends RenderCelestialEffectBase<TileThermalDistortionField> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double SHELL_RADIUS = 1.34D;
    private static final double COLUMN_BASE_Y = -0.62D;
    private static final double COLUMN_HEIGHT = 2.16D;
    private static final int COLUMN_RIBBONS = 7;
    private static final int COLUMN_SEGMENTS = 44;
    private static final int RIPPLE_SEGMENTS = 128;
    private static final int SPARK_COUNT = 18;

    private static final int HEAT_GLASS = 0xFFF0C7;
    private static final int HOT_ORANGE = 0xFF8A1C;
    private static final int DEEP_RED = 0xD92E00;
    private static final int EMBER_GOLD = 0xFFD66E;

    @Override
    protected void renderCelestialEffect(TileThermalDistortionField te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        if (naturalShader == null) {
            return;
        }

        try {
            drawTransparentHeatShell(ticks, naturalShader);
            drawRisingHeatColumns(ticks, naturalShader);
            drawEdgeRipples(ticks, naturalShader);
            drawHeatSparks(ticks, naturalShader);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("thermal distortion field render failed: " + ex.getMessage());
        }
    }

    private void drawTransparentHeatShell(float ticks, ShaderProgram naturalShader) {
        float breath = wave(ticks * 0.035D);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.055F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0D + breath * 0.018D, 0.92D + breath * 0.028D, 1.0D + breath * 0.018D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, SHELL_RADIUS + breath * 0.045D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.0F, HEAT_GLASS, HOT_ORANGE, DEEP_RED,
                0.060F + breath * 0.020F, breath, 0.46F, ticks * 0.030F, 271.0F, 18, 32);
        GlStateManager.popMatrix();

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * -0.090F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(7.0F + breath * 4.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(1.03D, 0.89D, 1.03D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, SHELL_RADIUS * 1.015D,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.6F, 0xFFD7A1, HOT_ORANGE, 0xFFFFFF,
                0.035F + breath * 0.018F, breath, 0.72F, ticks * 0.045F, 313.0F, 16, 30);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawRisingHeatColumns(float ticks, ShaderProgram naturalShader) {
        useAlphaBlend();
        for (int i = 0; i < COLUMN_RIBBONS; i++) {
            float pulse = wave(ticks * (0.030D + i * 0.002D) + i * 0.79D);
            float alpha = 0.070F + pulse * 0.040F;
            drawHeatColumnRibbon(naturalShader, ticks, i, alpha, pulse);
        }
    }

    private void drawHeatColumnRibbon(ShaderProgram shader, float ticks, int index, float alpha, float pulse) {
        if (shader == null || alpha <= 0.005F || !shader.begin()) {
            return;
        }

        try {
            int primary = index % 3 == 0 ? HEAT_GLASS : HOT_ORANGE;
            int secondary = index % 2 == 0 ? 0xFFB04A : DEEP_RED;
            setNaturalUniforms(shader, RenderNaturalShaderHelper.MODE_SOLAR, 2.0F + index * 0.13F,
                    primary, secondary, EMBER_GOLD, alpha, pulse, 0.82F,
                    ticks * 0.052F, 401.0F + index * 29.0F);

            double baseAngle = index * TWO_PI / COLUMN_RIBBONS;
            double rise = ticks * (0.020D + index * 0.0008D);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);

            for (int segment = 0; segment <= COLUMN_SEGMENTS; segment++) {
                double progress = segment / (double) COLUMN_SEGMENTS;
                double fade = Math.sin(Math.PI * progress);
                double twist = baseAngle + rise + progress * (1.60D + (index % 3) * 0.20D)
                        + Math.sin(ticks * 0.026D + progress * 9.0D + index) * 0.18D;
                double radius = 0.12D + fade * (0.48D + (index % 4) * 0.035D)
                        + Math.sin(ticks * 0.040D + progress * 12.0D + index * 1.7D) * 0.030D;
                double y = COLUMN_BASE_Y + progress * COLUMN_HEIGHT
                        + Math.sin(ticks * 0.030D + progress * 7.0D + index) * 0.055D;
                double halfWidth = (0.055D + fade * 0.125D) * (0.86D + pulse * 0.28D);
                double centerX = Math.cos(twist) * radius;
                double centerZ = Math.sin(twist) * radius;
                double tangentX = -Math.sin(twist);
                double tangentZ = Math.cos(twist);
                double normalX = Math.cos(twist);
                double normalZ = Math.sin(twist);

                addShaderVertex(buffer, centerX - tangentX * halfWidth, y, centerZ - tangentZ * halfWidth,
                        0.0D, progress, normalX, 0.16D, normalZ);
                addShaderVertex(buffer, centerX + tangentX * halfWidth, y, centerZ + tangentZ * halfWidth,
                        1.0D, progress, normalX, 0.16D, normalZ);
            }

            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private void drawEdgeRipples(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < 6; i++) {
            double progress = fract(ticks * (0.010D + i * 0.0015D) + i * 0.173D);
            double y = -0.34D + i * 0.145D + Math.sin(ticks * 0.022D + i) * 0.025D;
            double shellSlice = Math.sqrt(Math.max(0.0D, SHELL_RADIUS * SHELL_RADIUS - y * y));
            double radius = shellSlice * (0.78D + progress * 0.18D);
            double width = 0.030D + progress * 0.040D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 2 == 0 ? HOT_ORANGE : DEEP_RED;

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (ticks * (0.018D + i * 0.004D) + i * 31.0D),
                    0.0F, 1.0F, 0.0F);
            drawWavyShaderRing(naturalShader, radius, y, width, color,
                    (0.075F + fade * 0.085F) * (i == 0 ? 1.15F : 1.0F),
                    ticks, i * 0.61D);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.52D, 0.0D);
        drawWavyShaderRing(naturalShader, 1.02D + wave(ticks * 0.046D) * 0.055D, 0.0D, 0.055D,
                EMBER_GOLD, 0.095F + wave(ticks * 0.061D) * 0.070F, ticks, 7.3D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private static void drawWavyShaderRing(ShaderProgram shader, double radius, double y, double width,
                                          int color, float alpha, float ticks, double seed) {
        if (shader == null || radius <= 0.0D || width <= 0.0D || alpha <= 0.005F || !shader.begin()) {
            return;
        }

        try {
            setNaturalUniforms(shader, RenderNaturalShaderHelper.MODE_SOLAR, 2.65F + (float) seed * 0.07F,
                    color, HOT_ORANGE, HEAT_GLASS, alpha, wave(ticks * 0.040D + seed),
                    0.95F, ticks * 0.052F, 631.0F + (float) seed * 17.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);

            for (int i = 0; i <= RIPPLE_SEGMENTS; i++) {
                double progress = i / (double) RIPPLE_SEGMENTS;
                double angle = progress * TWO_PI;
                double ripple = Math.sin(angle * 5.0D - ticks * 0.055D + seed) * 0.026D
                        + Math.sin(angle * 11.0D + ticks * 0.034D + seed * 1.7D) * 0.012D;
                double inner = radius - width * 0.5D + ripple * 0.65D;
                double outer = radius + width * 0.5D + ripple;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                addShaderVertex(buffer, cos * outer, y, sin * outer, progress, 1.0D, 0.0D, 1.0D, 0.0D);
                addShaderVertex(buffer, cos * inner, y, sin * inner, progress, 0.0D, 0.0D, 1.0D, 0.0D);
            }

            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private void drawHeatSparks(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < SPARK_COUNT; i++) {
            double progress = fract(ticks * (0.008D + (i % 5) * 0.0011D) + i * 0.067D);
            double liftFade = Math.sin(Math.PI * progress);
            double angle = i * GOLDEN_ANGLE + ticks * (0.013D + (i % 4) * 0.0014D);
            double radius = 0.18D + liftFade * (0.46D + (i % 4) * 0.045D);
            double x = Math.cos(angle) * radius + Math.sin(ticks * 0.035D + i) * 0.035D;
            double y = COLUMN_BASE_Y + 0.14D + progress * 1.78D
                    + Math.sin(ticks * 0.044D + i * 1.8D) * 0.045D;
            double z = Math.sin(angle) * radius + Math.cos(ticks * 0.031D + i) * 0.035D;
            double size = 0.014D + liftFade * 0.020D + (i % 3) * 0.004D;
            float blink = wave(ticks * (0.080D + (i % 4) * 0.008D) + i * 0.9D);
            float alpha = (0.10F + blink * 0.22F) * (0.35F + (float) liftFade * 0.65F);
            int color = i % 7 == 0 ? 0xFFFFFF : (i % 3 == 0 ? EMBER_GOLD : HOT_ORANGE);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.scale(0.78D, 1.50D + blink * 0.35D, 0.78D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, size,
                    RenderNaturalShaderHelper.MODE_SOLAR, 3.0F + (i % 5) * 0.11F,
                    color, HOT_ORANGE, HEAT_GLASS, alpha, blink, 1.18F,
                    ticks * 0.070F, 557.0F + i * 19.0F, 6, 8);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void addShaderVertex(BufferBuilder buffer, double x, double y, double z,
                                        double u, double v, double normalX, double normalY,
                                        double normalZ) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) normalX, (float) normalY, (float) normalZ)
                .endVertex();
    }

    private static void setNaturalUniforms(ShaderProgram shader, float mode, float layer,
                                           int primaryColor, int secondaryColor, int accentColor,
                                           float alpha, float pulse, float intensity,
                                           float time, float seed) {
        float[] primary = unpackRGB(primaryColor);
        float[] secondary = unpackRGB(secondaryColor);
        float[] accent = unpackRGB(accentColor);
        shader.setUniform1f("uTime", time);
        shader.setUniform1f("uMode", mode);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uPulse", pulse);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uSecondaryColor", secondary[0], secondary[1], secondary[2]);
        shader.setUniform3f("uAccentColor", accent[0], accent[1], accent[2]);
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }
}
