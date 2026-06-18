package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileAlchemyTransmutationRing;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderAlchemyTransmutationRing extends RenderCelestialEffectBase<TileAlchemyTransmutationRing> {

    private static final String ARCANE_SHADER = "arcane_effect";
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final double GROUND_Y = -0.455D;
    private static final int RING_SEGMENTS = 160;
    private static final int RUNE_COUNT = 28;
    private static final int MOTE_COUNT = 22;
    private static final float OUTER_ROTATION_SPEED = 0.20F;
    private static final float INNER_ROTATION_SPEED = -0.34F;
    private static final float RUNE_SCROLL_SPEED = 0.014F;

    @Override
    protected void renderCelestialEffect(TileAlchemyTransmutationRing te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram(ARCANE_SHADER);
        if (shader == null) {
            return;
        }

        try {
            drawGroundCircles(shader, ticks);
            drawRotatingAlchemyFrames(shader, ticks);
            drawRuneCarousel(shader, ticks);
            drawCenterCore(shader, ticks);
            drawRisingMotes(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("alchemy transmutation ring render failed: " + ex.getMessage());
        }
    }

    private void drawGroundCircles(ShaderProgram shader, float ticks) {
        float slowPulse = wave(ticks * 0.032F);
        float fastPulse = wave(ticks * 0.061F + 1.7F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y, 0.0D);
        useAlphaBlend();
        ArcaneShaderEffectRenderer.drawFlatRingLayer(shader, ticks,
                0.18D, 0.34D, 0xE6F7D6, 0xFFE6A3,
                0.20F + slowPulse * 0.08F, 1.0F, 18.0F, 3.0F, slowPulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks,
                0.82D, 0.050D, 0xD9B34C, 0xFFF1B8,
                0.24F + slowPulse * 0.09F, 1.12F, 20.0F, 9.0F, slowPulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks,
                1.42D, 0.058D, 0x1EBB78, 0xC9FFDC,
                0.22F + fastPulse * 0.10F, 1.18F, 22.0F, 15.0F, fastPulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks,
                2.08D, 0.070D, 0xFFC75F, 0xFFF6CE,
                0.20F + slowPulse * 0.12F, 1.22F, 24.0F, 21.0F, slowPulse, RING_SEGMENTS);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks,
                2.78D, 0.090D, 0x2AE08E, 0xF4FFD7,
                0.17F + fastPulse * 0.11F, 1.24F, 26.0F, 28.0F, fastPulse, RING_SEGMENTS);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks,
                1.08D, 0.24D, 0.022D, 12, 0xFFE88A, 0xFFFFFF,
                0.31F + slowPulse * 0.14F, 1.35F, 30.0F, 35.0F, slowPulse);
        ArcaneShaderEffectRenderer.drawRadialMarksLayer(shader, ticks,
                2.43D, 0.34D, 0.028D, 24, 0x7DFFC2, 0xFFF5BC,
                0.22F + fastPulse * 0.16F, 1.28F, 34.0F, 42.0F, fastPulse);
        useAlphaBlend();
        GlStateManager.popMatrix();
    }

    private void drawRotatingAlchemyFrames(ShaderProgram shader, float ticks) {
        float trianglePulse = wave(ticks * 0.044F + 0.4F);
        float hexPulse = wave(ticks * 0.037F + 2.1F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y + 0.012D, 0.0D);
        GlStateManager.rotate(ticks * OUTER_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        drawPolygonRibbonLayer(shader, ticks, 6, 2.40D, 0.060D, 0.0D,
                0xD6A84D, 0xFFF0B0, 0.25F + hexPulse * 0.12F,
                1.25F, 24.0F, 51.0F, hexPulse);
        drawPolygonRibbonLayer(shader, ticks, 3, 1.74D, 0.058D, Math.PI / 2.0D,
                0x2DE58F, 0xD7FFE6, 0.27F + trianglePulse * 0.14F,
                1.35F, 26.0F, 58.0F, trianglePulse);
        drawVertexCaps(shader, ticks, 3, 1.74D, Math.PI / 2.0D, 0xD7FFE6, 0xFFFFFF,
                0.22F + trianglePulse * 0.18F, 63.0F);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y + 0.030D, 0.0D);
        GlStateManager.rotate(ticks * INNER_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        drawPolygonRibbonLayer(shader, ticks, 3, 1.14D, 0.048D, -Math.PI / 2.0D,
                0xFFE083, 0xFFFFFF, 0.30F + trianglePulse * 0.13F,
                1.45F, 30.0F, 71.0F, trianglePulse);
        drawPolygonRibbonLayer(shader, ticks, 6, 0.63D, 0.042D, Math.PI / 6.0D,
                0x63FFC0, 0xFFF2B8, 0.21F + hexPulse * 0.13F,
                1.25F, 22.0F, 77.0F, hexPulse);
        drawAlchemyQuadSpokes(shader, ticks, 6, 0.42D, 1.02D, 0.030D,
                0xB8FFD0, 0xFFF2B8, 0.18F + hexPulse * 0.12F, 83.0F, hexPulse);
        GlStateManager.popMatrix();
    }

    private void drawRuneCarousel(ShaderProgram shader, float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y + 0.045D, 0.0D);

        double head = fract(ticks * RUNE_SCROLL_SPEED);
        double segmentStep = TWO_PI / RUNE_COUNT;
        for (int i = 0; i < RUNE_COUNT; i++) {
            double runePosition = (double) i / RUNE_COUNT;
            double distance = circularDistance(runePosition, head);
            float leadGlow = (float) Math.max(0.0D, 1.0D - distance / 0.115D);
            float echoGlow = (float) Math.max(0.0D, 1.0D - circularDistance(runePosition, fract(head + 0.37D)) / 0.065D);
            float pulse = wave(ticks * 0.052F + i * 0.63F);
            float alpha = 0.08F + leadGlow * 0.45F + echoGlow * 0.18F + pulse * 0.045F;
            int primary = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE38A : 0x55FFB3);
            int secondary = i % 2 == 0 ? 0xFFF7CF : 0xD8FFE5;

            drawArcRibbonLayer(shader, ticks, 2.16D, 0.070D,
                    i * segmentStep + ticks * 0.0025D, segmentStep * 0.48D,
                    primary, secondary, alpha, 1.52F, 38.0F,
                    91.0F + i * 1.7F, Math.max(leadGlow, pulse), 4);
        }

        double innerHead = fract(1.0D - ticks * RUNE_SCROLL_SPEED * 1.45D);
        for (int i = 0; i < 14; i++) {
            double runePosition = (double) i / 14.0D;
            float glow = (float) Math.max(0.0D, 1.0D - circularDistance(runePosition, innerHead) / 0.13D);
            float pulse = wave(ticks * 0.066F + i);
            drawArcRibbonLayer(shader, ticks, 1.30D, 0.052D,
                    i * TWO_PI / 14.0D - ticks * 0.004D, TWO_PI / 14.0D * 0.34D,
                    0x7DFFC2, 0xFFF2B8, 0.10F + glow * 0.36F + pulse * 0.06F,
                    1.40F, 32.0F, 121.0F + i * 2.0F, Math.max(glow, pulse), 3);
        }

        GlStateManager.popMatrix();
    }

    private void drawCenterCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.070F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y + 0.18D + pulse * 0.018D, 0.0D);
        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.20D + pulse * 0.025D, 0xFFF7CE, 0xFFFFFF,
                0.66F + pulse * 0.16F, 1.72F, 24.0F, 131.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 18, 18);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.42D + pulse * 0.050D, 0x24D887, 0xFFE59B,
                0.18F + pulse * 0.10F, 1.38F, 18.0F, 137.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 20, 20);
        useAlphaBlend();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, GROUND_Y + 0.058D, 0.0D);
        drawArcRibbonLayer(shader, ticks, 0.48D, 0.036D, ticks * 0.016D, Math.PI * 1.35D,
                0xFFE08A, 0xFFFFFF, 0.26F + pulse * 0.14F,
                1.50F, 30.0F, 143.0F, pulse, 18);
        drawArcRibbonLayer(shader, ticks, 0.58D, 0.030D, -ticks * 0.019D + Math.PI, Math.PI * 1.10D,
                0x5DFFB6, 0xFFF6C9, 0.20F + pulse * 0.12F,
                1.36F, 26.0F, 149.0F, pulse, 16);
        GlStateManager.popMatrix();
    }

    private void drawRisingMotes(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < MOTE_COUNT; i++) {
            double progress = fract(ticks * (0.009D + (i % 5) * 0.0012D) + i * 0.137D);
            double fade = Math.sin(Math.PI * progress);
            double angle = i * GOLDEN_ANGLE + ticks * 0.010D + Math.sin(progress * TWO_PI + i) * 0.12D;
            double radius = 0.46D + (i % 7) * 0.245D + Math.sin(i * 1.31D) * 0.050D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = GROUND_Y + 0.08D + progress * (0.86D + (i % 4) * 0.075D);
            double size = 0.020D + fade * 0.026D + (i % 3) * 0.003D;
            float alpha = (float) (fade * 0.48D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, size,
                    i % 3 == 0 ? 0xFFF4B8 : 0x6DFFC0, 0xFFFFFF,
                    0.14F + alpha, 1.55F, 16.0F, 160.0F + i * 5.0F, (float) fade,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 7, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawVertexCaps(ShaderProgram shader, float ticks, int sides, double radius, double rotation,
                                int primaryColor, int secondaryColor, float alpha, float seed) {
        useAdditiveBlend();
        for (int i = 0; i < sides; i++) {
            double angle = rotation + TWO_PI * i / sides;
            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, 0.012D, Math.sin(angle) * radius);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks, 0.050D,
                    primaryColor, secondaryColor, alpha, 1.35F, 18.0F, seed + i * 3.0F, alpha,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 6, 6);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawAlchemyQuadSpokes(ShaderProgram shader, float ticks, int spokes,
                                       double innerRadius, double outerRadius, double halfWidth,
                                       int primaryColor, int secondaryColor, float alpha,
                                       float seed, float pulse) {
        if (!ArcaneShaderEffectRenderer.beginLayer(shader, ticks, ArcaneShaderEffectRenderer.LAYER_RING,
                primaryColor, secondaryColor, alpha, 1.18F, 24.0F, seed, pulse)) {
            return;
        }

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i < spokes; i++) {
                double angle = TWO_PI * i / spokes + ticks * 0.0015D;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double tangentX = -sin * halfWidth;
                double tangentZ = cos * halfWidth;
                addFlatQuad(buffer,
                        cos * innerRadius + tangentX, sin * innerRadius + tangentZ, 0.0D, 0.0D,
                        cos * outerRadius + tangentX, sin * outerRadius + tangentZ, 0.0D, 1.0D,
                        cos * outerRadius - tangentX, sin * outerRadius - tangentZ, 1.0D, 1.0D,
                        cos * innerRadius - tangentX, sin * innerRadius - tangentZ, 1.0D, 0.0D);
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private void drawPolygonRibbonLayer(ShaderProgram shader, float ticks, int sides,
                                        double radius, double width, double rotation,
                                        int primaryColor, int secondaryColor, float alpha,
                                        float intensity, float noiseScale, float seed, float pulse) {
        if (sides < 3 || radius <= 0.0D || width <= 0.0D
                || !ArcaneShaderEffectRenderer.beginLayer(shader, ticks, ArcaneShaderEffectRenderer.LAYER_RING,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawPolygonRibbonGeometry(sides, radius, width, rotation);
        } finally {
            shader.end();
        }
    }

    private void drawPolygonRibbonGeometry(int sides, double radius, double width, double rotation) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        double outerRadius = radius + width * 0.5D;
        double innerRadius = Math.max(0.0D, radius - width * 0.5D);
        for (int i = 0; i < sides; i++) {
            double progress0 = (double) i / sides;
            double progress1 = (double) (i + 1) / sides;
            double angle0 = rotation + TWO_PI * progress0;
            double angle1 = rotation + TWO_PI * progress1;
            addFlatQuad(buffer,
                    Math.cos(angle0) * outerRadius, Math.sin(angle0) * outerRadius, progress0, 1.0D,
                    Math.cos(angle1) * outerRadius, Math.sin(angle1) * outerRadius, progress1, 1.0D,
                    Math.cos(angle1) * innerRadius, Math.sin(angle1) * innerRadius, progress1, 0.0D,
                    Math.cos(angle0) * innerRadius, Math.sin(angle0) * innerRadius, progress0, 0.0D);
        }

        tessellator.draw();
    }

    private void drawArcRibbonLayer(ShaderProgram shader, float ticks, double radius, double width,
                                    double startAngle, double sweepAngle, int primaryColor, int secondaryColor,
                                    float alpha, float intensity, float noiseScale, float seed,
                                    float pulse, int segments) {
        if (radius <= 0.0D || width <= 0.0D || segments <= 0
                || !ArcaneShaderEffectRenderer.beginLayer(shader, ticks, ArcaneShaderEffectRenderer.LAYER_RING,
                primaryColor, secondaryColor, alpha, intensity, noiseScale, seed, pulse)) {
            return;
        }

        try {
            drawArcRibbonGeometry(radius, width, startAngle, sweepAngle, segments);
        } finally {
            shader.end();
        }
    }

    private void drawArcRibbonGeometry(double radius, double width, double startAngle,
                                       double sweepAngle, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        double innerRadius = Math.max(0.0D, radius - width * 0.5D);
        double outerRadius = radius + width * 0.5D;
        for (int i = 0; i < segments; i++) {
            double progress0 = (double) i / segments;
            double progress1 = (double) (i + 1) / segments;
            double angle0 = startAngle + sweepAngle * progress0;
            double angle1 = startAngle + sweepAngle * progress1;
            addFlatQuad(buffer,
                    Math.cos(angle0) * outerRadius, Math.sin(angle0) * outerRadius, progress0, 1.0D,
                    Math.cos(angle1) * outerRadius, Math.sin(angle1) * outerRadius, progress1, 1.0D,
                    Math.cos(angle1) * innerRadius, Math.sin(angle1) * innerRadius, progress1, 0.0D,
                    Math.cos(angle0) * innerRadius, Math.sin(angle0) * innerRadius, progress0, 0.0D);
        }

        tessellator.draw();
    }

    private static double circularDistance(double a, double b) {
        double distance = Math.abs(a - b);
        return Math.min(distance, 1.0D - distance);
    }

    private static void addFlatQuad(BufferBuilder buffer,
                                    double x1, double z1, double u1, double v1,
                                    double x2, double z2, double u2, double v2,
                                    double x3, double z3, double u3, double v3,
                                    double x4, double z4, double u4, double v4) {
        addFlatVertex(buffer, x1, z1, u1, v1);
        addFlatVertex(buffer, x2, z2, u2, v2);
        addFlatVertex(buffer, x3, z3, u3, v3);
        addFlatVertex(buffer, x1, z1, u1, v1);
        addFlatVertex(buffer, x3, z3, u3, v3);
        addFlatVertex(buffer, x4, z4, u4, v4);
    }

    private static void addFlatVertex(BufferBuilder buffer, double x, double z, double u, double v) {
        buffer.pos(x, 0.0D, z).tex(u, v).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}
