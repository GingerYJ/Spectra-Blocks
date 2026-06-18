package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileSoundwaveResonator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSoundwaveResonator extends RenderCelestialEffectBase<TileSoundwaveResonator> {

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double OUTER_WAVE_RADIUS = 2.74D;
    private static final double SPECTRUM_RADIUS = 1.34D;
    private static final int WAVE_RING_COUNT = 7;
    private static final int WAVE_RING_SEGMENTS = 128;
    private static final int SPECTRUM_BAR_COUNT = 32;
    private static final int PEAK_COUNT = 10;
    private static final int CORE_WHITE = 0xF7FFFF;
    private static final int PRESSURE_CYAN = 0x5AF7FF;
    private static final int RESONANCE_BLUE = 0x4C8DFF;
    private static final int OVERTONE_VIOLET = 0xB76DFF;
    private static final int WARM_PEAK = 0xFFE7A8;

    @Override
    protected void renderCelestialEffect(TileSoundwaveResonator te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawPressureShell(shader, ticks);
            drawExpandingWaveRings(shader, ticks);
            drawSpectrumBars(shader, ticks);
            drawResonancePeaks(shader, ticks);
            drawSourceCore(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("soundwave resonator shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawPressureShell(ShaderProgram shader, float ticks) {
        float breath = smoothWave(ticks * 0.037D);
        useAlphaBlend();

        setTechUniforms(shader, ticks, 6.0F, 0.15F, PRESSURE_CYAN, RESONANCE_BLUE, CORE_WHITE,
                0.045F + breath * 0.026F, 0.92F, 2.35F);
        drawShaderSphere(2.15D + breath * 0.16D, 24, 28);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(62.0F + ticks * 0.014F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.030F, 0.0F, 1.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 0.35F, OVERTONE_VIOLET, PRESSURE_CYAN, CORE_WHITE,
                0.055F + breath * 0.020F, 1.00F, 1.58F);
        drawWaveRing(1.56D + breath * 0.04D, 0.032D, 112, ticks * 0.022D, 0.012D);
        GlStateManager.popMatrix();
    }

    private void drawSourceCore(ShaderProgram shader, float ticks) {
        float beat = smoothWave(ticks * 0.125D);
        float shimmer = wave(ticks * 0.310D);
        useAdditiveBlend();

        setTechUniforms(shader, ticks, 6.0F, 1.0F, PRESSURE_CYAN, RESONANCE_BLUE, CORE_WHITE,
                0.19F + beat * 0.13F, 1.32F, 0.46F);
        drawShaderSphere(0.42D + beat * 0.070D, 22, 24);

        setTechUniforms(shader, ticks, 6.0F, 1.2F, CORE_WHITE, PRESSURE_CYAN, WARM_PEAK,
                0.44F + beat * 0.18F, 1.70F, 0.24F);
        drawShaderSphere(0.22D + beat * 0.035D, 20, 22);

        setTechUniforms(shader, ticks, 6.0F, 1.4F, CORE_WHITE, WARM_PEAK, PRESSURE_CYAN,
                0.50F + shimmer * 0.20F, 1.95F, 0.095F);
        drawShaderSphere(0.090D + shimmer * 0.020D, 14, 16);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.58F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        setTechUniforms(shader, ticks, 6.0F, 1.6F, CORE_WHITE, PRESSURE_CYAN, WARM_PEAK,
                0.16F + beat * 0.10F, 1.45F, 0.62F);
        drawWaveRing(0.50D + beat * 0.055D, 0.038D, 72, ticks * 0.070D, 0.006D);
        GlStateManager.popMatrix();

        useAlphaBlend();
    }

    private void drawExpandingWaveRings(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < WAVE_RING_COUNT; i++) {
            double progress = fract(ticks * 0.0108D + i / (double) WAVE_RING_COUNT);
            double radius = lerp(0.34D, OUTER_WAVE_RADIUS, progress);
            double fadeShape = Math.sin(progress * Math.PI);
            float fade = (float) (fadeShape * (1.0D - progress * 0.12D));
            float pulse = wave(ticks * 0.070D + i * 0.73D);
            double width = 0.026D + (1.0D - progress) * 0.046D + pulse * 0.010D;
            double ripple = 0.012D + fade * 0.024D;
            double lift = Math.sin(ticks * 0.044D + i * 1.19D) * 0.045D;
            int primary = i % 3 == 0 ? PRESSURE_CYAN : (i % 3 == 1 ? RESONANCE_BLUE : OVERTONE_VIOLET);
            int secondary = i % 2 == 0 ? CORE_WHITE : PRESSURE_CYAN;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, lift, 0.0D);
            GlStateManager.rotate((float) (Math.sin(ticks * 0.018D + i) * 8.0D), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) (ticks * 0.035D + i * 11.0D), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (Math.cos(ticks * 0.015D + i * 0.6D) * 5.0D), 0.0F, 0.0F, 1.0F);
            setTechUniforms(shader, ticks + i * 3.7F, 6.0F, 2.0F + i * 0.16F,
                    primary, secondary, WARM_PEAK,
                    0.060F + fade * 0.25F, 1.18F + fade * 0.42F, (float) radius);
            drawWaveRing(radius, width, WAVE_RING_SEGMENTS, ticks * 0.034D + i * 0.41D, ripple);
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < 3; i++) {
            double progress = fract(ticks * 0.0072D + i * 0.333D);
            double radius = lerp(0.52D, 2.18D, progress);
            float fade = (float) Math.sin(progress * Math.PI);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(72.0F + i * 31.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) (ticks * (0.020D + i * 0.006D) + i * 43.0D), 0.0F, 1.0F, 0.0F);
            setTechUniforms(shader, ticks, 6.0F, 3.2F + i * 0.18F,
                    i == 1 ? OVERTONE_VIOLET : PRESSURE_CYAN, CORE_WHITE, RESONANCE_BLUE,
                    0.040F + fade * 0.14F, 1.15F, (float) radius);
            drawWaveRing(radius, 0.024D + fade * 0.018D, 96, ticks * 0.050D + i, 0.014D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawSpectrumBars(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < SPECTRUM_BAR_COUNT; i++) {
            double band = i / (double) SPECTRUM_BAR_COUNT;
            double angle = TWO_PI * band + ticks * 0.006D;
            double overtone = Math.sin(ticks * (0.058D + (i % 5) * 0.004D) + i * 0.61D);
            double carrier = Math.sin(ticks * 0.018D + i * 0.27D);
            double heightSignal = 0.5D + 0.5D * (overtone * 0.78D + carrier * 0.22D);
            heightSignal = Math.max(0.0D, Math.min(1.0D, heightSignal));
            float pulse = (float) Math.pow(heightSignal, 1.35D);
            double height = 0.24D + pulse * (0.82D + 0.20D * Math.sin(i * 0.37D));
            double width = 0.045D + pulse * 0.020D;
            double depth = 0.030D + pulse * 0.014D;
            double radius = SPECTRUM_RADIUS + Math.sin(i * 1.7D) * 0.055D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double baseY = -0.66D + Math.sin(ticks * 0.026D + i) * 0.030D;
            double lean = Math.sin(ticks * 0.042D + i * 0.83D) * (0.030D + pulse * 0.035D);
            int primary = i % 4 == 0 ? WARM_PEAK : (i % 3 == 0 ? OVERTONE_VIOLET : PRESSURE_CYAN);
            int secondary = i % 2 == 0 ? RESONANCE_BLUE : CORE_WHITE;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, baseY, z);
            GlStateManager.rotate((float) Math.toDegrees(-angle) + 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (Math.sin(i * 0.47D) * 10.0D), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate((float) (Math.cos(i * 0.31D + ticks * 0.012D) * 7.0D), 1.0F, 0.0F, 0.0F);
            setTechUniforms(shader, ticks + i * 0.19F, 6.0F, 4.0F + (float) band,
                    primary, secondary, CORE_WHITE,
                    0.080F + pulse * 0.25F, 1.16F + pulse * 0.34F, (float) height);
            drawSpectrumBar(width, height, depth, lean);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawResonancePeaks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < PEAK_COUNT; i++) {
            double angle = i * GOLDEN_ANGLE + ticks * 0.014D;
            double lane = i % 4;
            double radius = 0.55D + lane * 0.43D + Math.sin(ticks * 0.030D + i) * 0.035D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = -0.16D + Math.sin(ticks * 0.053D + i * 0.92D) * 0.48D;
            float pulse = (float) Math.pow(wave(ticks * 0.115D + i * 1.31D), 2.7D);
            double size = 0.030D + pulse * 0.060D;
            int primary = i % 3 == 0 ? WARM_PEAK : CORE_WHITE;
            int secondary = i % 2 == 0 ? PRESSURE_CYAN : OVERTONE_VIOLET;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            setTechUniforms(shader, ticks + i * 0.42F, 6.0F, 5.0F + i * 0.10F,
                    primary, secondary, CORE_WHITE,
                    0.16F + pulse * 0.50F, 1.42F + pulse * 0.42F, (float) size);
            drawShaderSphere(size, 8, 10);

            setTechUniforms(shader, ticks + i * 0.42F, 6.0F, 5.5F + i * 0.08F,
                    secondary, primary, CORE_WHITE,
                    0.050F + pulse * 0.16F, 1.22F, (float) size);
            drawShaderSphere(size * (2.10D + pulse * 0.50D), 8, 10);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawWaveRing(double radius, double width, int segments, double phase, double ripple) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            double angle0 = TWO_PI * u0;
            double angle1 = TWO_PI * u1;
            double radius0 = radius + Math.sin(angle0 * 6.0D + phase) * ripple;
            double radius1 = radius + Math.sin(angle1 * 6.0D + phase) * ripple;
            double y0 = Math.sin(angle0 * 5.0D - phase * 1.7D) * ripple * 0.55D;
            double y1 = Math.sin(angle1 * 5.0D - phase * 1.7D) * ripple * 0.55D;
            addRingSegment(buffer, radius0, radius1, y0, y1, angle0, angle1, width, u0, u1);
        }

        tessellator.draw();
    }

    private static void drawSpectrumBar(double width, double height, double depth, double lean) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        double halfWidth = width * 0.5D;
        double halfDepth = depth * 0.5D;
        double x0 = -halfWidth;
        double x1 = halfWidth;
        double z0 = -halfDepth;
        double z1 = halfDepth;
        double topX0 = -halfWidth + lean;
        double topX1 = halfWidth + lean;

        addQuad(buffer, x0, 0.0D, z1, x1, 0.0D, z1, topX1, height, z1, topX0, height, z1,
                0.0D, 0.0D, 1.0D);
        addQuad(buffer, x1, 0.0D, z0, x0, 0.0D, z0, topX0, height, z0, topX1, height, z0,
                0.0D, 0.0D, -1.0D);
        addQuad(buffer, x0, 0.0D, z0, x0, 0.0D, z1, topX0, height, z1, topX0, height, z0,
                -1.0D, 0.0D, 0.0D);
        addQuad(buffer, x1, 0.0D, z1, x1, 0.0D, z0, topX1, height, z0, topX1, height, z1,
                1.0D, 0.0D, 0.0D);
        addQuad(buffer, topX0, height, z1, topX1, height, z1, topX1, height, z0, topX0, height, z0,
                0.0D, 1.0D, 0.0D);

        tessellator.draw();
    }

    private static void drawShaderSphere(double radius, int latitudeSegments, int longitudeSegments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int lat = 0; lat < latitudeSegments; lat++) {
            double theta0 = Math.PI * lat / latitudeSegments;
            double theta1 = Math.PI * (lat + 1.0D) / latitudeSegments;
            double y0 = Math.cos(theta0) * radius;
            double y1 = Math.cos(theta1) * radius;
            double ring0 = Math.sin(theta0) * radius;
            double ring1 = Math.sin(theta1) * radius;
            double v0 = lat / (double) latitudeSegments;
            double v1 = (lat + 1.0D) / latitudeSegments;

            for (int lon = 0; lon < longitudeSegments; lon++) {
                double u0 = lon / (double) longitudeSegments;
                double u1 = (lon + 1.0D) / longitudeSegments;
                double phi0 = TWO_PI * u0;
                double phi1 = TWO_PI * u1;
                double x00 = Math.cos(phi0) * ring0;
                double z00 = Math.sin(phi0) * ring0;
                double x01 = Math.cos(phi1) * ring0;
                double z01 = Math.sin(phi1) * ring0;
                double x10 = Math.cos(phi0) * ring1;
                double z10 = Math.sin(phi0) * ring1;
                double x11 = Math.cos(phi1) * ring1;
                double z11 = Math.sin(phi1) * ring1;

                addSphereVertex(buffer, x00, y0, z00, radius, u0, v0);
                addSphereVertex(buffer, x10, y1, z10, radius, u0, v1);
                addSphereVertex(buffer, x11, y1, z11, radius, u1, v1);
                addSphereVertex(buffer, x00, y0, z00, radius, u0, v0);
                addSphereVertex(buffer, x11, y1, z11, radius, u1, v1);
                addSphereVertex(buffer, x01, y0, z01, radius, u1, v0);
            }
        }

        tessellator.draw();
    }

    private static void addRingSegment(BufferBuilder buffer,
                                       double radius0, double radius1,
                                       double y0, double y1,
                                       double angle0, double angle1,
                                       double width, double u0, double u1) {
        double inner0 = Math.max(0.001D, radius0 - width * 0.5D);
        double outer0 = radius0 + width * 0.5D;
        double inner1 = Math.max(0.001D, radius1 - width * 0.5D);
        double outer1 = radius1 + width * 0.5D;
        double c0 = Math.cos(angle0);
        double s0 = Math.sin(angle0);
        double c1 = Math.cos(angle1);
        double s1 = Math.sin(angle1);

        addVertex(buffer, c0 * inner0, y0, s0 * inner0, u0, 0.0D, 0.0D, 1.0D, 0.0D);
        addVertex(buffer, c0 * outer0, y0, s0 * outer0, u0, 1.0D, 0.0D, 1.0D, 0.0D);
        addVertex(buffer, c1 * outer1, y1, s1 * outer1, u1, 1.0D, 0.0D, 1.0D, 0.0D);
        addVertex(buffer, c0 * inner0, y0, s0 * inner0, u0, 0.0D, 0.0D, 1.0D, 0.0D);
        addVertex(buffer, c1 * outer1, y1, s1 * outer1, u1, 1.0D, 0.0D, 1.0D, 0.0D);
        addVertex(buffer, c1 * inner1, y1, s1 * inner1, u1, 0.0D, 0.0D, 1.0D, 0.0D);
    }

    private static void addQuad(BufferBuilder buffer,
                                double x0, double y0, double z0,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                double x3, double y3, double z3,
                                double normalX, double normalY, double normalZ) {
        addVertex(buffer, x0, y0, z0, 0.0D, 0.0D, normalX, normalY, normalZ);
        addVertex(buffer, x1, y1, z1, 1.0D, 0.0D, normalX, normalY, normalZ);
        addVertex(buffer, x2, y2, z2, 1.0D, 1.0D, normalX, normalY, normalZ);
        addVertex(buffer, x0, y0, z0, 0.0D, 0.0D, normalX, normalY, normalZ);
        addVertex(buffer, x2, y2, z2, 1.0D, 1.0D, normalX, normalY, normalZ);
        addVertex(buffer, x3, y3, z3, 0.0D, 1.0D, normalX, normalY, normalZ);
    }

    private static void addSphereVertex(BufferBuilder buffer, double x, double y, double z,
                                        double radius, double u, double v) {
        double invRadius = radius <= 0.0001D ? 1.0D : 1.0D / radius;
        addVertex(buffer, x, y, z, u, v, x * invRadius, y * invRadius, z * invRadius);
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z,
                                  double u, double v,
                                  double normalX, double normalY, double normalZ) {
        double length = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length < 0.0001D) {
            normalX = 0.0D;
            normalY = 1.0D;
            normalZ = 0.0D;
            length = 1.0D;
        }

        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) (normalX / length), (float) (normalY / length), (float) (normalZ / length))
                .endVertex();
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

    private static float smoothWave(double phase) {
        float value = wave(phase);
        return value * value * (3.0F - 2.0F * value);
    }
}
