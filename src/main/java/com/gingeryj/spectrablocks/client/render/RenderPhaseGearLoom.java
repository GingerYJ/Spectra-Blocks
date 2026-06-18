package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TilePhaseGearLoom;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderPhaseGearLoom extends RenderCelestialEffectBase<TilePhaseGearLoom> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int GEAR_SEGMENTS = 96;
    private static final int TOOTH_COUNT = 28;
    private static final int BELT_SEGMENTS = 72;

    @Override
    protected void renderCelestialEffect(TilePhaseGearLoom te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCentralPhaseCore(shader, ticks);
            drawGearSet(shader, ticks);
            drawTransmissionBelts(shader, ticks);
            drawEscapementPulse(shader, ticks);
            drawCalibrationMarks(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("phase gear loom shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCentralPhaseCore(ShaderProgram shader, float ticks) {
        float pulse = smoothPulse(ticks * 0.040D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 0.15F, 0x4AFFF2, 0x7B6DFF, 0xFFFFFF,
                0.22F + pulse * 0.08F, 1.35F, 0.54F);
        drawSphere(0.42D + pulse * 0.045D, 18, 18);
        setTechUniforms(shader, ticks, 6.0F, 0.25F, 0xDFFFFF, 0x4AFFF2, 0xFFFFFF,
                0.18F + pulse * 0.12F, 1.55F, 0.25F);
        drawSphere(0.22D + pulse * 0.030D, 14, 14);
        setTechUniforms(shader, ticks, 6.0F, 0.36F, 0x7FFFF7, 0xDFFFFF, 0xFFFFFF,
                0.20F + pulse * 0.10F, 1.25F, 0.58F);
        RenderHelper.drawTexturedCircle(0.58D + pulse * 0.05D, 72, 0.018D);
        useAlphaBlend();
    }

    private void drawGearSet(ShaderProgram shader, float ticks) {
        drawGear(shader, ticks, 0.86D, 0.10D, 0.080D, 18, 0.0D,
                ticks * 0.010D, 0x46F3FF, 0x6E7DFF, 0.32F, 1.15F, 2.1F);
        drawGear(shader, ticks, 1.34D, -0.16D, 0.070D, TOOTH_COUNT, 31.0D,
                -ticks * 0.007D, 0x927BFF, 0xD9EEFF, 0.24F, 1.08F, 3.7F);
        drawGear(shader, ticks, 1.82D, 0.18D, 0.060D, 36, -39.0D,
                ticks * 0.0048D, 0x2EE7D8, 0x9EAEFF, 0.18F, 1.00F, 5.3F);
    }

    private void drawGear(ShaderProgram shader, float ticks, double radius, double y, double width,
                          int teeth, double tilt, double rotation, int primary, int secondary,
                          float alpha, float intensity, float seed) {
        float pulse = wave(ticks * 0.035D + seed);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, y, 0.0D);
        GlStateManager.rotate((float) tilt, 1.0F, 0.0F, 0.24F);
        GlStateManager.rotate((float) Math.toDegrees(rotation), 0.0F, 1.0F, 0.0F);

        useAlphaBlend();
        setTechUniforms(shader, ticks, 6.0F, seed, primary, secondary, 0xFFFFFF,
                alpha + pulse * 0.05F, intensity, (float) radius);
        drawToothedRing(radius, width, teeth, GEAR_SEGMENTS);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, seed + 0.3F, 0xEFFFFF, primary, 0xFFFFFF,
                alpha * 0.75F + pulse * 0.08F, intensity + 0.20F, (float) radius);
        RenderHelper.drawTexturedCircle(radius * 0.74D, 72, width * 0.40D);
        RenderHelper.drawTexturedCircle(radius * 1.04D, 96, width * 0.34D);
        drawSpokes(radius * 0.22D, radius * 0.82D, Math.max(6, teeth / 4), width * 0.40D);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawTransmissionBelts(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 3; i++) {
            double phase = ticks * (0.010D + i * 0.002D) + i * 1.31D;
            double y = -0.24D + i * 0.24D;
            float pulse = wave(ticks * 0.050D + i);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, y, 0.0D);
            GlStateManager.rotate(52.0F + i * 18.0F, 1.0F, 0.0F, 0.35F);
            setTechUniforms(shader, ticks, 6.0F, 4.0F + i, 0x68FFF2, 0x7A8AFF, 0xFFFFFF,
                    0.11F + pulse * 0.09F, 1.22F, 1.42F);
            drawBeltRibbon(1.10D + i * 0.25D, 0.22D, phase, 0.040D);
            setTechUniforms(shader, ticks, 6.0F, 4.6F + i, 0xFFFFFF, 0x53F4FF, 0xBFD8FF,
                    0.16F + pulse * 0.12F, 1.45F, 1.42F);
            drawMovingBeltPackets(1.10D + i * 0.25D, 0.22D, phase, i);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawEscapementPulse(ShaderProgram shader, float ticks) {
        double phase = fract(ticks * 0.0125D);
        float alpha = (float) Math.sin(phase * Math.PI);
        if (alpha <= 0.02F) {
            return;
        }

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 8.2F, 0xFFFFFF, 0x58FFF1, 0x8D8BFF,
                alpha * 0.36F, 1.55F, 2.12F);
        double radius = 0.78D + phase * 1.42D;
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) (ticks * 0.38D), 0.0F, 1.0F, 0.0F);
        RenderHelper.drawTexturedCircle(radius, 112, 0.030D + alpha * 0.020D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCalibrationMarks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        setTechUniforms(shader, ticks, 6.0F, 9.4F, 0x9DFFF8, 0xFFFFFF, 0x7387FF,
                0.20F + wave(ticks * 0.026D) * 0.08F, 1.15F, 2.32F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) (-ticks * 0.045D), 0.0F, 1.0F, 0.0F);
        drawTicks(2.20D, 0.18D, 48, 0.015D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private static void drawToothedRing(double radius, double width, int teeth, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double toothWave = Math.pow(Math.max(0.0D, Math.sin(progress * teeth * TWO_PI)), 3.0D);
            double inner = radius - width * 0.54D;
            double outer = radius + width * (0.58D + toothWave * 1.35D);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addTexVertex(buffer, cos * outer, 0.0D, sin * outer, progress, 1.0D);
            addTexVertex(buffer, cos * inner, 0.0D, sin * inner, progress, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawSpokes(double innerRadius, double outerRadius, int count, double width) {
        for (int i = 0; i < count; i++) {
            double angle = TWO_PI * i / count;
            RenderHelper.drawTexturedLine(
                    Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                    Math.cos(angle) * outerRadius, 0.0D, Math.sin(angle) * outerRadius,
                    width
            );
        }
    }

    private static void drawTicks(double radius, double length, int count, double width) {
        for (int i = 0; i < count; i++) {
            double angle = TWO_PI * i / count;
            double localLength = length * (i % 4 == 0 ? 1.55D : 1.0D);
            RenderHelper.drawTexturedLine(
                    Math.cos(angle) * (radius - localLength), 0.0D, Math.sin(angle) * (radius - localLength),
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius,
                    width * (i % 4 == 0 ? 1.35D : 1.0D)
            );
        }
    }

    private static void drawBeltRibbon(double radius, double waveHeight, double phase, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= BELT_SEGMENTS; i++) {
            double progress = i / (double) BELT_SEGMENTS;
            double angle = TWO_PI * progress + phase;
            double y = Math.sin(angle * 2.0D + phase) * waveHeight * 0.28D;
            double localRadius = radius + Math.sin(angle * 3.0D) * 0.030D;
            double halfWidth = width * (0.82D + 0.18D * Math.sin(angle * 5.0D));
            double inner = localRadius - halfWidth;
            double outer = localRadius + halfWidth;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addTexVertex(buffer, cos * outer, y, sin * outer, progress, 1.0D);
            addTexVertex(buffer, cos * inner, y, sin * inner, progress, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawMovingBeltPackets(double radius, double waveHeight, double phase, int seed) {
        for (int i = 0; i < 8; i++) {
            double progress = fractStatic(phase * 0.25D + i * 0.125D + seed * 0.07D);
            double angle = TWO_PI * progress + phase;
            double y = Math.sin(angle * 2.0D + phase) * waveHeight * 0.28D;
            double packetRadius = radius + Math.sin(angle * 3.0D) * 0.030D;
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * packetRadius, y, Math.sin(angle) * packetRadius);
            drawSphere(0.022D + (i % 3) * 0.006D, 6, 6);
            GlStateManager.popMatrix();
        }
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

    private static float smoothPulse(double time) {
        double sine = 0.5D + 0.5D * Math.sin(time);
        return (float) (sine * sine * (3.0D - 2.0D * sine));
    }

    private static double fractStatic(double value) {
        return value - Math.floor(value);
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
