package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderEchoingVoidBell extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final float EFFECT_VOID_BELL = 1.0F;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CORE_RADIUS = 0.24D;
    private static final double BELL_RADIUS = 0.42D;
    private static final double AURA_RADIUS = 0.78D;
    private static final int SPHERE_SEGMENTS = 18;
    private static final int RING_SEGMENTS = 96;
    private static final int ECHO_RING_COUNT = 5;
    private static final int ECHO_LINE_COUNT = 10;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram spaceShader = ShaderManager.getProgram("space_effect");
        if (spaceShader == null) {
            return;
        }

        drawVoidBellCore(spaceShader, ticks);
        drawSwingingClapper(spaceShader, ticks);
        drawEchoRings(spaceShader, ticks);
        drawVerticalEchoLines(spaceShader, ticks);
    }

    private void drawVoidBellCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.060F);

        useAlphaBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.18D, 0.0D);
        GlStateManager.rotate(ticks * 0.18F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.00D, 1.28D, 1.00D);
        drawSpaceSphere(shader, ticks, BELL_RADIUS + pulse * 0.025D, 0.0F,
                0.58F, 0.11F, 0x030006, 0x281044, 0x20D6FF, 0xF5FCFF);
        GlStateManager.popMatrix();

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.17D, 0.0D);
        GlStateManager.scale(1.0D, 0.82D, 1.0D);
        drawSpaceSphere(shader, ticks, AURA_RADIUS + pulse * 0.060D, 3.0F,
                0.16F + pulse * 0.05F, 0.37F, 0x0B0015, 0x462074, 0x39E4FF, 0xFFFFFF);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.20D, 0.0D);
        drawSpaceSphere(shader, ticks, CORE_RADIUS + pulse * 0.030D, 1.0F,
                0.74F + pulse * 0.12F, 0.53F, 0x050008, 0x6E38A8, 0x7EF4FF, 0xFFFFFF);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSwingingClapper(ShaderProgram spaceShader, float ticks) {
        float pulse = wave(ticks * 0.090F);
        double swing = Math.sin(ticks * 0.065D) * 0.115D;
        double x = swing;
        double y = -0.36D + Math.sin(ticks * 0.105D) * 0.018D;
        double z = Math.cos(ticks * 0.047D) * 0.032D;

        useAdditiveBlend();
        drawSpaceLine(spaceShader, ticks, 0.0D, 0.08D, 0.0D, x, y + 0.03D, z,
                0x7EF4FF, 0.20F + pulse * 0.10F, 0.018D, 0.93F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        drawSpaceSphere(spaceShader, ticks, 0.060D + pulse * 0.012D, 1.0F,
                0.86F, 0.79F, 0xE8FDFF, 0x7EF4FF, 0xFFFFFF, 0xFFFFFF);
        drawSpaceSphere(spaceShader, ticks, 0.16D + pulse * 0.035D, 2.0F,
                0.22F + pulse * 0.10F, 0.83F, 0x2C0D55, 0x37DFFF, 0xFFFFFF, 0xFFFFFF);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawEchoRings(ShaderProgram spaceShader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ECHO_RING_COUNT; i++) {
            double progress = fract(ticks * 0.010D + i / (double) ECHO_RING_COUNT);
            double radius = lerp(0.42D, 1.96D, progress);
            double thickness = 0.030D + (1.0D - progress) * 0.026D;
            double y = -0.04D + Math.sin(ticks * 0.025D + i * 1.7D) * 0.035D;
            float fade = (float) Math.sin(Math.PI * progress);
            float alpha = (0.19F + i * 0.018F) * fade * (1.0F - (float) progress * 0.52F);
            int color = i % 2 == 0 ? 0x42E6FF : 0xA67CFF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(4.0F + i * 7.0F, 1.0F, 0.0F, 0.25F);
            GlStateManager.rotate(ticks * (0.024F + i * 0.004F), 0.0F, 1.0F, 0.0F);
            drawSpaceRing(spaceShader, ticks, radius - thickness, radius + thickness,
                    2.0F, alpha * 0.44F, 0.20F + i * 0.13F,
                    0x05000B, color, 0xF5FCFF, 0xFFFFFF);
            drawSpaceCircle(spaceShader, ticks, radius + Math.sin(ticks * 0.075D + i) * 0.018D,
                    color, alpha, RING_SEGMENTS, 0.026D, 1.40F + i * 0.23F);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawVerticalEchoLines(ShaderProgram shader, float ticks) {
        if (shader == null) {
            return;
        }

        useAdditiveBlend();
        for (int i = 0; i < ECHO_LINE_COUNT; i++) {
            double angle = TWO_PI * i / ECHO_LINE_COUNT + ticks * 0.010D;
            double radius = 0.64D + (i % 3) * 0.16D + Math.sin(ticks * 0.030D + i) * 0.035D;
            double height = 0.42D + (i % 4) * 0.10D;
            double baseY = -0.46D + Math.sin(ticks * 0.044D + i * 0.9D) * 0.055D;
            float pulse = wave(ticks * 0.065F + i * 0.72F);
            float alpha = 0.07F + pulse * 0.13F;
            int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x42E6FF : 0x8B5CFF);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            drawSpaceLine(shader, ticks, x, baseY, z, x, baseY + height, z,
                    color, alpha, 0.018D, 2.20F + i * 0.17F);
        }
        useAlphaBlend();
    }

    private static void drawSpaceSphere(ShaderProgram shader, float ticks, double radius, float layer,
                                        float alpha, float seed, int primaryColor, int secondaryColor,
                                        int accentColor, int highlightColor) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || !shader.begin()) {
            return;
        }

        try {
            setSpaceUniforms(shader, ticks, EFFECT_VOID_BELL, layer, alpha, seed,
                    primaryColor, secondaryColor, accentColor, highlightColor);
            drawShaderSphere(radius, SPHERE_SEGMENTS, SPHERE_SEGMENTS);
        } finally {
            shader.end();
        }
    }

    private static void drawSpaceRing(ShaderProgram shader, float ticks, double innerRadius, double outerRadius,
                                      float layer, float alpha, float seed, int primaryColor, int secondaryColor,
                                      int accentColor, int highlightColor) {
        if (shader == null || alpha <= 0.005F || innerRadius <= 0.0D
                || outerRadius <= innerRadius || !shader.begin()) {
            return;
        }

        try {
            setSpaceUniforms(shader, ticks, EFFECT_VOID_BELL, layer, alpha, seed,
                    primaryColor, secondaryColor, accentColor, highlightColor);
            drawRingGeometry(innerRadius, outerRadius, RING_SEGMENTS);
        } finally {
            shader.end();
        }
    }

    private static void drawSpaceLine(ShaderProgram shader, float ticks,
                                      double x1, double y1, double z1,
                                      double x2, double y2, double z2,
                                      int color, float alpha, double width, float seed) {
        if (shader == null || alpha <= 0.005F || !shader.begin()) {
            return;
        }

        try {
            setSpaceUniforms(shader, ticks, EFFECT_VOID_BELL, 3.0F, alpha, seed,
                    0x05000B, color, 0xF5FCFF, 0xFFFFFF);
            RenderHelper.drawTexturedLine(x1, y1, z1, x2, y2, z2, width);
        } finally {
            shader.end();
        }
    }

    private static void drawSpaceCircle(ShaderProgram shader, float ticks, double radius, int color,
                                        float alpha, int segments, double width, float seed) {
        if (shader == null || alpha <= 0.005F || radius <= 0.0D || segments < 3 || !shader.begin()) {
            return;
        }

        try {
            setSpaceUniforms(shader, ticks, EFFECT_VOID_BELL, 3.0F, alpha, seed,
                    0x05000B, color, 0xF5FCFF, 0xFFFFFF);
            RenderHelper.drawTexturedCircle(radius, segments, width);
        } finally {
            shader.end();
        }
    }

    private static void drawRingGeometry(double innerRadius, double outerRadius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            buffer.pos(cos * outerRadius, 0.0D, sin * outerRadius)
                    .tex(progress, 1.0D)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
            buffer.pos(cos * innerRadius, 0.0D, sin * innerRadius)
                    .tex(progress, 0.0D)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void drawShaderSphere(double radius, int latSegs, int lonSegs) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = TWO_PI * lon / lonSegs;
                double phi1 = TWO_PI * (lon + 1) / lonSegs;
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
            }
        }
        tessellator.draw();
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                        double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void setSpaceUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                         float alpha, float seed, int primaryColor, int secondaryColor,
                                         int accentColor, int highlightColor) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.052F + seed * 5.0F);
        shader.setUniform1f("uTime", ticks * 0.035F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform1f("uPulse", pulse);
        setUniformColor(shader, "uPrimaryColor", primaryColor);
        setUniformColor(shader, "uSecondaryColor", secondaryColor);
        setUniformColor(shader, "uAccentColor", accentColor);
        setUniformColor(shader, "uHighlightColor", highlightColor);
    }

    private static void setUniformColor(ShaderProgram shader, String name, int color) {
        float[] rgb = unpackRGB(color);
        shader.setUniform3f(name, rgb[0], rgb[1], rgb[2]);
    }

    private static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }
}
