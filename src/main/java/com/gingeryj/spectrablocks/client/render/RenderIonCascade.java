package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileIonCascade;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderIonCascade extends RenderCelestialEffectBase<TileIonCascade> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int STREAM_COUNT = 18;
    private static final int IMPACT_MOTES = 20;
    private static final int RING_SEGMENTS = 80;

    @Override
    protected void renderCelestialEffect(TileIonCascade te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCoreColumn(shader, ticks);
            drawIonStreams(shader, ticks);
            drawScanRings(shader, ticks);
            drawImpactMotes(shader, ticks);
            drawCollectorPlate(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("ion cascade shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCoreColumn(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.060D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 1.2F, 0x46D7FF, 0x45FF9D, 0xEFFFFF,
                0.18F + pulse * 0.10F, 1.35F, 1.92F);
        drawEnergyColumn(0.16D + pulse * 0.025D, 2.16D + pulse * 0.20D, 32);
        setTechUniforms(shader, ticks, 6.0F, 1.4F, 0xEFFFFF, 0x46D7FF, 0x45FF9D,
                0.12F + pulse * 0.08F, 1.52F, 0.72F);
        drawSphere(0.30D + pulse * 0.025D, 16, 16);
        useAlphaBlend();
    }

    private void drawIonStreams(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STREAM_COUNT; i++) {
            double baseAngle = i * TWO_PI / STREAM_COUNT;
            double radius = 0.62D + (i % 4) * 0.16D;
            double phase = fract(ticks * (0.026D + (i % 3) * 0.002D) + i * 0.061D);
            double yTop = 1.16D;
            double yBottom = -1.18D;
            double y = lerp(yTop, yBottom, phase);
            double twist = ticks * 0.018D + phase * 1.8D + i * 0.22D;
            double angle = baseAngle + twist;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            float fade = (float) Math.sin(phase * Math.PI);
            int color = i % 3 == 0 ? 0x45FF9D : (i % 3 == 1 ? 0x46D7FF : 0x245CFF);

            setTechUniforms(shader, ticks, 6.0F, 2.0F + i * 0.03F, color, 0xEFFFFF, 0x45FF9D,
                    0.09F + fade * 0.22F, 1.24F, (float) radius);
            drawVerticalRibbon(x, yTop, z, x * 0.68D, yBottom, z * 0.68D,
                    0.014D + (i % 3) * 0.004D, phase);

            setTechUniforms(shader, ticks, 6.0F, 3.4F, 0xEFFFFF, color, 0x45FF9D,
                    0.12F + fade * 0.32F, 1.48F, 0.04F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x * (0.86D - phase * 0.18D), y, z * (0.86D - phase * 0.18D));
            drawSphere(0.024D + (i % 4) * 0.004D, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawScanRings(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 4; i++) {
            double phase = fract(ticks * (0.010D + i * 0.0015D) + i * 0.25D);
            double y = lerp(1.02D, -1.04D, phase);
            double radius = 0.70D + i * 0.20D + Math.sin(ticks * 0.026D + i) * 0.035D;
            float alpha = 0.12F + (float) Math.sin(phase * Math.PI) * 0.20F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate((float) (ticks * (0.10D + i * 0.035D)), 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 6.0F, 5.0F + i, i % 2 == 0 ? 0x46D7FF : 0x45FF9D,
                    0xEFFFFF, 0x245CFF, alpha, 1.22F, (float) radius);
            RenderHelper.drawTexturedCircle(radius, RING_SEGMENTS, 0.024D + i * 0.004D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawImpactMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < IMPACT_MOTES; i++) {
            double phase = fract(ticks * 0.018D + i * 0.087D);
            double angle = i * 2.399963229728653D + ticks * 0.014D;
            double radius = lerp(0.24D, 1.12D, phase);
            double y = -1.02D + Math.sin(phase * Math.PI) * 0.18D;
            float alpha = (float) Math.sin(phase * Math.PI) * 0.42F;
            int color = i % 2 == 0 ? 0x45FF9D : 0x46D7FF;

            setTechUniforms(shader, ticks, 6.0F, 6.0F, color, 0xEFFFFF, 0xFFFFFF,
                    0.10F + alpha, 1.45F, 0.04F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            drawSphere(0.018D + (i % 4) * 0.005D, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawCollectorPlate(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.052D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -1.08D, 0.0D);
        GlStateManager.rotate((float) (-ticks * 0.12D), 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 7.8F, 0x245CFF, 0x45FF9D, 0xEFFFFF,
                0.16F + pulse * 0.10F, 1.30F, 1.12F);
        drawFlatRing(0.34D, 1.14D, 96);
        setTechUniforms(shader, ticks, 6.0F, 8.2F, 0xEFFFFF, 0x46D7FF, 0x45FF9D,
                0.20F + pulse * 0.12F, 1.42F, 0.82F);
        RenderHelper.drawTexturedCircle(0.82D + pulse * 0.035D, 88, 0.032D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private static void drawEnergyColumn(double radius, double height, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double halfHeight = height * 0.5D;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            addTexVertex(buffer, x, -halfHeight, z, progress, 0.0D);
            addTexVertex(buffer, x, halfHeight, z, progress, 1.0D);
        }
        tessellator.draw();
    }

    private static void drawVerticalRibbon(double x0, double y0, double z0, double x1, double y1, double z1,
                                           double width, double phase) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        int segments = 5;
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double x = lerpStatic(x0, x1, progress);
            double y = lerpStatic(y0, y1, progress);
            double z = lerpStatic(z0, z1, progress);
            double sway = Math.sin(progress * Math.PI * 2.0D + phase * TWO_PI) * 0.040D;
            double sideX = -z;
            double sideZ = x;
            double sideLength = Math.sqrt(sideX * sideX + sideZ * sideZ);
            if (sideLength < 0.0001D) {
                sideX = 1.0D;
                sideZ = 0.0D;
                sideLength = 1.0D;
            }
            sideX = sideX / sideLength * width;
            sideZ = sideZ / sideLength * width;
            addTexVertex(buffer, x + sideX, y, z + sideZ + sway, progress, 1.0D);
            addTexVertex(buffer, x - sideX, y, z - sideZ + sway, progress, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawFlatRing(double innerRadius, double outerRadius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addTexVertex(buffer, cos * outerRadius, 0.0D, sin * outerRadius, progress, 1.0D);
            addTexVertex(buffer, cos * innerRadius, 0.0D, sin * innerRadius, progress, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawSphere(double radius, int latSegs, int lonSegs) {
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

    private static void addTexVertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z).tex(u, v).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private static double lerpStatic(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static void setTechUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                        int primary, int secondary, int tertiary,
                                        float alpha, float intensity, float scale) {
        shader.setUniform1f("uTime", ticks * 0.040F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uScale", scale);
        setColor(shader, "uPrimaryColor", primary);
        setColor(shader, "uSecondaryColor", secondary);
        setColor(shader, "uTertiaryColor", tertiary);
    }

    private static void setColor(ShaderProgram shader, String uniform, int color) {
        shader.setUniform3f(uniform,
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F);
    }
}
