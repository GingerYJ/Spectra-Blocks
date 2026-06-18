package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderCelestialCompassCore extends RenderCelestialEffectBase<TileScalableEffect> {

    private static final double CORE_RADIUS = 0.24D;
    private static final double INNER_DISC_RADIUS = 0.74D;
    private static final double MIDDLE_DISC_RADIUS = 1.28D;
    private static final double OUTER_DISC_RADIUS = 1.84D;
    private static final double MARKER_RADIUS = 2.08D;
    private static final double POINTER_LENGTH = 1.56D;
    private static final double POINTER_TAIL = 0.46D;
    private static final int RING_SEGMENTS = 144;
    private static final int MINOR_TICK_COUNT = 32;
    private static final int STAR_COUNT = 36;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int STAR_WHITE = 0xF8FCFF;
    private static final int STAR_GOLD = 0xFFE4A3;
    private static final int COMPASS_GOLD = 0xE9C36A;
    private static final int CELESTIAL_BLUE = 0x83D7FF;
    private static final int DEEP_BLUE = 0x245A88;
    private static final int SOFT_PURPLE = 0xBFA2FF;

    @Override
    protected void renderCelestialEffect(TileScalableEffect te, float ticks) {
        ShaderProgram celestialShader = ShaderManager.getProgram("celestial_effect");
        if (celestialShader == null) {
            return;
        }

        try {
            drawCentralStar(celestialShader, ticks);
            drawCompassDiscs(celestialShader, ticks);
            drawCardinalMarkers(celestialShader, ticks);
            drawSeekingPointer(celestialShader, ticks);
            drawOrbitingTickStars(celestialShader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("celestial compass core render failed: " + ex.getMessage());
        } finally {
            celestialShader.end();
            useAlphaBlend();
        }
    }

    private void drawCentralStar(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.045D);

        useAdditiveBlend();
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (2.25D + pulse * 0.16D), ticks,
                0.0F, 0.0F, CELESTIAL_BLUE, STAR_WHITE, 0.115F + pulse * 0.045F, 0.92F, 0.11F, 20);
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * (1.34D + pulse * 0.08D), ticks,
                0.0F, 1.0F, STAR_GOLD, STAR_WHITE, 0.58F + pulse * 0.12F, 1.32F, 0.37F, 18);
        RenderMiniatureGalaxy.drawShaderSphere(shader, CORE_RADIUS * 0.72D, ticks,
                0.0F, 2.0F, STAR_WHITE, 0xFFFFFF, 0.70F + pulse * 0.14F, 1.56F, 0.61F, 14);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.010F, 0.0F, 0.0F, 1.0F);
        drawShaderStarRays(shader, ticks, 0.20D, 0.68D + pulse * 0.08D,
                8, STAR_WHITE, 0.11F + pulse * 0.06F, ticks * 0.010D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCompassDiscs(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.026D);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        drawFlatCompassDisc(shader, ticks, INNER_DISC_RADIUS, 0.020D,
                CELESTIAL_BLUE, STAR_WHITE, 0.075F + pulse * 0.028F, ticks * 0.006D, 0.20F);
        drawFlatCompassDisc(shader, ticks, MIDDLE_DISC_RADIUS, 0.026D,
                COMPASS_GOLD, STAR_GOLD, 0.064F + pulse * 0.024F, -ticks * 0.004D, 0.54F);
        drawFlatCompassDisc(shader, ticks, OUTER_DISC_RADIUS, 0.032D,
                DEEP_BLUE, CELESTIAL_BLUE, 0.052F + pulse * 0.020F, ticks * 0.0025D, 0.86F);

        useAdditiveBlend();
        drawShaderCircle(shader, ticks, 0.48D, STAR_WHITE, 0.070F + pulse * 0.030F, 64, 0.010D, 1.3F);
        drawShaderCircle(shader, ticks, 1.02D, CELESTIAL_BLUE, 0.060F + pulse * 0.024F, 96, 0.012D, 1.7F);
        drawShaderCircle(shader, ticks, 1.58D, COMPASS_GOLD, 0.064F + pulse * 0.024F, 128, 0.013D, 2.1F);
        drawCompassCross(shader, ticks, OUTER_DISC_RADIUS * 0.94D, CELESTIAL_BLUE, 0.055F + pulse * 0.024F,
                ticks * 0.002D);
        GlStateManager.popMatrix();
        useAlphaBlend();

        drawTiltedInstrumentDisc(shader, ticks, 21.0F, 1.0F, 0.0F,
                1.08D, COMPASS_GOLD, STAR_GOLD, 0.036F + pulse * 0.018F, 0.18F);
        drawTiltedInstrumentDisc(shader, ticks, -34.0F, 0.0F, 1.0F,
                1.58D, CELESTIAL_BLUE, STAR_WHITE, 0.032F + pulse * 0.016F, -0.12F);
    }

    private void drawFlatCompassDisc(ShaderProgram shader, float ticks, double radius,
                                     double width, int bandColor, int lineColor, float alpha,
                                     double rotation, float seed) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) Math.toDegrees(rotation), 0.0F, 0.0F, 1.0F);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderRing(shader, radius - width, radius + width, ticks,
                0.0F, 3.0F + seed, bandColor, lineColor, alpha, 0.88F, seed, RING_SEGMENTS);

        useAdditiveBlend();
        drawShaderCircle(shader, ticks, radius, lineColor, alpha * 1.65F, RING_SEGMENTS, 0.011D, 3.8F + seed);
        drawTickMarks(shader, ticks, radius, MINOR_TICK_COUNT, lineColor, alpha * 1.25F, 0.060D, 0.12D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawTiltedInstrumentDisc(ShaderProgram shader, float ticks,
                                          float tilt, float axisX, float axisZ, double radius,
                                          int primaryColor, int accentColor, float alpha, float spinScale) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * spinScale, 0.0F, 1.0F, 0.0F);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderRing(shader, radius - 0.018D, radius + 0.018D, ticks,
                0.0F, 4.0F + spinScale, primaryColor, accentColor, alpha, 0.78F,
                1.3F + spinScale, RING_SEGMENTS);
        useAdditiveBlend();
        drawShaderCircle(shader, ticks, radius, accentColor, alpha * 1.45F, RING_SEGMENTS, 0.011D,
                4.4F + spinScale);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCardinalMarkers(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.038D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i;
            boolean north = i == 0;
            int color = north ? STAR_GOLD : (i % 2 == 0 ? SOFT_PURPLE : CELESTIAL_BLUE);
            double markerLength = north ? 0.42D : 0.31D;
            double markerWidth = north ? 0.12D : 0.085D;
            float markerAlpha = north ? 0.34F + pulse * 0.14F : 0.22F + pulse * 0.10F;

            drawRadialGuide(shader, ticks, angle, 0.58D, MARKER_RADIUS + markerLength * 0.25D,
                    color, markerAlpha * 0.34F);
            drawCardinalChevron(shader, ticks, angle, MARKER_RADIUS, markerLength, markerWidth,
                    color, markerAlpha);
            drawMarkerSideTicks(shader, ticks, angle, MARKER_RADIUS - 0.18D, color, markerAlpha * 0.76F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * (MARKER_RADIUS + markerLength * 0.70D),
                    Math.sin(angle) * (MARKER_RADIUS + markerLength * 0.70D), 0.0D);
            RenderMiniatureGalaxy.drawShaderSphere(shader, north ? 0.038D : 0.030D, ticks,
                    0.0F, 5.0F + i, color, STAR_WHITE, markerAlpha * 1.20F, 1.26F, 2.0F + i, 7);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSeekingPointer(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.050D);
        double searchAngle = ticks * 0.010D
                + Math.sin(ticks * 0.020D) * 0.28D
                + Math.sin(ticks * 0.006D + 1.7D) * 0.44D;

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) Math.toDegrees(searchAngle), 0.0F, 0.0F, 1.0F);

        drawShaderLine(shader, ticks, -POINTER_TAIL, 0.0D, 0.010D,
                POINTER_LENGTH, 0.0D, 0.010D, STAR_GOLD, 0.32F + pulse * 0.12F, 0.030D, 6.0F);
        drawShaderLine(shader, ticks, -POINTER_TAIL * 0.72D, 0.0D, 0.014D,
                POINTER_LENGTH * 0.88D, 0.0D, 0.014D, STAR_WHITE, 0.42F + pulse * 0.18F, 0.014D, 6.3F);
        drawNeedleHead(shader, ticks, POINTER_LENGTH, 0.18D, 0.28D, STAR_GOLD, 0.28F + pulse * 0.14F);
        drawNeedleHead(shader, ticks, -POINTER_TAIL, 0.10D, -0.18D, CELESTIAL_BLUE, 0.22F + pulse * 0.10F);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(Math.cos(searchAngle) * (POINTER_LENGTH + 0.08D), 0.016D,
                Math.sin(searchAngle) * (POINTER_LENGTH + 0.08D));
        RenderMiniatureGalaxy.drawShaderSphere(shader, 0.052D + pulse * 0.014D, ticks,
                0.0F, 7.0F, STAR_WHITE, STAR_GOLD, 0.56F + pulse * 0.16F, 1.42F, 7.0F, 8);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawOrbitingTickStars(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STAR_COUNT; i++) {
            double track = i % 3;
            double radius = 0.92D + track * 0.35D;
            double snapped = Math.floor(i * 1.777D) * (Math.PI * 2.0D / MINOR_TICK_COUNT);
            double drift = ticks * (0.0065D + track * 0.0017D) + Math.sin(ticks * 0.012D + i) * 0.020D;
            double angle = snapped + drift + (i % 5) * 0.014D;
            double y = Math.sin(ticks * 0.020D + i * 0.71D) * 0.030D;
            float localPulse = wave(ticks * 0.043D + i * 0.67D);
            int color = i % 11 == 0 ? SOFT_PURPLE : (i % 3 == 0 ? STAR_GOLD : CELESTIAL_BLUE);
            double size = 0.020D + (i % 4) * 0.004D + (i % 9 == 0 ? 0.010D : 0.0D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            RenderMiniatureGalaxy.drawShaderSphere(shader, size, ticks, 0.0F, 8.0F + (i % 4),
                    color, STAR_WHITE, 0.34F + localPulse * 0.28F, 1.28F, i * 0.17F, 7);
            GlStateManager.popMatrix();

            if (i % 6 == 0) {
                double nextAngle = angle - 0.10D - track * 0.018D;
                drawShaderLine(shader, ticks,
                        Math.cos(nextAngle) * radius, y * 0.45D, Math.sin(nextAngle) * radius,
                        Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                        color, 0.050F + localPulse * 0.035F, 0.010D, 8.6F + i * 0.03F);
            }
        }
        useAlphaBlend();
    }

    private void drawCompassCross(ShaderProgram shader, float ticks, double radius,
                                  int color, float alpha, double phase) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i + phase;
            drawShaderLine(shader, ticks,
                    Math.cos(angle) * 0.36D, 0.0D, Math.sin(angle) * 0.36D,
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius,
                    color, alpha, 0.014D, 9.1F + i * 0.15F);
        }
    }

    private void drawTickMarks(ShaderProgram shader, float ticks, double radius, int count, int color,
                               float alpha, double minorLength, double majorLength) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            double length = i % 8 == 0 ? majorLength : (i % 4 == 0 ? majorLength * 0.78D : minorLength);
            float localAlpha = alpha * (i % 8 == 0 ? 1.25F : 0.82F);
            double inner = radius - length * 0.58D;
            double outer = radius + length * 0.42D;
            drawShaderLine(shader, ticks,
                    Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner,
                    Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                    color, localAlpha, i % 8 == 0 ? 0.013D : 0.009D, 10.0F + i * 0.08F);
        }
    }

    private void drawRadialGuide(ShaderProgram shader, float ticks, double angle, double inner,
                                 double outer, int color, float alpha) {
        drawShaderLine(shader, ticks,
                Math.cos(angle) * inner, Math.sin(angle) * inner, 0.004D,
                Math.cos(angle) * outer, Math.sin(angle) * outer, 0.004D,
                color, alpha, 0.012D, 11.4F);
    }

    private void drawMarkerSideTicks(ShaderProgram shader, float ticks, double angle, double radius,
                                     int color, float alpha) {
        double tangentX = -Math.sin(angle);
        double tangentY = Math.cos(angle);
        double centerX = Math.cos(angle) * radius;
        double centerY = Math.sin(angle) * radius;
        drawShaderLine(shader, ticks,
                centerX - tangentX * 0.16D, centerY - tangentY * 0.16D, 0.006D,
                centerX + tangentX * 0.16D, centerY + tangentY * 0.16D, 0.006D,
                color, alpha, 0.011D, 12.2F);
    }

    private void drawCardinalChevron(ShaderProgram shader, float ticks, double angle, double radius,
                                     double length, double halfWidth, int color, float alpha) {
        if (shader == null || alpha <= 0.01F || !shader.begin()) {
            return;
        }

        try {
            setCelestialUniforms(shader, ticks, 3.0F, 8.0F, color, STAR_WHITE, alpha, 1.12F, 12.8F);
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);
            double tangentX = -dirY;
            double tangentY = dirX;
            double tipX = dirX * (radius + length);
            double tipY = dirY * (radius + length);
            double innerX = dirX * (radius - length * 0.12D);
            double innerY = dirY * (radius - length * 0.12D);
            double leftX = innerX + tangentX * halfWidth;
            double leftY = innerY + tangentY * halfWidth;
            double rightX = innerX - tangentX * halfWidth;
            double rightY = innerY - tangentY * halfWidth;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            addFlatVertex(buffer, tipX, tipY, 0.010D, 0.5D, 1.0D);
            addFlatVertex(buffer, leftX, leftY, 0.010D, 0.0D, 0.0D);
            addFlatVertex(buffer, rightX, rightY, 0.010D, 1.0D, 0.0D);
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private void drawNeedleHead(ShaderProgram shader, float ticks, double baseX, double halfWidth,
                                double length, int color, float alpha) {
        if (shader == null || alpha <= 0.01F || !shader.begin()) {
            return;
        }

        try {
            setCelestialUniforms(shader, ticks, 3.0F, 8.4F, color, STAR_WHITE, alpha, 1.18F, 13.6F);
            double tipX = baseX + length;
            double backX = baseX - Math.signum(length) * 0.050D;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            addFlatVertex(buffer, tipX, 0.0D, 0.018D, 0.5D, 1.0D);
            addFlatVertex(buffer, backX, halfWidth, 0.018D, 0.0D, 0.0D);
            addFlatVertex(buffer, backX, -halfWidth, 0.018D, 1.0D, 0.0D);
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private void drawShaderCircle(ShaderProgram shader, float ticks, double radius, int color,
                                  float alpha, int segments, double halfWidth, float seed) {
        RenderMiniatureGalaxy.drawShaderRing(shader, Math.max(0.0D, radius - halfWidth),
                radius + halfWidth, ticks, 3.0F, 7.0F, color, STAR_WHITE,
                alpha, 1.05F, seed, segments);
    }

    private void drawShaderLine(ShaderProgram shader, float ticks,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                int color, float alpha, double halfWidth, float seed) {
        RenderMiniatureGalaxy.drawShaderLine(shader, x1, y1, z1, x2, y2, z2,
                ticks, 3.0F, 7.6F, color, STAR_WHITE, alpha, 1.10F, seed, halfWidth);
    }

    private void drawShaderStarRays(ShaderProgram shader, float ticks, double innerRadius, double outerRadius,
                                    int rayCount, int color, float alpha, double phase) {
        if (rayCount < 2) {
            return;
        }

        for (int i = 0; i < rayCount; i++) {
            double angle = Math.PI * 2.0D * i / rayCount + phase;
            double rayPulse = 0.74D + 0.26D * Math.sin(phase * 7.0D + i * 1.618D);
            double outer = innerRadius + (outerRadius - innerRadius) * rayPulse;
            drawShaderLine(shader, ticks,
                    Math.cos(angle) * innerRadius, 0.0D, Math.sin(angle) * innerRadius,
                    Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer,
                    color, alpha, 0.014D, 14.0F + i * 0.17F);
        }
    }

    private void setCelestialUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                      int primaryColor, int accentColor, float alpha,
                                      float intensity, float seed) {
        float[] primary = RenderHelper.unpackRGB(primaryColor);
        float[] accent = RenderHelper.unpackRGB(accentColor);
        shader.setUniform1f("uTime", ticks * 0.025F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uAccentColor", accent[0], accent[1], accent[2]);
    }

    private void addFlatVertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
