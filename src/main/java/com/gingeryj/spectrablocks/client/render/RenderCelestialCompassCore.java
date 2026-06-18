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
        ShaderProgram colorShader = ShaderManager.getProgram("basic");
        if (celestialShader == null) {
            return;
        }

        try {
            drawCentralStar(celestialShader, colorShader, ticks);
            drawCompassDiscs(celestialShader, colorShader, ticks);
            drawCardinalMarkers(celestialShader, colorShader, ticks);
            drawSeekingPointer(celestialShader, colorShader, ticks);
            drawOrbitingTickStars(celestialShader, colorShader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("celestial compass core render failed: " + ex.getMessage());
        } finally {
            celestialShader.end();
            if (colorShader != null) {
                colorShader.end();
            }
            useAlphaBlend();
            RenderHelper.resetLineWidth();
        }
    }

    private void drawCentralStar(ShaderProgram shader, ShaderProgram colorShader, float ticks) {
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
        GlStateManager.glLineWidth(1.5F);
        RenderNaturalShaderHelper.drawBasicStarRays(colorShader, 0.20D, 0.68D + pulse * 0.08D,
                8, STAR_WHITE, 0.11F + pulse * 0.06F, ticks * 0.010D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCompassDiscs(ShaderProgram shader, ShaderProgram colorShader, float ticks) {
        float pulse = wave(ticks * 0.026D);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        drawFlatCompassDisc(shader, colorShader, ticks, INNER_DISC_RADIUS, 0.020D,
                CELESTIAL_BLUE, STAR_WHITE, 0.075F + pulse * 0.028F, ticks * 0.006D, 0.20F);
        drawFlatCompassDisc(shader, colorShader, ticks, MIDDLE_DISC_RADIUS, 0.026D,
                COMPASS_GOLD, STAR_GOLD, 0.064F + pulse * 0.024F, -ticks * 0.004D, 0.54F);
        drawFlatCompassDisc(shader, colorShader, ticks, OUTER_DISC_RADIUS, 0.032D,
                DEEP_BLUE, CELESTIAL_BLUE, 0.052F + pulse * 0.020F, ticks * 0.0025D, 0.86F);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.0F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, 0.48D, STAR_WHITE, 0.070F + pulse * 0.030F, 64);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.02D, CELESTIAL_BLUE, 0.060F + pulse * 0.024F, 96);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, 1.58D, COMPASS_GOLD, 0.064F + pulse * 0.024F, 128);
        GlStateManager.glLineWidth(1.8F);
        drawCompassCross(colorShader, OUTER_DISC_RADIUS * 0.94D, CELESTIAL_BLUE, 0.055F + pulse * 0.024F,
                ticks * 0.002D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();

        drawTiltedInstrumentDisc(shader, colorShader, ticks, 21.0F, 1.0F, 0.0F,
                1.08D, COMPASS_GOLD, STAR_GOLD, 0.036F + pulse * 0.018F, 0.18F);
        drawTiltedInstrumentDisc(shader, colorShader, ticks, -34.0F, 0.0F, 1.0F,
                1.58D, CELESTIAL_BLUE, STAR_WHITE, 0.032F + pulse * 0.016F, -0.12F);
    }

    private void drawFlatCompassDisc(ShaderProgram shader, ShaderProgram colorShader, float ticks, double radius,
                                     double width, int bandColor, int lineColor, float alpha,
                                     double rotation, float seed) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) Math.toDegrees(rotation), 0.0F, 0.0F, 1.0F);

        useAlphaBlend();
        RenderMiniatureGalaxy.drawShaderRing(shader, radius - width, radius + width, ticks,
                0.0F, 3.0F + seed, bandColor, lineColor, alpha, 0.88F, seed, RING_SEGMENTS);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.1F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, radius, lineColor, alpha * 1.65F, RING_SEGMENTS);
        drawTickMarks(colorShader, radius, MINOR_TICK_COUNT, lineColor, alpha * 1.25F, 0.060D, 0.12D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawTiltedInstrumentDisc(ShaderProgram shader, ShaderProgram colorShader, float ticks,
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
        GlStateManager.glLineWidth(1.2F);
        RenderNaturalShaderHelper.drawBasicCircle(colorShader, radius, accentColor, alpha * 1.45F, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCardinalMarkers(ShaderProgram shader, ShaderProgram colorShader, float ticks) {
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

            GlStateManager.glLineWidth(north ? 2.4F : 1.8F);
            drawRadialGuide(colorShader, angle, 0.58D, MARKER_RADIUS + markerLength * 0.25D,
                    color, markerAlpha * 0.34F);
            drawCardinalChevron(colorShader, angle, MARKER_RADIUS, markerLength, markerWidth,
                    color, markerAlpha);
            drawMarkerSideTicks(colorShader, angle, MARKER_RADIUS - 0.18D, color, markerAlpha * 0.76F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(Math.cos(angle) * (MARKER_RADIUS + markerLength * 0.70D),
                    Math.sin(angle) * (MARKER_RADIUS + markerLength * 0.70D), 0.0D);
            RenderMiniatureGalaxy.drawShaderSphere(shader, north ? 0.038D : 0.030D, ticks,
                    0.0F, 5.0F + i, color, STAR_WHITE, markerAlpha * 1.20F, 1.26F, 2.0F + i, 7);
            GlStateManager.popMatrix();
        }

        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawSeekingPointer(ShaderProgram shader, ShaderProgram colorShader, float ticks) {
        float pulse = wave(ticks * 0.050D);
        double searchAngle = ticks * 0.010D
                + Math.sin(ticks * 0.020D) * 0.28D
                + Math.sin(ticks * 0.006D + 1.7D) * 0.44D;

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) Math.toDegrees(searchAngle), 0.0F, 0.0F, 1.0F);

        GlStateManager.glLineWidth(3.0F);
        RenderNaturalShaderHelper.drawBasicLine(colorShader, -POINTER_TAIL, 0.0D, 0.010D,
                POINTER_LENGTH, 0.0D, 0.010D, STAR_GOLD, 0.32F + pulse * 0.12F);
        GlStateManager.glLineWidth(1.4F);
        RenderNaturalShaderHelper.drawBasicLine(colorShader, -POINTER_TAIL * 0.72D, 0.0D, 0.014D,
                POINTER_LENGTH * 0.88D, 0.0D, 0.014D, STAR_WHITE, 0.42F + pulse * 0.18F);
        drawNeedleHead(colorShader, POINTER_LENGTH, 0.18D, 0.28D, STAR_GOLD, 0.28F + pulse * 0.14F);
        drawNeedleHead(colorShader, -POINTER_TAIL, 0.10D, -0.18D, CELESTIAL_BLUE, 0.22F + pulse * 0.10F);

        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(Math.cos(searchAngle) * (POINTER_LENGTH + 0.08D), 0.016D,
                Math.sin(searchAngle) * (POINTER_LENGTH + 0.08D));
        RenderMiniatureGalaxy.drawShaderSphere(shader, 0.052D + pulse * 0.014D, ticks,
                0.0F, 7.0F, STAR_WHITE, STAR_GOLD, 0.56F + pulse * 0.16F, 1.42F, 7.0F, 8);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawOrbitingTickStars(ShaderProgram shader, ShaderProgram colorShader, float ticks) {
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
                GlStateManager.glLineWidth(1.0F);
                RenderNaturalShaderHelper.drawBasicLine(colorShader,
                        Math.cos(nextAngle) * radius, y * 0.45D, Math.sin(nextAngle) * radius,
                        Math.cos(angle) * radius, y, Math.sin(angle) * radius,
                        color, 0.050F + localPulse * 0.035F);
            }
        }
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawCompassCross(ShaderProgram colorShader, double radius, int color, float alpha, double phase) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i + phase;
            RenderNaturalShaderHelper.drawBasicLine(colorShader,
                    Math.cos(angle) * 0.36D, 0.0D, Math.sin(angle) * 0.36D,
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius, color, alpha);
        }
    }

    private void drawTickMarks(ShaderProgram colorShader, double radius, int count, int color, float alpha,
                               double minorLength, double majorLength) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            double length = i % 8 == 0 ? majorLength : (i % 4 == 0 ? majorLength * 0.78D : minorLength);
            float localAlpha = alpha * (i % 8 == 0 ? 1.25F : 0.82F);
            double inner = radius - length * 0.58D;
            double outer = radius + length * 0.42D;
            RenderNaturalShaderHelper.drawBasicLine(colorShader,
                    Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner,
                    Math.cos(angle) * outer, 0.0D, Math.sin(angle) * outer, color, localAlpha);
        }
    }

    private void drawRadialGuide(ShaderProgram colorShader, double angle, double inner,
                                 double outer, int color, float alpha) {
        RenderNaturalShaderHelper.drawBasicLine(colorShader,
                Math.cos(angle) * inner, Math.sin(angle) * inner, 0.004D,
                Math.cos(angle) * outer, Math.sin(angle) * outer, 0.004D,
                color, alpha);
    }

    private void drawMarkerSideTicks(ShaderProgram colorShader, double angle, double radius,
                                     int color, float alpha) {
        double tangentX = -Math.sin(angle);
        double tangentY = Math.cos(angle);
        double centerX = Math.cos(angle) * radius;
        double centerY = Math.sin(angle) * radius;
        RenderNaturalShaderHelper.drawBasicLine(colorShader,
                centerX - tangentX * 0.16D, centerY - tangentY * 0.16D, 0.006D,
                centerX + tangentX * 0.16D, centerY + tangentY * 0.16D, 0.006D,
                color, alpha);
    }

    private void drawCardinalChevron(ShaderProgram colorShader, double angle, double radius,
                                     double length, double halfWidth, int color, float alpha) {
        if (colorShader == null || !colorShader.begin()) {
            return;
        }

        try {
            setBasicUniforms(colorShader);
            float[] rgb = RenderHelper.unpackRGB(color);
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
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(tipX, tipY, 0.010D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            buffer.pos(leftX, leftY, 0.010D).color(rgb[0], rgb[1], rgb[2], alpha * 0.42F).endVertex();
            buffer.pos(rightX, rightY, 0.010D).color(rgb[0], rgb[1], rgb[2], alpha * 0.42F).endVertex();
            tessellator.draw();
        } finally {
            colorShader.end();
        }
    }

    private void drawNeedleHead(ShaderProgram colorShader, double baseX, double halfWidth,
                                double length, int color, float alpha) {
        if (colorShader == null || !colorShader.begin()) {
            return;
        }

        try {
            setBasicUniforms(colorShader);
            float[] rgb = RenderHelper.unpackRGB(color);
            double tipX = baseX + length;
            double backX = baseX - Math.signum(length) * 0.050D;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(tipX, 0.0D, 0.018D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            buffer.pos(backX, halfWidth, 0.018D).color(rgb[0], rgb[1], rgb[2], alpha * 0.45F).endVertex();
            buffer.pos(backX, -halfWidth, 0.018D).color(rgb[0], rgb[1], rgb[2], alpha * 0.45F).endVertex();
            tessellator.draw();
        } finally {
            colorShader.end();
        }
    }

    private void setBasicUniforms(ShaderProgram shader) {
        shader.setUniform1f("alpha", 1.0F);
        shader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
