package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileChromaticVortex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderChromaticVortex extends RenderCelestialEffectBase<TileChromaticVortex> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int[] SPECTRUM = {
            0xFF4D6D, 0xFF9B3D, 0xFFE85A, 0x7DFF82, 0x4FEAFF, 0x7384FF, 0xD774FF
    };
    private static final int SPIRAL_SEGMENTS = 68;
    private static final int CHROMA_MOTES = 21;

    @Override
    protected void renderCelestialEffect(TileChromaticVortex te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCore(shader, ticks);
            drawSpiralArms(shader, ticks);
            drawOuterRing(shader, ticks);
            drawChromaticMotes(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("chromatic vortex shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.060D);

        useAlphaBlend();
        setTechUniforms(shader, ticks, 3.0F, 0.0F, 0xFFFFFF, 0x4FEAFF, 0xD774FF,
                0.36F + pulse * 0.10F, 1.28F, 0.72F);
        drawSphere(0.72D + pulse * 0.035D, 20, 20);
        useAdditiveBlend();
        setTechUniforms(shader, ticks, 3.0F, 0.4F, 0xFFFFFF, 0xFFE85A, 0x4FEAFF,
                0.42F + pulse * 0.16F, 1.65F, 0.30F);
        drawSphere(0.30D + pulse * 0.025D, 18, 18);
        useAlphaBlend();
    }

    private void drawSpiralArms(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPECTRUM.length; i++) {
            int color = SPECTRUM[i];
            int nextColor = SPECTRUM[(i + 2) % SPECTRUM.length];
            float pulse = wave(ticks * 0.040D + i * 0.37D);

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (ticks * 0.070D + i * 360.0D / SPECTRUM.length),
                    0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(18.0F + i * 3.0F, 1.0F, 0.0F, 0.16F);
            setTechUniforms(shader, ticks, 3.0F, 1.0F + i * 0.18F, color, nextColor, 0xFFFFFF,
                    0.17F + pulse * 0.10F, 1.36F, 2.65F);
            drawSpiralRibbon(0.48D, 2.65D, i * 0.22D, TWO_PI * 1.55D,
                    0.040D + (i % 3) * 0.006D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawOuterRing(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) (-ticks * 0.032D), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(66.0F, 1.0F, 0.0F, 0.20F);
        setTechUniforms(shader, ticks, 3.0F, 4.2F, 0xFFFFFF, 0x4FEAFF, 0xD774FF,
                0.22F + wave(ticks * 0.032D) * 0.10F, 1.35F, 2.85F);
        RenderHelper.drawTexturedCircle(2.85D, 112, 0.036D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawChromaticMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CHROMA_MOTES; i++) {
            double progress = fract(ticks * 0.010D + i * 0.053D);
            double angle = i * 2.399963229728653D + ticks * 0.018D + progress * 1.5D;
            double radius = 0.58D + (i % 7) * 0.26D + Math.sin(ticks * 0.020D + i) * 0.04D;
            double y = Math.sin(angle * 1.3D + ticks * 0.012D) * 0.54D;
            float alpha = 0.14F + (float) Math.sin(progress * Math.PI) * 0.34F;
            int color = SPECTRUM[i % SPECTRUM.length];

            setTechUniforms(shader, ticks, 3.0F, 5.0F, color, 0xFFFFFF, SPECTRUM[(i + 3) % SPECTRUM.length],
                    alpha, 1.45F, 0.04F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            drawSphere(0.018D + (i % 4) * 0.006D, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawSpiralRibbon(double startRadius, double endRadius,
                                         double startAngle, double sweep, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= SPIRAL_SEGMENTS; i++) {
            double progress = i / (double) SPIRAL_SEGMENTS;
            double angle = startAngle + sweep * progress;
            double radius = startRadius + (endRadius - startRadius) * progress;
            double y = Math.sin(progress * Math.PI * 2.0D + startAngle) * 0.18D;
            double halfWidth = width * (1.0D - progress * 0.42D);
            double inner = Math.max(0.0D, radius - halfWidth);
            double outer = radius + halfWidth;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addTexVertex(buffer, cos * outer, y, sin * outer, progress, 1.0D);
            addTexVertex(buffer, cos * inner, -y * 0.35D, sin * inner, progress, 0.0D);
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
