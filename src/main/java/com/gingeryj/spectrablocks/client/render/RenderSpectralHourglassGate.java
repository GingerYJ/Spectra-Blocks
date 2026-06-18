package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSpectralHourglassGate extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double GATE_HEIGHT = 3.18D;
    private static final double FUNNEL_HEIGHT = 1.34D;
    private static final double FUNNEL_RADIUS = 0.84D;
    private static final double THROAT_RADIUS = 0.105D;
    private static final double ARC_RADIUS = 1.26D;
    private static final double ARC_HEIGHT = 1.62D;
    private static final int FUNNEL_SEGMENTS = 36;
    private static final int FUNNEL_RINGS = 8;
    private static final int STREAM_COUNT = 34;
    private static final int SPARK_COUNT = 26;
    private static final int ARC_SEGMENTS = 48;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int VIOLET = 0xB589FF;
    private static final int CYAN = 0x63F7FF;
    private static final int ROSE = 0xFF72D2;
    private static final int MINT = 0x87FFD9;
    private static final int WHITE = 0xF6FFFF;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram naturalShader = ShaderManager.getProgram("natural_effect");
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (naturalShader == null) {
            return;
        }

        drawFunnelPair(ticks, naturalShader);
        drawThroat(ticks, naturalShader, colorShader);
        drawSpectralSand(ticks, naturalShader);
        drawGateArcs(ticks, colorShader);
    }

    private void drawFunnelPair(float ticks, ShaderProgram naturalShader) {
        useAlphaBlend();
        float pulse = wave(ticks * 0.045D);
        drawFunnel(naturalShader, ticks, true, 0.13F + pulse * 0.035F, 0.18F, VIOLET, CYAN, MINT);
        drawFunnel(naturalShader, ticks + 11.0F, false, 0.12F + pulse * 0.032F, 0.62F, CYAN, ROSE, WHITE);

        useAdditiveBlend();
        drawFunnel(naturalShader, ticks + 23.0F, true, 0.050F + pulse * 0.016F, 1.18F, ROSE, CYAN, WHITE);
        drawFunnel(naturalShader, ticks + 37.0F, false, 0.046F + pulse * 0.016F, 1.54F, MINT, VIOLET, WHITE);
        useAlphaBlend();
    }

    private void drawFunnel(ShaderProgram shader, float ticks, boolean upper, float alpha, float layer,
                            int primaryColor, int secondaryColor, int accentColor) {
        if (!shader.begin()) {
            return;
        }

        try {
            float direction = upper ? 1.0F : -1.0F;
            setNaturalUniforms(shader, RenderNaturalShaderHelper.MODE_HOURGLASS, layer,
                    primaryColor, secondaryColor, accentColor, alpha,
                    wave(ticks * 0.040D + layer), 1.08F, ticks * 0.031F, upper ? 41.0F : 73.0F);
            drawFunnelGeometry(direction, ticks * 0.006D * direction);
        } finally {
            shader.end();
        }
    }

    private void drawThroat(float ticks, ShaderProgram naturalShader, ShaderProgram colorShader) {
        useAdditiveBlend();
        float pulse = wave(ticks * 0.092D);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.78D + pulse * 0.08D, 0.16D + pulse * 0.035D, 0.78D + pulse * 0.08D);
        RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.28D,
                RenderNaturalShaderHelper.MODE_HOURGLASS, 2.7F,
                WHITE, CYAN, ROSE, 0.50F, pulse, 1.32F,
                ticks * 0.064F, 119.0F, 14);
        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(1.7F);
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0D + ticks * 0.020D;
            double radius = 0.17D + (i % 2) * 0.035D;
            RenderNaturalShaderHelper.drawBasicLine(colorShader,
                    Math.cos(angle) * radius, -0.13D, Math.sin(angle) * radius,
                    Math.cos(angle + Math.PI) * radius, 0.13D, Math.sin(angle + Math.PI) * radius,
                    i % 2 == 0 ? CYAN : ROSE, 0.20F + pulse * 0.10F);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawSpectralSand(float ticks, ShaderProgram naturalShader) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_COUNT; i++) {
            double progress = fract(ticks * 0.024D + i * 0.083D);
            double y = FUNNEL_HEIGHT * 0.86D - progress * FUNNEL_HEIGHT * 1.72D;
            double pinch = Math.min(1.0D, Math.abs(y) / (FUNNEL_HEIGHT * 0.86D));
            double radius = 0.035D + pinch * pinch * 0.30D;
            double angle = i * GOLDEN_ANGLE + ticks * 0.019D + progress * 0.9D;
            int color = spectralColor(i);
            float alpha = 0.22F + 0.18F * wave(ticks * 0.052D + i * 0.61D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius * 0.38D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.024D + (i % 3) * 0.005D,
                    RenderNaturalShaderHelper.MODE_HOURGLASS, 3.1F + (i % 5) * 0.13F,
                    color, WHITE, CYAN, alpha, (float) progress, 1.18F,
                    ticks * 0.058F, i * 17.0F, 7);
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < SPARK_COUNT; i++) {
            double side = i < SPARK_COUNT / 2 ? 1.0D : -1.0D;
            int local = i % (SPARK_COUNT / 2);
            double drift = fract(ticks * 0.010D + local * 0.137D);
            double y = side * lerp(0.36D, 1.30D, drift);
            double radius = lerp(0.18D, 0.78D, drift);
            double angle = local * GOLDEN_ANGLE - side * ticks * 0.012D;
            float fade = (float) Math.sin(Math.PI * drift);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius * 0.42D);
            RenderNaturalShaderHelper.drawNaturalSphere(naturalShader, 0.020D + (local % 2) * 0.006D,
                    RenderNaturalShaderHelper.MODE_HOURGLASS, 4.0F + local * 0.07F,
                    spectralColor(local + i), WHITE, MINT, 0.09F + fade * 0.14F,
                    fade, 0.98F, ticks * 0.042F, 211.0F + local * 13.0F, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawGateArcs(float ticks, ShaderProgram colorShader) {
        if (colorShader == null) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.glLineWidth(2.8F);
        drawGateArc(colorShader, -1.0D, ticks, CYAN, 0.28F);
        drawGateArc(colorShader, 1.0D, ticks + 17.0F, ROSE, 0.25F);

        GlStateManager.glLineWidth(1.2F);
        drawInnerArc(colorShader, -1.0D, ticks + 31.0F, MINT, 0.15F);
        drawInnerArc(colorShader, 1.0D, ticks + 47.0F, VIOLET, 0.15F);

        for (int i = 0; i < 8; i++) {
            double side = i % 2 == 0 ? -1.0D : 1.0D;
            double y = -1.08D + (i / 2) * 0.72D;
            double pulse = wave(ticks * 0.070D + i);
            RenderNaturalShaderHelper.drawBasicLine(colorShader,
                    side * (ARC_RADIUS - 0.05D), y, 0.0D,
                    side * (ARC_RADIUS - 0.22D - pulse * 0.04D), y + 0.08D * (i % 3 - 1), 0.0D,
                    spectralColor(i), 0.10F + (float) pulse * 0.10F);
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawGateArc(ShaderProgram shader, double side, float ticks, int color, float alpha) {
        drawVerticalArc(shader, side, ARC_RADIUS, ARC_HEIGHT, ticks, color, alpha, 0.0D);
    }

    private void drawInnerArc(ShaderProgram shader, double side, float ticks, int color, float alpha) {
        drawVerticalArc(shader, side, ARC_RADIUS * 0.78D, ARC_HEIGHT * 0.88D, ticks, color, alpha, 0.16D);
    }

    private void drawVerticalArc(ShaderProgram shader, double side, double radius, double height,
                                 float ticks, int color, float alpha, double zOffset) {
        if (!shader.begin()) {
            return;
        }

        try {
            setBasicUniforms(shader);
            float[] rgb = unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= ARC_SEGMENTS; i++) {
                double progress = (double) i / ARC_SEGMENTS;
                double arch = Math.sin(Math.PI * progress);
                double y = (progress - 0.5D) * height * 2.0D;
                double x = side * (radius - arch * 0.30D);
                double z = zOffset * Math.sin(Math.PI * progress * 2.0D)
                        + Math.sin(ticks * 0.024D + progress * Math.PI * 3.0D) * 0.035D;
                float fade = (float) (0.24D + 0.76D * arch);
                buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], alpha * fade).endVertex();
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private static void drawFunnelGeometry(double direction, double twist) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int ring = 0; ring < FUNNEL_RINGS; ring++) {
            double p0 = (double) ring / FUNNEL_RINGS;
            double p1 = (ring + 1.0D) / FUNNEL_RINGS;
            for (int segment = 0; segment < FUNNEL_SEGMENTS; segment++) {
                double s0 = (double) segment / FUNNEL_SEGMENTS;
                double s1 = (segment + 1.0D) / FUNNEL_SEGMENTS;
                addFunnelVertex(buffer, direction, p0, s0, twist);
                addFunnelVertex(buffer, direction, p1, s0, twist);
                addFunnelVertex(buffer, direction, p1, s1, twist);
                addFunnelVertex(buffer, direction, p0, s0, twist);
                addFunnelVertex(buffer, direction, p1, s1, twist);
                addFunnelVertex(buffer, direction, p0, s1, twist);
            }
        }
        tessellator.draw();
    }

    private static void addFunnelVertex(BufferBuilder buffer, double direction,
                                        double progress, double segment, double twist) {
        double softened = progress * progress * (3.0D - 2.0D * progress);
        double radius = THROAT_RADIUS + (FUNNEL_RADIUS - THROAT_RADIUS) * softened;
        double y = direction * lerp(0.0D, FUNNEL_HEIGHT, progress);
        double angle = Math.PI * 2.0D * segment + twist + direction * progress * 0.78D;
        double zScale = 0.42D + progress * 0.16D;
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius * zScale;
        double normalX = Math.cos(angle);
        double normalZ = Math.sin(angle);

        buffer.pos(x, y, z)
                .tex(segment, direction > 0.0D ? progress : 1.0D - progress)
                .normal((float) normalX, 0.36F, (float) normalZ)
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

    private static void setBasicUniforms(ShaderProgram shader) {
        shader.setUniform1f("alpha", 1.0F);
        shader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static int spectralColor(int index) {
        switch (index % 5) {
            case 0:
                return CYAN;
            case 1:
                return VIOLET;
            case 2:
                return ROSE;
            case 3:
                return MINT;
            default:
                return WHITE;
        }
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }
}
